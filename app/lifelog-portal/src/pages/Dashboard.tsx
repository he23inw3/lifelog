import React, { useEffect, useState } from 'react';
import { Box, Typography, Paper, CircularProgress, LinearProgress, Stack } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { userApi } from '../api/userApi';
import type { MyDashboardResponse } from '../types';
import { PortalHeader } from '../components/PortalHeader';
import { Calendar, Clock, AlertCircle, Sparkles, ArrowRight, Award } from 'lucide-react';
import { useSnackbar } from 'notistack';
import { useNavigate } from 'react-router';

/**
 * AI振り返りおよび統計情報を提供するダッシュボードページコンポーネント。
 * 当月の日報登録数、累積稼働時間、時間外労働時間、最終登録日を表示し、AI分析のヒントを提供します。
 */
export const Dashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const { enqueueSnackbar } = useSnackbar();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState<MyDashboardResponse | null>(null);

  const fetchDashboardStats = async () => {
    try {
      setLoading(true);
      const data = await userApi.getMyDashboard();
      setStats(data);
    } catch (err) {
      console.error('Failed to fetch dashboard stats', err);
      enqueueSnackbar('ダッシュボード情報の取得に失敗しました。', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardStats();
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', backgroundColor: '#f8fafc' }}>
        <CircularProgress color="primary" />
      </Box>
    );
  }

  // 想定される月間基本労働時間 (20営業日 x 8時間 = 160時間)
  const TARGET_WORK_HOURS = 160;
  const workHours = stats?.monthlyWorkHours || 0;
  const workHoursPercentage = Math.min(100, Math.round((workHours / TARGET_WORK_HOURS) * 100));
  
  // 残業アラートカラー（30h以上で注意/黄、45h以上で警告/赤）
  const overtimeHours = stats?.monthlyOvertimeHours || 0;
  const getOvertimeColor = (hours: number) => {
    if (hours >= 45) return '#ef4444'; // 赤
    if (hours >= 30) return '#f59e0b'; // 黄
    return '#10b981'; // 緑
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%)',
        py: { xs: 3, sm: 6 },
        px: { xs: 2, sm: 3 },
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
      }}
    >
      <Box sx={{ maxWidth: 800, width: '100%' }}>
        {/* 共通ヘッダー */}
        <PortalHeader email={user?.email} onLogout={logout} />

        {/* メインコンテンツ */}
        <Stack spacing={{ xs: 2.5, sm: 4 }} sx={{ mt: 2 }}>
          {/* AI振り返り・サマリーカード */}
          <Paper
            elevation={0}
            sx={{
              p: { xs: 2.5, sm: 4 },
              borderRadius: '16px',
              background: 'linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%)',
              color: '#ffffff',
              position: 'relative',
              overflow: 'hidden',
              boxShadow: '0 10px 15px -3px rgba(59, 130, 246, 0.3)',
            }}
          >
            {/* 背景デコレーション */}
            <Box sx={{ position: 'absolute', right: '-40px', top: '-40px', width: 160, height: 160, borderRadius: '50%', background: 'rgba(255, 255, 255, 0.1)' }} />
            <Box sx={{ position: 'absolute', right: '40px', bottom: '-80px', width: 200, height: 200, borderRadius: '50%', background: 'rgba(255, 255, 255, 0.05)' }} />

            <Stack spacing={2} sx={{ position: 'relative', zIndex: 1 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Sparkles size={24} color="#fef08a" />
                <Typography variant="h5" sx={{ fontWeight: 800, fontSize: { xs: '1.25rem', sm: '1.5rem' } }}>
                  今月の振り返り & AI分析
                </Typography>
              </Box>
              <Typography variant="body1" sx={{ opacity: 0.9, lineHeight: 1.6, fontSize: { xs: '0.9rem', sm: '1rem' } }}>
                お疲れ様です！今月はこれまでに <strong>{stats?.monthlyLogCount || 0} 件</strong> の日報が登録されています。
                {stats && stats.monthlyLogCount > 0 ? (
                  ` 最後に登録された日報は「${stats.lastLogDate}」です。引き続きSlackからの簡単な入力で稼働を記録していきましょう。`
                ) : (
                  " まだ今月の日報は登録されていません。Slack からメッセージを送信して日報を登録してみましょう。"
                )}
              </Typography>
            </Stack>
          </Paper>

          {/* メトリクスグリッド */}
          <Box
            sx={{
              display: 'grid',
              gridTemplateColumns: { xs: '1fr', sm: 'repeat(3, 1fr)' },
              gap: { xs: 2, sm: 3 },
            }}
          >
            {/* 日報件数カード */}
            <Paper elevation={0} sx={{ p: { xs: 2.5, sm: 3 }, borderRadius: '16px', border: '1px solid #e2e8f0', bgcolor: '#ffffff', display: 'flex', flexDirection: 'column', gap: 1 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="subtitle2" color="text.secondary" sx={{ fontWeight: 700 }}>
                  当月日報件数
                </Typography>
                <Box sx={{ p: 1, borderRadius: '8px', bgcolor: 'rgba(79, 70, 229, 0.1)', color: '#4f46e5', display: 'flex', alignItems: 'center' }}>
                  <Calendar size={20} />
                </Box>
              </Box>
              <Typography variant="h3" sx={{ fontWeight: 800, color: '#1e293b', my: 1, fontSize: { xs: '2rem', sm: '3rem' } }}>
                {stats?.monthlyLogCount || 0} <span style={{ fontSize: '18px', fontWeight: 600 }}>件</span>
              </Typography>
              <Typography variant="caption" color="text.secondary">
                稼働日・休日の登録実績
              </Typography>
            </Paper>

            {/* 累積稼働時間カード */}
            <Paper elevation={0} sx={{ p: { xs: 2.5, sm: 3 }, borderRadius: '16px', border: '1px solid #e2e8f0', bgcolor: '#ffffff', display: 'flex', flexDirection: 'column', gap: 1 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="subtitle2" color="text.secondary" sx={{ fontWeight: 700 }}>
                  当月累積稼働
                </Typography>
                <Box sx={{ p: 1, borderRadius: '8px', bgcolor: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6', display: 'flex', alignItems: 'center' }}>
                  <Clock size={20} />
                </Box>
              </Box>
              <Typography variant="h3" sx={{ fontWeight: 800, color: '#1e293b', my: 1, fontSize: { xs: '2rem', sm: '3rem' } }}>
                {workHours} <span style={{ fontSize: '18px', fontWeight: 600 }}>h</span>
              </Typography>
              <Box sx={{ width: '100%', mt: 0.5 }}>
                <LinearProgress variant="determinate" value={workHoursPercentage} sx={{ height: 6, borderRadius: 3, bgcolor: '#f1f5f9', '& .MuiLinearProgress-bar': { bgcolor: '#3b82f6' } }} />
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 0.5 }}>
                  <Typography variant="caption" color="text.secondary">目安進捗: {workHoursPercentage}%</Typography>
                  <Typography variant="caption" color="text.secondary">目標: 160h</Typography>
                </Box>
              </Box>
            </Paper>

            {/* 残業時間カード */}
            <Paper elevation={0} sx={{ p: { xs: 2.5, sm: 3 }, borderRadius: '16px', border: '1px solid #e2e8f0', bgcolor: '#ffffff', display: 'flex', flexDirection: 'column', gap: 1 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="subtitle2" color="text.secondary" sx={{ fontWeight: 700 }}>
                  時間外労働
                </Typography>
                <Box sx={{ p: 1, borderRadius: '8px', bgcolor: 'rgba(16, 185, 129, 0.1)', color: getOvertimeColor(overtimeHours), display: 'flex', alignItems: 'center' }}>
                  <AlertCircle size={20} />
                </Box>
              </Box>
              <Typography variant="h3" sx={{ fontWeight: 800, color: getOvertimeColor(overtimeHours), my: 1, fontSize: { xs: '2rem', sm: '3rem' } }}>
                {overtimeHours} <span style={{ fontSize: '18px', fontWeight: 600 }}>h</span>
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {overtimeHours >= 45
                  ? '⚠️ 残業が上限に近づいています！'
                  : overtimeHours >= 30
                  ? '💡 やや残業が多くなっています。'
                  : '安定した残業時間内です。'}
              </Typography>
            </Paper>
          </Box>

          {/* AI分析・ヘルスケアセクション */}
          <Paper elevation={0} sx={{ p: { xs: 2.5, sm: 3 }, borderRadius: '16px', border: '1px solid #e2e8f0', bgcolor: '#ffffff' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <Award size={20} color="#4f46e5" />
              <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#1e293b' }}>
                AIによるコンディション分析について
              </Typography>
            </Box>
            <Typography variant="body2" sx={{ color: '#475569', lineHeight: 1.7, mb: 2, fontSize: { xs: '0.85rem', sm: '0.875rem' } }}>
              これまでに送信された日記や稼働状況に基づき、AIはあなたのモチベーションや感情の波を自動的に解析しています。
              感情の傾向（Happy 😊, Tired 😫, Stressed 😡 など）を可視化することで、働き方のバランス改善をサポートします。
            </Typography>
            <Stack direction="row" spacing={2}>
              <Box 
                onClick={() => navigate('/history')}
                sx={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: 0.5, 
                  color: '#4f46e5', 
                  fontWeight: 700, 
                  cursor: 'pointer',
                  fontSize: { xs: '0.85rem', sm: '0.875rem' },
                  '&:hover': { color: '#3730a3', textDecoration: 'underline' }
                }}
              >
                過去の日報履歴を詳しく見る <ArrowRight size={16} />
              </Box>
            </Stack>
          </Paper>
        </Stack>
      </Box>
    </Box>

  );
};
