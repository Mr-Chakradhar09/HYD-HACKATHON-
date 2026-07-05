import React from 'react';
import { Box, Card, CardContent, Typography, Grid, Chip, LinearProgress } from '@mui/material';
import { PeopleOutlined, TrendingUpOutlined, AssignmentOutlined, AutoAwesomeOutlined, AccessTimeOutlined, EmojiEventsOutlined, CheckCircleOutlined } from '@mui/icons-material';
import { useSelector } from 'react-redux';
import { BarChart, Bar, PieChart, Pie, Cell, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { GLOBAL_STATS, LOCATION_STATS, PARTICIPATION_DATA, MONTHLY_TREND } from '../data/dummyData';
import { api } from '../services/api';

const COLORS = { primary: '#6C63FF', secondary: '#00D9A6', warning: '#FFB547', error: '#FF6B6B', info: '#63B3ED' };

function KPICard({ icon, value, label, color, delay = 0 }) {
  return (
    <Card className="fade-in-up" sx={{ animationDelay: `${delay}ms` }}>
      <CardContent sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Box sx={{ width: 48, height: 48, borderRadius: 2, bgcolor: `${color}15`, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            {React.cloneElement(icon, { sx: { color, fontSize: 24 } })}
          </Box>
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 800, color: '#F1F5F9' }}>{value}</Typography>
            <Typography variant="body2" sx={{ color: '#94A3B8' }}>{label}</Typography>
          </Box>
          <TrendingUpOutlined sx={{ ml: 'auto', color: COLORS.secondary, fontSize: 20 }} />
        </Box>
      </CardContent>
    </Card>
  );
}

function EmployeeDashboard({ user }) {
  const [dbSubmitted, setDbSubmitted] = React.useState(false);
  const [loading, setLoading] = React.useState(true);
  const [surveyId, setSurveyId] = React.useState(null);
  const responses = useSelector((s) => s.survey.responses || []);

  React.useEffect(() => {
    let active = true;
    const fetchStatus = async () => {
      try {
        const activeSurvey = await api.getActiveSurvey();
        if (activeSurvey && activeSurvey.id) {
          if (active) setSurveyId(activeSurvey.id);
          const status = await api.getSubmissionStatus(activeSurvey.id);
          if (active && status && status.submitted) {
            setDbSubmitted(true);
          }
        }
      } catch (err) {
        console.warn("Could not query survey status from backend", err.message);
      } finally {
        if (active) setLoading(false);
      }
    };
    fetchStatus();
    return () => { active = false; };
  }, [user]);

  const localSubmitted = surveyId ? localStorage.getItem(`survey_submitted_${user.employeeId}_${surveyId}`) === 'true' : false;
  // If responses has an entry matching the current surveyId, it means they just submitted it in this session.
  const hasSubmittedSession = responses.some(r => r.surveyId === surveyId);
  const hasSubmitted = hasSubmittedSession || localSubmitted || dbSubmitted;
  
  return (
    <Box>
      <Card className="fade-in-up" sx={{ mb: 3, background: 'linear-gradient(135deg, rgba(108,99,255,0.15) 0%, rgba(0,217,166,0.08) 100%)' }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h4" sx={{ fontWeight: 800 }}>Welcome back, {user.name}! 👋</Typography>
          <Typography variant="body1" sx={{ color: '#94A3B8', mt: 1 }}>
            {hasSubmitted 
              ? "Thank you for completing this month's survey! Your feedback is being processed by our AI insights engine."
              : "Your voice matters. Complete this month's pulse survey to help shape a better workplace."
            }
          </Typography>
        </CardContent>
      </Card>
      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <KPICard 
            icon={<AssignmentOutlined />} 
            value={loading ? 'Checking...' : (hasSubmitted ? 'Completed' : 'Pending')} 
            label="July Survey Status" 
            color={hasSubmitted ? COLORS.secondary : COLORS.warning} 
          />
        </Grid>
        <Grid item xs={12} md={4}>
          <KPICard 
            icon={<EmojiEventsOutlined />} 
            value="July" 
            label="Current Survey Month" 
            color={COLORS.primary} 
            delay={100} 
          />
        </Grid>
        <Grid item xs={12} md={4}>
          {hasSubmitted ? (
            <KPICard 
              icon={<CheckCircleOutlined />} 
              value="100%" 
              label="Participation Complete" 
              color={COLORS.secondary} 
              delay={200} 
            />
          ) : (
            <KPICard 
              icon={<AccessTimeOutlined />} 
              value="11 Days" 
              label="Until Survey Closes" 
              color={COLORS.info} 
              delay={200} 
            />
          )}
        </Grid>
      </Grid>
      <Card className="fade-in-up" sx={{ mt: 3, animationDelay: '300ms' }}>
        <CardContent sx={{ p: 3 }}>
          <Typography variant="h6" sx={{ mb: 1 }}>💡 Quick Insight</Typography>
          <Typography variant="body2" sx={{ color: '#94A3B8' }}>"Your feedback directly influences next month's survey questions through our AI analysis engine. The more detailed your responses, the better we can identify and address workplace concerns."</Typography>
        </CardContent>
      </Card>
    </Box>
  );
}

function HRDashboard({ user }) {
  const [loading, setLoading] = React.useState(true);
  const [stats, setStats] = React.useState({
    totalSubmissions: 0,
    globalPulseScore: 0.0,
    positiveSentimentPercent: 60,
    neutralSentimentPercent: 25,
    negativeSentimentPercent: 15,
  });

  const drafts = useSelector((s) => s.survey.drafts || []);
  const pendingDraftsCount = drafts.filter(d => !d.approved).length;

  React.useEffect(() => {
    let active = true;
    const fetchHRStats = async () => {
      try {
        const res = await api.getReportDashboardOverview('2026-07');
        if (active && res) {
          setStats({
            totalSubmissions: res.totalResponses || 0,
            globalPulseScore: res.pulseScore || 0.0,
            positiveSentimentPercent: res.positiveSentiment || 60,
            neutralSentimentPercent: res.neutralSentiment || 25,
            negativeSentimentPercent: res.negativeSentiment || 15,
          });
        }
      } catch (err) {
        console.warn("Failed to fetch live HR metrics, using local fallback", err.message);
        if (active) {
          const fallback = LOCATION_STATS[user.location] || GLOBAL_STATS;
          setStats(fallback);
        }
      } finally {
        if (active) setLoading(false);
      }
    };
    fetchHRStats();
    return () => { active = false; };
  }, [user]);

  const sentimentData = [
    { name: 'Positive', value: stats.positiveSentimentPercent || 60, color: COLORS.secondary },
    { name: 'Neutral', value: stats.neutralSentimentPercent || 25, color: COLORS.warning },
    { name: 'Negative', value: stats.negativeSentimentPercent || 15, color: COLORS.error },
  ];

  return (
    <Box>
      <Card className="fade-in-up" sx={{ mb: 3, background: 'linear-gradient(135deg, rgba(108,99,255,0.15) 0%, rgba(0,217,166,0.08) 100%)' }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h4" sx={{ fontWeight: 800 }}>Hello, {user.name}! 📊</Typography>
          <Typography variant="body1" sx={{ color: '#94A3B8', mt: 1 }}>Here's the pulse overview for <Chip label={user.location} size="small" sx={{ ml: 1, bgcolor: 'rgba(108,99,255,0.15)', color: '#6C63FF' }} /></Typography>
        </CardContent>
      </Card>
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={6} md={3}><KPICard icon={<PeopleOutlined />} value={loading ? '...' : stats.totalSubmissions} label="Total Responses" color={COLORS.primary} /></Grid>
        <Grid item xs={6} md={3}><KPICard icon={<TrendingUpOutlined />} value={loading ? '...' : stats.globalPulseScore} label="Avg Pulse Score" color={COLORS.secondary} delay={100} /></Grid>
        <Grid item xs={6} md={3}><KPICard icon={<AutoAwesomeOutlined />} value={pendingDraftsCount} label="Pending Drafts" color={COLORS.warning} delay={200} /></Grid>
        <Grid item xs={6} md={3}><KPICard icon={<AssignmentOutlined />} value="2" label="Active Surveys" color={COLORS.info} delay={300} /></Grid>
      </Grid>
      <Grid container spacing={3}>
        <Grid item xs={12} md={7}>
          <Card className="fade-in-up" sx={{ animationDelay: '400ms' }}><CardContent sx={{ p: 3 }}>
            <Typography variant="h6" sx={{ mb: 2 }}>Survey Participation Trend</Typography>
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={PARTICIPATION_DATA}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
                <XAxis dataKey="month" stroke="#94A3B8" fontSize={12} />
                <YAxis stroke="#94A3B8" fontSize={12} />
                <Tooltip contentStyle={{ background: '#1E293B', border: '1px solid rgba(108,99,255,0.2)', borderRadius: 8 }} />
                <Bar dataKey="responses" fill="#6C63FF" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent></Card>
        </Grid>
        <Grid item xs={12} md={5}>
          <Card className="fade-in-up" sx={{ animationDelay: '500ms' }}><CardContent sx={{ p: 3 }}>
            <Typography variant="h6" sx={{ mb: 2 }}>Sentiment Breakdown</Typography>
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie data={sentimentData} cx="50%" cy="50%" innerRadius={60} outerRadius={100} paddingAngle={4} dataKey="value">
                  {sentimentData.map((entry, i) => <Cell key={i} fill={entry.color} />)}
                </Pie>
                <Tooltip contentStyle={{ background: '#1E293B', border: '1px solid rgba(108,99,255,0.2)', borderRadius: 8 }} />
                <Legend formatter={(v) => <span style={{ color: '#94A3B8', fontSize: 12 }}>{v}</span>} />
              </PieChart>
            </ResponsiveContainer>
          </CardContent></Card>
        </Grid>
      </Grid>
    </Box>
  );
}

function GlobalHRDashboard({ user }) {
  const [loading, setLoading] = React.useState(true);
  const [stats, setStats] = React.useState({
    totalSubmissions: 0,
    globalPulseScore: 0.0,
    npsScore: 0,
    positiveSentimentPercent: 0,
  });

  React.useEffect(() => {
    let active = true;
    const fetchGlobalStats = async () => {
      try {
        const res = await api.getReportDashboardOverview('2026-07');
        if (active && res) {
          setStats({
            totalSubmissions: res.totalResponses || 0,
            globalPulseScore: res.pulseScore || 0.0,
            npsScore: Math.round((res.positiveSentiment || 0) - (res.negativeSentiment || 0)),
            positiveSentimentPercent: res.positiveSentiment || 0,
          });
        }
      } catch (err) {
        console.warn("Failed to fetch live Global HR metrics, using local fallback", err.message);
        if (active) setStats(GLOBAL_STATS);
      } finally {
        if (active) setLoading(false);
      }
    };
    fetchGlobalStats();
    return () => { active = false; };
  }, [user]);

  const locations = Object.values(LOCATION_STATS);
  return (
    <Box>
      <Card className="fade-in-up" sx={{ mb: 3, background: 'linear-gradient(135deg, rgba(108,99,255,0.15) 0%, rgba(0,217,166,0.08) 100%)' }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h4" sx={{ fontWeight: 800 }}>Global HR Overview 🌍</Typography>
          <Typography variant="body1" sx={{ color: '#94A3B8', mt: 1 }}>Organization-wide sentiment across all locations for July 2026.</Typography>
        </CardContent>
      </Card>
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={6} md={3}><KPICard icon={<PeopleOutlined />} value={loading ? '...' : stats.totalSubmissions} label="Total Responses" color={COLORS.primary} /></Grid>
        <Grid item xs={6} md={3}><KPICard icon={<TrendingUpOutlined />} value={loading ? '...' : stats.globalPulseScore} label="Global Pulse" color={COLORS.secondary} delay={100} /></Grid>
        <Grid item xs={6} md={3}><KPICard icon={<EmojiEventsOutlined />} value={loading ? '...' : `${stats.npsScore}%`} label="NPS Score" color={COLORS.warning} delay={200} /></Grid>
        <Grid item xs={6} md={3}><KPICard icon={<AutoAwesomeOutlined />} value={loading ? '...' : `${stats.positiveSentimentPercent}%`} label="Positive Sentiment" color={COLORS.info} delay={300} /></Grid>
      </Grid>
      <Grid container spacing={3} sx={{ mb: 3 }}>
        {locations.map((loc, i) => (
          <Grid item xs={12} md={4} key={loc.location}>
            <Card className="fade-in-up" sx={{ animationDelay: `${400 + i * 100}ms`, borderLeft: `3px solid ${[COLORS.primary, COLORS.secondary, COLORS.warning][i]}` }}>
              <CardContent sx={{ p: 3 }}>
                <Typography variant="h6">{loc.location}</Typography>
                <Typography variant="h3" sx={{ fontWeight: 800, my: 1, color: [COLORS.primary, COLORS.secondary, COLORS.warning][i] }}>{loc.globalPulseScore}</Typography>
                <Box sx={{ display: 'flex', gap: 2 }}>
                  <Typography variant="caption" sx={{ color: '#94A3B8' }}>NPS: {loc.npsScore}%</Typography>
                  <Typography variant="caption" sx={{ color: '#94A3B8' }}>Responses: {loc.totalSubmissions}</Typography>
                </Box>
                <LinearProgress variant="determinate" value={loc.positiveSentimentPercent} sx={{ mt: 2, height: 6, borderRadius: 3, bgcolor: 'rgba(255,255,255,0.06)', '& .MuiLinearProgress-bar': { bgcolor: [COLORS.primary, COLORS.secondary, COLORS.warning][i] } }} />
                <Typography variant="caption" sx={{ color: '#94A3B8', mt: 0.5, display: 'block' }}>{loc.positiveSentimentPercent}% positive sentiment</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
      <Card className="fade-in-up" sx={{ animationDelay: '700ms' }}>
        <CardContent sx={{ p: 3 }}>
          <Typography variant="h6" sx={{ mb: 2 }}>Pulse Score Trend by Location</Typography>
          <ResponsiveContainer width="100%" height={320}>
            <LineChart data={MONTHLY_TREND}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
              <XAxis dataKey="month" stroke="#94A3B8" fontSize={12} />
              <YAxis stroke="#94A3B8" fontSize={12} domain={[6, 10]} />
              <Tooltip contentStyle={{ background: '#1E293B', border: '1px solid rgba(108,99,255,0.2)', borderRadius: 8 }} />
              <Legend formatter={(v) => <span style={{ color: '#94A3B8', fontSize: 12 }}>{v}</span>} />
              <Line type="monotone" dataKey="Chennai" stroke={COLORS.primary} strokeWidth={2} dot={{ r: 4 }} />
              <Line type="monotone" dataKey="Pune" stroke={COLORS.secondary} strokeWidth={2} dot={{ r: 4 }} />
              <Line type="monotone" dataKey="Hyderabad" stroke={COLORS.warning} strokeWidth={2} dot={{ r: 4 }} />
            </LineChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>
    </Box>
  );
}

export default function DashboardPage() {
  const { user, role } = useSelector((s) => s.auth);
  if (role === 'GLOBAL_HR') return <GlobalHRDashboard user={user} />;
  if (role === 'HR') return <HRDashboard user={user} />;
  return <EmployeeDashboard user={user} />;
}
