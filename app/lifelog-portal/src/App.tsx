import React, { useState, useEffect } from 'react';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { SnackbarProvider } from 'notistack';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router';
import { theme } from './theme';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Login } from './pages/Login';
import { Settings } from './pages/Settings';
import { Dashboard } from './pages/Dashboard';
import { History } from './pages/History';
import { Register } from './pages/Register';
import { userApi } from './api/userApi';
import { Box, CircularProgress } from '@mui/material';

/**
 * ログイン状態および初期読み込み中かどうかに応じて、
 * ダッシュボード、履歴、設定、ログイン画面を切り替えて描画するサブコンポーネント。
 *
 * @returns レンダリングされた MainApp コンポーネント
 */
const MainApp: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();
  const [checkingRegistration, setCheckingRegistration] = useState(false);
  const [isRegistered, setIsRegistered] = useState<boolean | null>(null);

  // 1. 起動時、または遷移時にURLから slackToken を検出して sessionStorage へ退避する
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const slackToken = params.get('slackToken');
    if (slackToken) {
      sessionStorage.setItem('slack_link_token', slackToken);
      // URLからクエリパラメータを取り除く
      const newUrl = window.location.pathname;
      window.history.replaceState({}, document.title, newUrl);
    }
  }, []);

  // 2. ユーザーがログインしている場合に、データベースに設定が存在するか確認する
  useEffect(() => {
    const checkUserRegistration = async () => {
      if (!isAuthenticated) {
        setIsRegistered(null);
        return;
      }

      setCheckingRegistration(true);
      try {
        await userApi.getMe();
        setIsRegistered(true);
      } catch (err: any) {
        // バックエンドが 404 (ResourceNotFoundException) を返した場合は未登録
        if (err.response?.status === 404) {
          setIsRegistered(false);
        } else {
          console.error("Failed to check user registration status, assuming registered.", err);
          // 通信エラーなどの場合は、無限ループ防止のためとりあえず true にフォールバックします
          setIsRegistered(true);
        }
      } finally {
        setCheckingRegistration(false);
      }
    };

    checkUserRegistration();
  }, [isAuthenticated]);

  if (isLoading || checkingRegistration) {
    return (
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '100vh',
          backgroundColor: '#f8fafc',
        }}
      >
        <CircularProgress color="primary" />
      </Box>
    );
  }

  const getRedirectPath = () => {
    if (isRegistered === false) {
      return '/register';
    }
    if (sessionStorage.getItem('slack_link_token')) {
      return '/settings';
    }
    return '/dashboard';
  };

  return (
    <Routes>
      {/* ログインしていない場合はログイン画面へ。ログイン済みなら登録状況に応じてリダイレクト */}
      <Route
        path="/login"
        element={
          isAuthenticated
            ? <Navigate to={getRedirectPath()} replace />
            : <Login />
        }
      />

      {/* アカウント初回登録画面。未ログインならログインへ、登録済みならダッシュボードへ */}
      <Route
        path="/register"
        element={
          isAuthenticated
            ? (isRegistered === true ? <Navigate to="/dashboard" replace /> : <Register />)
            : <Navigate to="/login" replace />
        }
      />

      {/* ダッシュボード。未登録なら登録画面へ強制リダイレクト */}
      <Route
        path="/dashboard"
        element={
          isAuthenticated
            ? (isRegistered === false ? <Navigate to="/register" replace /> : <Dashboard />)
            : <Navigate to="/login" replace />
        }
      />

      {/* 日報履歴。未登録なら登録画面へ強制リダイレクト */}
      <Route
        path="/history"
        element={
          isAuthenticated
            ? (isRegistered === false ? <Navigate to="/register" replace /> : <History />)
            : <Navigate to="/login" replace />
        }
      />

      {/* 連携設定。未登録なら登録画面へ強制リダイレクト */}
      <Route
        path="/settings"
        element={
          isAuthenticated
            ? (isRegistered === false ? <Navigate to="/register" replace /> : <Settings />)
            : <Navigate to="/login" replace />
        }
      />

      {/* ルートURL。ログイン状態および登録状況に応じて転送 */}
      <Route
        path="/"
        element={
          isAuthenticated
            ? <Navigate to={getRedirectPath()} replace />
            : <Navigate to="/login" replace />
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

/**
 * アプリケーションのルートコンポーネント。
 * MUI テーマ、CssBaseline、Snackbar通知機能、および認証プロバイダーを適用します。
 *
 * @returns レンダリングされた App コンポーネント
 */
function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <SnackbarProvider
        maxSnack={3}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
        autoHideDuration={4000}
      >
        <AuthProvider>
          <BrowserRouter>
            <MainApp />
          </BrowserRouter>
        </AuthProvider>
      </SnackbarProvider>
    </ThemeProvider>
  );
}

export default App;
