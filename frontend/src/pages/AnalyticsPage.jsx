import React, { useEffect, useState } from 'react';
import { Box, Card, CardContent, Typography, Grid, LinearProgress, CircularProgress } from '@mui/material';
import { TrendingUpOutlined, PeopleOutlined, EmojiEventsOutlined, SentimentSatisfiedAltOutlined } from '@mui/icons-material';
import { useDispatch, useSelector } from 'react-redux';
import { BarChart, Bar, AreaChart, Area, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { setGlobalStats, setLocationStats } from '../store/reportSlice';
import { GLOBAL_STATS, LOCATION_STATS, MONTHLY_TREND } from '../data/dummyData';
import { api } from '../services/api';

const COLORS = { primary: '#6C63FF', secondary: '#00D9A6', warning: '#FFB547', error: '#FF6B6B', info: '#63B3ED' };

export default function AnalyticsPage() {
  const dispatch = useDispatch();
  const globalStats = useSelector((s) => s.report.globalStats) || GLOBAL_STATS;
  const locationStats = useSelector((s) => s.report.locationStats) || LOCATION_STATS;
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadAnalytics = async () => {
      try {
        const overview = await api.getReportDashboardOverview();
        const locations = await api.getReportDashboardLocations();
        if (overview) {
          dispatch(setGlobalStats(overview));
          dispatch(setLocationStats(locations));
          setLoading(false);
          return;
        }
      } catch (err) {
        console.warn("Backend analytics query failed. Using local fallback.", err.message);
      }
      dispatch(setGlobalStats(GLOBAL_STATS));
      dispatch(setLocationStats(LOCATION_STATS));
      setLoading(false);
    };

    loadAnalytics();
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress sx={{ color: '#6C63FF' }} />
      </Box>
    );
  }

  const locData = Object.values(locationStats || LOCATION_STATS);
  const barData = locData.map(l => ({ name: l.location, 'Pulse Score': l.globalPulseScore || l.averagePulseScore, NPS: (l.npsScore || 0) / 10 }));
  const sentimentData = [
    { name: 'Positive', value: globalStats.positiveSentimentPercent || 65, color: COLORS.secondary },
    { name: 'Neutral', value: globalStats.neutralSentimentPercent || 20, color: COLORS.warning },
    { name: 'Negative', value: globalStats.negativeSentimentPercent || 15, color: COLORS.error },
  ];

  return (
    <Box>
      <Grid container spacing={3} sx={{ mb: 3 }}>
        {[
          { icon: <TrendingUpOutlined />, value: globalStats.globalPulseScore, label: 'Global Pulse Score', color: COLORS.primary },
          { icon: <EmojiEventsOutlined />, value: `${globalStats.npsScore}%`, label: 'Net Promoter Score', color: COLORS.secondary },
          { icon: <PeopleOutlined />, value: globalStats.totalSubmissions || globalStats.totalResponses, label: 'Total Submissions', color: COLORS.warning },
          { icon: <SentimentSatisfiedAltOutlined />, value: `${globalStats.positiveSentimentPercent}%`, label: 'Positive Sentiment', color: COLORS.info },
        ].map((kpi, i) => (
          <Grid item xs={6} md={3} key={i}>
            <Card className="fade-in-up" sx={{ animationDelay: `${i * 100}ms` }}>
              <CardContent sx={{ p: 3 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box sx={{ width: 48, height: 48, borderRadius: 2, bgcolor: `${kpi.color}15`, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    {React.cloneElement(kpi.icon, { sx: { color: kpi.color, fontSize: 24 } })}
                  </Box>
                  <Box>
                    <Typography variant="h4" sx={{ fontWeight: 800, color: '#F1F5F9' }}>{kpi.value}</Typography>
                    <Typography variant="body2" sx={{ color: '#94A3B8' }}>{kpi.label}</Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} md={7}>
          <Card className="fade-in-up" sx={{ animationDelay: '400ms' }}>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="h6" sx={{ mb: 2 }}>Pulse Score Trend by Location</Typography>
              <ResponsiveContainer width="100%" height={320}>
                <AreaChart data={MONTHLY_TREND}>
                  <defs>
                    <linearGradient id="colorChennai" x1="0" y1="0" x2="0" y2="1"><stop offset="5%" stopColor={COLORS.primary} stopOpacity={0.3} /><stop offset="95%" stopColor={COLORS.primary} stopOpacity={0} /></linearGradient>
                    <linearGradient id="colorPune" x1="0" y1="0" x2="0" y2="1"><stop offset="5%" stopColor={COLORS.secondary} stopOpacity={0.3} /><stop offset="95%" stopColor={COLORS.secondary} stopOpacity={0} /></linearGradient>
                    <linearGradient id="colorHyd" x1="0" y1="0" x2="0" y2="1"><stop offset="5%" stopColor={COLORS.warning} stopOpacity={0.3} /><stop offset="95%" stopColor={COLORS.warning} stopOpacity={0} /></linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
                  <XAxis dataKey="month" stroke="#94A3B8" fontSize={12} />
                  <YAxis stroke="#94A3B8" fontSize={12} domain={[6, 10]} />
                  <Tooltip contentStyle={{ background: '#1E293B', border: '1px solid rgba(108,99,255,0.2)', borderRadius: 8 }} />
                  <Legend formatter={(v) => <span style={{ color: '#94A3B8', fontSize: 12 }}>{v}</span>} />
                  <Area type="monotone" dataKey="Chennai" stroke={COLORS.primary} fillOpacity={1} fill="url(#colorChennai)" strokeWidth={2} />
                  <Area type="monotone" dataKey="Pune" stroke={COLORS.secondary} fillOpacity={1} fill="url(#colorPune)" strokeWidth={2} />
                  <Area type="monotone" dataKey="Hyderabad" stroke={COLORS.warning} fillOpacity={1} fill="url(#colorHyd)" strokeWidth={2} />
                </AreaChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={5}>
          <Card className="fade-in-up" sx={{ animationDelay: '500ms' }}>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="h6" sx={{ mb: 2 }}>Global Sentiment Distribution</Typography>
              <ResponsiveContainer width="100%" height={320}>
                <PieChart>
                  <Pie data={sentimentData} cx="50%" cy="50%" innerRadius={70} outerRadius={110} paddingAngle={4} dataKey="value" label={({ name, value }) => `${name}: ${value}%`}>
                    {sentimentData.map((entry, i) => <Cell key={i} fill={entry.color} />)}
                  </Pie>
                  <Tooltip contentStyle={{ background: '#1E293B', border: '1px solid rgba(108,99,255,0.2)', borderRadius: 8 }} />
                </PieChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Card className="fade-in-up" sx={{ animationDelay: '600ms' }}>
        <CardContent sx={{ p: 3 }}>
          <Typography variant="h6" sx={{ mb: 2 }}>Location Comparison</Typography>
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={barData}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
              <XAxis dataKey="name" stroke="#94A3B8" fontSize={12} />
              <YAxis stroke="#94A3B8" fontSize={12} />
              <Tooltip contentStyle={{ background: '#1E293B', border: '1px solid rgba(108,99,255,0.2)', borderRadius: 8 }} />
              <Legend formatter={(v) => <span style={{ color: '#94A3B8', fontSize: 12 }}>{v}</span>} />
              <Bar dataKey="Pulse Score" fill={COLORS.primary} radius={[6, 6, 0, 0]} />
              <Bar dataKey="NPS" fill={COLORS.secondary} radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>
    </Box>
  );
}
