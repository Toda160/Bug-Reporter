import { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  TextField,
  Button,
  Card,
  CardContent,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Avatar,
  Grid,
  Paper
} from '@mui/material';
import { useAuth } from '../contexts/AuthContext';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import CloseIcon from '@mui/icons-material/Close';
import api from '../services/api';

interface Comment {
  id: number;
  text: string;
  image?: string;
  createdAt: string;
  author: {
    id: string;
    username: string;
  };
}

interface CommentSectionProps {
  bugId: number;
}

const CommentSection = ({ bugId }: CommentSectionProps) => {
  const { user } = useAuth();
  const [comments, setComments] = useState<Comment[]>([]);
  const [newComment, setNewComment] = useState({ text: '', image: '' });
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [editComment, setEditComment] = useState<Comment | null>(null);
  const [editForm, setEditForm] = useState({ text: '', image: '' });
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchComments();
  }, [bugId]);

  const fetchComments = async () => {
    try {
      const response = await api.get(`/api/comments/bug/${bugId}`);
      setComments(response.data);
    } catch (error) {
      console.error('Error fetching comments:', error);
      setError('Failed to load comments');
    }
  };

  const handleSubmitComment = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/api/comments/create', {
        bugId,
        authorId: user?.id,
        text: newComment.text,
        image: newComment.image
      });
      setNewComment({ text: '', image: '' });
      fetchComments();
    } catch (error) {
      console.error('Error creating comment:', error);
      setError('Failed to create comment');
    }
  };

  const handleEditComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editComment) return;
    try {
      await api.put(`/api/comments/update/${editComment.id}`, {
        authorId: user?.id,
        text: editForm.text,
        image: editForm.image
      });
      setEditDialogOpen(false);
      setEditComment(null);
      fetchComments();
    } catch (error) {
      console.error('Error updating comment:', error);
      setError('Failed to update comment');
    }
  };

  const handleDeleteComment = async (commentId: number) => {
    if (!window.confirm('Are you sure you want to delete this comment?')) return;
    try {
      await api.delete(`/api/comments/delete/${commentId}?authorId=${user?.id}`);
      fetchComments();
    } catch (error) {
      console.error('Error deleting comment:', error);
      setError('Failed to delete comment');
    }
  };

  const openEditDialog = (comment: Comment) => {
    setEditComment(comment);
    setEditForm({ text: comment.text, image: comment.image || '' });
    setEditDialogOpen(true);
  };

  return (
    <Box sx={{ mt: 4 }}>
      <Typography variant="h6" gutterBottom>
        Comments
      </Typography>

      {/* New Comment Form */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <form onSubmit={handleSubmitComment}>
          <TextField
            fullWidth
            multiline
            rows={3}
            label="Add a comment"
            value={newComment.text}
            onChange={(e) => setNewComment(prev => ({ ...prev, text: e.target.value }))}
            margin="normal"
            required
          />
          <TextField
            fullWidth
            label="Image URL (optional)"
            value={newComment.image}
            onChange={(e) => setNewComment(prev => ({ ...prev, image: e.target.value }))}
            margin="normal"
          />
          <Button type="submit" variant="contained" sx={{ mt: 2 }}>
            Post Comment
          </Button>
        </form>
      </Paper>

      {/* Comments List */}
      {comments.map((comment) => (
        <Card key={comment.id} sx={{ mb: 2 }}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <Box>
                <Typography variant="subtitle2" color="text.secondary">
                  {comment.author.username} • {new Date(comment.createdAt).toLocaleString()}
                </Typography>
                <Typography variant="body1" sx={{ mt: 1 }}>
                  {comment.text}
                </Typography>
                {comment.image && (
                  <Box sx={{ mt: 1 }}>
                    <img src={comment.image} alt="comment" style={{ maxWidth: '100%', borderRadius: 4 }} />
                  </Box>
                )}
              </Box>
              {user?.id === comment.author.id && (
                <Box>
                  <IconButton size="small" onClick={() => openEditDialog(comment)}>
                    <EditIcon />
                  </IconButton>
                  <IconButton size="small" onClick={() => handleDeleteComment(comment.id)}>
                    <DeleteIcon />
                  </IconButton>
                </Box>
              )}
            </Box>
          </CardContent>
        </Card>
      ))}

      {/* Edit Comment Dialog */}
      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)}>
        <DialogTitle>
          Edit Comment
          <IconButton onClick={() => setEditDialogOpen(false)} sx={{ position: 'absolute', right: 8, top: 8 }}>
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          <form id="edit-comment-form" onSubmit={handleEditComment}>
            <TextField
              fullWidth
              multiline
              rows={3}
              label="Comment"
              value={editForm.text}
              onChange={(e) => setEditForm(prev => ({ ...prev, text: e.target.value }))}
              margin="normal"
              required
            />
            <TextField
              fullWidth
              label="Image URL (optional)"
              value={editForm.image}
              onChange={(e) => setEditForm(prev => ({ ...prev, image: e.target.value }))}
              margin="normal"
            />
          </form>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)}>Cancel</Button>
          <Button type="submit" form="edit-comment-form" variant="contained">
            Save Changes
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default CommentSection; 