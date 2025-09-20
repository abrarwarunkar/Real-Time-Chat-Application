import { useState } from 'react';
import { useAuth } from '../hooks/useAuth.jsx';
import { useChat } from '../hooks/useChat.jsx';
import ConversationList from './ConversationList';
import MessageList from './MessageList';
import MessageInput from './MessageInput';
import GroupCreationModal from './GroupCreationModal';
import GroupMemberModal from './GroupMemberModal';

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
    createGroupConversation,
    addMemberToGroup,
    clearChat,
  } = useChat();

  const [showGroupModal, setShowGroupModal] = useState(false);
  const [showMemberModal, setShowMemberModal] = useState(false);

  const [showMobileSidebar, setShowMobileSidebar] = useState(false);

  const handleSendMessage = (content, type = 'TEXT', attachmentUrl = null, mimeType = null) => {
    sendMessage(content, type, attachmentUrl, mimeType);
  };

  const handleSelectConversation = (conversation) => {
    selectConversation(conversation);
    setShowMobileSidebar(false); // Close mobile sidebar when conversation is selected
  };

  return (
    <div className="h-screen flex" style={{ backgroundColor: 'var(--chat-bg)' }}>
      {/* Mobile Sidebar Overlay */}
      {showMobileSidebar && (
        <div className="fixed inset-0 z-50 md:hidden">
          <div className="absolute inset-0 bg-black bg-opacity-50" onClick={() => setShowMobileSidebar(false)}></div>
          <div className="relative w-80 h-full">
            <ConversationList
              conversations={conversations}
              currentConversation={currentConversation}
              onSelectConversation={handleSelectConversation}
              onCreateConversation={createDirectConversation}
            />
          </div>
        </div>
      )}

      {/* Desktop: Show sidebar, Mobile: Hide sidebar by default */}
      <div className="hidden md:block" style={{ backgroundColor: 'var(--sidebar-bg)' }}>
        <ConversationList
          conversations={conversations}
          currentConversation={currentConversation}
          onSelectConversation={selectConversation}
          onCreateConversation={createDirectConversation}
          onCreateGroup={() => setShowGroupModal(true)}
        />
      </div>

      <div className="flex-1 flex flex-col">
        {currentConversation ? (
          <>
            {/* Enhanced Header */}
            <div className="flex items-center justify-between p-4 border-b" style={{ backgroundColor: 'var(--header-bg)', color: 'white' }}>
              <div className="flex items-center space-x-3">
                {/* Mobile menu button */}
                <button
                  onClick={() => setShowMobileSidebar(true)}
                  className="md:hidden btn-icon text-white"
                  title="Menu"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                  </svg>
                </button>

                <div className="avatar bg-gradient-to-br from-blue-500 to-purple-600">
                  {currentConversation.name ? currentConversation.name[0].toUpperCase() : 'C'}
                  {currentConversation.type === 'GROUP' && (
                    <div className="absolute -bottom-1 -right-1 w-4 h-4 bg-green-500 rounded-full flex items-center justify-center border-2 border-white">
                      <svg className="w-2 h-2 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                      </svg>
                    </div>
                  )}
                </div>
                <div>
                  <div className="flex items-center space-x-2">
                    <h2 className="text-lg font-semibold">
                      {currentConversation.name || 'Conversation'}
                    </h2>
                    {currentConversation.type === 'GROUP' && (
                      <span className="text-xs bg-green-600 px-2 py-1 rounded-full">
                        Group
                      </span>
                    )}
                  </div>
                  <div className="text-sm opacity-75">
                    {currentConversation.type === 'GROUP'
                      ? `${currentConversation.members?.length || 0} members`
                      : `${currentConversation.members?.length || 0} members`
                    }
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex space-x-1">
                {currentConversation.type === 'GROUP' && (
                  <button
                    onClick={() => setShowMemberModal(true)}
                    className="btn-icon text-white"
                    title="Add Members"
                  >
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                    </svg>
                  </button>
                )}
                <button
                  onClick={() => {
                    if (confirm('Are you sure you want to clear this chat? This action cannot be undone.')) {
                      clearChat();
                    }
                  }}
                  className="btn-icon text-white"
                  title="Clear Chat"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
                <button
                  onClick={() => {
                    if (confirm('Are you sure you want to log out?')) {
                      logout();
                    }
                  }}
                  className="btn-icon text-white"
                  title="Logout"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
                </button>
              </div>
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
          <div className="flex-1 flex items-center justify-center" style={{ backgroundColor: 'var(--chat-bg)' }}>
            <div className="text-center">
              <div className="w-24 h-24 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full mx-auto mb-6 flex items-center justify-center shadow-lg">
                <svg className="w-12 h-12 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
              </div>
              <h3 className="text-xl font-medium text-gray-800 mb-2">
                Welcome, {user?.username}!
              </h3>
              <p className="text-gray-600">
                Select a conversation to start chatting
              </p>
            </div>
          </div>
        )}
      </div>

      {/* Group Creation Modal */}
      <GroupCreationModal
        isOpen={showGroupModal}
        onClose={() => setShowGroupModal(false)}
        onCreateGroup={createGroupConversation}
      />

      {/* Group Member Modal */}
      <GroupMemberModal
        isOpen={showMemberModal}
        onClose={() => setShowMemberModal(false)}
        conversation={currentConversation}
        onAddMember={addMemberToGroup}
      />
    </div>
  );
};

export default Chat;