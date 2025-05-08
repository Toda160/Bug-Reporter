import { Box, Button, Typography, Paper, Grid, Card, CardContent, Avatar, Select, MenuItem, FormControl, InputLabel, Chip, List, ListItem, ListItemText } from '@mui/material';
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

  useEffect(() => {
    fetchBugs();
    fetchTags();
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
                        <Typography variant="h5" fontWeight={600}>{stat.value}</Typography>
                        <Typography variant="body2" color="text.secondary">{stat.label}</Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
              <Box sx={{ mt: 3, display: 'flex', justifyContent: 'center', gap: 2 }}>
                <FormControl sx={{ minWidth: 180 }}>
                  <InputLabel>Tag</InputLabel>
                  <Select
                    value={selectedTag}
                    label="Tag"
                    onChange={e => setSelectedTag(e.target.value as number)}
                  >
                    <MenuItem value="">All</MenuItem>
                    {(tags ?? []).map(tag => (
                      <MenuItem key={tag.id} value={tag.id}>{tag.name}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Box>
              <MuiBox sx={{ mt: 4 }}>
                <Typography variant="h6" align="center" gutterBottom>Bugs</Typography>
                <List>
                  {filteredBugs.map(bug => (
                    <ListItem key={bug.id} alignItems="flex-start">
                      <ListItemText
                        primary={bug.title}
                        secondary={
                          <>
                            <Typography component="span" variant="body2" color="text.primary">
                              {bug.description.length > 80 ? bug.description.slice(0, 80) + '...' : bug.description}
                            </Typography>
                            <Box sx={{ mt: 1 }}>
                              {(bug.tags ?? []).map(tag => <Chip key={tag.id} label={tag.name} size="small" sx={{ mr: 0.5 }} />)}
                            </Box>
                          </>
                        }
                      />
                    </ListItem>
                  ))}
                  {filteredBugs.length === 0 && (
                    <ListItem>
                      <ListItemText primary="No bugs found for this tag." />
                    </ListItem>
                  )}
                </List>
              </MuiBox>
            </>
          )}
        </Paper>
      </Box>
    </Box>
  );
};

export default Dashboard; 