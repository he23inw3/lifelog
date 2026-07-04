/**
 * バックエンド API 通信のベースとなる URL。
 * 環境変数 VITE_API_BASE_URL から取得するか、フォールバックとしてローカル開発環境のポートを指定します。
 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5000';
