import { useEffect, useState } from 'react';
import { Card, CardContent, Typography, CircularProgress, Alert, Box, Grid, Avatar } from '@mui/material';
import BugReportIcon from '@mui/icons-material/BugReport';
import { useNavigate } from 'react-router-dom';
import { useDialog } from '../App';

interface Bug {
  id: number;
  title: string;
  description: string;
  status: string;
  createdAt: string;
  author: { username: string };
}

const BugList = () => {
  const [bugs, setBugs] = useState<Bug[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchBugs = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await fetch('http://localhost:8080/api/bugs/list');
        if (!response.ok) throw new Error('Failed to fetch bugs');
        const data = await response.json();
        setBugs(data.sort((a: Bug, b: Bug) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()));
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    fetchBugs();
  }, []);

  if (loading) return <Box display="flex" justifyContent="center"><CircularProgress /></Box>;
  if (error) return <Alert severity="error">{error}</Alert>;

  return (
    <Box 
      sx={{ 
        mt: 4, 
        px: 2, 
        minHeight: '80vh', 
        background: 'linear-gradient(135deg, #e3f2fd 0%, #fce4ec 100%)',
      }}
    >
      <Typography variant="h4" fontWeight={700} align="center" gutterBottom sx={{ mb: 4 }}>
        All Bugs
      </Typography>
      <Grid container spacing={3} justifyContent="center">
        {bugs.map(bug => (
          <Grid item xs={12} sm={6} md={4} key={bug.id}>
            <Card
              elevation={4}
              sx={{
                borderRadius: 4,
                cursor: 'pointer',
                background: 'linear-gradient(120deg, #bbdefb 0%, #f8bbd0 100%)',
                transition: 'transform 0.2s',
                '&:hover': { transform: 'scale(1.03)', boxShadow: 8 }
              }}
              onClick={() => navigate(`/bugs/${bug.id}`)}
            >
              <CardContent sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'primary.main', mb: 1 }}>
                  <BugReportIcon />
                </Avatar>
                <Typography variant="h6" fontWeight={600} align="center">{bug.title}</Typography>
                <Typography variant="body2" color="text.secondary" align="center" sx={{ mb: 1 }}>
                  {bug.description.length > 80 ? bug.description.slice(0, 80) + '...' : bug.description}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Status: {bug.status} | Author: {bug.author?.username}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Created: {new Date(bug.createdAt).toLocaleString()}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default BugList; 