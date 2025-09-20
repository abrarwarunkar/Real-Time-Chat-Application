#!/usr/bin/env python3
"""
Load Testing Script for Chat Application Scalability
Tests thousands of concurrent users with WebSocket connections and messaging
"""

import asyncio
import websockets
import json
import time
import statistics
from datetime import datetime
import logging
import aiohttp
import uuid

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class LoadTestConfig:
    """Configuration for load testing"""
    def __init__(self):
        self.base_url = "http://localhost:8080"
        self.ws_url = "ws://localhost:8080/api/ws"
        self.api_url = "http://localhost:3000"  # Frontend URL

        # Test parameters
        self.total_users = 1000  # Total users to simulate
        self.concurrent_users = 100  # Concurrent users per batch
        self.messages_per_user = 10  # Messages each user sends
        self.test_duration = 300  # Test duration in seconds

        # Timing
        self.message_interval = 0.1  # Seconds between messages
        self.heartbeat_interval = 30  # WebSocket heartbeat interval

        # Results storage
        self.response_times = []
        self.errors = []
        self.success_count = 0

class ChatLoadTester:
    """Main load testing class"""

    def __init__(self, config: LoadTestConfig):
        self.config = config
        self.users = []
        self.active_connections = 0
        self.start_time = None

    async def create_test_users(self):
        """Create test users in batches"""
        logger.info(f"Creating {self.config.total_users} test users...")

        for i in range(0, self.config.total_users, self.config.concurrent_users):
            batch_size = min(self.config.concurrent_users, self.config.total_users - i)
            tasks = []

            for j in range(batch_size):
                user_id = i + j
                username = f"testuser_{user_id}"
                email = f"testuser_{user_id}@example.com"
                password = "testpass123"

                tasks.append(self.register_user(username, email, password))

            # Wait for batch to complete
            results = await asyncio.gather(*tasks, return_exceptions=True)

            for result in results:
                if isinstance(result, Exception):
                    logger.error(f"User registration failed: {result}")
                else:
                    self.users.append(result)

            logger.info(f"Created batch {i//self.config.concurrent_users + 1}, total users: {len(self.users)}")

    async def register_user(self, username: str, email: str, password: str):
        """Register a single user"""
        async with aiohttp.ClientSession() as session:
            user_data = {
                "username": username,
                "email": email,
                "password": password
            }

            try:
                async with session.post(f"{self.config.base_url}/api/auth/register", json=user_data) as response:
                    if response.status == 200:
                        data = await response.json()
                        return {
                            "username": username,
                            "token": data.get("token"),
                            "user_id": data.get("user", {}).get("id")
                        }
                    else:
                        raise Exception(f"Registration failed: {response.status}")
            except Exception as e:
                raise Exception(f"Registration error: {e}")

    async def simulate_user_session(self, user_data: dict):
        """Simulate a complete user session with WebSocket connection"""
        try:
            # Connect to WebSocket
            uri = f"{self.config.ws_url}?token={user_data['token']}"
            async with websockets.connect(uri) as websocket:
                self.active_connections += 1
                logger.debug(f"User {user_data['username']} connected. Active connections: {self.active_connections}")

                # Send heartbeat to maintain presence
                asyncio.create_task(self.send_heartbeat(websocket))

                # Send messages
                for i in range(self.config.messages_per_user):
                    message = {
                        "type": "CHAT",
                        "content": f"Load test message {i} from {user_data['username']} at {datetime.now().isoformat()}",
                        "conversationId": 1,  # Assuming a default conversation exists
                        "timestamp": datetime.now().isoformat()
                    }

                    start_time = time.time()
                    await websocket.send(json.dumps(message))

                    # Wait for acknowledgment
                    try:
                        response = await asyncio.wait_for(websocket.recv(), timeout=5.0)
                        end_time = time.time()
                        response_time = (end_time - start_time) * 1000  # Convert to milliseconds
                        self.config.response_times.append(response_time)
                        self.config.success_count += 1
                    except asyncio.TimeoutError:
                        self.config.errors.append("Message timeout")
                        logger.warning(f"Message timeout for user {user_data['username']}")

                    await asyncio.sleep(self.config.message_interval)

                # Simulate typing indicators
                for _ in range(5):
                    typing_msg = {"type": "TYPING", "conversationId": 1, "typing": True}
                    await websocket.send(json.dumps(typing_msg))
                    await asyncio.sleep(1)
                    typing_msg["typing"] = False
                    await websocket.send(json.dumps(typing_msg))
                    await asyncio.sleep(2)

                # Stay connected for presence testing
                await asyncio.sleep(self.config.test_duration - (self.config.messages_per_user * self.config.message_interval))

        except Exception as e:
            self.config.errors.append(str(e))
            logger.error(f"User session error for {user_data['username']}: {e}")
        finally:
            self.active_connections -= 1

    async def send_heartbeat(self, websocket):
        """Send periodic heartbeat to maintain connection"""
        try:
            while True:
                heartbeat = {"type": "HEARTBEAT", "timestamp": datetime.now().isoformat()}
                await websocket.send(json.dumps(heartbeat))
                await asyncio.sleep(self.config.heartbeat_interval)
        except Exception:
            pass  # Connection closed

    async def run_load_test(self):
        """Run the complete load test"""
        logger.info("Starting chat application load test...")
        self.start_time = time.time()

        # Create test users
        await self.create_test_users()

        if not self.users:
            logger.error("No users created, aborting test")
            return

        logger.info(f"Starting load test with {len(self.users)} users...")

        # Run user sessions in batches
        for i in range(0, len(self.users), self.config.concurrent_users):
            batch = self.users[i:i + self.config.concurrent_users]
            tasks = [self.simulate_user_session(user) for user in batch]

            # Start batch
            await asyncio.gather(*tasks, return_exceptions=True)

            logger.info(f"Completed batch {i//self.config.concurrent_users + 1}")

        # Wait for all connections to close
        while self.active_connections > 0:
            logger.info(f"Waiting for {self.active_connections} connections to close...")
            await asyncio.sleep(5)

    def generate_report(self):
        """Generate comprehensive test report"""
        end_time = time.time()
        duration = end_time - self.start_time

        report = {
            "test_summary": {
                "total_users": len(self.users),
                "concurrent_users": self.config.concurrent_users,
                "test_duration_seconds": duration,
                "messages_per_user": self.config.messages_per_user,
                "total_messages_sent": self.config.success_count,
                "total_errors": len(self.config.errors)
            },
            "performance_metrics": {},
            "error_analysis": {}
        }

        if self.config.response_times:
            report["performance_metrics"] = {
                "average_response_time_ms": statistics.mean(self.config.response_times),
                "median_response_time_ms": statistics.median(self.config.response_times),
                "min_response_time_ms": min(self.config.response_times),
                "max_response_time_ms": max(self.config.response_times),
                "95th_percentile_ms": statistics.quantiles(self.config.response_times, n=20)[18],  # 95th percentile
                "99th_percentile_ms": statistics.quantiles(self.config.response_times, n=100)[98],  # 99th percentile
                "messages_per_second": self.config.success_count / duration
            }

        # Error analysis
        error_counts = {}
        for error in self.config.errors:
            error_type = str(error).split(':')[0]
            error_counts[error_type] = error_counts.get(error_type, 0) + 1

        report["error_analysis"] = {
            "error_types": error_counts,
            "error_rate": len(self.config.errors) / (self.config.success_count + len(self.config.errors)) if (self.config.success_count + len(self.config.errors)) > 0 else 0
        }

        # Performance assessment
        avg_response_time = report["performance_metrics"].get("average_response_time_ms", float('inf'))
        if avg_response_time < 100:
            report["assessment"] = "EXCELLENT: Sub-100ms latency achieved"
        elif avg_response_time < 200:
            report["assessment"] = "GOOD: Under 200ms latency"
        elif avg_response_time < 500:
            report["assessment"] = "ACCEPTABLE: Under 500ms latency"
        else:
            report["assessment"] = "NEEDS_IMPROVEMENT: High latency detected"

        return report

async def main():
    """Main entry point"""
    config = LoadTestConfig()
    tester = ChatLoadTester(config)

    try:
        await tester.run_load_test()
    except KeyboardInterrupt:
        logger.info("Test interrupted by user")
    except Exception as e:
        logger.error(f"Test failed: {e}")
    finally:
        report = tester.generate_report()

        # Print report
        print("\n" + "="*80)
        print("CHAT APPLICATION LOAD TEST REPORT")
        print("="*80)
        print(json.dumps(report, indent=2))

        # Save detailed report
        with open("load_test_report.json", "w") as f:
            json.dump(report, f, indent=2)

        logger.info("Load test completed. Detailed report saved to load_test_report.json")

if __name__ == "__main__":
    asyncio.run(main())