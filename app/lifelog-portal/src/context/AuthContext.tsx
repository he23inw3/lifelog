import React, { createContext, useContext, useState, useEffect } from 'react';

/**
 * ログイン中のユーザー情報を表すインターフェース。
 */
interface User {
  /** ユーザーのメールアドレス。 */
  email: string;
}

/**
 * 認証コンテキストが提供するデータの型定義。
 */
interface AuthContextType {
  /** 現在ログインしているユーザーオブジェクト。未ログイン時は null。 */
  user: User | null;
  /** ユーザーが認証済み（ログイン済み）であるかどうかのフラグ。 */
  isAuthenticated: boolean;
  /** 認証状態の初期読み込み中であるかどうかのフラグ。 */
  isLoading: boolean;
  /**
   * 指定したメールアドレスと OIDC トークンでサインインを実行します。
   *
   * @param email - サインインするメールアドレス
   * @param token - 使用する OIDC ID トークン
   */
  login: (email: string, token: string) => void;
  /**
   * 現在のセッションを破棄し、ログアウトを実行します。
   */
  logout: () => void;
}

/**
 * 認証状態を共有するための React コンテキスト。
 */
const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * 認証プロバイダーコンポーネント。
 * ローカルストレージを使用した認証状態を管理し、配下の子コンポーネントへ提供します。
 *
 * @param props - 子コンポーネントを含むプロパティ
 * @returns レンダリングされた AuthProvider コンポーネント
 */
export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const email = localStorage.getItem('user_email');
    const token = localStorage.getItem('auth_token');
    if (email && token) {
      setUser({ email });
    }
    setIsLoading(false);
  }, []);

  const login = (email: string, token: string) => {
    localStorage.setItem('user_email', email);
    localStorage.setItem('auth_token', token);
    setUser({ email });
  };

  const logout = () => {
    localStorage.removeItem('user_email');
    localStorage.removeItem('auth_token');
    setUser(null);
  };

  const isAuthenticated = !!user;

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * 認証コンテキストを簡単に利用するためのカスタムフック。
 * `AuthProvider` の配下で呼び出す必要があります。
 *
 * @returns 認証コンテキストデータ
 * @throws `useAuth` が `AuthProvider` の外で呼び出された場合にエラーをスローします。
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
