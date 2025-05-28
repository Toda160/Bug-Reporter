import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Button, TextField, Typography, Paper, Alert, MenuItem, FormControl, InputLabel, Select, Chip } from '@mui/material';
import { useAuth } from '../contexts/AuthContext';

interface Tag {
  id: number;
  name: string;
}

const CreateBugPage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [form, setForm] = useState({ title: '', description: '', image: '', tags: [] as number[] });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [tags, setTags] = useState<Tag[]>([]);

  useEffect(() => {
    const fetchTags = async () => {
      try {
        const res = await fetch('http://localhost:8080/api/tags/list');
        if (!res.ok) throw new Error('Failed to fetch tags');
        const data = await res.json();
        setTags(Array.isArray(data) ? data : []);
      } catch {
        setTags([]);
      }
    };
    fetchTags();
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | { name?: string; value: unknown }>) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name as string]: value }));
  };

  const handleTagsChange = (e: any) => {
    setForm(prev => ({ ...prev, tags: e.target.value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await fetch('http://localhost:8080/api/bugs/report', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          authorId: user?.id,
          title: form.title,
          description: form.description,
          image: form.image,
          tagIds: form.tags
        })
      });
      if (!res.ok) throw new Error('Failed to create bug');
      navigate('/all-bugs');
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', minWidth: '100vw', width: '100vw', height: '100vh', background: 'linear-gradient(135deg, #e3f2fd 0%, #fce4ec 100%)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <Paper sx={{ p: 4, width: 400 }}>
        <Typography variant="h5" gutterBottom>Report a Bug</Typography>
        {error && <Alert severity="error">{error}</Alert>}
        <form onSubmit={handleSubmit}>
          <TextField
            fullWidth
            label="Title"
            name="title"
            value={form.title}
            onChange={handleChange}
            margin="normal"
            required
          />
          <TextField
            fullWidth
            label="Description"
            name="description"
            value={form.description}
            onChange={handleChange}
            margin="normal"
            required
            multiline
            rows={3}
          />
          <TextField
            fullWidth
            label="Image URL"
            name="image"
            value={form.image}
            onChange={handleChange}
            margin="normal"
          />
          <FormControl fullWidth margin="normal">
            <InputLabel>Tags</InputLabel>
            <Select
              multiple
              name="tags"
              value={form.tags}
              onChange={handleTagsChange}
              renderValue={(selected) =>
                Array.isArray(selected)
                  ? (selected as number[]).map(id => tags.find(t => t.id === id)?.name).join(', ')
                  : ''
              }
            >
              {tags.map(tag => (
                <MenuItem key={tag.id} value={tag.id}>{tag.name}</MenuItem>
              ))}
            </Select>
          </FormControl>
          <Button type="submit" variant="contained" fullWidth sx={{ mt: 2 }} disabled={loading}>
            {loading ? 'Reporting...' : 'Report Bug'}
          </Button>
        </form>
      </Paper>
    </Box>
  );
};

export default CreateBugPage; 