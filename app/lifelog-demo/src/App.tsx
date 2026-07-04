import { useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { Box, AppBar, Toolbar, IconButton, Typography } from '@mui/material';
import { SnackbarProvider } from 'notistack';
import { Menu } from 'lucide-react';

import { theme } from './theme';
import { DemoProvider } from './context/DemoContext';
import { Sidebar } from './components/Sidebar';

// 各画面
import { Home } from './pages/Home';
import { Calendar } from './pages/Calendar';
import { History } from './pages/History';
import { Reflection } from './pages/Reflection';
import { SlackFeed } from './pages/SlackFeed';

function App() {
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <SnackbarProvider maxSnack={3} anchorOrigin={{ vertical: 'top', horizontal: 'right' }}>
        <DemoProvider>
          <BrowserRouter>
            <Box sx={{ display: 'flex', minHeight: '100vh', backgroundColor: '#f4f6f9' }}>
              {/* モバイル用上部ヘッダー（デスクトップでは非表示） */}
              <AppBar
                position="fixed"
                sx={{
                  width: { sm: `calc(100% - 260px)` },
                  ml: { sm: `260px` },
                  display: { sm: 'none' },
                  backgroundColor: '#ffffff',
                  color: '#1e293b',
                  borderBottom: '1px solid #e2e8f0',
                  boxShadow: 'none',
                }}
              >
                <Toolbar>
                  <IconButton
                    color="inherit"
                    aria-label="open drawer"
                    edge="start"
                    onClick={handleDrawerToggle}
                    sx={{ mr: 2 }}
                  >
                    <Menu size={24} />
                  </IconButton>
                  <Typography variant="h6" noWrap component="div" sx={{ fontWeight: 800 }}>
                    LifeLog デモ
                  </Typography>
                </Toolbar>
              </AppBar>

              {/* サイドバー（レスポンシブ対応） */}
              <Sidebar mobileOpen={mobileOpen} onClose={handleDrawerToggle} />

              {/* メインコンテンツ表示エリア */}
              <Box
                component="main"
                sx={{
                  flexGrow: 1,
                  p: { xs: 2, sm: 4 },
                  pt: { xs: 10, sm: 4 }, // モバイル時は AppBar 分の隙間を確保
                  width: { sm: `calc(100% - 260px)` },
                  overflowX: 'auto',
                }}
              >
                <Routes>
                  <Route path="/" element={<Home />} />
                  <Route path="/calendar" element={<Calendar />} />
                  <Route path="/history" element={<History />} />
                  <Route path="/reflection" element={<Reflection />} />
                  <Route path="/slack-feed" element={<SlackFeed />} />
                </Routes>
              </Box>
            </Box>
          </BrowserRouter>
        </DemoProvider>
      </SnackbarProvider>
    </ThemeProvider>
  );
}

export default App;
