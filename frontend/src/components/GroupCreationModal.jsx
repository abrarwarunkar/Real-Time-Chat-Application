import { useState, useEffect } from 'react';
import { userAPI } from '../services/api';

const GroupCreationModal = ({ isOpen, onClose, onCreateGroup }) => {
  const [step, setStep] = useState(1); // 1: Group name, 2: Add members
  const [groupName, setGroupName] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [searching, setSearching] = useState(false);
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    if (!isOpen) {
      // Reset state when modal closes
      setStep(1);
      setGroupName('');
      setSearchQuery('');
      setSearchResults([]);
      setSelectedUsers([]);
    }
  }, [isOpen]);

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

  const toggleUserSelection = (user) => {
    setSelectedUsers(prev => {
      const isSelected = prev.some(u => u.id === user.id);
      if (isSelected) {
        return prev.filter(u => u.id !== user.id);
      } else {
        return [...prev, user];
      }
    });
  };

  const handleNext = () => {
    if (step === 1 && groupName.trim()) {
      setStep(2);
    }
  };

  const handleCreateGroup = async () => {
    if (!groupName.trim() || selectedUsers.length === 0) return;

    setCreating(true);
    try {
      const memberIds = selectedUsers.map(user => user.id);
      await onCreateGroup(groupName.trim(), memberIds);
      onClose();
    } catch (error) {
      console.error('Failed to create group:', error);
      // Error handling is now done by the API interceptor for 403 errors
      // For other errors, show a user-friendly message
      if (error.response?.status !== 403) {
        alert('Failed to create group. Please check your connection and try again.');
      }
    } finally {
      setCreating(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black bg-opacity-50" onClick={onClose}></div>

      {/* Modal */}
      <div className="relative bg-white rounded-lg shadow-xl max-w-md w-full mx-4 max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="bg-whatsapp-header text-white p-4 flex items-center justify-between">
          <div className="flex items-center">
            {step === 2 && (
              <button
                onClick={() => setStep(1)}
                className="mr-3 p-1 hover:bg-whatsapp-primary rounded-full"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </button>
            )}
            <h2 className="text-lg font-semibold">
              {step === 1 ? 'New Group' : 'Add Members'}
            </h2>
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

        {/* Content */}
        <div className="p-4">
          {step === 1 ? (
            // Step 1: Group Name
            <div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Group Name
                </label>
                <input
                  type="text"
                  value={groupName}
                  onChange={(e) => setGroupName(e.target.value)}
                  placeholder="Enter group name"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-whatsapp-primary focus:border-transparent"
                  maxLength={25}
                />
                <div className="text-xs text-gray-500 mt-1">
                  {groupName.length}/25
                </div>
              </div>
              <div className="flex justify-end">
                <button
                  onClick={handleNext}
                  disabled={!groupName.trim()}
                  className="px-4 py-2 bg-whatsapp-primary text-white rounded-lg hover:bg-green-600 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Next
                </button>
              </div>
            </div>
          ) : (
            // Step 2: Add Members
            <div>
              {/* Selected Users */}
              {selectedUsers.length > 0 && (
                <div className="mb-4">
                  <div className="flex flex-wrap gap-2">
                    {selectedUsers.map(user => (
                      <div
                        key={user.id}
                        className="flex items-center bg-whatsapp-light px-3 py-1 rounded-full"
                      >
                        <span className="text-sm">{user.username}</span>
                        <button
                          onClick={() => toggleUserSelection(user)}
                          className="ml-2 text-gray-500 hover:text-red-500"
                        >
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                          </svg>
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Search */}
              <div className="mb-4">
                <input
                  type="text"
                  placeholder="Search users..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-whatsapp-primary focus:border-transparent"
                  value={searchQuery}
                  onChange={(e) => handleSearch(e.target.value)}
                />
                {searching && <div className="text-sm text-gray-500 mt-2">Searching...</div>}
              </div>

              {/* Search Results */}
              <div className="max-h-60 overflow-y-auto">
                {searchResults.map(user => {
                  const isSelected = selectedUsers.some(u => u.id === user.id);
                  return (
                    <div
                      key={user.id}
                      onClick={() => toggleUserSelection(user)}
                      className={`flex items-center p-3 hover:bg-gray-50 cursor-pointer rounded-lg ${
                        isSelected ? 'bg-whatsapp-light' : ''
                      }`}
                    >
                      <div className="avatar small mr-3">
                        {user.username[0].toUpperCase()}
                      </div>
                      <div className="flex-1">
                        <div className="font-medium">{user.username}</div>
                        <div className="text-sm text-gray-500">{user.email}</div>
                      </div>
                      {isSelected && (
                        <div className="w-5 h-5 bg-whatsapp-primary rounded-full flex items-center justify-center">
                          <svg className="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                          </svg>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>

              {/* Create Button */}
              <div className="flex justify-end mt-4 pt-4 border-t">
                <button
                  onClick={handleCreateGroup}
                  disabled={selectedUsers.length === 0 || creating}
                  className="px-6 py-2 bg-whatsapp-primary text-white rounded-lg hover:bg-green-600 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {creating ? 'Creating...' : `Create Group (${selectedUsers.length})`}
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default GroupCreationModal;