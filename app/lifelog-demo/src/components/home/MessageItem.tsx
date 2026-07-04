import React from 'react';
import { Box, Avatar, Typography, Chip, Button } from '@mui/material';
import type { Message, LogDetailResponse } from '../../types';
import { AnalysisResultPanel } from './AnalysisResultPanel';

interface MessageItemProps {
  message: Message;
  onConfirm: (msgId: string, result: LogDetailResponse) => void;
  onCancel: (msgId: string) => void;
}

export const MessageItem: React.FC<MessageItemProps> = ({ message, onConfirm, onCancel }) => {
  const isBot = message.type === 'bot';
  const avatarColor = isBot ? (message.isError ? '#ef4444' : '#2bac76') : '#1976d2';
  const senderName = isBot ? 'LifeLog Bot' : 'ユーザー';

  return (
    <Box sx={{ display: 'flex', gap: 2 }}>
      <Avatar sx={{ bgcolor: avatarColor, fontWeight: 700, fontSize: '14px', width: 36, height: 36, flexShrink: 0 }}>
        {senderName.charAt(0)}
      </Avatar>
      
      <Box sx={{ flexGrow: 1 }}>
        <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1, mb: 0.5 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#1d1c1d' }}>
            {senderName}
          </Typography>
          {isBot && (
            <Chip 
              label="APP" 
              size="small" 
              sx={{ height: 16, fontSize: '9px', fontWeight: 800, bgcolor: '#f1f1f1', color: '#616061', borderRadius: '3px' }} 
            />
          )}
          <Typography variant="caption" sx={{ color: '#616061' }}>
            {message.timestamp}
          </Typography>
        </Box>
        
        {/* メッセージテキスト */}
        <Typography variant="body2" sx={{ color: '#1d1c1d', whiteSpace: 'pre-wrap', lineHeight: 1.6, mb: message.result ? 2 : 0 }}>
          {message.text}
        </Typography>

        {/* 成功時のAI解析結果ブロック */}
        {message.result && (
          <AnalysisResultPanel result={message.result} isPreview={false} />
        )}

        {/* 確定待ち状態のプレビュー (アクションボタン付き) */}
        {message.pendingResult && message.actionClicked === null && (
          <Box sx={{ mt: 1 }}>
            <AnalysisResultPanel result={message.pendingResult} isPreview={true} />

            {/* インタラクティブボタン */}
            <Box sx={{ display: 'flex', gap: 1.5, mt: 1 }}>
              <Button 
                variant="contained" 
                size="small"
                onClick={() => onConfirm(message.id, message.pendingResult!)}
                sx={{ 
                  backgroundColor: '#007a5a', 
                  color: '#ffffff', 
                  fontWeight: 700,
                  textTransform: 'none',
                  boxShadow: 'none',
                  '&:hover': {
                    backgroundColor: '#148567',
                    boxShadow: 'none'
                  }
                }}
              >
                確定する
              </Button>
              <Button 
                variant="outlined" 
                size="small"
                onClick={() => onCancel(message.id)}
                sx={{ 
                  borderColor: '#e01e5a', 
                  color: '#e01e5a', 
                  fontWeight: 700,
                  textTransform: 'none',
                  '&:hover': {
                    borderColor: '#c61b4f',
                    backgroundColor: 'rgba(224, 30, 90, 0.04)'
                  }
                }}
              >
                キャンセルする
              </Button>
            </Box>
          </Box>
        )}
      </Box>
    </Box>
  );
};
