import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, Typography, CircularProgress, Alert, Box, TextField, Button, Paper, Avatar, Grid, IconButton, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import BugReportIcon from '@mui/icons-material/BugReport';
import CommentIcon from '@mui/icons-material/Comment';
import CommentSection from './CommentSection';
import api from '../services/api';
import ThumbUpIcon from '@mui/icons-material/ThumbUp';
import ThumbDownIcon from '@mui/icons-material/ThumbDown';
import { useAuth } from '../contexts/AuthContext';
import { voteService } from '../services/api';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import CloseIcon from '@mui/icons-material/Close';

interface Bug {
  id: number;
  title: string;
  description: string;
  status: string;
  createdAt: string;
  author: { id: string; username: string; score: number };
  image?: string;
  voteCount?: number;
}

const BugDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [bug, setBug] = useState<Bug | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [voting, setVoting] = useState(false);
  const [editBugDialogOpen, setEditBugDialogOpen] = useState(false);
  const [editBugForm, setEditBugForm] = useState({ title: '', description: '' });
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  const fetchBug = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/api/bugs/details/${id}`);
      setBug(response.data);
      setEditBugForm({ title: response.data.title, description: response.data.description });
    } catch (err) {
      console.error('Error fetching bug:', err);
      setError('Failed to load bug details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) {
      fetchBug();
    }
  }, [id]);

  const handleVote = async (voteType: 'upvote' | 'downvote') => {
    if (!bug || !user) return;
    setVoting(true);
    try {
      await voteService.voteBug(bug.id, user.id, voteType);
      fetchBug();
    } catch (err) {
      // Optionally show error
    } finally {
      setVoting(false);
    }
  };

  const handleEditBugClick = () => {
    setEditBugDialogOpen(true);
  };

  const handleEditBugSave = async () => {
    if (!bug || !user) return; // Ensure bug and user are available

    try {
      // Construct the payload with updated title and description
      const payload = {
        title: editBugForm.title,
        description: editBugForm.description,
      };

      // Make the PATCH request to the backend endpoint
      await api.patch(`/api/moderation/bugs/${bug.id}/mods/${user.id}`, payload);

      // Close the dialog and refresh the bug details to show changes
      setEditBugDialogOpen(false);
      fetchBug();

    } catch (error) {
      console.error('Error saving edited bug:', error);
      // Optionally show an error message to the user
    }
  };

  const handleDeleteBug = () => {
    setDeleteDialogOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (!bug || !user) return;

    try {
      await api.delete(`/api/bugs/remove/${bug.id}?userId=${user.id}`);
      setDeleteDialogOpen(false);
      // Navigate back to the bugs list after successful deletion
      navigate('/bugs');
    } catch (error: any) {
      console.error('Error deleting bug:', error);
      setError(error.response?.data?.error || 'Failed to delete bug');
    }
  };

  if (loading) return <Box display="flex" justifyContent="center"><CircularProgress /></Box>;
  if (error) return <Alert severity="error">{error}</Alert>;
  if (!bug || !user) return <Alert severity="warning">Bug or user not found</Alert>;

  const isModerator = user?.role === 'MODERATOR';
  const canEditBug = isModerator || user?.id === bug.author.id;

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
                  Status: {bug.status} | Author: {bug.author?.username} (Score: {bug.author?.score?.toFixed(1) || 0})
                </Typography>
              </Box>
              {canEditBug && (
                <Box sx={{ ml: 'auto' }}>
                  <IconButton color="primary" onClick={handleEditBugClick}>
                    <EditIcon />
                  </IconButton>
                  <IconButton color="error" onClick={handleDeleteBug}>
                    <DeleteIcon />
                  </IconButton>
                </Box>
              )}
            </Box>
            <Typography variant="body1" sx={{ mb: 2 }}>{bug.description}</Typography>
            {bug.image && (
              <Box sx={{ mb: 2 }}>
                <img src={bug.image} alt="bug" style={{ maxWidth: '100%', borderRadius: 8 }} />
              </Box>
            )}
            {/* Voting UI */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
              <IconButton 
                data-testid="upvote-button"
                disabled={!user || String(user.id) === String(bug.author.id) || voting} 
                onClick={() => handleVote('upvote')}
              >
                <ThumbUpIcon />
              </IconButton>
              <Typography data-testid="bug-vote-count" variant="h6">{bug.voteCount ?? 0}</Typography>
              <IconButton 
                data-testid="downvote-button"
                disabled={!user || String(user.id) === String(bug.author.id) || voting} 
                onClick={() => handleVote('downvote')}
              >
                <ThumbDownIcon />
              </IconButton>
            </Box>
          </Paper>

          {/* Comments Section */}
          <CommentSection bugId={bug.id} bugStatus={bug.status} bugAuthorId={bug.author.id} currentUserId={user.id} />

          {/* Edit Bug Dialog */}
          <Dialog open={editBugDialogOpen} onClose={() => setEditBugDialogOpen(false)}>
            <DialogTitle>Edit Bug</DialogTitle>
            <DialogContent>
              <TextField
                autoFocus
                margin="dense"
                label="Title"
                type="text"
                fullWidth
                variant="standard"
                value={editBugForm.title}
                onChange={(e) => setEditBugForm({...editBugForm, title: e.target.value})}
              />
              <TextField
                margin="dense"
                label="Description"
                type="text"
                fullWidth
                multiline
                rows={4}
                variant="standard"
                value={editBugForm.description}
                onChange={(e) => setEditBugForm({...editBugForm, description: e.target.value})}
              />
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setEditBugDialogOpen(false)}>Cancel</Button>
              <Button onClick={handleEditBugSave}>Save</Button>
            </DialogActions>
          </Dialog>

          {/* Delete Confirmation Dialog */}
          <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
            <DialogTitle>Confirm Delete</DialogTitle>
            <DialogContent>
              <Typography>
                Are you sure you want to delete this bug? This action cannot be undone.
              </Typography>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
              <Button onClick={handleConfirmDelete} color="error">Delete</Button>
            </DialogActions>
          </Dialog>
        </Grid>
      </Grid>
    </Box>
  );
};

export default BugDetails; 