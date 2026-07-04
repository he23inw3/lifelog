package jp.he23inw3.asset.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.domain.model.Log;
import jp.he23inw3.asset.domain.model.Sentiment;
import jp.he23inw3.asset.domain.repository.dto.DailyLogSearchQuery;

/**
 * 登録された日報ログ情報の永続化および検索を行うためのリポジトリインターフェース。
 */
public interface DailyLogRepository {

    /**
     * 日報ログを永続化（新規追加または更新）します。
     *
     * @param log 保存対象の日報ログドメインモデル
     */
    void save(Log log);

    /**
     * 指定された検索条件に対応する日報ログ一覧を取得します。
     *
     * @param query 検索条件 DTO
     * @return 該当する日報ログの一覧リスト
     */
    List<Log> findByUserIdAndPeriod(DailyLogSearchQuery query);

    /**
     * 指定された Slack ユーザーおよび日付に対応する特定の日報ログを取得します。
     *
     * @param slackUserId Slack ユーザーID
     * @param date 対象の日付
     * @return 日報ログを格納した {@link Optional}。存在しない場合は {@link Optional#empty()}
     */
    Optional<Log> findByUserIdAndDate(String slackUserId, LocalDate date);

    /**
     * 指定された日付に登録された日報ログの総件数を取得します。
     *
     * @param date 対象の日付
     * @return 該当する日報ログの件数
     */
    long countByDate(LocalDate date);

    /**
     * 管理用条件に基づいて日報ログ一覧を検索します。
     *
     * @param user Slack ユーザーID（部分一致、空値の場合は全件）
     * @param from 検索開始日
     * @param to 検索終了日
     * @param holiday 休暇フラグ
     * @param sentiment 感情
     * @return 条件に合致する日報ログのリスト
     */
    List<Log> findByAdminQuery(String user, LocalDate from, LocalDate to, Boolean holiday, Sentiment sentiment);
}
