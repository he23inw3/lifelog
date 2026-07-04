import axios from 'axios';
import { API_BASE_URL } from '../constants';

/**
 * バックエンド API 通信用の Axios クライアントインスタンス。
 * 基本設定として `API_BASE_URL` と共通ヘッダー（Content-Type）を適用しています。
 */
export const client = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * リクエスト時に `localStorage` から `auth_token` (認証トークン) を取得し、
 * `Authorization: Bearer <token>` ヘッダーを自動付与するリクエストインターセプター。
 */
client.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
