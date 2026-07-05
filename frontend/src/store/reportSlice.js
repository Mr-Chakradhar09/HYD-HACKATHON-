import { createSlice } from '@reduxjs/toolkit';

const reportSlice = createSlice({
  name: 'report',
  initialState: { globalStats: null, locationStats: {}, loading: false },
  reducers: {
    setGlobalStats: (state, action) => { state.globalStats = action.payload; },
    setLocationStats: (state, action) => { state.locationStats = action.payload; },
    setLoading: (state, action) => { state.loading = action.payload; },
  },
});

export const { setGlobalStats, setLocationStats, setLoading } = reportSlice.actions;
export default reportSlice.reducer;
