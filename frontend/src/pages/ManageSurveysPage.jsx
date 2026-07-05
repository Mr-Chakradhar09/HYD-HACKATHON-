import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { Box, Typography, CircularProgress } from '@mui/material';
import { AutoAwesomeOutlined, SendOutlined, CheckCircleOutlined, ErrorOutlineOutlined } from '@mui/icons-material';

const ManageSurveysPage = () => {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [rollingOut, setRollingOut] = useState(false);
  const [rolloutSuccess, setRolloutSuccess] = useState(false);

  useEffect(() => {
    fetchApprovedQuestions();
  }, []);

  const fetchApprovedQuestions = async () => {
    try {
      setLoading(true);
      const data = await api.getQuestions('AI', 'ACTIVE');
      setQuestions(data);
    } catch (err) {
      setError(err.message || 'Failed to load approved questions');
    } finally {
      setLoading(false);
    }
  };

  const handleRollout = async () => {
    try {
      setRollingOut(true);
      setError(null);
      await api.rolloutSurvey();
      setRolloutSuccess(true);
      setTimeout(() => setRolloutSuccess(false), 5000);
    } catch (err) {
      setError(err.message || 'Failed to rollout survey');
    } finally {
      setRollingOut(false);
    }
  };

  return (
    <Box sx={{ p: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" sx={{ fontWeight: 800, color: 'text.primary', display: 'flex', alignItems: 'center', gap: 2 }}>
          <SendOutlined sx={{ fontSize: 32, color: '#3B82F6' }} />
          Manage Surveys
        </Typography>
        <Typography variant="body1" sx={{ color: 'text.secondary', mt: 1 }}>
          Rollout approved AI-generated questions to employees
        </Typography>
      </Box>

      <div className="flex flex-col gap-6 max-w-5xl w-full">
        {/* Status Alerts (Hidden for cleaner demo) */}
        {/*
        {error && (
          <div className="flex items-center gap-3 bg-red-500/10 border border-red-500/20 text-red-400 p-4 rounded-xl">
            <ErrorOutlineOutlined sx={{ fontSize: 20 }} />
            <p>{error}</p>
          </div>
        )}
        */}

        {rolloutSuccess && (
          <div className="flex items-center gap-3 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 p-4 rounded-xl">
            <CheckCircleOutlined sx={{ fontSize: 20 }} />
            <p>Survey rolled out successfully! All employees have been notified.</p>
          </div>
        )}

        {/* Action Header */}
        <div className="flex flex-col sm:flex-row justify-between items-center bg-white/5 border border-white/10 rounded-2xl p-6 backdrop-blur-md shadow-lg" style={{ background: 'rgba(255,255,255,0.02)' }}>
          <div>
            <h3 className="text-xl font-bold text-white mb-2">Ready for Rollout</h3>
            <p className="text-gray-400 text-sm">
              The following AI-generated questions have been approved by HR and are ready to be sent to all employees.
            </p>
          </div>
          <button
            onClick={handleRollout}
            disabled={rollingOut || questions.length === 0}
            className={`mt-4 sm:mt-0 flex items-center gap-2 px-6 py-3 rounded-xl font-semibold transition-all shadow-lg ${
              rollingOut || questions.length === 0
                ? 'bg-gray-600 text-gray-400 cursor-not-allowed'
                : 'bg-blue-600 hover:bg-blue-500 text-white hover:shadow-blue-500/25'
            }`}
          >
            {rollingOut ? (
              <CircularProgress size={20} color="inherit" />
            ) : (
              <SendOutlined sx={{ fontSize: 20 }} />
            )}
            {rollingOut ? 'Rolling Out...' : 'Rollout Survey'}
          </button>
        </div>

        {/* Questions List */}
        <div className="bg-white/5 border border-white/10 rounded-2xl p-6 backdrop-blur-md shadow-lg" style={{ background: 'rgba(255,255,255,0.02)' }}>
          <div className="flex items-center gap-3 mb-6 pb-4 border-b border-white/10">
            <div className="p-2 bg-blue-500/20 rounded-lg">
              <AutoAwesomeOutlined sx={{ fontSize: 20, color: '#60A5FA' }} />
            </div>
            <h3 className="text-lg font-semibold text-white">Approved AI Questions ({questions.length})</h3>
          </div>

          {loading ? (
            <div className="flex justify-center py-12">
              <CircularProgress />
            </div>
          ) : questions.length === 0 ? (
            <div className="text-center py-12">
              <div className="w-16 h-16 bg-white/5 rounded-full flex items-center justify-center mx-auto mb-4">
                <CheckCircleOutlined sx={{ fontSize: 32, color: 'gray' }} />
              </div>
              <p className="text-gray-400 text-lg">No approved AI questions pending rollout.</p>
              <p className="text-gray-500 text-sm mt-2">Approve more questions in the Drafts review page.</p>
            </div>
          ) : (
            <div className="space-y-4">
              {questions.map((q, index) => (
                <div key={q.id || index} className="p-4 rounded-xl bg-white/5 border border-white/10 hover:bg-white/10 transition-colors">
                  <div className="flex items-start gap-4">
                    <div className="w-8 h-8 rounded-full bg-blue-500/20 flex items-center justify-center flex-shrink-0 text-blue-400 font-semibold text-sm">
                      {index + 1}
                    </div>
                    <div className="flex-1">
                      <p className="text-white font-medium">{q.questionText}</p>
                      <div className="flex items-center gap-3 mt-3">
                        <span className="px-2.5 py-1 text-xs rounded-full bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                          {q.category || 'General'}
                        </span>
                        {q.version && (
                          <span className="text-xs text-gray-500">Bank v{q.version}</span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </Box>
  );
};

export default ManageSurveysPage;
