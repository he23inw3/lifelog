import { client } from './axios';
import type { MyDashboardResponse } from '../types';

/**
 * ダッシュボード統計データ関連の API 通信を行うオブジェクト。
 */
export const dashboardApi = {
  /**
   * ログイン中のユーザーのマイダッシュボード統計情報（稼働時間、登録数等）を取得します。
   *
   * @returns ダッシュボード統計データのレスポンス Promise
   */
  getDashboardStats: async (): Promise<MyDashboardResponse> => {
    const response = await client.get<MyDashboardResponse>('/api/v1/users/me/dashboard');
    return response.data;
  },
};
