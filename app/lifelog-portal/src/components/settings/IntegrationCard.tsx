import React from 'react';
import { Card, CardContent, Stack, Box, Typography } from '@mui/material';

/**
 * IntegrationCard コンポーネント用のプロパティ定義。
 */
interface IntegrationCardProps {
  /** カード左側に表示されるサービスアイコン。 */
  icon: React.ReactNode;
  /** アイコン背景色 (CSS カラーコードまたはrgba)。 */
  iconBgColor: string;
  /** アイコン自身の描画色 (CSS カラーコード)。 */
  iconColor: string;
  /** 連携サービスのタイトル名（例: "Google Calendar"）。 */
  title: string;
  /** 連携サービスの説明文。 */
  description: string;
  /** カード右側に表示されるアクションエリア（ボタンや連携済みステータス等）。 */
  action: React.ReactNode;
  /** カード下部に追加表示される、連携手順や詳細情報等の任意の子コンポーネント。 */
  children?: React.ReactNode;
}

/**
 * 各種外部連携設定用の汎用カードコンポーネント。
 * 左側にアイコンと説明、右側に接続ボタンまたは接続ステータス、下部に追加の連携詳細エリアを設けます。
 *
 * @param props - コンポーネントのプロパティ
 * @returns レンダリングされた IntegrationCard コンポーネント
 */
export const IntegrationCard: React.FC<IntegrationCardProps> = ({
  icon,
  iconBgColor,
  iconColor,
  title,
  description,
  action,
  children,
}) => {
  return (
    <Card>
      <CardContent sx={{ p: 4 }}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={3}
          sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between' }}
        >
          <Stack direction="row" spacing={3} sx={{ alignItems: 'center' }}>
            <Box
              sx={{
                p: 2,
                borderRadius: 4,
                backgroundColor: iconBgColor,
                color: iconColor,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              {icon}
            </Box>
            <Box>
              <Typography variant="h5" component="h2" sx={{ mb: 0.5, fontWeight: 700 }}>
                {title}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {description}
              </Typography>
            </Box>
          </Stack>

          <Box sx={{ minWidth: 150 }}>
            {action}
          </Box>
        </Stack>

        {children}
      </CardContent>
    </Card>
  );
};
