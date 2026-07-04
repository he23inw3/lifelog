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

export interface DemoCalendarListResponse {
  /** イベント総数 */
  totalSize: number;
  /** カレンダーイベント一覧 */
  calendarEvents: DemoCalendarListResponse.CalendarEvent[];
}

export namespace DemoCalendarListResponse {
  /**
   * カレンダーイベントのレスポンスを表すインターフェース。
   */
  export interface CalendarEvent {
    /** 対象のカレンダーID。 */
    calendarId: string;
    /** イベント発生日（YYYY-MM-DD 形式）。 */
    date: string;
    /** イベントのタイトル。 */
    title: string;
    /** イベントの説明文。 */
    description: string;
    /** 休日フラグ。 */
    holiday: boolean;
    /** カレンダーとの同期完了日時（ISO-8601 形式）。 */
    syncedAt: string;
  }
}

/**
 * デモ用 Slack メッセージ一覧レスポンスを表すインターフェース。
 */
export interface DemoMessageListResponse {
  /** メッセージ総数 */
  totalSize: number;
  /** メッセージ一覧 */
  messages: DemoMessageListResponse.DemoMessage[];
}

export namespace DemoMessageListResponse {
  /**
   * デモ用の Slack メッセージ履歴を表すインターフェース。
   */
  export interface DemoMessage {
    /** 対象ユーザーの Slack ユーザーID。 */
    slackUserId: string;
    /** メッセージの送信種別。 */
    type: string;
    /** メッセージ本文。 */
    text: string;
    /** メッセージ送信日時（ISO-8601 形式）。 */
    timestamp: string;
  }
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
  lastLogDate: string;
}

/**
 * デモチャット内の個別メッセージを表すインターフェース。
 */
export interface Message {
  id: string;
  type: 'user' | 'bot';
  text: string;
  timestamp: string;
  isHoliday?: boolean;
  result?: LogDetailResponse; // 確定後の解析結果
  isError?: boolean;
  hasConfirmActions?: boolean; // 確定・キャンセルボタンを表示するか
  pendingResult?: LogDetailResponse; // 確定待ちのデータ
  actionClicked?: 'confirm' | 'cancel' | null; // クリックされたアクション
}

