import React from 'react';
import { Box, Typography, Tooltip, IconButton, Avatar } from '@mui/material';
import { Hash, PanelLeftClose } from 'lucide-react';

interface SlackSidebarProps {
  open: boolean;
  onClose: () => void;
}

export const SlackSidebar: React.FC<SlackSidebarProps> = ({ open, onClose }) => {
  return (
    <Box sx={{
      width: open ? 260 : 0,
      transition: 'width 0.2s ease',
      bgcolor: '#3F0E40',
      color: '#b29bb3',
      display: 'flex',
      flexDirection: 'column',
      overflow: 'hidden',
      borderRight: '1px solid #522653',
      flexShrink: 0
    }}>
      {/* ワークスペースヘッダー */}
      <Box sx={{ p: 2, borderBottom: '1px solid #522653', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Box sx={{ width: 12, height: 12, bgcolor: '#2bac76', borderRadius: '50%' }} />
          <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#ffffff' }}>
            LifeLog Workspace
          </Typography>
        </Box>
        <Tooltip title="サイドバーを閉じる">
          <IconButton size="small" onClick={onClose} sx={{ color: '#b29bb3', '&:hover': { color: '#ffffff' } }}>
            <PanelLeftClose size={18} />
          </IconButton>
        </Tooltip>
      </Box>

      {/* サイドバーメニューセクション */}
      <Box sx={{ p: 2, flexGrow: 1, overflowY: 'auto' }}>
        <Typography variant="caption" sx={{ fontWeight: 700, color: '#b29bb3', textTransform: 'uppercase', letterSpacing: '0.5px', display: 'block', mb: 1 }}>
          チャンネル
        </Typography>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5, mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 1.5, py: 0.8, borderRadius: '6px', color: '#b29bb3', cursor: 'pointer', '&:hover': { bgcolor: '#350d36' } }}>
            <Hash size={16} />
            <Typography variant="body2" sx={{ fontWeight: 500 }}>general</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 1.5, py: 0.8, borderRadius: '6px', color: '#b29bb3', cursor: 'pointer', '&:hover': { bgcolor: '#350d36' } }}>
            <Hash size={16} />
            <Typography variant="body2" sx={{ fontWeight: 500 }}>random</Typography>
          </Box>
        </Box>

        <Typography variant="caption" sx={{ fontWeight: 700, color: '#b29bb3', textTransform: 'uppercase', letterSpacing: '0.5px', display: 'block', mb: 1 }}>
          ダイレクトメッセージ (DM)
        </Typography>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 1.5, py: 0.8, borderRadius: '6px', bgcolor: '#1164A3', color: '#ffffff', cursor: 'pointer' }}>
            <Avatar sx={{ bgcolor: '#ffffff', color: '#2bac76', width: 20, height: 20, fontSize: '11px', fontWeight: 800 }}>L</Avatar>
            <Typography variant="body2" sx={{ fontWeight: 700 }}>LifeLog Bot</Typography>
            <Box sx={{ width: 8, height: 8, bgcolor: '#2bac76', borderRadius: '50%', ml: 'auto' }} />
          </Box>
        </Box>
      </Box>
    </Box>
  );
};
