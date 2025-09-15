import { useState } from 'react';
import { userAPI } from '../services/api';
import { useAuth } from '../hooks/useAuth.jsx';

const ConversationList = ({ conversations, currentConversation, onSelectConversation, onCreateConversation }) => {
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
    <div className="w-80 bg-white border-r border-gray-200 flex flex-col">
      {/* Profile Header */}
      <div className="p-4 border-b border-gray-200 bg-gray-50">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center text-white font-semibold">
              {user?.username?.[0]?.toUpperCase() || 'U'}
            </div>
            <div>
              <div className="font-medium text-gray-900">{user?.username}</div>
              <div className="text-sm text-gray-500">{user?.email}</div>
            </div>
          </div>
          <button
            onClick={logout}
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-200 rounded-full"
            title="Logout"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
          </button>
        </div>
      </div>
      
      <div className="p-4 border-b border-gray-200">
        <div className="flex justify-between items-center mb-4">
          <h1 className="text-xl font-semibold">Chats</h1>
          <button
            onClick={() => setShowNewChat(!showNewChat)}
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-full"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
          </button>
        </div>

        {showNewChat && (
          <div className="mb-4">
            <input
              type="text"
              placeholder="Search users..."
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              value={searchQuery}
              onChange={(e) => handleSearch(e.target.value)}
            />
            {searching && <div className="text-sm text-gray-500 mt-2">Searching...</div>}
            {searchResults.length > 0 && (
              <div className="mt-2 max-h-40 overflow-y-auto">
                {searchResults.map(user => (
                  <div
                    key={user.id}
                    onClick={() => handleCreateChat(user)}
                    className="flex items-center p-2 hover:bg-gray-100 cursor-pointer rounded"
                  >
                    <div className="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center mr-3">
                      {user.username[0].toUpperCase()}
                    </div>
                    <div>
                      <div className="font-medium">{user.username}</div>
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

      <div className="flex-1 overflow-y-auto">
        {conversations.map(conversation => (
          <div
            key={conversation.id}
            onClick={() => onSelectConversation(conversation)}
            className={`flex items-center p-4 hover:bg-gray-50 cursor-pointer border-b border-gray-100 ${
              currentConversation?.id === conversation.id ? 'bg-blue-50' : ''
            }`}
          >
            <div className="w-12 h-12 bg-gray-300 rounded-full flex items-center justify-center mr-3">
              {conversation.name ? conversation.name[0].toUpperCase() : 'C'}
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex justify-between items-baseline">
                <h3 className="font-medium truncate">{conversation.name || 'Conversation'}</h3>
                {conversation.lastMessage && (
                  <span className="text-xs text-gray-500">
                    {formatTime(conversation.lastMessage.createdAt)}
                  </span>
                )}
              </div>
              {conversation.lastMessage && (
                <p className="text-sm text-gray-600 truncate">
                  {conversation.lastMessage.content}
                </p>
              )}
              {conversation.unreadCount > 0 && (
                <div className="flex justify-between items-center mt-1">
                  <div></div>
                  <span className="bg-blue-500 text-white text-xs rounded-full px-2 py-1">
                    {conversation.unreadCount}
                  </span>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ConversationList;