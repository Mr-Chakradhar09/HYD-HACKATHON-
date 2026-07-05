import { createSlice } from '@reduxjs/toolkit';

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: null,
    isAuthenticated: false,
    role: null,
    // HR-initiated registrations: email -> { email, name, location, role, registeredByHR, status }
    pendingRegistrations: {},
    // Fully registered users (email -> user object with password)
    registeredUsers: {},
  },
  reducers: {
    login: (state, action) => {
      state.user = action.payload;
      state.isAuthenticated = true;
      state.role = action.payload.role;
    },
    logout: (state) => {
      state.user = null;
      state.isAuthenticated = false;
      state.role = null;
    },
    // HR creates a pending registration with employee email
    hrRegisterEmployee: (state, action) => {
      const { email, name, location } = action.payload;
      state.pendingRegistrations[email.toLowerCase()] = {
        email: email.toLowerCase(),
        name,
        location,
        role: 'EMPLOYEE',
        registeredByHR: state.user?.name || 'HR Admin',
        status: 'PENDING',
        createdAt: new Date().toISOString(),
      };
    },
    // Employee completes registration by setting password
    completeRegistration: (state, action) => {
      const { email, password } = action.payload;
      const pending = state.pendingRegistrations[email.toLowerCase()];
      if (pending) {
        const newUser = {
          id: `USR-${Date.now().toString().slice(-4)}`,
          username: email.split('@')[0],
          email: pending.email,
          name: pending.name,
          location: pending.location,
          role: 'EMPLOYEE',
          password,
        };
        state.registeredUsers[email.toLowerCase()] = newUser;
        state.pendingRegistrations[email.toLowerCase()].status = 'COMPLETED';
      }
    },
  },
});

export const { login, logout, hrRegisterEmployee, completeRegistration } = authSlice.actions;
export default authSlice.reducer;
