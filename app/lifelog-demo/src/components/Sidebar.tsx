import { NavLink } from 'react-router';
import { Box, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Typography, Avatar, Divider } from '@mui/material';
import { Home, Calendar, ListTodo, BrainCircuit, MessageSquare, ShieldAlert } from 'lucide-react';
import { useDemo } from '../context/DemoContext';

/**
 * サイドバーの幅（ピクセル単位）。
 */
const DRAWER_WIDTH = 260;

/**
 * サイドバーメニューの項目一覧の定義。
 */
const menuItems = [
  { text: 'ホーム', path: '/', icon: <Home size={20} /> },
  { text: 'カレンダー', path: '/calendar', icon: <Calendar size={20} /> },
  { text: '履歴（最近の活動）', path: '/history', icon: <ListTodo size={20} /> },
  { text: 'AI振り返り', path: '/reflection', icon: <BrainCircuit size={20} /> },
  { text: 'Slack通知履歴', path: '/slack-feed', icon: <MessageSquare size={20} /> },
];

interface SidebarProps {
  mobileOpen: boolean;
  onClose: () => void;
}

/**
 * アプリケーションのグローバル左側ナビゲーションサイドバーコンポーネント。
 * デモユーザー情報およびバックエンド API 接続エラー警告を表示します。
 *
 * @returns レンダリングされたサイドバー Drawer コンポーネント
 */
export const Sidebar: React.FC<SidebarProps> = ({ mobileOpen, onClose }) => {
  const { user, error } = useDemo();

  const drawerContent = (
    <>
      {/* ロゴエリア */}
      <Box sx={{ p: 3, display: 'flex', alignItems: 'center', gap: 1 }}>
        <Box
          sx={{
            width: 32,
            height: 32,
            borderRadius: '8px',
            backgroundColor: '#1976d2',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontWeight: 'bold',
          }}
        >
          L
        </Box>
        <Typography variant="h6" sx={{ fontWeight: 800, color: '#1a202c', letterSpacing: '0.5px' }}>
          LifeLog <span style={{ fontSize: '12px', fontWeight: 500, color: '#718096' }}>デモ</span>
        </Typography>
      </Box>

      <Divider sx={{ borderColor: '#edf2f7' }} />

      {/* メニューリスト */}
      <List sx={{ px: 2, py: 3, flexGrow: 1 }}>
        {menuItems.map((item) => (
          <ListItem key={item.text} disablePadding sx={{ mb: 1 }}>
            <ListItemButton
              component={NavLink}
              to={item.path}
              onClick={onClose}
              sx={{
                borderRadius: '8px',
                color: '#4a5568',
                '&.active': {
                  backgroundColor: '#ebf8ff',
                  color: '#1976d2',
                  '& .MuiListItemIcon-root': {
                    color: '#1976d2',
                  },
                },
                '&:hover:not(.active)': {
                  backgroundColor: '#f7fafc',
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: 40, color: '#718096' }}>
                {item.icon}
              </ListItemIcon>
              <ListItemText
                primary={<Typography sx={{ fontSize: '14px', fontWeight: 600 }}>{item.text}</Typography>}
              />
            </ListItemButton>
          </ListItem>
        ))}
      </List>

      <Divider sx={{ borderColor: '#edf2f7' }} />

      {/* エラーやバックエンド死活状態警告の簡易表示 */}
      {error && (
        <Box sx={{ m: 2, p: 2, bgcolor: '#fff5f5', borderRadius: '8px', display: 'flex', gap: 1, alignItems: 'flex-start' }}>
          <ShieldAlert size={18} color="#e53e3e" style={{ flexShrink: 0, marginTop: 2 }} />
          <Typography variant="caption" sx={{ color: '#c53030', fontWeight: 500 }}>
            API 接続エラー
          </Typography>
        </Box>
      )}

      {/* ユーザープロフィールエリア */}
      <Box sx={{ p: 2, backgroundColor: '#f7fafc', display: 'flex', alignItems: 'center', gap: 2 }}>
        <Avatar
          sx={{
            bgcolor: '#1976d2',
            color: '#fff',
            fontWeight: 'bold',
            fontSize: '14px',
          }}
        >
          {user?.userName ? user.userName.charAt(0) : 'D'}
        </Avatar>
        <Box sx={{ overflow: 'hidden' }}>
          <Typography variant="body2" sx={{ fontWeight: 700, color: '#2d3748', whiteSpace: 'nowrap', textOverflow: 'ellipsis', overflow: 'hidden' }}>
            {user?.userName || 'デモユーザー'}
          </Typography>
          <Typography variant="caption" sx={{ color: '#718096', display: 'block', whiteSpace: 'nowrap', textOverflow: 'ellipsis', overflow: 'hidden' }}>
            {user?.email || 'demo@example.com'}
          </Typography>
        </Box>
      </Box>
    </>
  );

  return (
    <Box
      component="nav"
      sx={{ width: { sm: DRAWER_WIDTH }, flexShrink: { sm: 0 } }}
    >
      {/* モバイル用一時的ドロワー */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={onClose}
        ModalProps={{
          keepMounted: true, // モバイルでのナビゲーションのパフォーマンス向上
        }}
        sx={{
          display: { xs: 'block', sm: 'none' },
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            boxSizing: 'border-box',
            backgroundColor: '#ffffff',
            borderRight: '1px solid #e2e8f0',
            display: 'flex',
            flexDirection: 'column',
          },
        }}
      >
        {drawerContent}
      </Drawer>

      {/* デスクトップ用恒常的サイドバー */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', sm: 'block' },
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            boxSizing: 'border-box',
            backgroundColor: '#ffffff',
            borderRight: '1px solid #e2e8f0',
            display: 'flex',
            flexDirection: 'column',
          },
        }}
        open
      >
        {drawerContent}
      </Drawer>
    </Box>
  );
};

