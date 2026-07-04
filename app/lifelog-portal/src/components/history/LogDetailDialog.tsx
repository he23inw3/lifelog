import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Chip,
  Box,
  Typography,
  Divider,
  CircularProgress,
} from '@mui/material';
import {
  Calendar as CalendarIcon,
  Clock,
  Briefcase,
  BookOpen,
  FileText,
  Sparkles,
} from 'lucide-react';
import type { LogDetailResponse } from '../../types';
import { getSentimentIcon, getSentimentText } from '../../utils';

/**
 * LogDetailDialog コンポーネント用のプロパティ定義。
 */
interface LogDetailDialogProps {
  /** ダイアログを表示するかどうかを表すフラグ。 */
  open: boolean;
  /** ダイアログを閉じる処理を行うコールバック関数。 */
  onClose: () => void;
  /** 表示対象の日報データ。 */
  log: LogDetailResponse | null;
  /** 詳細データの読み込み中フラグ。 */
  loading?: boolean;
  /** ダイアログ下部に関連する API 情報を表示するかどうか。 */
  showApiInfo?: boolean;
  /** ダイアログのタイトル（省略時は '日報詳細'）。 */
  title?: string;
}

/**
 * 特定の日付の日報ログ（作業実績、日記、感情、原文など）を綺麗に配置して表示する共通モーダルコンポーネント。
 */
export const LogDetailDialog: React.FC<LogDetailDialogProps> = ({
  open,
  onClose,
  log,
  loading = false,
  showApiInfo = false,
  title = '日報詳細',
}) => {
  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm" sx={{ '& .MuiPaper-root': { borderRadius: '12px' } }}>
      <DialogTitle
        sx={{
          fontWeight: 800,
          pb: 1.5,
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          borderBottom: '1px solid #f1f5f9',
        }}
      >
        <span>{title}</span>
        {log && (
          <Chip
            icon={getSentimentIcon(log.sentiment, 16)}
            label={getSentimentText(log.sentiment)}
            variant="outlined"
            sx={{ fontWeight: 700, borderColor: '#cbd5e1', bgcolor: '#f8fafc' }}
          />
        )}
      </DialogTitle>

      <DialogContent sx={{ py: 3 }}>
        {loading ? (
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 4, gap: 2 }}>
            <CircularProgress size={40} />
            <Typography variant="body2" sx={{ color: '#64748b' }}>
              日報を読み込み中...
            </Typography>
          </Box>
        ) : log ? (
          <Box>
            {/* 日程および時間情報 */}
            <Box sx={{ display: 'flex', gap: 4, mb: 3, flexWrap: 'wrap' }}>
              <Box sx={{ flex: 1, minWidth: '120px' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#64748b', mb: 0.5 }}>
                  <CalendarIcon size={16} />
                  <Typography variant="caption" sx={{ fontWeight: 700 }}>
                    日付
                  </Typography>
                </Box>
                <Typography variant="body1" sx={{ fontWeight: 800, ml: 3, color: '#0f172a' }}>
                  {log.logDate}
                </Typography>
              </Box>
              <Box sx={{ flex: 1, minWidth: '120px' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#64748b', mb: 0.5 }}>
                  <Clock size={16} />
                  <Typography variant="caption" sx={{ fontWeight: 700 }}>
                    総稼働時間
                  </Typography>
                </Box>
                <Typography variant="body1" sx={{ fontWeight: 800, ml: 3, color: '#0f172a' }}>
                  {log.holiday ? '0.0h (休日/休暇)' : `${log.workHours}h`}
                </Typography>
              </Box>
            </Box>

            <Divider sx={{ my: 2, borderColor: '#e2e8f0' }} />

            {/* 作業実績の表示 */}
            <Box sx={{ mb: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#475569', mb: 1 }}>
                <Briefcase size={16} />
                <Typography variant="subtitle2" sx={{ fontWeight: 800 }}>
                  作業実績 (日報)
                </Typography>
              </Box>
              <Typography
                variant="body2"
                sx={{
                  color: '#334155',
                  backgroundColor: '#f8fafc',
                  p: 2,
                  borderRadius: '8px',
                  border: '1px solid #e2e8f0',
                  whiteSpace: 'pre-wrap',
                  lineHeight: 1.6,
                }}
              >
                {log.tasks || (log.holiday ? '（本日は休日・休暇です）' : '業務内容の抽出なし')}
              </Typography>
            </Box>

            {/* プライベート（日記）の表示 */}
            <Box sx={{ mb: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#475569', mb: 1 }}>
                <BookOpen size={16} />
                <Typography variant="subtitle2" sx={{ fontWeight: 800 }}>
                  プライベート (日記)
                </Typography>
              </Box>
              <Typography
                variant="body2"
                sx={{
                  color: '#334155',
                  backgroundColor: '#f8fafc',
                  p: 2,
                  borderRadius: '8px',
                  border: '1px solid #e2e8f0',
                  whiteSpace: 'pre-wrap',
                  lineHeight: 1.6,
                }}
              >
                {log.diary || '日記内容の抽出なし'}
              </Typography>
            </Box>

            {/* 入力された原文の表示 */}
            <Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#475569', mb: 1 }}>
                <FileText size={16} />
                <Typography variant="subtitle2" sx={{ fontWeight: 800 }}>
                  入力された原文
                </Typography>
              </Box>
              <Typography
                variant="body2"
                sx={{
                  color: '#64748b',
                  fontStyle: 'italic',
                  backgroundColor: '#f8fafc',
                  p: 2,
                  borderRadius: '8px',
                  border: '1px solid #e2e8f0',
                  whiteSpace: 'pre-wrap',
                  lineHeight: 1.6,
                }}
              >
                "{log.rawText}"
              </Typography>
            </Box>
          </Box>
        ) : (
          <Box sx={{ py: 4, textAlign: 'center' }}>
            <Typography variant="body1" sx={{ color: '#475569', fontWeight: 700, mb: 1 }}>
              この日のログはありません
            </Typography>
            <Typography variant="body2" sx={{ color: '#64748b' }}>
              Slack から日報を入力すると、こちらに反映されます。
            </Typography>
          </Box>
        )}
      </DialogContent>

      <DialogActions sx={{ p: 2, borderTop: '1px solid #f1f5f9' }}>
        {showApiInfo && log && (
          <Box sx={{ flexGrow: 1, pl: 1, display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <Sparkles size={14} color="#64748b" />
            <Typography variant="caption" sx={{ color: '#64748b', fontWeight: 600 }}>
              使用API: BE-API107
            </Typography>
          </Box>
        )}
        <Button onClick={onClose} variant="contained" color="primary" sx={{ borderRadius: '8px', textTransform: 'none', fontWeight: 700 }}>
          閉じる
        </Button>
      </DialogActions>
    </Dialog>
  );
};
