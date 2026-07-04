import React, { useState, useEffect } from 'react';
import { Box, Card, CardContent, Typography, TextField, Stack, Alert } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { userApi } from '../api/userApi';
import { PortalButton } from '../components/PortalButton';
import { CheckCircle2, AlertCircle } from 'lucide-react';
import { useSnackbar } from 'notistack';

/**
 * ログインユーザーの初回アカウント登録画面。
 * Slack 連携用のトークンが存在する場合は Slack ユーザーIDの自動解決を行い、
 * Google カレンダー ID には Google アカウントのメールアドレスを自動入力して登録を簡略化します。
 */
export const Register: React.FC = () => {
  const { user } = useAuth();
  const { enqueueSnackbar } = useSnackbar();

  const [userName, setUserName] = useState('');
  const [remindTime, setRemindTime] = useState('22:00');
  const [googleCalendarId, setGoogleCalendarId] = useState(user?.email || '');
  const [slackUserId, setSlackUserId] = useState('');
  const [slackToken, setSlackToken] = useState<string | null>(null);
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // sessionStorage から退避しておいた slackToken を取得
    const token = sessionStorage.getItem('slack_link_token');
    if (token) {
      setSlackToken(token);
    }
  }, []);

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!userName.trim() || !remindTime.trim() || !googleCalendarId.trim()) {
      setError('必須項目をすべて入力してください。');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const payload: any = {
        userName: userName.trim(),
        remindTime: remindTime.trim(),
        googleCalendarId: googleCalendarId.trim(),
      };

      if (slackToken) {
        payload.slackToken = slackToken;
      } else {
        if (!slackUserId.trim()) {
          setError('Slack ユーザー ID が必要です。Slack アプリから連携リンクを開き直すか、手動で入力してください。');
          setLoading(false);
          return;
        }
        payload.slackUserId = slackUserId.trim();
      }

      await userApi.registerUser(payload);
      
      // 登録成功
      enqueueSnackbar('アカウント登録が完了しました！', { variant: 'success' });
      
      // 連携完了のためトークンを削除
      sessionStorage.removeItem('slack_link_token');

      // ダッシュボードへリダイレクト（全体をリロードして登録状態を再チェックさせる）
      window.location.href = '/dashboard';
    } catch (err: any) {
      console.error('Registration failed', err);
      const errMsg = err.response?.data?.error || '登録に失敗しました。入力値を確認してください。';
      setError(errMsg);
      enqueueSnackbar(errMsg, { variant: 'error' });
    } finally {
      setLoading(false);
    }
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
          maxWidth: 520,
          width: '100%',
          position: 'relative',
          overflow: 'hidden',
          borderRadius: '16px',
          boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1)',
          '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '4px',
            background: 'linear-gradient(90deg, #4f46e5 0%, #3b82f6 100%)',
          },
        }}
      >
        <CardContent sx={{ p: 4 }}>
          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Typography
              variant="h4"
              component="h1"
              sx={{
                fontWeight: 800,
                background: 'linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%)',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
                mb: 1,
              }}
            >
              LifeLog アカウント初回登録
            </Typography>
            <Typography variant="body2" color="text.secondary">
              LifeLog の利用を開始するため、以下の初期設定を入力してください。
            </Typography>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 3, borderRadius: '8px' }} icon={<AlertCircle size={18} />}>
              {error}
            </Alert>
          )}

          {slackToken ? (
            <Alert severity="success" sx={{ mb: 3, borderRadius: '8px' }} icon={<CheckCircle2 size={18} />}>
              Slack連携情報を検出しました。<br />
              <strong>Slackアカウントは自動的にこのアカウントに紐づけられます。</strong>
            </Alert>
          ) : (
            <Alert severity="warning" sx={{ mb: 3, borderRadius: '8px' }} icon={<AlertCircle size={18} />}>
              Slackの連携リンクから直接アクセスされていません。<br />
              Slack内で <strong>/lifelog-link</strong> を実行して払い出されたリンクからアクセスし直すか、手動でIDを入力してください。
            </Alert>
          )}

          <Stack component="form" onSubmit={handleRegister} spacing={3}>
            {/* 表示名 */}
            <TextField
              label="ユーザー名（表示名）"
              placeholder="例: 山田 太郎"
              required
              fullWidth
              value={userName}
              onChange={(e) => setUserName(e.target.value)}
              disabled={loading}
            />

            {/* Google カレンダー ID */}
            <TextField
              label="Google カレンダー ID"
              placeholder="例: example@gmail.com"
              required
              fullWidth
              value={googleCalendarId}
              onChange={(e) => setGoogleCalendarId(e.target.value)}
              helperText="日報を同期するGoogleカレンダーのIDです（通常はGoogleアカウントのメールアドレス）。"
              disabled={loading}
            />

            {/* リマインド送信予定時刻 */}
            <TextField
              label="日報リマインド時刻"
              placeholder="22:00"
              required
              fullWidth
              value={remindTime}
              onChange={(e) => setRemindTime(e.target.value)}
              helperText="日報の入力がない場合にSlackで催促通知を送る時間です（HH:mm 形式）。"
              disabled={loading}
              slotProps={{
                htmlInput: {
                  pattern: "^([01]\\d|2[0-3]):[0-5]\\d$"
                }
              }}
            />

            {/* Slack ユーザー ID (トークンが無い場合のみ手動入力) */}
            {!slackToken && (
              <TextField
                label="Slack ユーザー ID"
                placeholder="例: U12345678"
                required
                fullWidth
                value={slackUserId}
                onChange={(e) => setSlackUserId(e.target.value)}
                helperText="Slack ワークスペースでのあなたのユーザーIDです（プロフィールの詳細などから確認できます）。"
                disabled={loading}
              />
            )}

            <PortalButton
              fullWidth
              type="submit"
              variant="contained"
              color="primary"
              size="large"
              disabled={loading}
              sx={{ height: 50, borderRadius: '8px', fontWeight: 700, mt: 1 }}
            >
              {loading ? '登録中...' : '登録を完了する'}
            </PortalButton>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
};
