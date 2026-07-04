import React from 'react';
import { Button as MuiButton } from '@mui/material';
import type { ButtonProps as MuiButtonProps } from '@mui/material';

/**
 * PortalButton コンポーネント用のプロパティ定義。
 * MUI の ButtonProps をそのまま継承します。
 */
export type PortalButtonProps = MuiButtonProps;

/**
 * アプリケーション共通のスタイルを適用したボタンコンポーネント。
 * 角の丸み (borderRadius) や共通デザインを統一します。
 *
 * @param props - MUI ButtonProps を継承したプロパティ
 * @returns レンダリングされた PortalButton コンポーネント
 */
export const PortalButton: React.FC<PortalButtonProps> = ({ sx, children, ...props }) => {
  return (
    <MuiButton
      sx={{
        borderRadius: 3, // 12px
        textTransform: 'none',
        fontWeight: 600,
        ...sx,
      }}
      {...props}
    >
      {children}
    </MuiButton>
  );
};
