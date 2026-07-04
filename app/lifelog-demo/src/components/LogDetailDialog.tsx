import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Grid,
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
import type { LogDetailResponse } from '../types';
import { getSentimentIcon } from '../utils';

/**
 * LogDetailDialog コンポーネントのプロップス定義。
 */
interface LogDetailDialogProps {
  /** ダイアログを表示するかどうかを表すフラグ。 */
  open: boolean;
  /** ダイアログを閉じる処理を行うコールバック関数。 */
  onClose: () => void;
  /** 表示対象の日報データ。データが存在しない場合はメッセージまたはローディング画面が表示されます。 */
  log: LogDetailResponse | null;
  /** 詳細データの読み込み中フラグ（省略時は false として扱われます）。 */
  loading?: boolean;
  /** ダイアログ下部に関連する API 使用情報を表示するかどうかを表すフラグ。 */
  showApiInfo?: boolean;
  /** ダイアログのタイトル（省略時は '日報詳細' になります）。 */
  title?: string;
}

/**
 * 特定の日付の日報ログ（作業実績、日記、感情、原文など）を綺麗に配置して表示する共通モーダルコンポーネント（閲覧専用）。
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
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle
        sx={{
          fontWeight: 800,
          pb: 1,
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <span>{title}</span>
        {log && (
          <Chip
            icon={getSentimentIcon(log.sentiment, 18)}
            label={`感情: ${log.sentiment}`}
            variant="outlined"
            sx={{ fontWeight: 600 }}
          />
        )}
      </DialogTitle>

      <DialogContent dividers sx={{ borderColor: '#edf2f7', py: 3 }}>
        {loading ? (
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 4, gap: 2 }}>
            <CircularProgress size={40} />
            <Typography variant="body2" sx={{ color: '#718096' }}>
              日報を読み込み中...
            </Typography>
          </Box>
        ) : log ? (
          <Box>
            {/* 日程および時間情報 */}
            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid size={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#4a5568' }}>
                  <CalendarIcon size={16} />
                  <Typography variant="caption" sx={{ color: '#718096' }}>
                    日付
                  </Typography>
                </Box>
                <Typography variant="body1" sx={{ fontWeight: 700, ml: 3 }}>
                  {log.logDate}
                </Typography>
              </Grid>
              <Grid size={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#4a5568' }}>
                  <Clock size={16} />
                  <Typography variant="caption" sx={{ color: '#718096' }}>
                    総稼働時間
                  </Typography>
                </Box>
                <Typography variant="body1" sx={{ fontWeight: 700, ml: 3 }}>
                  {log.holiday ? '0.0h (休日/休暇)' : `${log.workHours}h`}
                </Typography>
              </Grid>
            </Grid>

            <Divider sx={{ my: 2, borderColor: '#edf2f7' }} />

            {/* 作業実績の表示 */}
            <Box sx={{ mb: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#4a5568', mb: 1 }}>
                <Briefcase size={16} />
                <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                  作業実績 (日報)
                </Typography>
              </Box>
              <Typography
                variant="body2"
                sx={{
                  color: '#2d3748',
                  backgroundColor: '#f8fafc',
                  p: 2,
                  borderRadius: '8px',
                  border: '1px solid #edf2f7',
                  whiteSpace: 'pre-wrap',
                }}
              >
                {log.tasks || (log.holiday ? '（本日は休日・休暇です）' : '業務内容の抽出なし')}
              </Typography>
            </Box>

            {/* プライベート（日記）の表示 */}
            <Box sx={{ mb: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#4a5568', mb: 1 }}>
                <BookOpen size={16} />
                <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                  プライベート (日記)
                </Typography>
              </Box>
              <Typography
                variant="body2"
                sx={{
                  color: '#2d3748',
                  backgroundColor: '#f8fafc',
                  p: 2,
                  borderRadius: '8px',
                  border: '1px solid #edf2f7',
                  whiteSpace: 'pre-wrap',
                }}
              >
                {log.diary || '日記内容の抽出なし'}
              </Typography>
            </Box>

            {/* 入力された原文の表示 */}
            <Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#4a5568', mb: 1 }}>
                <FileText size={16} />
                <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                  入力された原文
                </Typography>
              </Box>
              <Typography
                variant="body2"
                sx={{
                  color: '#718096',
                  fontStyle: 'italic',
                  backgroundColor: '#f7fafc',
                  p: 2,
                  borderRadius: '8px',
                  border: '1px solid #edf2f7',
                  whiteSpace: 'pre-wrap',
                }}
              >
                "{log.rawText}"
              </Typography>
            </Box>
          </Box>
        ) : (
          <Box sx={{ py: 4, textAlign: 'center' }}>
            <Typography variant="body1" sx={{ color: '#4a5568', fontWeight: 600, mb: 1 }}>
              この日のログはありません
            </Typography>
            <Typography variant="body2" sx={{ color: '#718096' }}>
              「ホーム」画面から日報を入力すると、このカレンダーに反映されます。
            </Typography>
          </Box>
        )}
      </DialogContent>

      <DialogActions sx={{ p: 2 }}>
        {showApiInfo && log && (
          <Box sx={{ flexGrow: 1, pl: 1, display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <Sparkles size={14} color="#718096" />
            <Typography variant="caption" sx={{ color: '#718096' }}>
              使用API: BE-API501, BE-API107
            </Typography>
          </Box>
        )}
        <Button onClick={onClose} variant="outlined">
          閉じる
        </Button>
      </DialogActions>
    </Dialog>
  );
};
