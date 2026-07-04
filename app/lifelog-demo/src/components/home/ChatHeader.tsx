import React from 'react';
import { Box, Avatar, Typography, IconButton } from '@mui/material';
import { PanelLeftOpen } from 'lucide-react';

interface ChatHeaderProps {
  sidebarOpen: boolean;
  onOpenSidebar: () => void;
}

export const ChatHeader: React.FC<ChatHeaderProps> = ({ sidebarOpen, onOpenSidebar }) => {
  return (
    <Box sx={{ px: 3, py: 2, borderBottom: '1px solid #e2e8f0', display: 'flex', alignItems: 'center', gap: 1.5, bgcolor: '#ffffff' }}>
      {!sidebarOpen && (
        <IconButton size="small" onClick={onOpenSidebar} sx={{ color: '#616061', mr: 1 }}>
          <PanelLeftOpen size={20} />
        </IconButton>
      )}
      <Avatar sx={{ bgcolor: '#2bac76', width: 24, height: 24, fontSize: '11px', fontWeight: 800 }}>L</Avatar>
      <Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#1d1c1d', lineHeight: 1.2 }}>
            LifeLog Bot
          </Typography>
          <Box sx={{ width: 8, height: 8, bgcolor: '#2bac76', borderRadius: '50%' }} />
          <Typography variant="caption" sx={{ color: '#616061', fontWeight: 500 }}>(ダイレクトメッセージ)</Typography>
        </Box>
        <Typography variant="caption" sx={{ color: '#616061' }}>
          Botとの個人チャットです。当月内の任意の日付の日報を、他人の目を気にせず登録・更新できます。
        </Typography>
      </Box>
    </Box>
  );
};
