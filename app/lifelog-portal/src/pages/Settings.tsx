import React, { useEffect, useState } from 'react';
import { Box, Typography, CircularProgress, Backdrop, Alert, Stack } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { API_BASE_URL } from '../constants';
import { userApi } from '../api/userApi';
import type { IntegrationStatus } from '../types';
import { Calendar, Link2, CheckCircle2, AlertCircle } from 'lucide-react';
import { useSnackbar } from 'notistack';
import { PortalHeader } from '../components/PortalHeader';
import { IntegrationCard } from '../components/settings/IntegrationCard';
import { PortalButton } from '../components/PortalButton';

/**
 * アカウント設定およびサービス連携設定を表示するメインページコンポーネント。
 * Google Calendar および Slack の連携状態を表示し、連携のための各種アクションを提供します。
 *
 * @returns レンダリングされた Settings コンポーネント
 */
export const Settings: React.FC = () => {
  const { user, logout } = useAuth();
  const { enqueueSnackbar } = useSnackbar();
  const [loading, setLoading] = useState(true);
  const [linking, setLinking] = useState(false);
  const [integrations, setIntegrations] = useState<IntegrationStatus | null>(null);
  const [linkError, setLinkError] = useState<string | null>(null);

  /**
   * 現在の外部サービス連携状態をバックエンドから取得します。
   */
  const fetchIntegrations = async () => {
    try {
      setLoading(true);
      const data = await userApi.getIntegrations();
      setIntegrations(data);
    } catch (err) {
      console.error('Failed to fetch integrations', err);
      enqueueSnackbar('Failed to load integration status.', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchIntegrations();
  }, []);

  // Intercept query parameters (slackToken and google redirection)
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    let slackToken = params.get('slackToken');
    const googleStatus = params.get('google');
    const googleMessage = params.get('message');

    if (!slackToken) {
      slackToken = sessionStorage.getItem('slack_link_token');
    }

    if (slackToken) {
      handleSlackLinkage(slackToken);
      sessionStorage.removeItem('slack_link_token');
    }

    if (googleStatus) {
      if (googleStatus === 'success') {
        enqueueSnackbar('Google Calendar linked successfully!', { variant: 'success' });
      } else {
        const errMsg = googleMessage ? `: ${googleMessage}` : '';
        enqueueSnackbar(`Google Calendar link failed${errMsg}.`, { variant: 'error' });
      }
      // Clean query params
      const newUrl = window.location.pathname;
      window.history.replaceState({}, document.title, newUrl);
    }
  }, []);

  /**
   * Slack アカウント連携のためのトークンをバックエンドへ送信し、連携処理を実行します。
   *
   * @param token - Slack 連携コマンドから取得した認証用トークン
   */
  const handleSlackLinkage = async (token: string) => {
    setLinking(true);
    setLinkError(null);
    try {
      await userApi.linkSlack(token);
      enqueueSnackbar('Slack account successfully linked!', { variant: 'success' });
      fetchIntegrations();
      // Clean query params
      const newUrl = window.location.pathname;
      window.history.replaceState({}, document.title, newUrl);
    } catch (err: any) {
      console.error('Slack linkage failed', err);
      const errorMsg = err.response?.data?.error || 'Slack linkage failed. The token may be invalid or expired.';
      setLinkError(errorMsg);
      enqueueSnackbar(errorMsg, { variant: 'error' });
    } finally {
      setLinking(false);
    }
  };

  /**
   * Google OAuth 認証フローを開始するために、バックエンドのログインエンドポイントへリダイレクトします。
   */
  const handleGoogleLink = () => {
    if (!user) return;
    // Redirect browser to google login endpoint in backend
    window.location.href = `${API_BASE_URL}/api/v1/auth/google/login?email=${encodeURIComponent(user.email)}`;
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', backgroundColor: '#f8fafc' }}>
        <CircularProgress color="primary" />
      </Box>
    );
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #e0e7ff 0%, #f1f5f9 100%)',
        py: { xs: 3, sm: 6 },
        px: { xs: 2, sm: 3 },
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
      }}
    >
      <Backdrop sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }} open={linking}>
        <Stack spacing={2} sx={{ alignItems: 'center' }}>
          <CircularProgress color="primary" />
          <Typography variant="h6">Linking Slack Account...</Typography>
        </Stack>
      </Backdrop>

      <Box sx={{ maxWidth: 800, width: '100%' }}>
        {/* Header */}
        <PortalHeader email={user?.email} onLogout={logout} />

        {linkError && (
          <Alert severity="error" sx={{ mb: 4, borderRadius: 3 }}>
            {linkError}
          </Alert>
        )}

        <Stack spacing={4}>
          {/* Google Calendar Card */}
          <IntegrationCard
            icon={<Calendar size={32} />}
            iconBgColor="rgba(6, 182, 212, 0.1)"
            iconColor="#06b6d4"
            title="Google Calendar"
            description="Sync your daily log diaries directly to your Google Calendar events."
            action={
              integrations?.googleLinked ? (
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', color: '#10b981' }}>
                  <CheckCircle2 size={20} />
                  <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Connected</Typography>
                </Stack>
              ) : (
                <PortalButton
                  variant="contained"
                  color="primary"
                  onClick={handleGoogleLink}
                  startIcon={<Link2 size={18} />}
                >
                  Connect
                </PortalButton>
              )
            }
          >
            {integrations?.googleLinked && integrations.googleCalendarId && (
              <Box sx={{ mt: 3, pt: 3, borderTop: '1px solid rgba(0, 0, 0, 0.06)' }}>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
                  Target Calendar ID:
                </Typography>
                <Typography variant="body1" sx={{ fontFamily: 'monospace', color: 'text.primary' }}>
                  {integrations.googleCalendarId}
                </Typography>
              </Box>
            )}
          </IntegrationCard>

          {/* Slack Integration Card */}
          <IntegrationCard
            icon={<Link2 size={32} />}
            iconBgColor="rgba(139, 92, 246, 0.1)"
            iconColor="#8b5cf6"
            title="Slack Workspace"
            description="Interact with LifeLog AI directly from your Slack messages."
            action={
              integrations?.slackLinked ? (
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', color: '#10b981' }}>
                  <CheckCircle2 size={20} />
                  <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Connected</Typography>
                </Stack>
              ) : (
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', color: '#f59e0b' }}>
                  <AlertCircle size={20} />
                  <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Not Linked</Typography>
                </Stack>
              )
            }
          >
            {integrations?.slackLinked && integrations.slackUserId ? (
              <Box sx={{ mt: 3, pt: 3, borderTop: '1px solid rgba(0, 0, 0, 0.06)' }}>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
                  Slack User ID:
                </Typography>
                <Typography variant="body1" sx={{ fontFamily: 'monospace', color: 'text.primary' }}>
                  {integrations.slackUserId}
                </Typography>
              </Box>
            ) : (
              <Box
                sx={{
                  mt: 3,
                  p: 3,
                  borderRadius: 3,
                  backgroundColor: 'rgba(99, 102, 241, 0.04)',
                  border: '1px dashed rgba(99, 102, 241, 0.15)',
                }}
              >
                <Typography variant="subtitle2" sx={{ color: 'primary.main', mb: 1, fontWeight: 600 }}>
                  How to Link:
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ lineHeight: 1.6 }}>
                  1. Go to your Slack workspace and type the slash command: <strong>/lifelog-link</strong>
                  <br />
                  2. Click the secure link returned by LifeLog to complete the integration setup.
                </Typography>
              </Box>
            )}
          </IntegrationCard>
        </Stack>
      </Box>
    </Box>
  );
};
