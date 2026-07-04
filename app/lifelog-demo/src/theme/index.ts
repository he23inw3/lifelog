import { createTheme } from '@mui/material/styles';

/**
 * デモ画面用の明るく見やすいプレミアム・ライトテーマ定義。
 */
export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2',      // 信頼感のあるブルー
      light: '#42a5f5',
      dark: '#1565c0',
      contrastText: '#fff',
    },
    secondary: {
      main: '#2e7d32',      // 癒しのあるグリーン
      light: '#4caf50',
      dark: '#1b5e20',
      contrastText: '#fff',
    },
    background: {
      default: '#f4f6f9',   // 落ち着いた薄いグレー
      paper: '#ffffff',     // カードやダイアログの白背景
    },
    text: {
      primary: '#1a202c',   // 非常に見やすい濃いグレー
      secondary: '#4a5568', // 補足テキスト用のグレー
    },
  },
  typography: {
    fontFamily: [
      'Inter',
      'Outfit',
      'Roboto',
      '"Helvetica Neue"',
      'Arial',
      'sans-serif',
    ].join(','),
    h5: {
      fontWeight: 700,
      color: '#1a202c',
    },
    h6: {
      fontWeight: 600,
      color: '#1a202c',
    },
    subtitle1: {
      fontWeight: 500,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          textTransform: 'none',
          fontWeight: 600,
          boxShadow: 'none',
          '&:hover': {
            boxShadow: '0 4px 12px rgba(25, 118, 210, 0.15)',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: '0 4px 20px rgba(0, 0, 0, 0.05)',
          border: '1px solid rgba(226, 232, 240, 0.8)',
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: 16,
          boxShadow: '0 10px 30px rgba(0, 0, 0, 0.08)',
        },
      },
    },
  },
});
