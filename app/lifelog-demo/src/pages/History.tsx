import React, { useState, useEffect } from 'react';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import { Box, Typography, Card, CardContent, Button, Chip, CircularProgress } from '@mui/material';
import { ListTodo, Eye, AlertCircle } from 'lucide-react';
import { logApi } from '../api/logApi';
import type { LogListResponse, LogDetailResponse } from '../types';
import { getSentimentIcon } from '../utils';
import dayjs from 'dayjs';
import { LogDetailDialog } from '../components/LogDetailDialog';
import { useSnackbar } from 'notistack';

/**
 * 利用者の日報（ライフログ）履歴一覧を表示するページコンポーネント。
 * DataGrid テーブルを用いて日付、平日/休日、稼働時間、感情などを一覧表示し、
 * 各行の「詳細」ボタンから詳細モーダルダイアログを展開できます。
 */
export const History: React.FC = () => {
  const { enqueueSnackbar } = useSnackbar();
  const [logs, setLogs] = useState<LogListResponse.Log[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedLog, setSelectedLog] = useState<LogDetailResponse | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [loadingDetail, setLoadingDetail] = useState(false);

  const fetchLogs = async () => {
    try {
      setLoading(true);
      // 今月の開始日と終了日を特定
      const from = dayjs().startOf('month').format('YYYY-MM-DD');
      const to = dayjs().endOf('month').format('YYYY-MM-DD');
      const response = await logApi.getLogs(from, to);
      const data = response.logs;
      
      // DataGrid 用に id (日付) を持たせてセット
      const dataWithIds = data.map((log, index) => ({
        ...log,
        id: log.logDate || `index-${index}`,
      }));
      // 日付順に降順ソートして表示
      dataWithIds.sort((a, b) => dayjs(b.logDate).diff(dayjs(a.logDate)));
      
      setLogs(dataWithIds);
    } catch (err: any) {
      console.error('Failed to fetch logs:', err);
      const errMsg = err.response?.data?.error || err.response?.data?.message || '日報履歴の取得に失敗しました。';
      enqueueSnackbar(errMsg, { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  const handleOpenDetail = async (logDate: string) => {
    setLoadingDetail(true);
    setSelectedLog(null);
    setDetailOpen(true);
    try {
      const response = await logApi.getLogDetails(logDate);
      setSelectedLog(response);
    } catch (err: any) {
      console.error('Failed to fetch log details:', err);
      const errMsg = err.response?.data?.error || err.response?.data?.message || '詳細情報の取得に失敗しました。';
      enqueueSnackbar(errMsg, { variant: 'error' });
      setDetailOpen(false);
    } finally {
      setLoadingDetail(false);
    }
  };

  // DataGrid カラム定義
  const columns: GridColDef[] = [
    {
      field: 'logDate',
      headerName: '日付',
      width: 120,
      renderCell: (params) => (
        <Typography variant="body2" sx={{ fontWeight: 700 }}>
          {params.value}
        </Typography>
      ),
    },
    {
      field: 'holiday',
      headerName: '区分',
      width: 100,
      renderCell: (params) => {
        const isHolidayWork = params.value && (params.row.workHours > 0);
        return (
          <Chip
            label={isHolidayWork ? '💼 休日出勤' : (params.value ? '🌴 休日' : '💻 平日')}
            size="small"
            color={isHolidayWork ? 'secondary' : (params.value ? 'warning' : 'default')}
            sx={{ fontWeight: 600, fontSize: '11px', height: '22px' }}
          />
        );
      },
    },
    {
      field: 'workHours',
      headerName: '稼働時間',
      width: 100,
      valueGetter: (value, row) => {
        if (row.holiday) {
          return row.workHours > 0 ? `${row.workHours}h` : '0.0h';
        }
        return `${value || 0.0}h`;
      },
    },
    {
      field: 'tasks',
      headerName: '作業（日報）',
      flex: 1,
      minWidth: 200,
      renderCell: (params) => (
        <Typography
          variant="body2"
          sx={{
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            color: '#4a5568',
            maxWidth: '100%',
          }}
        >
          {params.value || (params.row.holiday ? '（本日は休日・休暇です）' : '業務内容の抽出なし')}
        </Typography>
      ),
    },
    {
      field: 'diary',
      headerName: 'プライベート（日記）',
      flex: 1,
      minWidth: 200,
      renderCell: (params) => (
        <Typography
          variant="body2"
          sx={{
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            color: '#4a5568',
            maxWidth: '100%',
          }}
        >
          {params.value || '日記内容の抽出なし'}
        </Typography>
      ),
    },
    {
      field: 'sentiment',
      headerName: '感情',
      width: 120,
      renderCell: (params) => (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, height: '100%' }}>
          {getSentimentIcon(params.value, 18)}
          <Typography variant="body2" sx={{ fontSize: '12px', fontWeight: 600 }}>
            {params.value}
          </Typography>
        </Box>
      ),
    },
    {
      field: 'actions',
      headerName: '操作',
      width: 100,
      sortable: false,
      renderCell: (params) => (
        <Button
          size="small"
          variant="outlined"
          onClick={() => handleOpenDetail(params.row.logDate)}
          startIcon={<Eye size={12} />}
          sx={{
            py: 0.2,
            fontSize: '11px',
            textTransform: 'none',
            borderRadius: '6px',
          }}
        >
          詳細
        </Button>
      ),
    },
  ];

  return (
    <Box sx={{ py: 2 }}>
      {/* ページタイトル */}
      <Box sx={{ mb: 4, display: 'flex', alignItems: 'center', gap: 1.5 }}>
        <ListTodo size={28} color="#1976d2" />
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 800 }}>
            履歴（最近の活動）
          </Typography>
          <Typography variant="body2" sx={{ color: '#718096' }}>
            登録された日報・日記の全履歴一覧です。
          </Typography>
        </Box>
      </Box>

      {/* 履歴テーブルカード */}
      <Card sx={{ backgroundColor: '#ffffff' }}>
        <CardContent sx={{ p: 3 }}>
          {loading ? (
            <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 8, gap: 2 }}>
              <CircularProgress size={40} />
              <Typography variant="body2" sx={{ color: '#718096' }}>履歴データを取得中...</Typography>
            </Box>
          ) : (
            <Box sx={{ height: 500, width: '100%' }}>
              <DataGrid
                rows={logs}
                columns={columns}
                initialState={{
                  pagination: {
                    paginationModel: { pageSize: 10, page: 0 },
                  },
                }}
                pageSizeOptions={[10, 20]}
                disableRowSelectionOnClick
                sx={{
                  border: 'none',
                  fontFamily: 'Inter, sans-serif',
                  '& .MuiDataGrid-columnHeaders': {
                    backgroundColor: '#f8fafc',
                    borderBottom: '1px solid #edf2f7',
                    fontWeight: 700,
                  },
                  '& .MuiDataGrid-row': {
                    borderBottom: '1px solid #edf2f7',
                    '&:hover': {
                      backgroundColor: '#f8fafc',
                    },
                  },
                  '& .MuiDataGrid-cell': {
                    display: 'flex',
                    alignItems: 'center',
                  },
                  '& .MuiDataGrid-footerContainer': {
                    borderTop: '1px solid #edf2f7',
                  },
                }}
              />
            </Box>
          )}

          {/* 使用API情報 */}
          <Box sx={{ mt: 3, bgcolor: '#ebf8ff', p: 2, borderRadius: '8px', border: '1px solid #bee3f8' }}>
            <Typography variant="caption" sx={{ color: '#2b6cb0', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 0.5, mb: 1 }}>
              <AlertCircle size={14} /> デモ画面で使用されたAPI
            </Typography>
            <Typography variant="caption" sx={{ color: '#2d3748', display: 'block', mb: 0.5 }}>
              • <strong>BE-API106 (日報一覧取得)</strong>: 指定した日付範囲 (当月) の全日報ログ一覧を取得
            </Typography>
            <Typography variant="caption" sx={{ color: '#2d3748', display: 'block' }}>
              • <strong>BE-API107 (日報詳細取得)</strong>: 指定した日付の日報詳細情報を取得
            </Typography>
          </Box>
        </CardContent>
      </Card>

      <LogDetailDialog
        open={detailOpen}
        onClose={() => setDetailOpen(false)}
        log={selectedLog}
        loading={loadingDetail}
        title="日報詳細"
      />
    </Box>
  );
};
