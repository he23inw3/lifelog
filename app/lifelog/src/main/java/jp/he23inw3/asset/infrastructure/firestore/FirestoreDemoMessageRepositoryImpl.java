package jp.he23inw3.asset.infrastructure.firestore;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import jp.he23inw3.asset.domain.constant.FirestoreCollectionNames;
import jp.he23inw3.asset.domain.exception.ExternalServiceException;
import jp.he23inw3.asset.domain.model.DemoMessage;
import jp.he23inw3.asset.domain.repository.DemoMessageRepository;
import jp.he23inw3.asset.domain.util.DateTimeUtil;
import jp.he23inw3.asset.domain.util.InstantUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Firestore をデータストアとした DemoMessageRepository の実装クラス。
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FirestoreDemoMessageRepositoryImpl implements DemoMessageRepository {

    private final Firestore firestore;

    /**
     * 指定された Slack ユーザーIDに該当するデモ用メッセージ一覧を取得します。
     *
     * @param slackUserId Slack ユーザーID
     * @return デモ用メッセージのリスト
     */
    @Override
    public List<DemoMessage> findBySlackUserId(String slackUserId) {
        try {
            String collectionName = FirestoreCollectionNames.DEMO_SLACK_MESSAGES;
            QuerySnapshot querySnapshot = firestore.collection(collectionName)
                    .whereEqualTo("slackUserId", slackUserId)
                    .get()
                    .get();

            return querySnapshot.getDocuments().stream().map(doc -> {
                Long timestampLong = doc.getLong("timestamp");
                LocalDateTime timestamp = DateTimeUtil.toLocalDateTime(InstantUtil.toInstant(timestampLong));
                return DemoMessage.builder()
                        .slackUserId(doc.getString("slackUserId"))
                        .type(doc.getString("type"))
                        .text(doc.getString("text"))
                        .timestamp(timestamp)
                        .build();
            }).collect(Collectors.toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("デモメッセージの取得トランザクションが中断されました。", e);
        } catch (ExecutionException e) {
            throw new ExternalServiceException("デモメッセージの取得クエリ実行に失敗しました。", e);
        }
    }
}
