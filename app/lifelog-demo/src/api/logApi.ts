import { client } from './axios';
import type { LogListResponse, LogDetailResponse } from '../types';

/**
 * 日報（ライフログ）関連の API 通信を行うオブジェクト。
 */
export const logApi = {
  /**
   * 新しい日報を登録します（Gemini による解析とカレンダー登録処理を含みます）。
   *
   * @param rawText - 解析対象となる未加工のテキスト
   * @param holiday - 休日フラグ
   * @returns 登録された日報情報のレスポンス Promise
   */
  createLog: async (rawText: string, holiday: boolean): Promise<LogDetailResponse> => {
    const response = await client.post<LogDetailResponse>('/api/v1/users/me/logs', {
      rawText,
      holiday,
    });
    return response.data;
  },

  /**
   * 新しい日報を解析します（登録・同期は行いません）。
   *
   * @param rawText - 解析対象となる未加工のテキスト
   * @param holiday - 休日フラグ
   * @returns 解析された日報情報のレスポンス Promise
   */
  analyzeLog: async (rawText: string, holiday: boolean): Promise<LogDetailResponse> => {
    const response = await client.post<LogDetailResponse>('/api/v1/users/me/logs/analyze', {
      rawText,
      holiday,
    });
    return response.data;
  },


  /**
   * 指定された期間内の日報一覧を取得します。
   *
   * @param from - 取得開始日 (YYYY-MM-DD 形式、任意)
   * @param to - 取得終了日 (YYYY-MM-DD 形式、任意)
   * @returns 日報情報の配列レスポンス Promise
   */
  getLogs: async (from?: string, to?: string): Promise<LogListResponse> => {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);
    const response = await client.get<LogListResponse>('/api/v1/users/me/logs', { params });
    return response.data;
  },

  /**
   * 指定した日付の日報詳細情報を取得します。
   *
   * @param logDate - ログ取得対象日 (YYYY-MM-DD 形式)
   * @returns 日報詳細情報のレスポンス Promise
   */
  getLogDetails: async (logDate: string): Promise<LogDetailResponse> => {
    const response = await client.get<LogDetailResponse>(`/api/v1/users/me/logs/${logDate}`);
    return response.data;
  },
};
