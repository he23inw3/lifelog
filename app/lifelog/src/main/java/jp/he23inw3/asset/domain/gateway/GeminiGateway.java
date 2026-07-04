package jp.he23inw3.asset.domain.gateway;

import java.time.LocalDateTime;
import jp.he23inw3.asset.domain.model.GeminiParseResult;

/**
 * Vertex AI Gemini モデルと連携し、自然言語テキストの解析および生成を行うためのゲートウェイインターフェース。
 */
public interface GeminiGateway {

    /**
     * ユーザーが入力した日報テキストを解析し、構造化された JSON データ（ドメインモデル）にパースします。
     *
     * @param rawText ユーザーの入力テキスト
     * @param now 解析時の現在日時
     * @param dayStatus 本日の暦状況（"平日", "祝日", "休暇" など）
     * @return 解析結果を格納した {@link GeminiParseResult} オブジェクト
     */
    GeminiParseResult parse(String rawText, LocalDateTime now, String dayStatus);

    /**
     * 月末日報サマリーをもとに、ユーザーへの月間振り返りレポートテキストを生成します。
     *
     * @param monthlySummaryText 1ヶ月分の日報を結合した要約用インプットテキスト
     * @return 生成された日本語の振り返りレポート本文
     */
    String generateMonthlyReport(String monthlySummaryText);
}
