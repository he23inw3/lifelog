import { client } from './axios';
import type { DemoCalendarListResponse, DemoMessageListResponse } from '../types';

/**
 * デモモード用の API 通信を行うオブジェクト。
 */
export const demoApi = {
  /**
   * 指定した月のデモ用擬似カレンダーイベント一覧を取得します。
   *
   * @param month - 対象月（YYYY-MM 形式）
   * @returns 擬似カレンダーイベントのレスポンス Promise
   */
  getDemoCalendarEvents: async (month: string): Promise<DemoCalendarListResponse.CalendarEvent[]> => {
    const response = await client.get<DemoCalendarListResponse>('/api/v1/demo/calendar', {
      params: { month },
    });
    return response.data.calendarEvents ?? [];
  },

  /**
   * 送信されたデモ用の擬似 Slack メッセージ履歴一覧を取得します。
   *
   * @param slackUserId - 対象の Slack ユーザーID（省略時は全ユーザー対象）
   * @returns 擬似 Slack メッセージ履歴の配列レスポンス Promise
   */
  getDemoSlackMessages: async (slackUserId?: string): Promise<DemoMessageListResponse.DemoMessage[]> => {
    const params = new URLSearchParams();
    if (slackUserId) params.append('slackUserId', slackUserId);
    const response = await client.get<DemoMessageListResponse>('/api/v1/demo/messages', { params });
    return response.data.messages ?? [];
  },
};

