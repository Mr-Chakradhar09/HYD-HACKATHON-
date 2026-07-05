import React, { useState, useEffect } from 'react';
import { Box, Card, CardContent, Typography, Button, Chip, Stepper, Step, StepLabel, TextField, CircularProgress } from '@mui/material';
import { CheckCircleOutlined, ArrowForwardOutlined, ArrowBackOutlined, CelebrationOutlined } from '@mui/icons-material';
import { useSelector, useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { setActiveSurvey, addResponse } from '../store/surveySlice';
import { login } from '../store/authSlice';
import { addNotification } from '../store/notificationSlice';
import { BASELINE_SURVEYS } from '../data/dummyData';
import { api } from '../services/api';

const CATEGORY_COLORS = { 
  Collaboration: '#6C63FF', 
  'Work-Life Balance': '#00D9A6', 
  Growth: '#FFB547', 
  Leadership: '#63B3ED', 
  Compensation: '#FF6B6B', 
  Onboarding: '#A78BFA',
  ONBOARDING: '#A78BFA'
};

export default function SurveyPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const user = useSelector((s) => s.auth.user);
  const responses = useSelector((s) => s.survey.responses || []);
  
  const [survey, setSurvey] = useState(null);
  const [loading, setLoading] = useState(true);
  const [step, setStep] = useState(0);
  const [ratings, setRatings] = useState({});
  const [comment, setComment] = useState('');
  const [submitted, setSubmitted] = useState(false);

  const isOnboarding = user?.status === 'PENDING_ONBOARDING';

  useEffect(() => {
    const fetchSurvey = async () => {
      try {
        let surveyData = null;
        if (isOnboarding) {
          surveyData = await api.getOnboardingSurvey();
        } else {
          surveyData = await api.getActiveSurvey();
        }

        if (surveyData) {
          setSurvey(surveyData);
          dispatch(setActiveSurvey(surveyData));
          setLoading(false);
          return;
        }
      } catch (err) {
        console.warn("Backend survey fetch failed. Loading mock fallback.", err.message);
      }

      // Mock fallback
      const localSurvey = isOnboarding 
        ? {
            id: 'SV-ONBOARD',
            title: 'Virtusa New Hire Onboarding Survey',
            location: 'ONBOARDING',
            monthYear: '07-2026',
            active: true,
            questions: [
              { id: 'Q1', text: 'The recruitment and hiring process was smooth and transparent.', category: 'ONBOARDING', themeTag: 'Baseline' },
              { id: 'Q2', text: 'I received all necessary laptop hardware, accounts, and system access on my first day.', category: 'ONBOARDING', themeTag: 'Baseline' },
              { id: 'Q3', text: 'The onboarding induction sessions and training were helpful and informative.', category: 'ONBOARDING', themeTag: 'Baseline' },
              { id: 'Q4', text: 'I feel welcomed by my team members and project lead.', category: 'ONBOARDING', themeTag: 'Baseline' },
              { id: 'Q5', text: 'I understand what is expected of me in my role during the first 90 days.', category: 'ONBOARDING', themeTag: 'Baseline' },
            ]
          }
        : (BASELINE_SURVEYS[user?.location] || BASELINE_SURVEYS.Chennai);

      setSurvey(localSurvey);
      dispatch(setActiveSurvey(localSurvey));
      setLoading(false);
    };

    fetchSurvey();
  }, [user]);

  const hasSubmitted = responses.length > 0 || submitted;

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress sx={{ color: '#6C63FF' }} />
      </Box>
    );
  }

  if (hasSubmitted) {
    return (
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '60vh' }}>
        <Card className="fade-in-up pulse-glow" sx={{ maxWidth: 500, textAlign: 'center', background: 'linear-gradient(135deg, rgba(0,217,166,0.1) 0%, rgba(108,99,255,0.08) 100%)' }}>
          <CardContent sx={{ p: 5 }}>
            <CelebrationOutlined sx={{ fontSize: 64, color: '#00D9A6', mb: 2 }} />
            <Typography variant="h4" sx={{ fontWeight: 800, mb: 1 }}>All Set! 🎉</Typography>
            <Typography variant="body1" sx={{ color: '#94A3B8', mb: 3 }}>
              {isOnboarding 
                ? 'Your onboarding survey has been recorded. Your account is now fully active! Welcome to the Virtusa team.' 
                : 'Your monthly pulse survey response has been submitted successfully. Thank you for your feedback!'}
            </Typography>
            <Button variant="contained" onClick={() => navigate('/app/dashboard')} sx={{ background: 'linear-gradient(135deg, #6C63FF, #5A52E0)' }}>Go to Dashboard</Button>
          </CardContent>
        </Card>
      </Box>
    );
  }

  const questions = survey?.questions || [];
  const currentQ = questions[step];
  const isLast = step === questions.length - 1;

  const handleSubmit = async () => {
    const responsePayload = {
      id: `RESP-${Date.now()}`,
      surveyId: survey.id,
      username: user.username,
      location: user.location,
      ratings,
      openEndedText: comment
    };

    try {
      // 1. Submit response to backend
      await api.submitSurveyResponse(survey.id.toString(), ratings, comment);
      
      // 2. If it's an onboarding survey, complete onboarding activation
      if (isOnboarding) {
        await api.completeOnboarding(user.email);
        dispatch(login({ ...user, status: 'ACTIVE' }));
        localStorage.setItem(`onboarded_${user.employeeId}`, 'true');
      } else {
        localStorage.setItem(`survey_submitted_${user.employeeId}_${survey.id}`, 'true');
      }

      dispatch(addResponse(responsePayload));
      dispatch(addNotification({ 
        id: `N-${Date.now()}`, 
        recipient: user.username, 
        subject: isOnboarding ? 'Onboarding Survey Completed' : 'Survey Submitted', 
        message: isOnboarding 
          ? 'Welcome to Virtusa! Your onboarding feedback has been successfully processed.' 
          : `Your survey response for ${user.location} has been submitted.`, 
        timestamp: new Date().toISOString() 
      }));
      
      toast.success(isOnboarding ? 'Onboarding completed successfully! Welcome to Virtusa! 🎉' : 'Survey submitted successfully! Thank you!');
      setSubmitted(true);
      return;
    } catch (err) {
      console.warn("Backend survey submit failed. Doing local fallback...", err.message);
    }

    // Local Fallback
    if (isOnboarding) {
      dispatch(login({ ...user, status: 'ACTIVE' }));
      localStorage.setItem(`onboarded_${user.employeeId}`, 'true');
    } else {
      localStorage.setItem(`survey_submitted_${user.employeeId}_${survey.id}`, 'true');
    }
    dispatch(addResponse(responsePayload));
    dispatch(addNotification({ 
      id: `N-${Date.now()}`, 
      recipient: user.username, 
      subject: isOnboarding ? 'Onboarding Survey Completed' : 'Survey Submitted', 
      message: 'Recorded locally.', 
      timestamp: new Date().toISOString() 
    }));
    toast.success('Survey submitted successfully! 🎉');
    setSubmitted(true);
  };

  if (questions.length === 0) {
    return (
      <Box sx={{ textAlign: 'center', py: 5 }}>
        <Typography variant="h6" sx={{ color: '#94A3B8' }}>No questions found in this survey.</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ maxWidth: 800, mx: 'auto' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }} className="fade-in-up">
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 700 }}>{survey.title}</Typography>
          <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
            <Chip label={survey.location} size="small" sx={{ bgcolor: 'rgba(108,99,255,0.15)', color: '#6C63FF' }} />
            <Chip label={survey.monthYear || `${survey.month}-${survey.year}`} size="small" variant="outlined" sx={{ borderColor: 'rgba(255,255,255,0.12)' }} />
          </Box>
        </Box>
      </Box>

      <Stepper activeStep={step} alternativeLabel sx={{ mb: 4, '& .MuiStepIcon-root.Mui-active': { color: '#6C63FF' }, '& .MuiStepIcon-root.Mui-completed': { color: '#00D9A6' } }}>
        {questions.map((q, i) => <Step key={q.id || i}><StepLabel>{''}</StepLabel></Step>)}
      </Stepper>

      <Card className="slide-in-right" key={step} sx={{ mb: 3 }}>
        <CardContent sx={{ p: 4 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
            <Chip label={currentQ.category} size="small" sx={{ bgcolor: `${CATEGORY_COLORS[currentQ.category] || '#6C63FF'}20`, color: CATEGORY_COLORS[currentQ.category] || '#6C63FF' }} />
            <Typography variant="caption" sx={{ color: '#94A3B8' }}>Question {step + 1} of {questions.length}</Typography>
          </Box>
          <Typography variant="h6" sx={{ mb: 3, fontWeight: 600, lineHeight: 1.5 }}>{currentQ.text || currentQ.questionText}</Typography>

          <Typography variant="body2" sx={{ color: '#94A3B8', mb: 1.5 }}>Rate from 1 (Strongly Disagree) to 10 (Strongly Agree)</Typography>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 3 }}>
            {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((n) => (
              <Button key={n} variant={ratings[currentQ.id || currentQ.questionText] === n ? 'contained' : 'outlined'} onClick={() => setRatings({ ...ratings, [currentQ.id || currentQ.questionText]: n })}
                sx={{
                  minWidth: 44, height: 44, borderRadius: 2, fontWeight: 700,
                  borderColor: ratings[currentQ.id || currentQ.questionText] === n ? 'transparent' : 'rgba(255,255,255,0.12)',
                  background: ratings[currentQ.id || currentQ.questionText] === n ? (n >= 7 ? 'linear-gradient(135deg, #00D9A6, #00B88A)' : n >= 4 ? 'linear-gradient(135deg, #FFB547, #E09C30)' : 'linear-gradient(135deg, #FF6B6B, #E05555)') : 'transparent',
                  color: ratings[currentQ.id || currentQ.questionText] === n ? '#fff' : '#94A3B8',
                  '&:hover': { borderColor: '#6C63FF', bgcolor: 'rgba(108,99,255,0.08)' },
                }}>
                {n}
              </Button>
            ))}
          </Box>

          {isLast && (
            <TextField fullWidth multiline rows={4} label="Share your onboarding experience / thoughts (optional)" value={comment} onChange={(e) => setComment(e.target.value)}
              placeholder="Tell us more about your experience — what's working well, what could improve..." sx={{ mt: 1 }} />
          )}
        </CardContent>
      </Card>

      <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
        <Button disabled={step === 0} onClick={() => setStep(step - 1)} startIcon={<ArrowBackOutlined />} sx={{ color: '#94A3B8' }}>Previous</Button>
        {isLast ? (
          <Button variant="contained" onClick={handleSubmit} disabled={Object.keys(ratings).length < questions.length} endIcon={<CheckCircleOutlined />}
            sx={{ background: 'linear-gradient(135deg, #00D9A6, #00B88A)', color: '#0A0E1A', '&:hover': { background: 'linear-gradient(135deg, #33E3BA, #00D9A6)' } }}>
            Submit Survey
          </Button>
        ) : (
          <Button variant="contained" onClick={() => setStep(step + 1)} disabled={!ratings[currentQ.id || currentQ.questionText]} endIcon={<ArrowForwardOutlined />}
            sx={{ background: 'linear-gradient(135deg, #6C63FF, #5A52E0)' }}>
            Next
          </Button>
        )}
      </Box>
    </Box>
  );
}
