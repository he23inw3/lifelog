import axios from 'axios';
import { API_BASE_URL } from '../constants';

/**
 * バックエンド API 通信用の Axios インスタンス。
 * 基本設定として `API_BASE_URL` と共通ヘッダー（Content-Type）を適用しています。
 */
export const client = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});
