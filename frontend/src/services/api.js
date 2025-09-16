import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL ? import.meta.env.VITE_API_URL + '/api' : '/api';

const api = axios.create({
  baseURL: API_BASE,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const response = await axios.post(`${API_BASE}/auth/refresh`, {
            refreshToken
          });
          const { accessToken } = response.data;
          localStorage.setItem('accessToken', accessToken);
          error.config.headers.Authorization = `Bearer ${accessToken}`;
          return api.request(error.config);
        } catch {
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  refresh: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
};

export const userAPI = {
  getMe: () => api.get('/users/me'),
  searchUsers: (query) => api.get(`/users/search?query=${query}`),
  getPresence: (userId) => api.get(`/users/${userId}/presence`),
};

export const conversationAPI = {
  getConversations: () => api.get('/conversations'),
  getConversation: (id) => api.get(`/conversations/${id}`),
  createDirect: (userId) => api.post('/conversations/direct', { userId }),
  createGroup: (name, memberIds) => api.post('/conversations/group', { name, memberIds }),
  getMessages: (id, page = 0, size = 50) => api.get(`/conversations/${id}/messages?page=${page}&size=${size}`),
  markAsRead: (id, messageId) => api.post(`/conversations/${id}/read`, { messageId }),
};

export const messageAPI = {
  sendMessage: (data) => api.post('/messages', data),
  updateStatus: (messageId, status) => api.post('/messages/status', { messageId, status }),
  clearChat: (conversationId) => api.post(`/messages/clear/${conversationId}`),
};

export const fileAPI = {
  upload: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/files', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },
};

export default api;