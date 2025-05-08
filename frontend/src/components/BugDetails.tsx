import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Card, CardContent, Typography, CircularProgress, Alert, Box, TextField, Button, Paper, Avatar, Grid } from '@mui/material';
import BugReportIcon from '@mui/icons-material/BugReport';
import CommentIcon from '@mui/icons-material/Comment';
import CommentSection from './CommentSection';
import api from '../services/api';

interface Bug {
  id: number;
  title: string;
  description: string;
  status: string;
  createdAt: string;
  author: { username: string };
  image?: string;
}

const BugDetails = () => {
  const { id } = useParams();
  const [bug, setBug] = useState<Bug | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchBug = async () => {
      try {
        setLoading(true);
        const response = await api.get(`/api/bugs/details/${id}`);
        setBug(response.data);
      } catch (err) {
        console.error('Error fetching bug:', err);
        setError('Failed to load bug details');
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchBug();
    }
  }, [id]);

  if (loading) return <Box display="flex" justifyContent="center"><CircularProgress /></Box>;
  if (error) return <Alert severity="error">{error}</Alert>;
  if (!bug) return <Alert severity="warning">Bug not found</Alert>;

  return (
    <Box sx={{ mt: 4, px: 2, minHeight: '80vh', background: 'linear-gradient(135deg, #e3f2fd 0%, #fce4ec 100%)' }}>
      <Grid container justifyContent="center">
        <Grid item xs={12} md={8}>
          <Paper elevation={4} sx={{ p: 4, borderRadius: 4, mb: 4, background: 'rgba(255,255,255,0.95)' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
              <Avatar sx={{ bgcolor: 'primary.main', mr: 2 }}>
                <BugReportIcon />
              </Avatar>
              <Box>
                <Typography variant="h5" fontWeight={700}>{bug.title}</Typography>
                <Typography variant="body2" color="text.secondary">
                  Status: {bug.status} | Author: {bug.author?.username} | Created: {new Date(bug.createdAt).toLocaleString()}
                </Typography>
              </Box>
            </Box>
            <Typography variant="body1" sx={{ mb: 2 }}>{bug.description}</Typography>
            {bug.image && (
              <Box sx={{ mb: 2 }}>
                <img src={bug.image} alt="bug" style={{ maxWidth: '100%', borderRadius: 8 }} />
              </Box>
            )}
          </Paper>

          {/* Comments Section */}
          <CommentSection bugId={Number(id)} />
        </Grid>
      </Grid>
    </Box>
  );
};

export default BugDetails; 