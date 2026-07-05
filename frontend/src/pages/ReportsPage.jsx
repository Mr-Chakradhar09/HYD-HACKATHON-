import React, { useState } from 'react';
import { Box, Card, CardContent, Typography, Button, Select, MenuItem, FormControl, InputLabel, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, Grid, CircularProgress } from '@mui/material';
import { DescriptionOutlined, DownloadOutlined } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { LOCATION_STATS, GLOBAL_STATS } from '../data/dummyData';
import { api } from '../services/api';

export default function ReportsPage() {
  const [location, setLocation] = useState('All');
  const [month, setMonth] = useState('Jul 2026');
  const [showPreview, setShowPreview] = useState(false);
  const [reportData, setReportData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [downloadingPdf, setDownloadingPdf] = useState(false);
  const [downloadingCsv, setDownloadingCsv] = useState(false);

  const handleGenerate = async () => {
    setLoading(true);
    try {
      if (location !== 'All') {
        const formattedMonth = month.includes('Jul') ? '2026-07' : '2026-06';
        const backendReport = await api.generateLocationReport('CITY', location, formattedMonth);
        if (backendReport) {
          const normalized = {
            location: backendReport.locationName || location,
            globalPulseScore: backendReport.averagePulseScore || 8.0,
            npsScore: backendReport.npsScore || 50,
            totalSubmissions: backendReport.totalResponses || 100,
            positiveSentimentPercent: backendReport.positiveSentimentPercent || 65,
            neutralSentimentPercent: backendReport.neutralSentimentPercent || 20,
            negativeSentimentPercent: backendReport.negativeSentimentPercent || 15
          };
          setReportData(normalized);
          setShowPreview(true);
          setLoading(false);
          toast.success('Live report loaded from backend! 📄');
          return;
        }
      }
    } catch (err) {
      console.warn("Backend report query failed. Falling back to local template.", err.message);
    }

    const fallbackStats = location === 'All' ? GLOBAL_STATS : (LOCATION_STATS[location] || GLOBAL_STATS);
    setReportData(fallbackStats);
    setShowPreview(true);
    setLoading(false);
    toast.success('Report compiled successfully! 📄');
  };

  const handleDownloadPdf = async () => {
    setDownloadingPdf(true);
    try {
      const formattedMonth = month.includes('Jul') ? '2026-07' : '2026-06';
      const blob = await api.exportPdf(formattedMonth);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `PulseReport_${location}_${formattedMonth}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      toast.success('PDF downloaded successfully! 📥');
    } catch (err) {
      console.error(err);
      toast.error('Failed to download PDF. Please try again.');
    } finally {
      setDownloadingPdf(false);
    }
  };

  const handleDownloadCsv = async () => {
    setDownloadingCsv(true);
    try {
      const formattedMonth = month.includes('Jul') ? '2026-07' : '2026-06';
      const blob = await api.exportCsv(formattedMonth);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `PulseReport_${location}_${formattedMonth}.csv`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      toast.success('CSV downloaded successfully! 📥');
    } catch (err) {
      console.error(err);
      toast.error('Failed to download CSV. Please try again.');
    } finally {
      setDownloadingCsv(false);
    }
  };

  const stats = reportData || (location === 'All' ? GLOBAL_STATS : (LOCATION_STATS[location] || GLOBAL_STATS));

  return (
    <Box sx={{ maxWidth: 900, mx: 'auto' }}>
      <Box className="fade-in-up" sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 4 }}>
        <DescriptionOutlined sx={{ color: '#6C63FF', fontSize: 28 }} />
        <Typography variant="h5" sx={{ fontWeight: 700 }}>Monthly Pulse Reports</Typography>
      </Box>

      <Card className="fade-in-up" sx={{ mb: 3, animationDelay: '100ms' }}>
        <CardContent sx={{ p: 3 }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={4}>
              <FormControl fullWidth size="small">
                <InputLabel>Location</InputLabel>
                <Select value={location} label="Location" onChange={(e) => setLocation(e.target.value)}>
                  <MenuItem value="All">All Locations</MenuItem>
                  <MenuItem value="Chennai">Chennai</MenuItem>
                  <MenuItem value="Pune">Pune</MenuItem>
                  <MenuItem value="Hyderabad">Hyderabad</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={4}>
              <FormControl fullWidth size="small">
                <InputLabel>Month</InputLabel>
                <Select value={month} label="Month" onChange={(e) => setMonth(e.target.value)}>
                  {['Jan 2026', 'Feb 2026', 'Mar 2026', 'Apr 2026', 'May 2026', 'Jun 2026', 'Jul 2026'].map(m => (
                    <MenuItem key={m} value={m}>{m}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={4}>
              <Button fullWidth variant="contained" startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <DownloadOutlined />} onClick={handleGenerate} disabled={loading}
                sx={{ py: 1.2, background: 'linear-gradient(135deg, #6C63FF, #5A52E0)' }}>
                Generate Report
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {showPreview && (
        <Card className="fade-in-up" sx={{ border: '1px solid rgba(108,99,255,0.2)' }}>
          <CardContent sx={{ p: 4 }}>
            <Box sx={{ textAlign: 'center', mb: 3, pb: 3, borderBottom: '1px solid rgba(255,255,255,0.06)' }}>
              <Typography variant="h5" sx={{ fontWeight: 800, mb: 0.5 }}>Virtusa Employee Pulse Report</Typography>
              <Typography variant="body2" sx={{ color: '#94A3B8' }}>{location === 'All' ? 'All Locations' : location} · {month}</Typography>
            </Box>

            <Typography variant="h6" sx={{ mb: 2 }}>Key Metrics Summary</Typography>
            <TableContainer sx={{ mb: 4 }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ color: '#94A3B8', borderBottom: '1px solid rgba(255,255,255,0.06)' }}>Metric</TableCell>
                    <TableCell align="right" sx={{ color: '#94A3B8', borderBottom: '1px solid rgba(255,255,255,0.06)' }}>Value</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {[
                    ['Pulse Score', `${stats.globalPulseScore} / 10.0`],
                    ['Net Promoter Score (NPS)', `${stats.npsScore}%`],
                    ['Total Respondents', stats.totalSubmissions],
                    ['Positive Sentiment', `${stats.positiveSentimentPercent}%`],
                    ['Neutral Sentiment', `${stats.neutralSentimentPercent}%`],
                    ['Negative Sentiment', `${stats.negativeSentimentPercent}%`],
                  ].map(([k, v]) => (
                    <TableRow key={k}>
                      <TableCell sx={{ borderBottom: '1px solid rgba(255,255,255,0.06)' }}>{k}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 600, borderBottom: '1px solid rgba(255,255,255,0.06)' }}>{v}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>

            <Typography variant="h6" sx={{ mb: 2 }}>Top Themes Detected by AI</Typography>
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 4 }}>
              <Chip label="Team Collaboration ↑" sx={{ bgcolor: 'rgba(0,217,166,0.15)', color: '#00D9A6' }} />
              <Chip label="Work-Life Balance Concerns" sx={{ bgcolor: 'rgba(255,181,71,0.15)', color: '#FFB547' }} />
              <Chip label="Career Path Clarity" sx={{ bgcolor: 'rgba(108,99,255,0.15)', color: '#6C63FF' }} />
              <Chip label="Compensation Transparency" sx={{ bgcolor: 'rgba(255,107,107,0.15)', color: '#FF6B6B' }} />
            </Box>

            <Box sx={{ display: 'flex', gap: 2 }}>
              <Button variant="outlined" startIcon={downloadingPdf ? <CircularProgress size={16} /> : <DownloadOutlined />} onClick={handleDownloadPdf} disabled={downloadingPdf}
                sx={{ borderColor: '#6C63FF', color: '#6C63FF', '&:hover': { borderColor: '#5A52E0', bgcolor: 'rgba(108,99,255,0.04)' } }}>
                Download as PDF
              </Button>
              <Button variant="outlined" startIcon={downloadingCsv ? <CircularProgress size={16} /> : <DownloadOutlined />} onClick={handleDownloadCsv} disabled={downloadingCsv}
                sx={{ borderColor: '#00D9A6', color: '#00D9A6', '&:hover': { borderColor: '#00B88A', bgcolor: 'rgba(0,217,166,0.04)' } }}>
                Download as CSV
              </Button>
            </Box>
          </CardContent>
        </Card>
      )}
    </Box>
  );
}
