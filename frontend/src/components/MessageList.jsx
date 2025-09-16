import { useEffect, useRef } from 'react';
import { useAuth } from '../hooks/useAuth.jsx';

const MessageList = ({ messages, typingUsers }) => {
  const messagesEndRef = useRef(null);
  const { user } = useAuth();

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const formatTime = (timestamp) => {
    // Assume timestamp is in UTC from server
    const date = new Date(timestamp + 'Z');
    return date.toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'SENT':
        return <span className="text-gray-400">✓</span>;
      case 'DELIVERED':
        return <span className="text-gray-500">✓✓</span>;
      case 'READ':
        return <span className="text-blue-500">✓✓</span>;
      default:
        return null;
    }
  };

  const renderMessage = (message) => {
    // Convert both IDs to strings for comparison to handle type mismatches
    const messageUserId = String(message.sender?.id || message.sender?.username);
    const currentUserId = String(user?.id || user?.username);
    const isOwn = messageUserId === currentUserId;
    
    console.log('Message sender:', message.sender, 'Current user:', user, 'IsOwn:', isOwn);
    
    return (
      <div
        key={message.id}
        className={`flex ${isOwn ? 'justify-end' : 'justify-start'} mb-4`}
      >
        <div className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${
          isOwn 
            ? 'bg-blue-500 text-white' 
            : 'bg-gray-200 text-gray-800'
        }`}>
          {!isOwn && (
            <div className="text-xs font-medium mb-1">
              {message.sender.username}
            </div>
          )}
          
          {message.type === 'TEXT' && (
            <div>{message.content}</div>
          )}
          
          {message.type === 'IMAGE' && (
            <div>
              <img 
                src={message.attachmentUrl} 
                alt="Shared image"
                className="max-w-full h-auto rounded mb-2"
              />
              {message.content && <div>{message.content}</div>}
            </div>
          )}
          
          {message.type === 'FILE' && (
            <div>
              <a 
                href={message.attachmentUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center space-x-2 underline"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
                </svg>
                <span>{message.content || 'File'}</span>
              </a>
            </div>
          )}
          
          <div className={`flex items-center justify-between mt-1 text-xs ${
            isOwn ? 'text-blue-100' : 'text-gray-500'
          }`}>
            <span>{formatTime(message.createdAt)}</span>
            {isOwn && (
              <span className="ml-2">
                {getStatusIcon(message.status)}
              </span>
            )}
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="flex-1 overflow-y-auto p-4 space-y-4">
      {messages.map(renderMessage)}
      
      {typingUsers.size > 0 && (
        <div className="flex justify-start mb-4">
          <div className="bg-gray-200 text-gray-800 px-4 py-2 rounded-lg">
            <div className="flex items-center space-x-1">
              <span className="text-sm">
                {Array.from(typingUsers).join(', ')} {typingUsers.size === 1 ? 'is' : 'are'} typing
              </span>
              <div className="flex space-x-1">
                <div className="w-1 h-1 bg-gray-500 rounded-full animate-bounce"></div>
                <div className="w-1 h-1 bg-gray-500 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
                <div className="w-1 h-1 bg-gray-500 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
              </div>
            </div>
          </div>
        </div>
      )}
      
      <div ref={messagesEndRef} />
    </div>
  );
};

export default MessageList;