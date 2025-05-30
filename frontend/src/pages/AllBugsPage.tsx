import { useEffect, useState } from 'react';
import {
  Box, Typography, Paper, Grid, Card, CardContent, Avatar, Button, TextField, Chip, IconButton, Dialog, DialogTitle, DialogContent, DialogActions, InputAdornment, MenuItem, Select, FormControl, InputLabel
} from '@mui/material';
import BugReportIcon from '@mui/icons-material/BugReport';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import SearchIcon from '@mui/icons-material/Search';
import CloseIcon from '@mui/icons-material/Close';
import { useAuth } from '../contexts/AuthContext';
import api from '../services/api';
import CommentSection from '../components/CommentSection';
import { useNavigate } from 'react-router-dom';

interface Bug {
  id: number;
  title: string;
  description: string;
  status: string;
  createdAt: string;
  author: { id: string; username: string; score: number };
  image?: string;
  bugTags: { id: number; tag: { id: number; name: string } }[];
}

interface Tag {
  id: number;
  name: string;
}

const AllBugsPage = () => {
  const { user } = useAuth();
  const [bugs, setBugs] = useState<Bug[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [search, setSearch] = useState('');
  const [selectedTag, setSelectedTag] = useState<number | ''>('');
  const [showMine, setShowMine] = useState(false);
  const [authorSearch, setAuthorSearch] = useState('');
  const [tagDialogOpen, setTagDialogOpen] = useState(false);
  const [newTag, setNewTag] = useState('');
  const [bugDialogOpen, setBugDialogOpen] = useState(false);
  const [bugForm, setBugForm] = useState({
    title: '',
    description: '',
    image: '',
    tags: [] as number[],
  });
  const [bugError, setBugError] = useState('');
  const [bugLoading, setBugLoading] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [editBug, setEditBug] = useState<Bug | null>(null);
  const [editForm, setEditForm] = useState({ title: '', description: '', image: '', status: '' });
  const [editLoading, setEditLoading] = useState(false);
  const [expandedBugId, setExpandedBugId] = useState<number | null>(null);
  const navigate = useNavigate();

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

  const handleCreateTag = async () => {
    await fetch('http://localhost:8080/api/tags/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: newTag })
    });
    setNewTag('');
    setTagDialogOpen(false);
    fetchTags();
  };

  const handleBugFormChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement> | { target: { name: string; value: unknown } }) => {
    const { name, value } = e.target;
    setBugForm(prev => ({ ...prev, [name as string]: value }));
  };

  const handleBugTagsChange = (e: any) => {
    setBugForm(prev => ({ ...prev, tags: e.target.value }));
  };

  const handleReportBug = async (e: React.FormEvent) => {
    e.preventDefault();
    setBugLoading(true);
    setBugError('');
    try {
      const res = await fetch('http://localhost:8080/api/bugs/report', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          authorId: user?.id,
          title: bugForm.title,
          description: bugForm.description,
          image: bugForm.image,
          tagIds: bugForm.tags
        })
      });
      if (!res.ok) throw new Error('Failed to create bug');
      setBugDialogOpen(false);
      setBugForm({ title: '', description: '', image: '', tags: [] });
      fetchBugs();
    } catch (err: any) {
      setBugError(err.message);
    } finally {
      setBugLoading(false);
    }
  };

  const openEditDialog = (bug: Bug) => {
    setEditBug(bug);
    setEditForm({ 
      title: bug.title, 
      description: bug.description, 
      image: bug.image || '', 
      status: bug.status 
    });
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
    setEditLoading(true);
    try {
      const response = await api.put(`/api/bugs/update/${editBug.id}`, editForm);
      if (response.status === 200) {
        setEditDialogOpen(false);
        setEditBug(null);
        fetchBugs();
      }
    } catch (error) {
      console.error('Error updating bug:', error);
      setBugError('Failed to update bug. Please try again.');
    } finally {
      setEditLoading(false);
    }
  };

  const handleDelete = async (bugId: number) => {
    if (!window.confirm('Are you sure you want to delete this bug?')) {
      return;
    }
    if (!user?.id) {
      setBugError('You must be logged in to delete a bug.');
      return;
    }
    try {
      const response = await api.delete(`/api/bugs/remove/${bugId}?userId=${user.id}`);
      if (response.status === 204) {
        fetchBugs();
      } else {
        setBugError(`Failed to delete bug: ${response.statusText}`);
      }
    } catch (error) {
      console.error('Error deleting bug:', error);
      setBugError('Failed to delete bug. Please try again.');
    }
  };

  // Filtering logic
  const filteredBugs = bugs
    .filter(bug =>
      (!search || (bug.title && bug.title.toLowerCase().includes(search.toLowerCase()))) &&
      (!authorSearch || (bug.author && bug.author.username && bug.author.username.toLowerCase().includes(authorSearch.toLowerCase()))) &&
      (!showMine || (user && bug.author && String(bug.author.id) === String(user.id))) &&
      (!selectedTag || (Array.isArray(bug.bugTags) && bug.bugTags.some(bugTag => bugTag && bugTag.tag && String(bugTag.tag.id) === String(selectedTag))))
    )
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

  return (
    <Box>
      <Box sx={{ width: '100%', mb: 4 }}>
        <Paper elevation={4} sx={{ p: 2, borderRadius: 0, mb: 3, background: 'rgba(255,255,255,0.97)' }}>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, mb: 1, alignItems: 'center' }}>
            <TextField
              label="Search by title"
              value={search}
              onChange={e => setSearch(e.target.value)}
              InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment> }}
              sx={{ minWidth: 200 }}
            />
            <TextField
              label="Filter by author"
              value={authorSearch}
              onChange={e => setAuthorSearch(e.target.value)}
              sx={{ minWidth: 180 }}
            />
            <FormControl sx={{ minWidth: 160 }}>
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
            <Button variant={showMine ? 'contained' : 'outlined'} onClick={() => setShowMine(v => !v)}>
              My Bugs
            </Button>
            <Button variant="outlined" startIcon={<AddIcon />} onClick={() => setTagDialogOpen(true)}>
              New Tag
            </Button>
          </Box>
        </Paper>
      </Box>
      <Grid
        container
        spacing={3}
        justifyContent="center"
       >
        {filteredBugs.map((bug) => (
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
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                  <Avatar sx={{ bgcolor: 'primary.main', mr: 1 }}><BugReportIcon /></Avatar>
                  <Typography variant="h6" fontWeight={600}>{bug.title}</Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>{bug.description.length > 80 ? bug.description.slice(0, 80) + '...' : bug.description}</Typography>
                {bug.image && (
  <Box
    sx={{
      mb: 1,
      width: '100%',           
      maxHeight: 150,          
      overflow: 'hidden',
      borderRadius: 1,      // MUI spacing unit ≈8px
      backgroundColor: '#f0f0f0' // optional neutral bg for letterboxing
    }}
  >
    <img
      src={bug.image}
      alt="bug"
      style={{
        width: '100%',
        height: '100%',
        objectFit: 'contain'  // scales whole image down without cropping
      }}
    />
  </Box>
)}

                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 1 }}>
                  {(bug.bugTags ?? []).map(bugTag => (
                    bugTag && bugTag.tag ? (
                      <Chip key={bugTag.id} label={bugTag.tag.name} size="small" />
                    ) : null
                  ))}
                </Box>
                <Typography variant="caption" color="text.secondary">
                  Status: {bug.status} | Author: {bug.author?.username} (Score: {bug.author?.score?.toFixed(1) || 0})
                </Typography>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                  Created: {new Date(bug.createdAt).toLocaleString()}
                </Typography>
                {bug.author.id === user?.id && (
                  <Box sx={{ mt: 1, display: 'flex', gap: 1 }}>
                    <IconButton 
                      color="primary" 
                      size="small"
                      onClick={() => openEditDialog(bug)}
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton 
                      color="error" 
                      size="small"
                      onClick={() => handleDelete(bug.id)}
                    >
                      <DeleteIcon />
                    </IconButton>
                  </Box>
                )}
                <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}>
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={() => setExpandedBugId(expandedBugId === bug.id ? null : bug.id)}
                  >
                    {expandedBugId === bug.id ? 'Hide Comments' : 'Show Comments'}
                  </Button>
                </Box>
                {expandedBugId === bug.id && (
                  <Box sx={{ mt: 2 }}>
                    <CommentSection 
                      bugId={bug.id} 
                      bugStatus={bug.status}
                      bugAuthorId={bug.author.id}
                      currentUserId={user?.id}
                    />
                  </Box>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
      <Dialog open={tagDialogOpen} onClose={() => setTagDialogOpen(false)}>
        <DialogTitle>Create New Tag</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Tag Name"
            fullWidth
            value={newTag}
            onChange={e => setNewTag(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTagDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleCreateTag} variant="contained" disabled={!newTag}>Create</Button>
        </DialogActions>
      </Dialog>
      <Dialog open={bugDialogOpen} onClose={() => setBugDialogOpen(false)}>
        <DialogTitle>
          Report a Bug
          <IconButton onClick={() => setBugDialogOpen(false)} sx={{ position: 'absolute', right: 8, top: 8 }}>
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          {bugError && <Typography color="error" sx={{ mb: 1 }}>{bugError}</Typography>}
          <form onSubmit={handleReportBug} id="bug-form">
            <TextField
              fullWidth
              label="Title"
              name="title"
              value={bugForm.title}
              onChange={handleBugFormChange}
              margin="normal"
              required
            />
            <TextField
              fullWidth
              label="Description"
              name="description"
              value={bugForm.description}
              onChange={handleBugFormChange}
              margin="normal"
              required
              multiline
              rows={3}
            />
            <TextField
              fullWidth
              label="Image URL"
              name="image"
              value={bugForm.image}
              onChange={handleBugFormChange}
              margin="normal"
            />
            <FormControl fullWidth margin="normal">
              <InputLabel>Tags</InputLabel>
              <Select
                multiple
                name="tags"
                value={bugForm.tags}
                onChange={handleBugTagsChange}
                renderValue={(selected) =>
                  Array.isArray(selected)
                    ? (selected as number[]).map(id => (tags ?? []).find(t => t.id === id)?.name).join(', ')
                    : ''
                }
              >
                {(tags ?? []).map(tag => (
                  <MenuItem key={tag.id} value={tag.id}>{tag.name}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </form>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setBugDialogOpen(false)}>Cancel</Button>
          <Button type="submit" form="bug-form" variant="contained" disabled={bugLoading}>
            {bugLoading ? 'Reporting...' : 'Report Bug'}
          </Button>
        </DialogActions>
      </Dialog>
      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)}>
        <DialogTitle>
          Edit Bug
          <IconButton onClick={() => setEditDialogOpen(false)} sx={{ position: 'absolute', right: 8, top: 8 }}>
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          {bugError && <Typography color="error" sx={{ mb: 1 }}>{bugError}</Typography>}
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

export default AllBugsPage; 