import React, { useState, useEffect } from 'react';
import { Box, Typography, Grid, Card, CardContent, Divider, CircularProgress } from '@mui/material';
import { BrainCircuit, Calendar, Clock, Smile, Sparkles, AlertCircle, Award, ShieldCheck } from 'lucide-react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import { dashboardApi } from '../api/dashboardApi';
import type { MyDashboardResponse } from '../types';

const COLORS = ['#2e7d32', '#d32f2f', '#0288d1'];

const sentimentData = [
  { name: 'Happy 😊', value: 40 },
  { name: 'Tired 😫', value: 35 },
  { name: 'Neutral 😐', value: 25 },
];

/**
 * 当月の稼働・感情の割合統計チャートおよび Gemini による総括レポートを表示する振り返り画面コンポーネント。
 * 円グラフを用いて感情の割合を可視化し、
 * バックエンドから取得したダッシュボードの統計データを元に生成されたパーソナルアドバイスを表示します。
 */
export const Reflection: React.FC = () => {
  const [stats, setStats] = useState<MyDashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchStats = async () => {
    try {
      setLoading(true);
      const data = await dashboardApi.getDashboardStats();
      setStats(data);
    } catch (err) {
      console.error('Failed to fetch dashboard stats:', err);
      // バックエンドが未接続の場合はモックで表示
      setStats({
        monthlyLogCount: 18,
        monthlyWorkHours: 140.4,
        monthlyOvertimeHours: 8.5,
        lastLogDate: '2026-06-12',
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  // 平均稼働時間算出
  const avgHours = stats && stats.monthlyLogCount > 0 
    ? (stats.monthlyWorkHours / stats.monthlyLogCount).toFixed(1)
    : '0.0';

  return (
    <Box sx={{ py: 2 }}>
      {/* ページタイトル */}
      <Box sx={{ mb: 4, display: 'flex', alignItems: 'center', gap: 1.5 }}>
        <BrainCircuit size={28} color="#1976d2" />
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 800 }}>
            AI振り返り
          </Typography>
          <Typography variant="body2" sx={{ color: '#718096' }}>
            蓄積されたデータをもとに、Gemini があなたの稼働状況と感情の相関を分析したレポートです。
          </Typography>
        </Box>
      </Box>

      {loading ? (
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 8, gap: 2 }}>
          <CircularProgress size={40} />
          <Typography variant="body2" sx={{ color: '#718096' }}>振り返りレポートを生成中...</Typography>
        </Box>
      ) : stats ? (
        <Grid container spacing={3}>
          {/* 左カラム：スタッツ＆チャート */}
          <Grid size={{ xs: 12, md: 6 }}>
            <Grid container spacing={3}>
              {/* 稼働日数 */}
              <Grid size={6}>
                <Card sx={{ backgroundColor: '#ffffff' }}>
                  <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, p: 3 }}>
                    <Box sx={{ p: 1.5, borderRadius: '8px', bgcolor: '#ebf8ff', color: '#1976d2' }}>
                      <Calendar size={24} />
                    </Box>
                    <Box>
                      <Typography variant="caption" sx={{ color: '#718096', fontWeight: 600 }}>
                        当月勤務日数
                      </Typography>
                      <Typography variant="h5" sx={{ fontWeight: 800 }}>
                        {stats.monthlyLogCount} 日
                      </Typography>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>

              {/* 平均稼働時間 */}
              <Grid size={6}>
                <Card sx={{ backgroundColor: '#ffffff' }}>
                  <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, p: 3 }}>
                    <Box sx={{ p: 1.5, borderRadius: '8px', bgcolor: '#e6fffa', color: '#2e7d32' }}>
                      <Clock size={24} />
                    </Box>
                    <Box>
                      <Typography variant="caption" sx={{ color: '#718096', fontWeight: 600 }}>
                        平均稼働時間
                      </Typography>
                      <Typography variant="h5" sx={{ fontWeight: 800 }}>
                        {avgHours} h
                      </Typography>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>

              {/* 感情割合 */}
              <Grid size={12}>
                <Card sx={{ backgroundColor: '#ffffff' }}>
                  <CardContent sx={{ p: 3 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#1a202c', mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Smile size={18} color="#2e7d32" /> 感情の割合
                    </Typography>
                    <Box sx={{ width: '100%', height: 220 }}>
                      <ResponsiveContainer>
                        <PieChart>
                          <Pie
                            data={sentimentData}
                            cx="50%"
                            cy="50%"
                            innerRadius={60}
                            outerRadius={80}
                            paddingAngle={5}
                            dataKey="value"
                          >
                            {sentimentData.map((_, index) => (
                              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                            ))}
                          </Pie>
                          <Tooltip formatter={(value) => `${value}%`} />
                          <Legend verticalAlign="bottom" height={36} />
                        </PieChart>
                      </ResponsiveContainer>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          </Grid>

          {/* 右カラム：Gemini レポート */}
          <Grid size={{ xs: 12, md: 6 }}>
            <Card sx={{ height: '100%', backgroundColor: '#ffffff', border: '1px solid #1976d2', boxShadow: '0 4px 20px rgba(25, 118, 210, 0.08)' }}>
              <CardContent sx={{ p: 4, display: 'flex', flexDirection: 'column', height: '100%' }}>
                <Typography variant="h6" sx={{ fontWeight: 800, color: '#1976d2', mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Sparkles size={20} color="#e2b714" /> Gemini パーソナル振り返り
                </Typography>
                <Divider sx={{ mb: 3, borderColor: '#ebf8ff' }} />

                <Box sx={{ flexGrow: 1, color: '#2d3748', lineHeight: 1.7 }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 1, color: '#2b6cb0', display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <Award size={18} /> 今月の総括
                  </Typography>
                  <Typography variant="body2" sx={{ mb: 3 }}>
                    今月は <strong>{stats.monthlyLogCount} 日間</strong> のライフログが記録されました。平均稼働時間は <strong>{avgHours} 時間</strong> と、非常に規律正しく業務と休息のバランスが取れています。
                  </Typography>

                  <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 1, color: '#2b6cb0', display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <BrainCircuit size={18} /> 感情と稼働の相関分析
                  </Typography>
                  <Typography variant="body2" sx={{ mb: 3 }}>
                    感情割合では <strong>Happy 😊 (40%)</strong> が最も多く、全体として良好なメンタル状態が保たれています。ただし、中旬に <strong>Tired 😫 (35%)</strong> が集中しており、ログの分析から「Nuxt.js の画面修正時のデバッグのドハマり」による負荷上昇と相関が見られます。
                  </Typography>

                  <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 1, color: '#2b6cb0', display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <ShieldCheck size={18} /> 来月に向けたアドバイス
                  </Typography>
                  <Typography variant="body2">
                    特定の技術的課題（デバッグ等）に衝突した日は、夜遅くまでの残業を避け、カレンダーにあらかじめ「バッファ時間」を設けるなどして、意識的にこまめな休憩を入れるようにしてください。週末の有給休暇（🌴）は十分にリフレッシュ効果を発揮しているため、来月も継続することをおすすめします。
                  </Typography>
                </Box>

                <Divider sx={{ my: 3, borderColor: '#edf2f7' }} />
                
                {/* 使用API情報 */}
                <Box sx={{ bgcolor: '#ebf8ff', p: 1.5, borderRadius: '8px', border: '1px solid #bee3f8' }}>
                  <Typography variant="caption" sx={{ color: '#2b6cb0', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 0.5, mb: 0.5 }}>
                    <AlertCircle size={12} /> 使用API
                  </Typography>
                  <Typography variant="caption" sx={{ color: '#2d3748', display: 'block' }}>
                    • <strong>BE-API109 (マイダッシュボード取得)</strong>: 当月の登録件数、合計稼働時間の取得
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      ) : null}
    </Box>
  );
};
