package jp.he23inw3.asset.domain.repository.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

/**
 * 日報検索条件を保持する DTO クラス。
 */
@Value
@Builder
public class DailyLogSearchQuery {
    /** Slack ユーザーID */
    String slackUserId;
    /** 取得開始日 */
    LocalDate start;
    /** 取得終了日 */
    LocalDate end;
}
