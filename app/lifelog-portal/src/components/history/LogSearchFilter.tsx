import React from 'react';
import { Paper, TextField, Button } from '@mui/material';
import { Search } from 'lucide-react';

interface Props {
  fromDate: string;
  toDate: string;
  onFromChange: (v: string) => void;
  onToChange: (v: string) => void;
  onSubmit: (e: React.FormEvent) => void;
}

/**
 * 日報履歴の検索フォームコンポーネント。
 * 開始日・終了日の指定と検索実行を担います。
 *
 * @param props フォームの状態と変更ハンドラ
 */
export const LogSearchFilter: React.FC<Props> = ({
  fromDate,
  toDate,
  onFromChange,
  onToChange,
  onSubmit,
}) => {
  return (
    <Paper
      elevation={0}
      component="form"
      onSubmit={onSubmit}
      sx={{
        p: 3,
        borderRadius: '12px',
        border: '1px solid #e2e8f0',
        bgcolor: '#ffffff',
        mb: 4,
        display: 'flex',
        alignItems: 'center',
        gap: 2,
        flexWrap: 'wrap',
      }}
    >
      <TextField
        label="開始日"
        type="date"
        size="small"
        value={fromDate}
        onChange={(e) => onFromChange(e.target.value)}
        slotProps={{ inputLabel: { shrink: true } }}
        sx={{ flexGrow: 1, minWidth: '150px' }}
      />
      <TextField
        label="終了日"
        type="date"
        size="small"
        value={toDate}
        onChange={(e) => onToChange(e.target.value)}
        slotProps={{ inputLabel: { shrink: true } }}
        sx={{ flexGrow: 1, minWidth: '150px' }}
      />
      <Button
        type="submit"
        variant="contained"
        color="primary"
        startIcon={<Search size={16} />}
        sx={{
          height: '40px',
          px: 3,
          borderRadius: '8px',
          textTransform: 'none',
          fontWeight: 700,
          boxShadow: 'none',
          '&:hover': { boxShadow: 'none' },
        }}
      >
        検索
      </Button>
    </Paper>
  );
};
