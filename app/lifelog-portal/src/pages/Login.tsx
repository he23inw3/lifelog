import React, { useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Alert } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { PortalButton } from '../components/PortalButton';

/**
 * JWTをデコードしてペイロードオブジェクトを返します。
 *
 * @param token JWT文字列
 * @returns デコードされたペイロード、または無効な場合は null
 */
const decodeJwt = (token: string): { email?: string } | null => {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      window.atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
};

/**
 * Google ID Token (OIDC) の入力を要求するログインページコンポーネント。
 *
 * @returns レンダリングされた Login コンポーネント
 */
export const Login: React.FC = () => {
  const { login } = useAuth();
  const [tokenInput, setTokenInput] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [decodedEmail, setDecodedEmail] = useState<string | null>(null);

  /**
   * トークン入力値が変更された際のハンドラ。
   * 入力されたトークンが有効なJWTであり、emailが含まれているかを即時判定します。
   */
  const handleTokenChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value.trim();
    setTokenInput(val);
    setError(null);
    setDecodedEmail(null);

    if (val) {
      const decoded = decodeJwt(val);
      if (decoded && decoded.email) {
        setDecodedEmail(decoded.email);
      } else {
        setError('Invalid Google ID Token or missing email claim.');
      }
    }
  };

  /**
   * 入力されたトークンを使用してサインインを実行します。
   */
  const handleSignIn = () => {
    if (!tokenInput) {
      setError('Please paste a Google ID Token.');
      return;
    }
    const decoded = decodeJwt(tokenInput);
    if (!decoded || !decoded.email) {
      setError('Invalid Google ID Token or missing email claim.');
      return;
    }
    login(decoded.email, tokenInput);
  };

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #e0e7ff 0%, #f1f5f9 100%)',
        p: 2,
      }}
    >
      <Card
        sx={{
          maxWidth: 480,
          width: '100%',
          position: 'relative',
          overflow: 'hidden',
          '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '4px',
            background: 'linear-gradient(90deg, #6366f1 0%, #0ea5e9 100%)',
          },
        }}
      >
        <CardContent sx={{ p: 4 }}>
          <Box sx={{ textAlign: 'center', mb: 3 }}>
            <Typography
              variant="h4"
              component="h1"
              sx={{
                fontWeight: 800,
                background: 'linear-gradient(135deg, #4f46e5 0%, #0ea5e9 100%)',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
                mb: 1,
              }}
            >
              LifeLog Portal
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Configure your Google Calendar & Slack integrations
            </Typography>
          </Box>

          <Box sx={{ mt: 3 }}>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 1, fontWeight: 600 }}>
              Google ID Token (OIDC)
            </Typography>
            <TextField
              fullWidth
              multiline
              rows={4}
              placeholder="Paste your Google ID Token (JWT) here..."
              value={tokenInput}
              onChange={handleTokenChange}
              error={!!error}
              helperText={error}
              variant="outlined"
              sx={{
                mb: 2,
                '& .MuiInputBase-input': {
                  fontFamily: 'monospace',
                  fontSize: '0.85rem',
                }
              }}
            />
            {decodedEmail && (
              <Alert severity="success" sx={{ mb: 2, borderRadius: 2 }}>
                Identified email: <strong>{decodedEmail}</strong>
              </Alert>
            )}
            <PortalButton
              fullWidth
              variant="contained"
              color="primary"
              size="large"
              onClick={handleSignIn}
              disabled={!!error || !tokenInput}
              sx={{ height: 50, mb: 3 }}
            >
              Sign In
            </PortalButton>
          </Box>

          <Box
            sx={{
              p: 2,
              borderRadius: 3,
              backgroundColor: 'rgba(99, 102, 241, 0.04)',
              border: '1px dashed rgba(99, 102, 241, 0.15)',
            }}
          >
            <Typography variant="subtitle2" sx={{ color: 'primary.main', mb: 1, fontWeight: 600 }}>
              How to get an ID Token locally:
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5, fontSize: '0.85rem', lineHeight: 1.5 }}>
              Run the following command in your terminal to print a Google ID Token:
            </Typography>
            <Box
              sx={{
                p: 1.5,
                backgroundColor: 'rgba(0, 0, 0, 0.05)',
                borderRadius: 2,
                fontFamily: 'monospace',
                fontSize: '0.8rem',
                wordBreak: 'break-all',
                color: 'text.primary',
                border: '1px solid rgba(0, 0, 0, 0.08)'
              }}
            >
              gcloud auth print-identity-token
            </Box>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};
