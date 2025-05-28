import axios from 'axios';

// Spring Boot backend URL
const API_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor to include auth token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auth services
export const authService = {
  login: async (username: string, password: string) => {
    const response = await api.post('/auth/login', { username, password });
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
    }
    return response.data;
  },

  register: async (userData: {
    username: string;
    email: string;
    password: string;
    phone?: string;
    role: string;
  }) => {
    const response = await api.post('/api/users/register', userData);
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  changePassword: async (userId: string, newPassword: string) => {
    const response = await api.put(`/api/users/update/${userId}`, {
      password: newPassword,
    });
    return response.data;
  },

  deleteAccount: async () => {
    const response = await api.delete('/auth/account');
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    return response.data;
  },
};

// User services
export const userService = {
  getProfile: async (userId: string) => {
    const response = await api.get(`/api/users/details/${userId}`);
    return response.data;
  },

  getBugs: async (userId: string) => {
    const response = await api.get(`/api/bugs/user/${userId}`);
    return response.data;
  },
};

export const voteService = {
  voteBug: async (bugId: number, userId: string, voteType: 'upvote' | 'downvote') => {
    return api.post(`/api/votes/bug/${bugId}`, { userId, voteType });
  },
  voteComment: async (commentId: number, userId: string, voteType: 'upvote' | 'downvote') => {
    return api.post(`/api/votes/create`, {
      userId,
      commentId,
      voteType: voteType === 'upvote' ? 'upvote' : 'downvote'
    });
  },
  acceptComment: async (bugId: number, commentId: number, userId: string) => {
    return api.post(`/api/bugs/${bugId}/accept/${commentId}`, { userId });
  }
};

export default api; 