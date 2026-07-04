import React, { createContext, useContext, useState, useEffect } from 'react';
import { userApi } from '../api/userApi';
import type { UserSettingResponse } from '../types';

/**
 * デモコンテキストの状態と操作を表す型定義。
 */
interface DemoContextType {
  /** 現在のログインユーザー情報（またはデモユーザー設定）。 */
  user: UserSettingResponse | null;
  /** ローディング状態フラグ。 */
  loading: boolean;
  /** エラーメッセージ文字列（バックエンド接続エラー等の警告用）。 */
  error: string | null;
  /** ユーザー情報を再取得する関数。 */
  refreshUser: () => Promise<void>;
  /** グローバルのローディング状態を手動で切り替える関数。 */
  setGlobalLoading: (loading: boolean) => void;
}

/**
 * デモ状態管理用コンテキスト。
 */
const DemoContext = createContext<DemoContextType | undefined>(undefined);

/**
 * アプリケーション全体にデモユーザー状態と API 疎通状態を提供するプロバイダーコンポーネント。
 *
 * @param props - 子要素コンポーネント
 * @returns React コンテキストプロバイダー要素
 */
export const DemoProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserSettingResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchUser = async () => {
    try {
      setError(null);
      const data = await userApi.getMe();
      setUser(data);
    } catch (err: any) {
      console.error('Failed to connect to backend:', err);
      setError('バックエンドのAPIサーバーとの接続に失敗しました。Quarkusアプリケーションが起動しているか確認してください。');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUser();
  }, []);

  const refreshUser = async () => {
    setLoading(true);
    await fetchUser();
  };

  const setGlobalLoading = (val: boolean) => {
    setLoading(val);
  };

  return (
    <DemoContext.Provider value={{ user, loading, error, refreshUser, setGlobalLoading }}>
      {children}
    </DemoContext.Provider>
  );
};

/**
 * デモコンテキスト値を取得するためのカスタムフック。
 * `DemoProvider` の配下で使用される必要があります。
 *
 * @returns デモコンテキストの提供値
 * @throws `DemoProvider` の配下で使用されていない場合は例外をスローします。
 */
export const useDemo = () => {
  const context = useContext(DemoContext);
  if (!context) {
    throw new Error('useDemo must be used within a DemoProvider');
  }
  return context;
};
