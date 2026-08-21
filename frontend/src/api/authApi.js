import api from './axios';

export const signup = (email, password) =>
  api.post('/auth/signup', { email, password });

export const login = (email, password) =>
  api.post('/auth/login', { email, password });

export const refreshToken = (refreshToken) =>
  api.post('/auth/refresh', { refreshToken });

export const logout = (refreshToken) =>
  api.post('/auth/logout', { refreshToken });

export const forgotPassword = (email) =>
  api.post('/auth/forgot-password', { email });

export const resetPassword = (token, newPassword) =>
  api.post('/auth/reset-password', { token, newPassword });

export const getCurrentUser = () =>
  api.get('/users/me');

export const getAllUsers = () =>
  api.get('/admin/users');

export const changePassword = (currentPassword, newPassword) =>
  api.put('/users/me/password', { currentPassword, newPassword });