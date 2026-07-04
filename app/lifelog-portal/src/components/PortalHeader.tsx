import React from 'react';
import { Box, Typography, Tabs, Tab } from '@mui/material';
import { LogOut, LayoutDashboard, History, Settings } from 'lucide-react';
import { PortalButton } from './PortalButton';
import { useNavigate, useLocation } from 'react-router';

/**
 * PortalHeader コンポーネント用のプロパティ定義。
 */
interface PortalHeaderProps {
  /** 現在ログインしているユーザーのメールアドレス。 */
  email?: string;
  /** ログアウト処理が呼び出されたときのイベントハンドラ。 */
  onLogout: () => void;
}

/**
 * 画面上部に表示される共通ヘッダーコンポーネント。
 * アプリのタイトル、ログイン中のユーザー情報、ページ間のナビゲーション（タブ）、およびログアウトボタンを提供します。
 *
 * @param props - コンポーネントのプロパティ
 * @returns レンダリングされた PortalHeader コンポーネント
 */
export const PortalHeader: React.FC<PortalHeaderProps> = ({ email, onLogout }) => {
  const navigate = useNavigate();
  const location = useLocation();

  // 現在のパスからアクティブなタブの値を決定する
  const currentPath = location.pathname;
  const activeValue = ['/dashboard', '/history', '/settings'].includes(currentPath)
    ? currentPath
    : '/dashboard';

  const handleTabChange = (_event: React.SyntheticEvent, newValue: string) => {
    navigate(newValue);
  };

  return (
    <Box sx={{ mb: 4, width: '100%' }}>
      {/* 上部エリア：タイトル・ユーザー情報・ログアウト */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <Box sx={{ minWidth: 0, flexGrow: 1 }}>
          <Typography variant="h4" component="h1" sx={{ fontWeight: 800, mb: 0.5, color: '#1e293b', letterSpacing: '-0.5px', fontSize: { xs: '1.75rem', sm: '2.125rem' } }}>
            LifeLog Portal
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ wordBreak: 'break-all' }}>
            ログインアカウント: <span style={{ color: '#4f46e5', fontWeight: 600 }}>{email}</span>
          </Typography>
        </Box>
        <PortalButton
          variant="outlined"
          color="error"
          size="small"
          startIcon={<LogOut size={16} />}
          onClick={onLogout}
          sx={{ borderRadius: '8px', px: 2, flexShrink: 0 }}
        >
          ログアウト
        </PortalButton>
      </Box>

      {/* ナビゲーションタブ（狭い画面では横スクロール可能に） */}
      <Tabs
        value={activeValue}
        onChange={handleTabChange}
        textColor="primary"
        indicatorColor="primary"
        variant="scrollable"
        scrollButtons="auto"
        allowScrollButtonsMobile
        sx={{
          borderBottom: '1px solid #e2e8f0',
          '& .MuiTabs-scroller': {
            overflow: 'auto !important',
          },
          '& .MuiTab-root': {
            textTransform: 'none',
            fontWeight: 700,
            fontSize: '15px',
            minWidth: { xs: 90, sm: 120 },
            py: 1.5,
            color: '#64748b',
            display: 'inline-flex',
            alignItems: 'center',
            '&.Mui-selected': {
              color: '#4f46e5',
            },
          },
          '& .MuiTabs-indicator': {
            backgroundColor: '#4f46e5',
            height: '3px',
            borderRadius: '3px 3px 0 0',
          },
        }}
      >
        <Tab
          icon={<LayoutDashboard size={18} style={{ marginRight: '6px', marginBottom: 0 }} />}
          iconPosition="start"
          label="振り返り"
          value="/dashboard"
        />
        <Tab
          icon={<History size={18} style={{ marginRight: '6px', marginBottom: 0 }} />}
          iconPosition="start"
          label="日報履歴"
          value="/history"
        />
        <Tab
          icon={<Settings size={18} style={{ marginRight: '6px', marginBottom: 0 }} />}
          iconPosition="start"
          label="連携設定"
          value="/settings"
        />
      </Tabs>
    </Box>
  );
};
