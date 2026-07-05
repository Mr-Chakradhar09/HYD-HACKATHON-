import React from 'react';
import { Box, Card, CardContent, Typography, Button, Chip } from '@mui/material';
import { NotificationsNoneOutlined, DeleteSweepOutlined } from '@mui/icons-material';
import { useSelector, useDispatch } from 'react-redux';
import { clearNotifications } from '../store/notificationSlice';
import { toast } from 'react-toastify';
import { MOCK_NOTIFICATIONS } from '../data/dummyData';

const TYPE_COLORS = { info: '#63B3ED', success: '#00D9A6', warning: '#FFB547', alert: '#FF6B6B' };

function getRelativeTime(ts) {
  const diff = Date.now() - new Date(ts).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
}

function getColor(subject) {
  if (subject.toLowerCase().includes('alert') || subject.toLowerCase().includes('low')) return TYPE_COLORS.alert;
  if (subject.toLowerCase().includes('approved') || subject.toLowerCase().includes('improvement')) return TYPE_COLORS.success;
  if (subject.toLowerCase().includes('draft') || subject.toLowerCase().includes('analytics')) return TYPE_COLORS.warning;
  return TYPE_COLORS.info;
}

export default function NotificationsPage() {
  const dispatch = useDispatch();
  const reduxNotifications = useSelector((s) => s.notification.notifications);
  const allNotifications = [...reduxNotifications, ...MOCK_NOTIFICATIONS];

  const handleClear = () => {
    dispatch(clearNotifications());
    toast.info('Notifications cleared');
  };

  if (allNotifications.length === 0) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '50vh' }}>
        <NotificationsNoneOutlined sx={{ fontSize: 80, color: '#94A3B8', mb: 2 }} />
        <Typography variant="h6" sx={{ color: '#94A3B8' }}>No notifications yet</Typography>
        <Typography variant="body2" sx={{ color: '#64748B' }}>You're all caught up!</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ maxWidth: 800, mx: 'auto' }}>
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 3 }}>
        <Button variant="outlined" size="small" startIcon={<DeleteSweepOutlined />} onClick={handleClear}
          sx={{ borderColor: 'rgba(255,255,255,0.12)', color: '#94A3B8', '&:hover': { borderColor: '#FF6B6B', color: '#FF6B6B' } }}>
          Clear All
        </Button>
      </Box>
      {allNotifications.map((n, i) => {
        const color = getColor(n.subject);
        return (
          <Card key={n.id || i} className="fade-in-up" sx={{ mb: 2, animationDelay: `${i * 80}ms`, borderLeft: `3px solid ${color}` }}>
            <CardContent sx={{ p: 3 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <Box>
                  <Typography variant="h6" sx={{ fontWeight: 600, mb: 0.5 }}>{n.subject}</Typography>
                  <Typography variant="body2" sx={{ color: '#94A3B8', lineHeight: 1.7 }}>{n.message}</Typography>
                </Box>
                <Chip label={getRelativeTime(n.timestamp)} size="small" variant="outlined" sx={{ borderColor: 'rgba(255,255,255,0.08)', color: '#64748B', fontSize: 11, flexShrink: 0 }} />
              </Box>
            </CardContent>
          </Card>
        );
      })}
    </Box>
  );
}
