package jp.he23inw3.asset.adapter.rest;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import jp.he23inw3.asset.adapter.constant.ApiPath;
import jp.he23inw3.asset.adapter.constant.ApiTag;
import jp.he23inw3.asset.adapter.dto.BatchExecutionDetailHistoryResponse;
import jp.he23inw3.asset.adapter.dto.BatchExecutionHistoryListResponse;
import jp.he23inw3.asset.adapter.dto.BatchExecutionHistoryQueryRequest;
import jp.he23inw3.asset.adapter.mapper.BatchExecutionHistoryMapper;
import jp.he23inw3.asset.adapter.util.DateParser;
import jp.he23inw3.asset.domain.model.BatchExecutionHistory;
import jp.he23inw3.asset.domain.model.BatchStatus;
import jp.he23inw3.asset.domain.repository.dto.BatchExecutionHistoryQuery;
import jp.he23inw3.asset.usecase.BatchExecutionHistoryUseCase;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * 管理者用バッチ実行履歴エンドポイントを提供する REST リソースクラス。
 */
@Path(ApiPath.ADMIN_BATCHES)
@Tag(name = ApiTag.ADMIN)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class BatchResource {

    private final BatchExecutionHistoryUseCase batchHistoryUseCase;

    private final BatchExecutionHistoryMapper batchHistoryMapper;

    /**
     * バッチ実行履歴の一覧を取得します。
     *
     * @param request 検索条件 DTO
     * @return バッチ実行履歴 DTO リスト
     */
    @GET
    @Path("/histories")
    @Operation(operationId = "BE-API403", summary = "バッチ実行履歴の一覧取得", description = "検索条件に対応するバッチの実行履歴一覧を取得します")
    public BatchExecutionHistoryListResponse getBatchHistories(@BeanParam BatchExecutionHistoryQueryRequest request) {
        List<BatchExecutionHistory> histories = batchHistoryUseCase.getHistories(buildQuery(request));
        List<BatchExecutionHistoryListResponse.BatchExecutionHistory> responseHistories = batchHistoryMapper
                .toResponseHistoryList(histories);
        return BatchExecutionHistoryListResponse.builder()
                .totalSize(CollectionUtils.size(responseHistories))
                .batchExecutionHistories(responseHistories)
                .build();
    }

    /**
     * バッチ実行履歴の詳細を取得します。
     *
     * @param id 実行履歴ID
     * @return バッチ実行履歴レスポンス DTO
     */
    @GET
    @Path("/histories/{id}")
    @Operation(operationId = "BE-API410", summary = "バッチ詳細", description = "指定されたIDに対応するバッチ実行履歴の詳細情報を取得します")
    public BatchExecutionDetailHistoryResponse getBatchHistoryDetail(@PathParam("id") String id) {
        BatchExecutionHistory history = batchHistoryUseCase.getHistory(id);
        return batchHistoryMapper.toDetailResponseHistory(history);
    }

    // -------------------------------------------------------------------------
    // ヘルパーメソッド
    // -------------------------------------------------------------------------

    /**
     * リクエストパラメータから検索条件を構築します。
     *
     * @param request クエリパラメータオブジェクト
     * @return 検索条件オブジェクト
     */
    private BatchExecutionHistoryQuery buildQuery(BatchExecutionHistoryQueryRequest request) {
        BatchExecutionHistoryQuery.BatchExecutionHistoryQueryBuilder builder = BatchExecutionHistoryQuery.builder();

        if (request.getLimit() != null) {
            builder.limit(request.getLimit());
        }
        if (request.getOffset() != null) {
            builder.offset(request.getOffset());
        }
        if (StringUtils.isNotBlank(request.getStart())) {
            builder.start(DateParser.parseDate(request.getStart()).atStartOfDay());
        }
        if (StringUtils.isNotBlank(request.getEnd())) {
            builder.end(DateParser.parseDate(request.getEnd()).atTime(23, 59, 59));
        }
        if (StringUtils.isNotBlank(request.getBatchId())) {
            builder.batchId(request.getBatchId());
        }
        if (StringUtils.isNotBlank(request.getStatus())) {
            builder.status(BatchStatus.fromValue(request.getStatus()));
        }

        return builder.build();
    }
}
