import { useState, useEffect } from 'react';
import { userAPI } from '../services/api';

const GroupMemberModal = ({ isOpen, onClose, conversation, onAddMember }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [adding, setAdding] = useState(false);

  useEffect(() => {
    if (!isOpen) {
      setSearchQuery('');
      setSearchResults([]);
    }
  }, [isOpen]);

  const handleSearch = async (query) => {
    setSearchQuery(query);
    if (query.trim()) {
      setSearching(true);
      try {
        const response = await userAPI.searchUsers(query);
        // Filter out users who are already members
        const existingMemberIds = new Set(conversation.members?.map(m => m.id) || []);
        const filteredResults = response.data.filter(user => !existingMemberIds.has(user.id));
        setSearchResults(filteredResults);
      } catch (error) {
        console.error('Search failed:', error);
      } finally {
        setSearching(false);
      }
    } else {
      setSearchResults([]);
    }
  };

  const handleAddMember = async (user) => {
    setAdding(true);
    try {
      await onAddMember(conversation.id, user.id);
      setSearchQuery('');
      setSearchResults([]);
      // Remove the added user from search results
      setSearchResults(prev => prev.filter(u => u.id !== user.id));
    } catch (error) {
      console.error('Failed to add member:', error);
    } finally {
      setAdding(false);
    }
  };

  if (!isOpen || !conversation) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black bg-opacity-50" onClick={onClose}></div>

      {/* Modal */}
      <div className="relative bg-white rounded-lg shadow-xl max-w-md w-full mx-4 max-h-[80vh] overflow-hidden">
        {/* Header */}
        <div className="bg-whatsapp-header text-white p-4 flex items-center justify-between">
          <div className="flex items-center">
            <h2 className="text-lg font-semibold">Add Members</h2>
          </div>
          <button
            onClick={onClose}
            className="p-1 hover:bg-whatsapp-primary rounded-full"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Group Info */}
        <div className="p-4 bg-gray-50 border-b">
          <div className="flex items-center space-x-3">
            <div className="avatar bg-whatsapp-primary">
              {conversation.name ? conversation.name[0].toUpperCase() : 'G'}
            </div>
            <div>
              <h3 className="font-medium text-gray-900">{conversation.name}</h3>
              <p className="text-sm text-gray-500">{conversation.members?.length || 0} members</p>
            </div>
          </div>
        </div>

        {/* Content */}
        <div className="p-4">
          {/* Search */}
          <div className="mb-4">
            <input
              type="text"
              placeholder="Search users to add..."
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-whatsapp-primary focus:border-transparent"
              value={searchQuery}
              onChange={(e) => handleSearch(e.target.value)}
            />
            {searching && <div className="text-sm text-gray-500 mt-2">Searching...</div>}
          </div>

          {/* Search Results */}
          <div className="max-h-60 overflow-y-auto">
            {searchResults.map(user => (
              <div
                key={user.id}
                className="flex items-center justify-between p-3 hover:bg-gray-50 rounded-lg"
              >
                <div className="flex items-center space-x-3">
                  <div className="avatar small">
                    {user.username[0].toUpperCase()}
                  </div>
                  <div>
                    <div className="font-medium">{user.username}</div>
                    <div className="text-sm text-gray-500">{user.email}</div>
                  </div>
                </div>
                <button
                  onClick={() => handleAddMember(user)}
                  disabled={adding}
                  className="px-3 py-1 bg-whatsapp-primary text-white text-sm rounded-lg hover:bg-green-600 disabled:opacity-50"
                >
                  {adding ? 'Adding...' : 'Add'}
                </button>
              </div>
            ))}
            {searchQuery && !searching && searchResults.length === 0 && (
              <div className="text-center text-gray-500 py-4">
                No users found
              </div>
            )}
          </div>

          {/* Current Members */}
          <div className="mt-6">
            <h4 className="font-medium text-gray-900 mb-3">Current Members</h4>
            <div className="space-y-2 max-h-40 overflow-y-auto">
              {conversation.members?.map(member => (
                <div key={member.id} className="flex items-center space-x-3 p-2">
                  <div className="avatar small">
                    {member.username[0].toUpperCase()}
                  </div>
                  <div className="flex-1">
                    <div className="font-medium text-sm">{member.username}</div>
                    <div className="text-xs text-gray-500">{member.email}</div>
                  </div>
                  {member.role === 'ADMIN' && (
                    <span className="text-xs bg-whatsapp-primary text-white px-2 py-1 rounded-full">
                      Admin
                    </span>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default GroupMemberModal;