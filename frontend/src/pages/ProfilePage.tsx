import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Paper,
  Typography,
  Box,
  Avatar,
  List,
  ListItem,
  ListItemText,
  Chip,
  Grid,
  Card,
  CardContent,
  CircularProgress,
  Alert,
  Button,
  IconButton,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem
} from '@mui/material';
import type { GridProps } from '@mui/material/Grid';
import { BugReport as BugIcon, Settings as SettingsIcon } from '@mui/icons-material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import { useAuth } from '../contexts/AuthContext';
import { userService } from '../services/api';
import api from '../services/api';

interface Bug {
  id: number;
  title: string;
  description: string;
  status: string;
  createdAt: string;
  image?: string;
}

interface UserProfile {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  score: number;
  avatar?: string;
  username: string;
}

const ProfilePage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [bugs, setBugs] = useState<Bug[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [editBug, setEditBug] = useState<Bug | null>(null);
  const [editForm, setEditForm] = useState({ title: '', description: '', image: '', status: '' });
  const [editLoading, setEditLoading] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        setError(null);
        const [profileData, bugsData] = await Promise.all([
          userService.getProfile(id!),
          userService.getBugs(id!)
        ]);
        setProfile(profileData);
        setBugs(bugsData);
      } catch (err: any) {
        console.error('Error fetching profile data:', err);
        setError(err.response?.data?.message || 'Failed to load profile data');
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchData();
    }
  }, [id]);

  if (loading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="100vh"
      >
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Container maxWidth="md">
        <Alert severity="error" sx={{ mt: 4 }}>
          {error}
        </Alert>
      </Container>
    );
  }

  if (!profile) {
    return (
      <Container maxWidth="md">
        <Alert severity="warning" sx={{ mt: 4 }}>
          Profile not found
        </Alert>
      </Container>
    );
  }

  const isOwnProfile = user?.id === profile.id;

  const openEditDialog = (bug: Bug) => {
    setEditBug(bug);
    setEditForm({ title: bug.title, description: bug.description, image: bug.image || '', status: bug.status });
    setEditDialogOpen(true);
  };

  const handleEditTextFieldChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setEditForm(prev => ({ ...prev, [name]: value }));
  };

  const handleEditSelectChange = (e: any) => {
    const { name, value } = e.target;
    setEditForm(prev => ({ ...prev, [name]: value }));
  };

  const handleEditSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editBug) return;
    console.log('Starting edit submission for bug:', editBug.id);
    console.log('Edit form data:', editForm);
    setEditLoading(true);
    try {
      console.log('Sending PUT request to:', `/api/bugs/update/${editBug.id}`);
      const response = await api.put(`/api/bugs/update/${editBug.id}`, editForm);
      console.log('Edit response:', response);
      if (response.status === 200) {
        setEditDialogOpen(false);
        setEditBug(null);
        // Refresh bugs
        const bugsData = await userService.getBugs(id!);
        setBugs(bugsData);
      }
    } catch (error) {
      console.error('Error updating bug:', error);
      setError('Failed to update bug. Please try again.');
    } finally {
      setEditLoading(false);
    }
  };

  const handleDelete = async (bugId: number) => {
    console.log('Delete button clicked for bug:', bugId);
    if (!window.confirm('Are you sure you want to delete this bug?')) {
      console.log('Delete cancelled by user');
      return;
    }
    try {
      console.log('Sending DELETE request to:', `/api/bugs/remove/${bugId}`);
      const response = await api.delete(`/api/bugs/remove/${bugId}`);
      console.log('Delete response:', response);
      if (response.status === 204) {
        // Refresh bugs
        const bugsData = await userService.getBugs(id!);
        setBugs(bugsData);
      }
    } catch (error) {
      console.error('Error deleting bug:', error);
      setError('Failed to delete bug. Please try again.');
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', minWidth: '100vw', width: '100vw', height: '100vh', background: 'linear-gradient(135deg, #e3f2fd 0%, #fce4ec 100%)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <Paper elevation={3} sx={{ p: 4, width: 600, maxWidth: '95vw' }}>
        <Grid container spacing={4}>
          <Grid item xs={12} md={4}>
            <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <Typography variant="h4" gutterBottom>
                {profile.username}
              </Typography>
              <Typography variant="body1" color="text.secondary" gutterBottom>
                {profile.email}
              </Typography>
              <Chip
                icon={<BugIcon />}
                label={`Score: ${profile.score}`}
                color="primary"
                sx={{ mt: 2 }}
              />
              {isOwnProfile && (
                <Button
                  variant="outlined"
                  startIcon={<SettingsIcon />}
                  onClick={() => navigate('/settings')}
                  sx={{ mt: 2 }}
                >
                  Account Settings
                </Button>
              )}
            </Box>
          </Grid>
          <Grid item xs={12} md={8}>
            <Typography variant="h6" gutterBottom>
              Reported Bugs
            </Typography>
            {bugs.length === 0 ? (
              <Alert severity="info">
                No bugs reported yet
              </Alert>
            ) : (
              <List>
                {bugs.map((bug) => (
                  <Card key={bug.id} sx={{ mb: 2 }}>
                    <CardContent>
                      <ListItem
                        secondaryAction={
                          <Box sx={{ display: 'flex', gap: 1 }}>
                            <IconButton 
                              color="primary" 
                              size="small" 
                              onClick={() => {
                                console.log('Edit button clicked for bug:', bug.id);
                                openEditDialog(bug);
                              }}
                            >
                              <EditIcon />
                            </IconButton>
                            <IconButton 
                              color="error" 
                              size="small" 
                              onClick={() => {
                                console.log('Delete button clicked for bug:', bug.id);
                                handleDelete(bug.id);
                              }}
                            >
                              <DeleteIcon />
                            </IconButton>
                          </Box>
                        }
                      >
                        <ListItemText
                          primary={bug.title}
                          secondary={
                            <>
                              <Typography component="span" variant="body2" color="text.primary">
                                {bug.description}
                              </Typography>
                              <br />
                              <Typography component="span" variant="caption" color="text.secondary">
                                Status: {bug.status} | Created: {bug.createdAt}
                              </Typography>
                            </>
                          }
                        />
                      </ListItem>
                    </CardContent>
                  </Card>
                ))}
              </List>
            )}
          </Grid>
        </Grid>
      </Paper>
      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)}>
        <DialogTitle>Edit Bug</DialogTitle>
        <DialogContent>
          <form id="edit-bug-form" onSubmit={handleEditSubmit}>
            <TextField
              fullWidth
              label="Title"
              name="title"
              value={editForm.title}
              onChange={handleEditTextFieldChange}
              margin="normal"
              required
            />
            <TextField
              fullWidth
              label="Description"
              name="description"
              value={editForm.description}
              onChange={handleEditTextFieldChange}
              margin="normal"
              required
              multiline
              rows={3}
            />
            <TextField
              fullWidth
              label="Image URL"
              name="image"
              value={editForm.image}
              onChange={handleEditTextFieldChange}
              margin="normal"
            />
            <FormControl fullWidth margin="normal">
              <InputLabel>Status</InputLabel>
              <Select
                name="status"
                value={editForm.status}
                label="Status"
                onChange={handleEditSelectChange}
              >
                <MenuItem value="Received">Received</MenuItem>
                <MenuItem value="In progress">In progress</MenuItem>
                <MenuItem value="Solved">Solved</MenuItem>
              </Select>
            </FormControl>
          </form>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)}>Cancel</Button>
          <Button type="submit" form="edit-bug-form" variant="contained" disabled={editLoading}>
            {editLoading ? 'Saving...' : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ProfilePage; 