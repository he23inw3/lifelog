package jp.he23inw3.asset.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import jp.he23inw3.asset.usecase.GoogleAuthUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleAuthResourceTest {

    @Mock
    GoogleAuthUseCase googleAuthUseCase;

    @InjectMocks
    GoogleAuthResource target;

    @Test
    @DisplayName("ログイン処理時、GoogleAuthUseCaseの戻り値のURLにリダイレクトされること")
    void login_Success() {
        String email = "user@example.com";
        String expectedUrl = "http://localhost:5173/settings?google=success";
        when(googleAuthUseCase.getLoginRedirectUrl(email)).thenReturn(expectedUrl);

        Response response = target.login(email);

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getLocation().toString()).isEqualTo(expectedUrl);
        verify(googleAuthUseCase).getLoginRedirectUrl(email);
    }

    @Test
    @DisplayName("コールバック処理時、GoogleAuthUseCaseの戻り値のURLにリダイレクトされること")
    void callback_Success() {
        String state = "encrypted-state";
        String code = "auth-code";
        String error = null;
        String expectedUrl = "http://localhost:5173/settings?google=success";
        when(googleAuthUseCase.handleCallback(state, code, error)).thenReturn(expectedUrl);

        Response response = target.callback(state, code, error);

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getLocation().toString()).isEqualTo(expectedUrl);
        verify(googleAuthUseCase).handleCallback(state, code, error);
    }
}
