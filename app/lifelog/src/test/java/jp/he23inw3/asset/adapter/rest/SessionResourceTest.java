package jp.he23inw3.asset.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import jp.he23inw3.asset.adapter.dto.SessionListResponse;
import jp.he23inw3.asset.adapter.dto.SessionResetResponse;
import jp.he23inw3.asset.adapter.mapper.SessionMapper;
import jp.he23inw3.asset.domain.constant.UserMessageConstants;
import jp.he23inw3.asset.domain.model.Session;
import jp.he23inw3.asset.usecase.SessionUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionResourceTest {

    @Mock
    SessionUseCase sessionUseCase;

    @Mock
    SessionMapper sessionMapper;

    @InjectMocks
    SessionResource target;

    // =========================================================================
    // セッション一覧取得 (BE-API409)
    // =========================================================================

    @Test
    @DisplayName("セッション一覧が正常に取得できること")
    void getSessions_Success() {
        List<Session> sessions = Collections.singletonList(Session.builder().slackUserId("U123").build());
        List<SessionListResponse.SessionResponse> responses = Collections
                .singletonList(SessionListResponse.SessionResponse.builder().slackUserId("U123").build());

        when(sessionUseCase.getAllSessions()).thenReturn(sessions);
        when(sessionMapper.toResponseList(sessions)).thenReturn(responses);

        SessionListResponse result = target.getSessions();

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(result.getSessions()).hasSize(1);
        assertThat(result.getSessions().get(0).getSlackUserId()).isEqualTo("U123");
        verify(sessionUseCase).getAllSessions();
        verify(sessionMapper).toResponseList(sessions);
    }

    // =========================================================================
    // セッションリセット (BE-API201)
    // =========================================================================

    @Test
    @DisplayName("特定ユーザーの対話セッションをリセットし、HTTP 200 OKを返却すること")
    void resetSession_Success() {
        String slackUserId = "U123456";
        SessionResetResponse mockResponse = SessionResetResponse.builder()
                .slackUserId(slackUserId)
                .message(UserMessageConstants.SESSION_RESET_MESSAGE)
                .build();
        when(sessionMapper.toResetResponse(slackUserId, UserMessageConstants.SESSION_RESET_MESSAGE)).thenReturn(mockResponse);

        SessionResetResponse response = target.resetSession(slackUserId);

        verify(sessionUseCase).resetSession(slackUserId);
        assertThat(response).isEqualTo(mockResponse);
    }
}
