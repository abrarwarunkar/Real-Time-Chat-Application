import { useAuth } from '../hooks/useAuth.jsx';
import { useChat } from '../hooks/useChat.jsx';
import ConversationList from './ConversationList';
import MessageList from './MessageList';
import MessageInput from './MessageInput';

const Chat = () => {
  const { user, logout } = useAuth();
  const {
    conversations,
    currentConversation,
    messages,
    typingUsers,
    loading,
    selectConversation,
    sendMessage,
    sendTypingIndicator,
    createDirectConversation,
    clearChat,
  } = useChat();

  const handleSendMessage = (content, type = 'TEXT', attachmentUrl = null, mimeType = null) => {
    sendMessage(content, type, attachmentUrl, mimeType);
  };

  return (
    <div className="h-screen flex bg-gray-100">
      <ConversationList
        conversations={conversations}
        currentConversation={currentConversation}
        onSelectConversation={selectConversation}
        onCreateConversation={createDirectConversation}
      />
      
      <div className="flex-1 flex flex-col">
        {currentConversation ? (
          <>
            <div className="bg-white border-b border-gray-200 p-4 flex justify-between items-center">
              <div>
                <h2 className="text-lg font-semibold">
                  {currentConversation.name || 'Conversation'}
                </h2>
                <div className="text-sm text-gray-500">
                  {currentConversation.members?.length} members
                </div>
              </div>
              <button
                onClick={() => {
                  if (confirm('Are you sure you want to clear this chat? This action cannot be undone.')) {
                    clearChat();
                  }
                }}
                className="p-2 text-gray-500 hover:text-red-600 hover:bg-gray-100 rounded-full"
                title="Clear Chat"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </button>
            </div>
            
            <MessageList 
              messages={messages} 
              typingUsers={typingUsers}
            />
            
            <MessageInput
              onSendMessage={handleSendMessage}
              onTyping={sendTypingIndicator}
            />
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center bg-white">
            <div className="text-center">
              <div className="w-16 h-16 bg-gray-200 rounded-full mx-auto mb-4 flex items-center justify-center">
                <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
              </div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                Welcome, {user?.username}!
              </h3>
              <p className="text-gray-500">
                Select a conversation to start chatting
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Chat;