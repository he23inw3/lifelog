import React, { useEffect, useState } from 'react';
import { Box, Typography, CircularProgress, Stack } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { userApi } from '../api/userApi';
import type { LogListResponse, LogDetailResponse } from '../types';
import { PortalHeader } from '../components/PortalHeader';
import { LogDetailDialog } from '../components/history/LogDetailDialog';
import { LogSearchFilter } from '../components/history/LogSearchFilter';
import { LogCard } from '../components/history/LogCard';
import { LogEmptyState } from '../components/history/LogEmptyState';
import { useSnackbar } from 'notistack';
import dayjs from 'dayjs';

/**
 * ログインユーザーの過去の日報履歴を一覧表示・検索できる履歴ページコンポーネント。
 * 日付範囲でのフィルタリング機能を提供し、日報をクリックすると詳細ダイアログを表示します。
 *
 * 各 UI 要素は以下のコンポーネントに分割されています：
 * - {@link LogSearchFilter} — 検索フォーム（開始日・終了日・検索ボタン）
 * - {@link LogCard} — 1件分の日報カード
 * - {@link LogEmptyState} — 0件時の空状態表示
 */
export const History: React.FC = () => {
  const { user, logout } = useAuth();
  const { enqueueSnackbar } = useSnackbar();

  // 検索用の日付範囲（初期値：今月1日〜今日）
  const [fromDate, setFromDate] = useState(() => dayjs().startOf('month').format('YYYY-MM-DD'));
  const [toDate, setToDate] = useState(() => dayjs().format('YYYY-MM-DD'));

  const [logs, setLogs] = useState<LogListResponse.Log[]>([]);
  const [loading, setLoading] = useState(true);

  // 詳細ダイアログ状態
  const [selectedLogDate, setSelectedLogDate] = useState<string | null>(null);
  const [detailLog, setDetailLog] = useState<LogDetailResponse | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [syncingDate, setSyncingDate] = useState<string | null>(null);

  /**
   * Googleカレンダー再同期
   * @param e マウスイベント
   * @param logDate 対象日付(YYYY-MM-DD)
   */
  const handleSyncCalendar = async (e: React.MouseEvent, logDate: string) => {
    e.stopPropagation();
    setSyncingDate(logDate);
    try {
      await userApi.syncCalendar(logDate);
      enqueueSnackbar(`${logDate} の日報をカレンダーに同期しました。`, { variant: 'success' });
    } catch (err: any) {
      console.error('Failed to sync calendar', err);
      const errMsg = err.response?.data?.error || 'カレンダーへの同期に失敗しました。Google連携の設定をご確認ください。';
      enqueueSnackbar(errMsg, { variant: 'error' });
    } finally {
      setSyncingDate(null);
    }
  };

  /**
   * 日報履歴を取得
   * @param silent ローディング表示を抑制する場合は true
   */
  const fetchLogs = async (silent = false) => {
    try {
      if (!silent) setLoading(true);
      const response = await userApi.getLogs(fromDate, toDate);
      const data = response.logs;

      // 日付の降順（新しい順）にソート
      const sorted = [...data].sort((a, b) => b.logDate.localeCompare(a.logDate));
      setLogs(sorted);
    } catch (err: any) {
      console.error('Failed to fetch logs', err);
      const errMsg = err.response?.data?.error || err.response?.data?.message || '日報履歴の取得に失敗しました。';
      enqueueSnackbar(errMsg, { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  /**
   * 検索ボタン押下
   * @param e フォームイベント
   */
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();

    // クライアントサイドでの日付バリデーション
    const start = dayjs(fromDate);
    const end = dayjs(toDate);

    if (start.isAfter(end)) {
      enqueueSnackbar('開始日は終了日以前の日付を指定してください。', { variant: 'error' });
      return;
    }

    if (end.diff(start, 'day') > 31) {
      enqueueSnackbar('日報の検索範囲は最大で31日間までです。', { variant: 'error' });
      return;
    }

    fetchLogs();
  };

  /**
   * 日報詳細ダイアログ表示
   * @param logDate 対象日付(YYYY-MM-DD)
   */
  const handleOpenDetail = async (logDate: string) => {
    setSelectedLogDate(logDate);
    setDetailOpen(true);
    setLoadingDetail(true);
    setDetailLog(null);
    try {
      const response = await userApi.getLogDetail(logDate);
      setDetailLog(response);
    } catch (err) {
      console.error('Failed to fetch log detail', err);
      enqueueSnackbar('日報詳細の取得に失敗しました。', { variant: 'error' });
      setDetailOpen(false);
    } finally {
      setLoadingDetail(false);
    }
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

        {/* 検索・フィルターパネル */}
        <LogSearchFilter
          fromDate={fromDate}
          toDate={toDate}
          onFromChange={setFromDate}
          onToChange={setToDate}
          onSubmit={handleSearch}
        />

        {/* 履歴リスト */}
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
            <CircularProgress color="primary" />
          </Box>
        ) : logs.length === 0 ? (
          <LogEmptyState />
        ) : (
          <Stack spacing={2}>
            <Typography variant="subtitle2" color="text.secondary" sx={{ fontWeight: 700, px: 1 }}>
              検索結果: {logs.length} 件
            </Typography>

            {logs.map((log) => (
              <LogCard
                key={log.logDate}
                log={log}
                isSyncing={syncingDate === log.logDate}
                onOpenDetail={handleOpenDetail}
                onSyncCalendar={handleSyncCalendar}
              />
            ))}
          </Stack>
        )}

        {/* 詳細表示モーダルダイアログ */}
        <LogDetailDialog
          open={detailOpen}
          onClose={() => setDetailOpen(false)}
          log={detailLog}
          loading={loadingDetail}
          showApiInfo
          title={selectedLogDate ? `${selectedLogDate} の日報詳細` : ''}
        />
      </Box>
    </Box>
  );
};
