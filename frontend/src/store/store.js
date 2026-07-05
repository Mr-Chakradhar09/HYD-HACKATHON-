import { configureStore } from '@reduxjs/toolkit';
import authReducer from './authSlice';
import surveyReducer from './surveySlice';
import reportReducer from './reportSlice';
import notificationReducer from './notificationSlice';

const store = configureStore({
  reducer: {
    auth: authReducer,
    survey: surveyReducer,
    report: reportReducer,
    notification: notificationReducer,
  },
});

export default store;
