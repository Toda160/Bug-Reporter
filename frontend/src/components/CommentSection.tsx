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
import ThumbUpIcon from '@mui/icons-material/ThumbUp';
import ThumbDownIcon from '@mui/icons-material/ThumbDown';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { voteService } from '../services/api';

interface Comment {
  id: number;
  text: string;
  author: { id: string; username: string; score: number };
  createdAt: string;
  image?: string;
  accepted?: boolean;
  voteCount?: number;
}

interface CommentSectionProps {
  bugId: number;
  bugStatus: string;
  bugAuthorId: string;
  currentUserId?: string;
}

const CommentSection = ({ bugId, bugStatus, bugAuthorId, currentUserId }: CommentSectionProps) => {
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

  useEffect(() => {
    console.log('currentUserId:', currentUserId, 'bugAuthorId:', bugAuthorId);
  }, [currentUserId, bugAuthorId]);

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

  const handleVoteComment = async (commentId: number, voteType: 'upvote' | 'downvote') => {
    if (!user) return;
    try {
      await voteService.voteComment(commentId, user.id, voteType);
      fetchComments();
    } catch (err) {
      // Optionally show error
    }
  };

  const handleAcceptComment = async (commentId: number) => {
    if (!user) return;
    try {
      await voteService.acceptComment(bugId, commentId, user.id);
      fetchComments();
    } catch (err) {
      // Optionally show error
    }
  };

  const openEditDialog = (comment: Comment) => {
    setEditComment(comment);
    setEditForm({ text: comment.text, image: comment.image || '' });
    setEditDialogOpen(true);
  };

  const isModerator = user?.role === 'MODERATOR';

  return (
    <Box sx={{ mt: 4 }}>
      <Typography variant="h6" gutterBottom>
        Comments
      </Typography>

      {/* New Comment Form */}
      {bugStatus !== 'Solved' && (
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
              data-testid="comment-input"
            />
            <TextField
              fullWidth
              label="Image URL (optional)"
              value={newComment.image}
              onChange={(e) => setNewComment(prev => ({ ...prev, image: e.target.value }))}
              margin="normal"
              data-testid="comment-image-input"
            />
            <Button type="submit" variant="contained" sx={{ mt: 2 }} data-testid="submit-comment">
              Post Comment
            </Button>
          </form>
        </Paper>
      )}

      {/* Comments List */}
      {comments.map((comment) => (
        <Card key={comment.id} sx={{ mb: 2, background: comment.accepted ? '#e6ffe6' : undefined }}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <Box>
                <Typography variant="subtitle2" color="text.secondary">
                  {comment.author.username} (Score: {comment.author.score?.toFixed(1) || 0}) • {new Date(comment.createdAt).toLocaleString()}
                  {comment.accepted && (
                    <CheckCircleIcon color="success" sx={{ ml: 1 }} />
                  )}
                </Typography>
                <Typography variant="body1" sx={{ mt: 1 }}>
                  {comment.text}
                </Typography>
                {comment.image && (
  <Box
    sx={{
      mb: 1,
      width: 200,           
      height: 150,          
      overflow: 'hidden',
      borderRadius: 1,     
      backgroundColor: '#f0f0f0' 
    }}
  >
    <img
      src={comment.image}
      alt="comment"
      style={{
        width: '100%',
        height: '100%',
        objectFit: 'contain'  
      }}
    />
  </Box>
)}
                {/* Voting UI for comment */}
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 1 }}>
                  <Typography variant="body2">{comment.voteCount ?? 0}</Typography>
                  <IconButton disabled={!user || String(user.id) === String(comment.author.id)} onClick={() => handleVoteComment(comment.id, 'upvote')}><ThumbUpIcon /></IconButton>
                  <IconButton disabled={!user || String(user.id) === String(comment.author.id)} onClick={() => handleVoteComment(comment.id, 'downvote')}><ThumbDownIcon /></IconButton>
                  {/* Accept button for bug creator */}
                  {user && currentUserId === bugAuthorId && bugStatus !== 'Solved' && !comment.accepted && (
                    <Button size="small" color="success" onClick={() => handleAcceptComment(comment.id)} sx={{ ml: 2 }}>
                      Accept
                    </Button>
                  )}
                  {comment.accepted && <Typography color="success.main" sx={{ ml: 2 }}>Accepted</Typography>}
                </Box>
              </Box>
              {(isModerator || user?.id === comment.author.id) && (
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