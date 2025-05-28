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

  const [bugs, setBugs] = useState<Bug[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [selectedTag, setSelectedTag] = useState<number | ''>('');
  const [bugCount, setBugCount] = useState<number | null>(null);
  const [loadingBugCount, setLoadingBugCount] = useState<boolean>(true);
  const [commentCount, setCommentCount] = useState<number | null>(null);
  const [loadingCommentCount, setLoadingCommentCount] = useState<boolean>(true);
  const [voteCount, setVoteCount] = useState<number | null>(null);
  const [loadingVoteCount, setLoadingVoteCount] = useState<boolean>(true);

  useEffect(() => {
    fetchBugs();
    fetchTags();
    fetchBugCount();
    fetchCommentCount();
    fetchVoteCount();
  }, []);

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

  const fetchBugCount = async () => {
    setLoadingBugCount(true);
    try {
      const res = await fetch('http://localhost:8080/api/bugs/count');
      if (!res.ok) throw new Error('Failed to fetch bug count');
      const data = await res.json();
      setBugCount(typeof data === 'number' ? data : data.count ?? null);
    } catch (err) {
      setBugCount(null);
    } finally {
      setLoadingBugCount(false);
    }
  };

  const fetchCommentCount = async () => {
    setLoadingCommentCount(true);
    try {
      const res = await fetch('http://localhost:8080/api/comments/count');
      if (!res.ok) throw new Error('Failed to fetch comment count');
      const data = await res.json();
      setCommentCount(typeof data === 'number' ? data : data.count ?? null);
    } catch (err) {
      setCommentCount(null);
    } finally {
      setLoadingCommentCount(false);
    }
  };

  const fetchVoteCount = async () => {
    setLoadingVoteCount(true);
    try {
      const res = await fetch('http://localhost:8080/api/votes/count');
      if (!res.ok) throw new Error('Failed to fetch vote count');
      const data = await res.json();
      setVoteCount(typeof data === 'number' ? data : data.count ?? null);
    } catch (err) {
      setVoteCount(null);
    } finally {
      setLoadingVoteCount(false);
    }
  };

  const filteredBugs = selectedTag
    ? bugs.filter(bug => Array.isArray(bug.tags) && bug.tags.some(tag => tag.id === selectedTag))
    : bugs;

  const stats = [
    { label: 'Bugs Reported', value: bugCount, loading: loadingBugCount, icon: <BugReportIcon fontSize="large" color="primary" /> },
    { label: 'Comments', value: commentCount, loading: loadingCommentCount, icon: <CommentIcon fontSize="large" color="secondary" /> },
    { label: 'Votes', value: voteCount, loading: loadingVoteCount, icon: <ThumbUpIcon fontSize="large" sx={{ color: '#43a047' }} /> },
  ];

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
                          {stat.loading ? (
                            <CircularProgress size={20} />
                          ) : stat.value !== null && stat.value !== undefined ? (
                            stat.value
                          ) : (
                            '-'
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