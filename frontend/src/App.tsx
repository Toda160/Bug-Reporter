import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProfilePage from './pages/ProfilePage';
import AccountSettings from './pages/AccountSettings';
import { CssBaseline, ThemeProvider, createTheme, Box, Dialog } from '@mui/material';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navigation from './components/Navigation';
import BugList from './components/BugList';
import BugDetails from './components/BugDetails';
import Dashboard from './pages/Dashboard';
import CreateBugPage from './pages/CreateBugPage';
import AllBugsPage from './pages/AllBugsPage';
import { createContext, useContext, useState } from 'react';

// Create a context for managing dialogs
interface DialogContextType {
  openTagDialog: () => void;
  closeTagDialog: () => void;
  openBugDialog: () => void;
  closeBugDialog: () => void;
}

const DialogContext = createContext<DialogContextType | undefined>(undefined);

export const useDialog = () => {
  const context = useContext(DialogContext);
  if (!context) {
    throw new Error('useDialog must be used within a DialogProvider');
  }
  return context;
};

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

function App() {
  const [tagDialogOpen, setTagDialogOpen] = useState(false);
  const [bugDialogOpen, setBugDialogOpen] = useState(false);

  const dialogContextValue = {
    openTagDialog: () => setTagDialogOpen(true),
    closeTagDialog: () => setTagDialogOpen(false),
    openBugDialog: () => setBugDialogOpen(true),
    closeBugDialog: () => setBugDialogOpen(false),
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <DialogContext.Provider value={dialogContextValue}>
          <Router>
            <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
              <Navigation />
              <Box component="main" sx={{ flexGrow: 1 }}>
                <Routes>
                  <Route path="/login" element={<LoginPage />} />
                  <Route path="/register" element={<RegisterPage />} />
                  <Route path="/bugs" element={<BugList />} />
                  <Route path="/bugs/:id" element={<BugDetails />} />
                  <Route
                    path="/profile/:id"
                    element={
                      <ProtectedRoute>
                        <ProfilePage />
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="/settings"
                    element={
                      <ProtectedRoute>
                        <AccountSettings />
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="/dashboard"
                    element={
                      <ProtectedRoute>
                        <Dashboard />
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="/bugs/create"
                    element={
                      <ProtectedRoute>
                        <CreateBugPage />
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="/all-bugs"
                    element={
                      <ProtectedRoute>
                        <AllBugsPage />
                      </ProtectedRoute>
                    }
                  />
                  <Route path="/" element={<Dashboard />} />
                </Routes>
              </Box>
            </Box>

            {/* Global Dialogs */}
            <Dialog 
              open={tagDialogOpen} 
              onClose={() => setTagDialogOpen(false)}
              sx={{ 
                '& .MuiDialog-paper': {
                  margin: 2,
                  maxWidth: '90vw',
                  maxHeight: '90vh'
                },
                '& .MuiBackdrop-root': {
                  backgroundColor: 'rgba(0, 0, 0, 0.5)'
                }
              }}
              style={{
                position: 'fixed',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                zIndex: 9999
              }}
            >
              {/* ...tag dialog content... */}
            </Dialog>
            
            <Dialog 
              open={bugDialogOpen} 
              onClose={() => setBugDialogOpen(false)}
              sx={{ 
                '& .MuiDialog-paper': {
                  margin: 2,
                  maxWidth: '90vw',
                  maxHeight: '90vh'
                },
                '& .MuiBackdrop-root': {
                  backgroundColor: 'rgba(0, 0, 0, 0.5)'
                }
              }}
              style={{
                position: 'fixed',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                zIndex: 9999
              }}
            >
              {/* ...bug dialog content... */}
            </Dialog>
          </Router>
        </DialogContext.Provider>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
