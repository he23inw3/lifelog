import React from 'react';
import { Typography, Paper } from '@mui/material';
import { AlertCircle } from 'lucide-react';

/**
 * 日報履歴が0件の場合に表示する空状態コンポーネント。
 */
export const LogEmptyState: React.FC = () => {
  return (
    <Paper
      elevation={0}
      sx={{
        p: 6,
        borderRadius: '12px',
        border: '1px solid #e2e8f0',
        bgcolor: '#ffffff',
        textAlign: 'center',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 2,
      }}
    >
      <AlertCircle size={40} color="#94a3b8" />
      <Typography variant="h6" sx={{ fontWeight: 700, color: '#475569' }}>
        対象の日報履歴が見つかりませんでした
      </Typography>
      <Typography variant="body2" color="text.secondary">
        指定された日付範囲を変更するか、Slack から新しく日報を入力してください。
      </Typography>
    </Paper>
  );
};
