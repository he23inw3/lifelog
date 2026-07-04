import React, { useState, useEffect, useRef } from 'react';
import { Box, Typography, Avatar, CircularProgress, Paper, IconButton, Chip, useTheme, useMediaQuery } from '@mui/material';
import { AlertCircle, PanelLeftOpen } from 'lucide-react';
import { demoApi } from '../api/demoApi';
import type { DemoMessageListResponse } from '../types';
import dayjs from 'dayjs';
import { SlackSidebar } from '../components/home/SlackSidebar';

/**
 * Slack 通知履歴表示ページコンポーネント。
 * デモ用に保存された自動リマインドや登録完了通知、確定確認などのメッセージ履歴を
 * Slack のチャット風 UI で時系列に表示します。
 */
export const SlackFeed: React.FC = () => {
  const [messages, setMessages] = useState<DemoMessageListResponse.DemoMessage[]>([]);
  const [loading, setLoading] = useState(true);

  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm')); // 600px未満をモバイルと判定
  const [sidebarOpen, setSidebarOpen] = useState(!isMobile);

  // ウィンドウのリサイズに応じて開閉状態を自動追従させる
  useEffect(() => {
    setSidebarOpen(!isMobile);
  }, [isMobile]);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  const fetchMessages = async () => {
    try {
      setLoading(true);
      const data = await demoApi.getDemoSlackMessages();
      // システムから能動的にプッシュ送信される「リマインド」または「振り返りレポート」のみを表示する
      const filtered = data.filter(msg =>
        msg.text.includes('リマインド') ||
        msg.text.includes('未入力') ||
        msg.text.includes('振り返り') ||
        msg.text.includes('レポート')
      );
      setMessages(filtered);
    } catch (err) {
      console.error('Failed to fetch Slack messages:', err);
      // バックエンド未接続の場合はモックデータを表示してデモ体験を可能にする
      setMessages([
        {
          slackUserId: 'DEMO_USER',
          type: 'POST',
          text: '【リマインド】夜遅く失礼します。今日一日どうでした？稼働時間と作業内容を教えてください！',
          timestamp: dayjs().subtract(1, 'hour').toISOString(),
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMessages();
  }, []);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, loading]);

  return (
    <Box sx={{
      display: 'flex',
      height: 'calc(100vh - 100px)',
      bgcolor: '#f8fafc',
      border: '1px solid #e2e8f0',
      borderRadius: '16px',
      overflow: 'hidden',
      boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)'
    }}>
      {/* 1. Slack風左サイドバー */}
      <SlackSidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      {/* 2. Slack風メインチャットエリア */}
      <Box sx={{ display: 'flex', flexDirection: 'column', flexGrow: 1, bgcolor: '#ffffff', height: '100%' }}>
        {/* チャットヘッダー */}
        <Box sx={{ px: 3, py: 2, borderBottom: '1px solid #e2e8f0', display: 'flex', alignItems: 'center', gap: 1.5, bgcolor: '#ffffff' }}>
          {!sidebarOpen && (
            <IconButton size="small" onClick={() => setSidebarOpen(true)} sx={{ color: '#616061', mr: 1 }}>
              <PanelLeftOpen size={20} />
            </IconButton>
          )}
          <Avatar sx={{ bgcolor: '#2bac76', width: 24, height: 24, fontSize: '11px', fontWeight: 800 }}>L</Avatar>
          <Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#1d1c1d', lineHeight: 1.2 }}>
                LifeLog Bot
              </Typography>
              <Box sx={{ width: 8, height: 8, bgcolor: '#2bac76', borderRadius: '50%' }} />
              <Typography variant="caption" sx={{ color: '#616061', fontWeight: 500 }}>(ダイレクトメッセージ - 通知)</Typography>
            </Box>
            <Typography variant="caption" sx={{ color: '#616061' }}>
              Slack へ送信された自動リマインド、確認通知、登録完了通知などの履歴です。
            </Typography>
          </Box>
        </Box>

        {/* チャットメッセージ表示領域 */}
        <Box sx={{ flexGrow: 1, overflowY: 'auto', p: 3, display: 'flex', flexDirection: 'column', gap: 3 }}>

          {/* デモ環境の説明（ピン留め風インフォメーション） */}
          <Paper elevation={0} sx={{ p: 2, bgcolor: '#f0f9ff', border: '1px solid #e0f2fe', borderRadius: '12px', display: 'flex', gap: 1.5 }}>
            <AlertCircle size={20} color="#0284c7" style={{ flexShrink: 0, marginTop: '2px' }} />
            <Box>
              <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#0369a1', mb: 0.5 }}>
                【ダイレクトメッセージ通知ログ】
              </Typography>
              <Typography variant="caption" sx={{ color: '#334155', display: 'block', lineHeight: 1.5, mb: 1 }}>
                この画面では、システムの動作に伴ってユーザー宛てに Slack DM として送信された通知の履歴を表示します。
                自動リマインドや、完了確認、エラー発生時の通知など、実機でのやり取りをシミュレートしたメッセージが蓄積されます。
              </Typography>
              <Typography variant="caption" sx={{ color: '#2b6cb0', fontWeight: 700, display: 'block', mb: 0.5 }}>
                使用API:
              </Typography>
              <Typography variant="caption" sx={{ color: '#475569', display: 'block' }}>
                • <strong>BE-API502 (デモSlackメッセージ一覧取得)</strong>: デモ動作時に `demo_slack_messages` コレクションに保存されたメッセージログを取得
              </Typography>
            </Box>
          </Paper>

          {loading ? (
            <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 8, gap: 2 }}>
              <CircularProgress size={40} />
              <Typography variant="body2" sx={{ color: '#718096' }}>メッセージ履歴を読み込み中...</Typography>
            </Box>
          ) : messages.length === 0 ? (
            <Box sx={{ py: 8, textAlign: 'center' }}>
              <Typography variant="body1" sx={{ color: '#718096', fontWeight: 600 }}>
                まだSlack通知はありません
              </Typography>
              <Typography variant="body2" sx={{ color: '#a0aec0' }}>
                日報の登録やバッチ処理を実行すると、ここにメッセージが届きます。
              </Typography>
            </Box>
          ) : (
            messages.map((msg, index) => {
              const isSystem = msg.type === 'UPDATE';
              const isConfirm = msg.type === 'CONFIRM';
              const isError = msg.type === 'ERROR';

              const avatarColor = isError ? '#ef4444' : '#2bac76';
              const senderName = 'LifeLog Bot';

              // タイムスタンプフォーマット
              const timeText = dayjs(msg.timestamp).isValid()
                ? dayjs(msg.timestamp).format('HH:mm')
                : dayjs.unix(Number(msg.timestamp)).isValid()
                  ? dayjs.unix(Number(msg.timestamp)).format('HH:mm')
                  : msg.timestamp;

              return (
                <Box key={index} sx={{ display: 'flex', gap: 2, '&:hover': { bgcolor: '#f7fafc', mx: -3, px: 3, py: 0.5, borderRadius: '4px' } }}>
                  <Avatar sx={{ bgcolor: avatarColor, fontWeight: 700, fontSize: '14px', width: 36, height: 36, flexShrink: 0 }}>
                    L
                  </Avatar>

                  <Box sx={{ flexGrow: 1 }}>
                    <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1, mb: 0.5 }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#1d1c1d' }}>
                        {senderName}
                      </Typography>
                      <Chip
                        label="APP"
                        size="small"
                        sx={{ height: 16, fontSize: '9px', fontWeight: 800, bgcolor: '#f1f1f1', color: '#616061', borderRadius: '3px' }}
                      />
                      <Typography variant="caption" sx={{ color: '#616061' }}>
                        {timeText}
                      </Typography>
                    </Box>
                    <Typography
                      variant="body2"
                      sx={{
                        color: isError ? '#ef4444' : '#1d1c1d',
                        whiteSpace: 'pre-wrap',
                        lineHeight: 1.6,
                        bgcolor: isConfirm ? '#ebf8ff' : isSystem ? '#f0fdf4' : isError ? '#fef2f2' : 'transparent',
                        p: isConfirm || isSystem || isError ? 2 : 0,
                        borderRadius: '8px',
                        border: isConfirm ? '1px solid #bee3f8' : isSystem ? '1px solid #c6f6d5' : isError ? '1px solid #fca5a5' : 'none',
                      }}
                    >
                      {msg.text}
                    </Typography>
                  </Box>
                </Box>
              );
            })
          )}
          <div ref={messagesEndRef} />
        </Box>
      </Box>
    </Box>
  );
};
