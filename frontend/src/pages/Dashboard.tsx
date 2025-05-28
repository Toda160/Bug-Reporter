import { Box, Button, Typography, Paper, Grid, Card, CardContent, Avatar, Select, MenuItem, FormControl, InputLabel, Chip, List, ListItem, ListItemText, CircularProgress } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import BugReportIcon from '@mui/icons-material/BugReport';
import CommentIcon from '@mui/icons-material/Comment';
import ThumbUpIcon from '@mui/icons-material/ThumbUp';
import { useEffect, useState } from 'react';

interface Bug {
  id: number;
  title: string;
  description: string;
  status: string;
  createdAt: string;
  author: { id: string; username: string };
  image?: string;
  tags: { id: number; name: string }[];
}

interface Tag {
  id: number;
  name: string;
}

const Dashboard = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  // Dummy stats for now; replace with real data if available
  const stats = [
    { label: 'Bugs Reported', value: 12, icon: <BugReportIcon fontSize="large" color="primary" /> },
    { label: 'Comments', value: 34, icon: <CommentIcon fontSize="large" color="secondary" /> },
    { label: 'Votes', value: 56, icon: <ThumbUpIcon fontSize="large" sx={{ color: '#43a047' }} /> },
  ];

  const [bugs, setBugs] = useState<Bug[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [selectedTag, setSelectedTag] = useState<number | ''>('');
  const [userBugCount, setUserBugCount] = useState<number | null>(null);
  const [loadingUserBugCount, setLoadingUserBugCount] = useState<boolean>(true);
  const [userCommentCount, setUserCommentCount] = useState<number | null>(null);
  const [loadingUserCommentCount, setLoadingUserCommentCount] = useState<boolean>(true);
  const [userVoteCount, setUserVoteCount] = useState<number | null>(null);
  const [loadingUserVoteCount, setLoadingUserVoteCount] = useState<boolean>(true);

  useEffect(() => {
    fetchBugs();
    fetchTags();
    if (user) {
      fetchUserBugCount(user.id);
      fetchUserCommentCount(user.id);
      fetchUserVoteCount(user.id);
    }
  }, [user]);

  const fetchBugs = async () => {
    const res = await fetch('http://localhost:8080/api/bugs/list');
    const data = await res.json();
    setBugs(data);
  };

  const fetchTags = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/tags/list');
      if (!res.ok) throw new Error('Failed to fetch tags');
      const data = await res.json();
      if (Array.isArray(data)) {
        setTags(data);
      } else {
        setTags([]);
      }
    } catch (err) {
      setTags([]);
    }
  };

  const fetchUserBugCount = async (userId: string) => {
    setLoadingUserBugCount(true);
    try {
      // Assuming an endpoint /api/user/bugs/count exists that returns { count: number }
      const res = await fetch(`/api/user/bugs/count?userId=${userId}`);
      if (!res.ok) throw new Error('Failed to fetch user bug count');
      const data = await res.json();
      setUserBugCount(data.count);
    } catch (err) {
      console.error("Error fetching user bug count:", err);
      setUserBugCount(null); // Or set to 0, depending on desired behavior on error
    } finally {
      setLoadingUserBugCount(false);
    }
  };

  const fetchUserCommentCount = async (userId: string) => {
    setLoadingUserCommentCount(true);
    try {
      // Assuming an endpoint /api/user/comments/count exists that returns { count: number }
      const res = await fetch(`/api/user/comments/count?userId=${userId}`);
      if (!res.ok) throw new Error('Failed to fetch user comment count');
      const data = await res.json();
      setUserCommentCount(data.count);
    } catch (err) {
      console.error("Error fetching user comment count:", err);
      setUserCommentCount(null);
    } finally {
      setLoadingUserCommentCount(false);
    }
  };

  const fetchUserVoteCount = async (userId: string) => {
    setLoadingUserVoteCount(true);
    try {
      // Assuming an endpoint /api/user/votes/count exists that returns { count: number }
      const res = await fetch(`/api/user/votes/count?userId=${userId}`);
      if (!res.ok) throw new Error('Failed to fetch user vote count');
      const data = await res.json();
      setUserVoteCount(data.count);
    } catch (err) {
      console.error("Error fetching user vote count:", err);
      setUserVoteCount(null);
    } finally {
      setLoadingUserVoteCount(false);
    }
  };

  const filteredBugs = selectedTag
    ? bugs.filter(bug => Array.isArray(bug.tags) && bug.tags.some(tag => tag.id === selectedTag))
    : bugs;

  return (
    <Box sx={{ minHeight: '100vh', width: '100vw', background: 'linear-gradient(135deg, #e3f2fd 0%, #fce4ec 100%)' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 'calc(100vh - 64px)' }}>
        <Paper elevation={4} sx={{ p: 5, borderRadius: 4, width: '100%', maxWidth: 600, textAlign: 'center', background: 'rgba(255,255,255,0.95)' }}>
          {!user ? (
            <>
              <Typography variant="h3" fontWeight={700} gutterBottom>
                Welcome to Bug Reporter!
              </Typography>
              <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                Please log in or register to continue.
              </Typography>
              <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 3 }}>
                <Button variant="contained" size="large" onClick={() => navigate('/login')}>Login</Button>
                <Button variant="outlined" size="large" onClick={() => navigate('/register')}>Register</Button>
              </Box>
            </>
          ) : (
            <>
              <Avatar sx={{ width: 80, height: 80, mx: 'auto', mb: 2, bgcolor: 'primary.main', fontSize: 36 }}>
                {user?.username?.[0]?.toUpperCase() || '?'}
              </Avatar>
              <Typography variant="h4" fontWeight={700} gutterBottom>
                Welcome, {user?.username || 'User'}!
              </Typography>
              <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                Manage your bugs, comments, and more from your dashboard.
              </Typography>
              <Grid container spacing={3} justifyContent="center" sx={{ my: 2 }}>
                {stats.map((stat, idx) => (
                  <Grid item xs={12} sm={4} key={stat.label}>
                    <Card elevation={2} sx={{ borderRadius: 3, py: 2, px: 1, background: 'linear-gradient(120deg, #bbdefb 0%, #f8bbd0 100%)' }}>
                      <CardContent sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                        {stat.icon}
                        <Typography variant="h5" fontWeight={600}>
                          {stat.label === 'Bugs Reported' ? (
                            loadingUserBugCount ? (
                              <CircularProgress size={20} />
                            ) : userBugCount !== null ? (
                              userBugCount
                            ) : (
                              '-'
                            )
                          ) : stat.label === 'Comments' ? (
                            loadingUserCommentCount ? (
                              <CircularProgress size={20} />
                            ) : userCommentCount !== null ? (
                              userCommentCount
                            ) : (
                              '-'
                            )
                          ) : stat.label === 'Votes' ? (
                            loadingUserVoteCount ? (
                              <CircularProgress size={20} />
                            ) : userVoteCount !== null ? (
                              userVoteCount
                            ) : (
                              '-'
                            )
                          ) : (
                            stat.value // Fallback for any other stats
                          )}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">{stat.label}</Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            </>
          )}
        </Paper>
      </Box>
    </Box>
  );
};

export default Dashboard; 