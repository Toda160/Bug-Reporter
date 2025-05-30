import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  CircularProgress,
  Select,
  MenuItem,
  InputLabel,
  FormControl
} from '@mui/material';
import { useAuth } from '../contexts/AuthContext';
import { authService } from '../services/api';
import api from '../services/api';

interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  username: string;
  avatar?: string;
  role: string;
}

const AccountSettings = () => {
  const navigate = useNavigate();
  const { logout, user } = useAuth();
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmNewPassword: ''
  });
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const [userList, setUserList] = useState<User[]>([]);
  const [selectedUserToModerateId, setSelectedUserToModerateId] = useState<string | ''>('');
  const [banReason, setBanReason] = useState('');
  const [moderationLoading, setModerationLoading] = useState(false);
  const [moderationError, setModerationError] = useState('');
  const [moderationSuccess, setModerationSuccess] = useState('');

  useEffect(() => {
    if (user?.role === 'MODERATOR') {
      const fetchUsers = async () => {
        try {
          const response = await api.get<User[]>('/api/users/list');
          setUserList(response.data);
        } catch (err: any) {
          console.error('Failed to fetch users:', err);
        }
      };
      fetchUsers();
    }
  }, [user]);

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setPasswordData(prev => ({
      ...prev,
      [name]: value
    }));
    if (error) setError('');
    if (success) setSuccess('');
  };

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (passwordData.newPassword !== passwordData.confirmNewPassword) {
      setError('New passwords do not match!');
      return;
    }

    if (!user?.id) {
      setError('User not found. Please log in again.');
      return;
    }

    setLoading(true);
    try {
      await authService.changePassword(
        user.id,
        passwordData.newPassword
      );
      setSuccess('Password changed successfully!');
      setPasswordData({
        currentPassword: '',
        newPassword: '',
        confirmNewPassword: ''
      });
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to change password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteAccount = async () => {
    setLoading(true);
    try {
      await authService.deleteAccount();
      logout();
      navigate('/login');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete account. Please try again.');
      setDeleteDialogOpen(false);
    } finally {
      setLoading(false);
    }
  };

  const handleBanUser = async () => {
    if (!user?.id || !selectedUserToModerateId || !banReason) {
      setModerationError('Please select a user and provide a ban reason.');
      return;
    }

    setModerationLoading(true);
    setModerationError('');
    setModerationSuccess('');

    try {
      await api.post(
        `/api/moderation/users/${selectedUserToModerateId}/ban/mods/${user.id}`,
        banReason,
        { headers: { 'Content-Type': 'text/plain' } }
      );
      const bannedUser = userList.find(u => u.id === selectedUserToModerateId);
      setModerationSuccess(`User ${bannedUser?.username || 'selected'} banned successfully.`);
      setSelectedUserToModerateId('');
      setBanReason('');
    } catch (err: any) {
      setModerationError(err.response?.data?.message || 'Failed to ban user.');
    } finally {
      setModerationLoading(false);
    }
  };

  const handleUnbanUser = async () => {
    if (!user?.id || !selectedUserToModerateId) {
      setModerationError('Please select a user.');
      return;
    }

    setModerationLoading(true);
    setModerationError('');
    setModerationSuccess('');

    try {
      await api.post(
        `/api/moderation/users/${selectedUserToModerateId}/unban/mods/${user.id}`
      );
      const unbannedUser = userList.find(u => u.id === selectedUserToModerateId);
      setModerationSuccess(`User ${unbannedUser?.username || 'selected'} unbanned successfully.`);
      setSelectedUserToModerateId('');
    } catch (err: any) {
      setModerationError(err.response?.data?.message || 'Failed to unban user.');
    } finally {
      setModerationLoading(false);
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', minWidth: '100vw', width: '100vw', height: '100vh', background: 'linear-gradient(135deg, #e3f2fd 0%, #fce4ec 100%)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <Paper elevation={3} sx={{ p: 4, width: 500, maxWidth: '95vw' }}>
        <Typography variant="h4" component="h1" gutterBottom align="center">
          Account Settings
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" sx={{ mb: 2 }}>
            {success}
          </Alert>
        )}

        {moderationError && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {moderationError}
          </Alert>
        )}

        {moderationSuccess && (
          <Alert severity="success" sx={{ mb: 2 }}>
            {moderationSuccess}
          </Alert>
        )}

        <form onSubmit={handlePasswordSubmit}>
          <TextField
            fullWidth
            label="Current Password"
            name="currentPassword"
            type="password"
            value={passwordData.currentPassword}
            onChange={handlePasswordChange}
            margin="normal"
            required
            disabled={loading}
          />
          <TextField
            fullWidth
            label="New Password"
            name="newPassword"
            type="password"
            value={passwordData.newPassword}
            onChange={handlePasswordChange}
            margin="normal"
            required
            disabled={loading}
          />
          <TextField
            fullWidth
            label="Confirm New Password"
            name="confirmNewPassword"
            type="password"
            value={passwordData.confirmNewPassword}
            onChange={handlePasswordChange}
            margin="normal"
            required
            disabled={loading}
          />
          <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 3 }}
            disabled={loading}
          >
            {loading ? <CircularProgress size={24} /> : 'Change Password'}
          </Button>
        </form>

        {user?.role === 'MODERATOR' && (
          <Box sx={{ mt: 4, pt: 3, borderTop: 1, borderColor: 'divider' }}>
            <Typography variant="h6" gutterBottom>
              Moderator Actions
            </Typography>
            <FormControl fullWidth margin="normal">
              <InputLabel id="select-user-to-moderate-label">Select User to Moderate</InputLabel>
              <Select
                labelId="select-user-to-moderate-label"
                id="select-user-to-moderate"
                value={selectedUserToModerateId}
                label="Select User to Moderate"
                onChange={(e) => setSelectedUserToModerateId(e.target.value as string)}
                disabled={moderationLoading || userList.length === 0}
              >
                <MenuItem value="">Select a user</MenuItem>
                {userList.map((u) => (
                  <MenuItem key={u.id} value={u.id}>
                    {u.username} ({u.email})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {selectedUserToModerateId && (
              <Box sx={{ mt: 2 }}>
                <TextField
                  fullWidth
                  label="Ban Reason"
                  value={banReason}
                  onChange={(e) => setBanReason(e.target.value)}
                  margin="normal"
                  disabled={moderationLoading}
                  multiline
                  rows={2}
                />
                <Button
                  variant="contained"
                  color="error"
                  onClick={handleBanUser}
                  disabled={moderationLoading || !banReason || !selectedUserToModerateId}
                  sx={{ mr: 1 }}
                >
                  {moderationLoading ? <CircularProgress size={24} /> : 'Ban User'}
                </Button>
                <Button
                  variant="outlined"
                  color="success"
                  onClick={handleUnbanUser}
                  disabled={moderationLoading || !selectedUserToModerateId}
                >
                  {moderationLoading ? <CircularProgress size={24} /> : 'Unban User'}
                </Button>
              </Box>
            )}
          </Box>
        )}

        <Box sx={{ mt: user?.role === 'MODERATOR' ? 2 : 4, pt: 3, borderTop: 1, borderColor: 'divider' }}>
          <Typography variant="h6" color="error" gutterBottom>
            Danger Zone
          </Typography>
          <Button
            variant="outlined"
            color="error"
            fullWidth
            onClick={() => setDeleteDialogOpen(true)}
            disabled={loading}
          >
            Delete Account
          </Button>
        </Box>
      </Paper>

      <Dialog
        open={deleteDialogOpen}
        onClose={() => !loading && setDeleteDialogOpen(false)}
      >
        <DialogTitle>Delete Account</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete your account? This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => setDeleteDialogOpen(false)}
            disabled={loading}
          >
            Cancel
          </Button>
          <Button
            onClick={handleDeleteAccount}
            color="error"
            disabled={loading}
          >
            {loading ? <CircularProgress size={24} /> : 'Delete Account'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AccountSettings; 