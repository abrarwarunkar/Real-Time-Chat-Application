import { useState } from 'react';
import { userAPI } from '../services/api';
import { useAuth } from '../hooks/useAuth.jsx';

const ConversationList = ({ conversations, currentConversation, onSelectConversation, onCreateConversation, onCreateGroup }) => {
  const { user, logout } = useAuth();
  const [showNewChat, setShowNewChat] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);

  const handleSearch = async (query) => {
    setSearchQuery(query);
    if (query.trim()) {
      setSearching(true);
      try {
        const response = await userAPI.searchUsers(query);
        setSearchResults(response.data);
      } catch (error) {
        console.error('Search failed:', error);
      } finally {
        setSearching(false);
      }
    } else {
      setSearchResults([]);
    }
  };

  const handleCreateChat = async (user) => {
    try {
      const conversation = await onCreateConversation(user.id);
      onSelectConversation(conversation);
      setShowNewChat(false);
      setSearchQuery('');
      setSearchResults([]);
    } catch (error) {
      console.error('Failed to create conversation:', error);
    }
  };

  const formatTime = (timestamp) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;
    
    if (diff < 24 * 60 * 60 * 1000) {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
    return date.toLocaleDateString();
  };

  return (
    <div className="w-80 flex flex-col" style={{ backgroundColor: 'var(--sidebar-bg)', color: 'white' }}>
      {/* Profile Header */}
      <div className="p-4 border-b" style={{ backgroundColor: 'var(--header-bg)' }}>
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="avatar">
              {user?.username?.[0]?.toUpperCase() || 'U'}
            </div>
            <div>
              <div className="font-medium">{user?.username}</div>
              <div className="text-sm opacity-75 flex items-center">
                <div className="status-online mr-2"></div>
                Online
              </div>
            </div>
          </div>
          <button
            onClick={logout}
            className="btn-icon text-white"
            title="Logout"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
          </button>
        </div>
      </div>
      
      <div className="p-4 bg-whatsapp-dark">
        <div className="flex justify-between items-center mb-4">
          <h1 className="text-xl font-semibold text-white">Chats</h1>
          <div className="flex space-x-2">
            <button
              onClick={() => setShowNewChat(!showNewChat)}
              className="p-2 text-white hover:bg-whatsapp-primary rounded-full transition-colors"
              title="New Chat"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
              </svg>
            </button>
            <button
              onClick={onCreateGroup}
              className="p-2 text-white hover:bg-whatsapp-primary rounded-full transition-colors"
              title="New Group"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </button>
          </div>
        </div>

        {showNewChat && (
          <div className="mb-4">
            <input
              type="text"
              placeholder="Search users..."
              className="w-full px-3 py-2 bg-white border-0 rounded-lg focus:outline-none focus:ring-2 focus:ring-whatsapp-primary"
              value={searchQuery}
              onChange={(e) => handleSearch(e.target.value)}
            />
            {searching && <div className="text-sm text-gray-300 mt-2">Searching...</div>}
            {searchResults.length > 0 && (
              <div className="mt-2 max-h-40 overflow-y-auto bg-white rounded-lg">
                {searchResults.map(user => (
                  <div
                    key={user.id}
                    onClick={() => handleCreateChat(user)}
                    className="flex items-center p-3 hover:bg-gray-50 cursor-pointer"
                  >
                    <div className="avatar small bg-whatsapp-primary mr-3">
                      {user.username[0].toUpperCase()}
                    </div>
                    <div>
                      <div className="font-medium text-gray-900">{user.username}</div>
                      <div className="text-sm text-gray-500">{user.email}</div>
                    </div>
                    {user.online && (
                      <div className="ml-auto w-2 h-2 bg-green-500 rounded-full"></div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      <div className="flex-1 overflow-y-auto custom-scrollbar">
        {conversations.map(conversation => (
          <div
            key={conversation.id}
            onClick={() => onSelectConversation(conversation)}
            className={`sidebar-item ${currentConversation?.id === conversation.id ? 'active' : ''}`}
          >
            <div className="avatar mr-3 flex-shrink-0 relative">
              {conversation.name ? conversation.name[0].toUpperCase() : 'C'}
              {conversation.type === 'GROUP' && (
                <div className="absolute -bottom-1 -right-1 w-4 h-4 bg-green-500 rounded-full flex items-center justify-center border-2 border-gray-700">
                  <svg className="w-2 h-2 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                </div>
              )}
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex justify-between items-baseline mb-1">
                <div className="flex items-center space-x-2">
                  <h3 className="font-medium text-white truncate">{conversation.name || 'Conversation'}</h3>
                  {conversation.type === 'GROUP' && (
                    <span className="text-xs text-gray-400 bg-gray-700 px-2 py-1 rounded-full">
                      Group
                    </span>
                  )}
                </div>
                {conversation.lastMessage && (
                  <span className="text-xs text-gray-400">
                    {formatTime(conversation.lastMessage.createdAt)}
                  </span>
                )}
              </div>
              <div className="flex items-center justify-between">
                {conversation.lastMessage && (
                  <p className="text-sm text-gray-400 truncate flex-1">
                    {conversation.lastMessage.sender?.username && conversation.type === 'GROUP'
                      ? `${conversation.lastMessage.sender.username}: ${conversation.lastMessage.content}`
                      : conversation.lastMessage.content
                    }
                  </p>
                )}
                {conversation.unreadCount > 0 && (
                  <span className="bg-blue-500 text-white text-xs rounded-full px-2 py-1 min-w-[20px] text-center ml-2 font-medium">
                    {conversation.unreadCount}
                  </span>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ConversationList;