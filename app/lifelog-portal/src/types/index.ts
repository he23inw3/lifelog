/**
 * ユーザーの外部サービス連携状態を表すインターフェース。
 */
export interface IntegrationStatus {
  /** Google アカウント（Google Calendar）との連携が完了しているかどうか。 */
  googleLinked: boolean;
  /** 同期先の Google カレンダーの ID。未設定時は null。 */
  googleCalendarId: string | null;
  /** Slack アカウントとの連携が完了しているかどうか。 */
  slackLinked: boolean;
  /** 連携された Slack のユーザーID。未設定時は null。 */
  slackUserId: string | null;
}

/**
 * 利用者設定情報のレスポンスを表すインターフェース。
 */
export interface UserSettingResponse {
  /** Slack のユーザーID。例: U12345678 */
  slackUserId: string;
  /** ユーザーのメールアドレス。 */
  email: string;
  /** ユーザー名。 */
  userName: string;
  /** 日報の催促メッセージ送信予定時刻。例: 09:00 */
  remindTime: string;
  /** 同期先の Google カレンダーID。 */
  googleCalendarId: string;
  /** アカウントが有効かどうかを表すフラグ。 */
  active: boolean;
  /** Google アカウントとの連携が完了しているかどうかを表すフラグ。 */
  googleLinked: boolean;
  /** レコードの作成日時（ISO-8601 形式）。 */
  createdAt: string;
}

/**
 * 日報（ライフログ）一覧・検索レスポンスを表すインターフェース。
 */
export interface LogListResponse {
  /** 総件数 */
  totalSize: number;
  /** 日報ログリスト */
  logs: LogListResponse.Log[];
}

export namespace LogListResponse {
  /**
   * 日報（ライフログ）個別情報を表すインターフェース（一覧用）。
   */
  export interface Log {
    /** ログ対象日（YYYY-MM-DD 形式）。 */
    logDate: string;
    /** 休日フラグ。 */
    holiday: boolean;
    /** 登録されたタスク・業務内容。 */
    tasks: string;
    /** 稼働時間。 */
    workHours: number;
    /** 時間外労働時間。 */
    overtimeHours: number;
    /** 日記または特記事項。 */
    diary: string;
    /** 解析された感情（happy, neutral, tired, stressed 等）。 */
    sentiment: string;
  }
}

/**
 * 日報（ライフログ）詳細・登録・解析レスポンスを表すインターフェース。
 */
export interface LogDetailResponse {
  /** Slack のユーザーID。 */
  slackUserId: string;
  /** ログ対象日（YYYY-MM-DD 形式）。 */
  logDate: string;
  /** ユーザーが入力した未加工のテキスト。 */
  rawText: string;
  /** 休日フラグ。 */
  holiday: boolean;
  /** 登録されたタスク・業務内容。 */
  tasks: string;
  /** 稼働時間。 */
  workHours: number;
  /** 時間外労働時間。 */
  overtimeHours: number;
  /** 日記または特記事項。 */
  diary: string;
  /** 解析された感情（happy, neutral, tired, stressed 等）。 */
  sentiment: string;
  /** レコードの作成日時（ISO-8601 形式）。 */
  createdAt: string;
}

/**
 * マイダッシュボードの統計情報を表すインターフェース。
 */
export interface MyDashboardResponse {
  /** 当月の累積日報登録数。 */
  monthlyLogCount: number;
  /** 当月の累積稼働時間。 */
  monthlyWorkHours: number;
  /** 当月の累積時間外労働時間。 */
  monthlyOvertimeHours: number;
  /** 最後に登録された日報の日付（YYYY-MM-DD 形式）。 */
  lastLogDate: string | null;
}

/**
 * ユーザー初回登録 API リクエストインターフェース。
 */
export interface UserRegistrationRequest {
  /** Slack ユーザー ID。トークン解決時は省略可能 */
  slackUserId?: string;
  /** Slack 連携用一時トークン。トークン解決時は必須 */
  slackToken?: string;
  /** ユーザー名（表示名） */
  userName: string;
  /** Slack リマインド送信時刻（HH:mm 形式）。例: "22:00" */
  remindTime: string;
  /** 同期先 Google カレンダー ID */
  googleCalendarId: string;
  /** 有効化フラグ */
  active?: boolean;
  /** Google 連携フラグ */
  googleLinked?: boolean;
}
