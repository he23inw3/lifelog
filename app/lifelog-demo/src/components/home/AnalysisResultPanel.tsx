import React from 'react';
import { Paper, Typography, Grid, Box, Divider, Chip } from '@mui/material';
import { Sparkles, CheckCircle2 } from 'lucide-react';
import type { LogDetailResponse } from '../../types';
import { getSentimentIcon, getSentimentText } from '../../utils';

interface AnalysisResultPanelProps {
  result: LogDetailResponse;
  isPreview?: boolean;
}

export const AnalysisResultPanel: React.FC<AnalysisResultPanelProps> = ({ result, isPreview = false }) => {
  return (
    <Paper 
      elevation={0} 
      sx={{ 
        p: 2.5, 
        borderLeft: `4px solid ${isPreview ? '#cbd5e1' : '#2bac76'}`, 
        border: '1px solid #e2e8f0', 
        borderLeftWidth: '4px',
        borderRadius: '8px', 
        backgroundColor: '#f8fafc',
        maxWidth: 700,
        mt: 1,
        mb: isPreview ? 1.5 : 0
      }}
    >
      <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#1e293b', mb: 2, display: 'flex', alignItems: 'center', gap: 0.8 }}>
        <Sparkles size={16} color={isPreview ? '#94a3b8' : '#e2b714'} />
        {isPreview ? '登録内容のプレビュー' : 'AI解析詳細'}
      </Typography>

      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid size={{ xs: 6, sm: 4 }}>
          <Typography variant="caption" sx={{ color: '#64748b', display: 'block' }}>対象日付</Typography>
          <Typography variant="body2" sx={{ fontWeight: 700, color: '#0f172a' }}>{result.logDate}</Typography>
        </Grid>
        <Grid size={{ xs: 6, sm: 4 }}>
          <Typography variant="caption" sx={{ color: '#64748b', display: 'block' }}>稼働時間</Typography>
          <Typography variant="body2" sx={{ fontWeight: 700, color: '#0f172a' }}>
            {result.holiday ? (result.workHours > 0 ? `${result.workHours}h` : '0.0h') : `${result.workHours}h`}
          </Typography>
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8 }}>
            {getSentimentIcon(result.sentiment, 20)}
            <Box>
              <Typography variant="caption" sx={{ color: '#64748b', display: 'block' }}>感情</Typography>
              <Typography variant="body2" sx={{ fontWeight: 700, color: '#0f172a' }}>
                {getSentimentText(result.sentiment)}
              </Typography>
            </Box>
          </Box>
        </Grid>
      </Grid>

      <Divider sx={{ my: 1.5, borderColor: '#e2e8f0' }} />

      <Box sx={{ mb: 1.5 }}>
        <Typography variant="caption" sx={{ color: '#64748b', display: 'block', mb: 0.5, fontWeight: 700 }}>作業内容（日報）</Typography>
        <Typography variant="body2" sx={{ color: '#334155', bgcolor: '#ffffff', p: 1.5, borderRadius: '6px', border: '1px solid #e2e8f0' }}>
          {result.tasks || (result.holiday ? '（本日は休日・有給休暇です）' : '業務内容の抽出なし')}
        </Typography>
      </Box>

      <Box sx={{ mb: isPreview ? 1 : 2 }}>
        <Typography variant="caption" sx={{ color: '#64748b', display: 'block', mb: 0.5, fontWeight: 700 }}>日記</Typography>
        <Typography variant="body2" sx={{ color: '#334155', bgcolor: '#ffffff', p: 1.5, borderRadius: '6px', border: '1px solid #e2e8f0' }}>
          {result.diary || '日記内容の抽出なし'}
        </Typography>
      </Box>

      {/* 確定後のみ同期バッジを表示 */}
      {!isPreview && (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mt: 1 }}>
          <Chip
            icon={<CheckCircle2 size={12} color="#15803d" />}
            label="Google Calendar 同期"
            size="small"
            sx={{ bgcolor: '#f0fdf4', color: '#166534', border: '1px solid #bbf7d0', fontWeight: 600 }}
          />
          <Chip
            icon={<CheckCircle2 size={12} color="#15803d" />}
            label="BigQuery 保存完了"
            size="small"
            sx={{ bgcolor: '#f0fdf4', color: '#166534', border: '1px solid #bbf7d0', fontWeight: 600 }}
          />
          <Chip
            icon={<CheckCircle2 size={12} color="#15803d" />}
            label="Firestore 保存完了"
            size="small"
            sx={{ bgcolor: '#f0fdf4', color: '#166534', border: '1px solid #bbf7d0', fontWeight: 600 }}
          />
        </Box>
      )}
    </Paper>
  );
};
