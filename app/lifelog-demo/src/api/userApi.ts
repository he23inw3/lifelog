import { client } from './axios';
import type { UserSettingResponse } from '../types';

/**
 * ユーザー情報関連の API 通信を行うオブジェクト。
 */
export const userApi = {
  /**
   * ログイン中のユーザー情報（デモモード時は固定デモユーザー）を取得します。
   *
   * @returns ユーザー設定情報のレスポンス Promise
   */
  getMe: async (): Promise<UserSettingResponse> => {
    const response = await client.get<UserSettingResponse>('/api/v1/users/me');
    return response.data;
  },
};
