import { client } from './axios';
import type { IntegrationStatus, LogListResponse, LogDetailResponse, MyDashboardResponse, UserSettingResponse, UserRegistrationRequest } from '../types';

/**
 * ユーザー情報・連携・日報関連の API 通信を行うオブジェクト。
 */
export const userApi = {
  /**
   * 外部連携状態（Google Calendar, Slack）を取得します。
   *
   * @returns 外部連携状態のレスポンスオブジェクト
   */
  getIntegrations: async (): Promise<IntegrationStatus> => {
    const response = await client.get<IntegrationStatus>('/api/v1/users/me/integrations');
    return response.data;
  },

  /**
   * 提供されたワンタイムトークンを使用して Slack アカウント連携を行います。
   *
   * @param token - Slack 連携コマンドにより生成されたワンタイム認証用トークン
   * @returns 処理完了を表す Promise
   */
  linkSlack: async (token: string): Promise<void> => {
    await client.post('/api/v1/users/me/link-slack', { token });
  },

  /**
   * 条件に合致する自身の日報ログ一覧を取得します。
   *
   * @param from - 取得開始日 (YYYY-MM-DD)
   * @param to - 取得終了日 (YYYY-MM-DD)
   * @returns 日報ログの配列を表す Promise
   */
  getLogs: async (from?: string, to?: string): Promise<LogListResponse> => {
    const params: Record<string, string> = {};
    if (from) params.from = from;
    if (to) params.to = to;
    const response = await client.get<LogListResponse>('/api/v1/users/me/logs', { params });
    return response.data;
  },

  /**
   * 指定日の日報詳細を取得します。
   *
   * @param logDate - 対象日付 (YYYY-MM-DD)
   * @returns 日報詳細を表す Promise
   */
  getLogDetail: async (logDate: string): Promise<LogDetailResponse> => {
    const response = await client.get<LogDetailResponse>(`/api/v1/users/me/logs/${logDate}`);
    return response.data;
  },

  /**
   * 自身の当月統計情報を取得します。
   *
   * @returns マイダッシュボードの統計情報を表す Promise
   */
  getMyDashboard: async (): Promise<MyDashboardResponse> => {
    const response = await client.get<MyDashboardResponse>('/api/v1/users/me/dashboard');
    return response.data;
  },

  /**
   * ログイン中の自身のユーザー基本設定（アカウント情報）を取得します。
   * 登録されていない場合は 404 エラーがスローされます。
   *
   * @returns ユーザー設定情報を表す Promise
   */
  getMe: async (): Promise<UserSettingResponse> => {
    const response = await client.get<UserSettingResponse>('/api/v1/users/me');
    return response.data;
  },

  /**
   * ユーザーアカウント（初期設定情報）の新規登録を行います。
   *
   * @param req - 初回登録リクエストオブジェクト
   * @returns 登録完了後のユーザー設定情報を表す Promise
   */
  registerUser: async (req: UserRegistrationRequest): Promise<UserSettingResponse> => {
    const response = await client.post<UserSettingResponse>('/api/v1/users/register', req);
    return response.data;
  },

  /**
   * 指定日の日報を Google カレンダーに再同期します。
   *
   * @param logDate - 対象日付 (YYYY-MM-DD)
   * @returns 同期完了を表す Promise
   */
  syncCalendar: async (logDate: string): Promise<void> => {
    await client.post(`/api/v1/users/me/logs/${logDate}/calendar-sync`);
  },
};
