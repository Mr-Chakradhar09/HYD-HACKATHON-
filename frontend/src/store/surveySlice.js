import { createSlice } from '@reduxjs/toolkit';

const surveySlice = createSlice({
  name: 'survey',
  initialState: { activeSurvey: null, responses: [], drafts: [], loading: false },
  reducers: {
    setActiveSurvey: (state, action) => { state.activeSurvey = action.payload; },
    addResponse: (state, action) => { state.responses.push(action.payload); },
    setDrafts: (state, action) => { state.drafts = action.payload; },
    approveDraft: (state, action) => {
      const draft = state.drafts.find(d => d.id === action.payload);
      if (draft) draft.approved = true;
    },
    setLoading: (state, action) => { state.loading = action.payload; },
  },
});

export const { setActiveSurvey, addResponse, setDrafts, approveDraft, setLoading } = surveySlice.actions;
export default surveySlice.reducer;
