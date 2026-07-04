import React from 'react';
import { Box, Typography, Paper, CircularProgress, Button, Chip } from '@mui/material';
import type { LogListResponse } from '../../types';
import { getSentimentIcon, getSentimentText } from '../../utils';
import { Clock, ChevronRight, RefreshCw } from 'lucide-react';
import dayjs from 'dayjs';

interface Props {
  log: LogListResponse.Log;
  isSyncing: boolean;
  onOpenDetail: (logDate: string) => void;
  onSyncCalendar: (e: React.MouseEvent, logDate: string) => void;
}

/**
 * 1件分の日報履歴カードコンポーネント。
 * 日付・感情・稼働時間・作業内容プレビューと、カレンダー再同期ボタンを表示します。
 *
 * @param props 日報データ、同期状態フラグ、イベントハンドラ
 */
export const LogCard: React.FC<Props> = ({ log, isSyncing, onOpenDetail, onSyncCalendar }) => {
  return (
    <Paper
      elevation={0}
      onClick={() => onOpenDetail(log.logDate)}
      sx={{
        p: { xs: 2.5, sm: 3 },
        borderRadius: '12px',
        border: '1px solid #e2e8f0',
        bgcolor: '#ffffff',
        cursor: 'pointer',
        transition: 'all 0.2s ease-in-out',
        '&:hover': {
          transform: 'translateY(-2px)',
          boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
          borderColor: '#cbd5e1',
        },
        display: 'flex',
        flexDirection: { xs: 'column', sm: 'row' },
        alignItems: { xs: 'stretch', sm: 'center' },
        gap: { xs: 2, sm: 3 },
      }}
    >
      {/* 左側：日程情報と感情（モバイル時は横並びに） */}
      <Box sx={{ 
        minWidth: { xs: 'auto', sm: '100px' }, 
        display: 'flex', 
        flexDirection: { xs: 'row', sm: 'column' }, 
        justifyContent: 'space-between',
        alignItems: { xs: 'center', sm: 'flex-start' },
        gap: 0.5 
      }}>
        <Box sx={{ display: 'flex', flexDirection: { xs: 'row', sm: 'column' }, alignItems: 'baseline', gap: { xs: 1, sm: 0 } }}>
          <Typography variant="body1" sx={{ fontWeight: 800, color: '#1e293b' }}>
            {dayjs(log.logDate).format('M/D')}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {dayjs(log.logDate).format('YYYY年')}
          </Typography>
        </Box>
        <Box sx={{ mt: { xs: 0, sm: 1 } }}>
          <Chip
            icon={getSentimentIcon(log.sentiment, 14)}
            label={getSentimentText(log.sentiment).split(' ')[0]}
            size="small"
            sx={{
              fontWeight: 700,
              fontSize: '11px',
              height: 20,
              borderColor: '#e2e8f0',
              bgcolor: '#f8fafc',
            }}
            variant="outlined"
          />
        </Box>
      </Box>

      {/* 中央：稼働時間・プレビュー */}
      <Box sx={{ flexGrow: 1, minWidth: 0 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1, flexWrap: 'wrap' }}>
          {log.holiday ? (
            <Chip
              label="休日・休暇"
              size="small"
              color="error"
              variant="outlined"
              sx={{ fontWeight: 700, height: 20, fontSize: '11px' }}
            />
          ) : (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color: '#475569' }}>
              <Clock size={14} />
              <Typography variant="body2" sx={{ fontWeight: 700 }}>
                {log.workHours}h
              </Typography>
              {log.overtimeHours > 0 && (
                <Typography variant="caption" sx={{ color: '#f59e0b', fontWeight: 700, ml: 0.5 }}>
                  (時間外: {log.overtimeHours}h)
                </Typography>
              )}
            </Box>
          )}
        </Box>
        {/* 作業内容のプレビュー */}
        <Typography
          variant="body2"
          color="text.secondary"
          sx={{
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            lineHeight: 1.5,
            minHeight: '3em',
          }}
        >
          {log.tasks || (log.holiday ? '（本日は休日・休暇です）' : '業務内容の抽出なし')}
        </Typography>
      </Box>

      {/* 右側：再同期ボタン＋矢印アイコン（モバイル時は下部に右寄せで配置） */}
      <Box
        sx={{ 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: { xs: 'flex-end', sm: 'flex-start' },
          gap: 1, 
          flexShrink: 0,
          mt: { xs: 1, sm: 0 },
          pt: { xs: 1.5, sm: 0 },
          borderTop: { xs: '1px solid #f1f5f9', sm: 'none' }
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <Button
          size="small"
          variant="outlined"
          color="primary"
          startIcon={isSyncing ? <CircularProgress size={12} /> : <RefreshCw size={12} />}
          disabled={isSyncing}
          onClick={(e) => onSyncCalendar(e, log.logDate)}
          sx={{
            textTransform: 'none',
            fontSize: '11px',
            fontWeight: 700,
            height: 28,
            px: 1.5,
            borderRadius: '6px',
            whiteSpace: 'nowrap',
          }}
        >
          同期
        </Button>
        <ChevronRight size={20} color="#94a3b8" />
      </Box>
    </Paper>

  );
};
