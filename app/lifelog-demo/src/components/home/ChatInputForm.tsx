import React from 'react';
import { Box, InputBase, FormControlLabel, Checkbox, Divider, Typography, Button, CircularProgress } from '@mui/material';
import { Send } from 'lucide-react';

interface ChatInputFormProps {
  inputText: string;
  onChangeInput: (val: string) => void;
  isHoliday: boolean;
  onChangeHoliday: (val: boolean) => void;
  onSubmit: (e?: React.FormEvent) => void;
  loading: boolean;
  promptingInput: boolean;
}

export const ChatInputForm: React.FC<ChatInputFormProps> = ({
  inputText,
  onChangeInput,
  isHoliday,
  onChangeHoliday,
  onSubmit,
  loading,
  promptingInput,
}) => {
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(e);
  };

  return (
    <Box sx={{ p: 3, pt: 0, bgcolor: '#ffffff' }}>
      <form onSubmit={handleSubmit}>
        <Box sx={{ 
          border: '1.5px solid #cbd5e1', 
          borderRadius: '10px', 
          bgcolor: '#ffffff',
          '&:focus-within': {
            borderColor: '#1164A3',
            boxShadow: '0 0 0 1px #1164A3'
          }
        }}>
          {/* 入力欄 */}
          <InputBase
            fullWidth
            multiline
            rows={3}
            placeholder={
              promptingInput 
                ? "追加の情報を入力してください... (例: 稼働時間は8時間、休みです、など)" 
                : "LifeLog Bot へのメッセージ (例: 6月24日は終日設計。8時間稼働。日記: ラーメン食べた)"
            }
            value={inputText}
            onChange={(e) => onChangeInput(e.target.value)}
            onKeyDown={(e) => {
              // Ctrl + Enter または Cmd + Enter で送信
              if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
                e.preventDefault();
                onSubmit();
              }
            }}
            disabled={loading}
            sx={{ px: 2, py: 1.5, fontSize: '15px' }}
          />

          {/* ツールバー (送信・休日オプション) */}
          <Box sx={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center', 
            px: 1.5, 
            py: 1, 
            borderTop: '1px solid #f1f5f9',
            bgcolor: '#f8fafc',
            borderBottomLeftRadius: '8px',
            borderBottomRightRadius: '8px'
          }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <FormControlLabel
                control={
                  <Checkbox 
                    checked={isHoliday} 
                    onChange={(e) => onChangeHoliday(e.target.checked)}
                    size="small" 
                    color="primary"
                    disabled={loading}
                  />
                }
                label={
                  <Typography variant="caption" sx={{ color: '#475569', fontWeight: 700 }}>
                    本日は休日・休暇
                  </Typography>
                }
                sx={{ m: 0 }}
              />
              <Divider orientation="vertical" flexItem sx={{ mx: 1, borderColor: '#cbd5e1' }} />
              <Typography variant="caption" sx={{ color: '#94a3b8' }}>
                [Ctrl+Enter] で送信
              </Typography>
            </Box>

            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <Button
                type="submit"
                disabled={loading || !inputText.trim()}
                sx={{ 
                  minWidth: 0,
                  px: 2.5,
                  py: 0.8,
                  borderRadius: '4px',
                  backgroundColor: '#007a5a',
                  fontWeight: 700,
                  color: '#ffffff',
                  textTransform: 'none',
                  '&:hover': {
                    backgroundColor: '#148567',
                  },
                  '&.Mui-disabled': {
                    backgroundColor: '#f1f5f9',
                    color: '#cbd5e1'
                  }
                }}
                startIcon={loading ? <CircularProgress size={14} color="inherit" /> : <Send size={14} />}
              >
                送信
              </Button>
            </Box>
          </Box>
        </Box>
      </form>
    </Box>
  );
};
