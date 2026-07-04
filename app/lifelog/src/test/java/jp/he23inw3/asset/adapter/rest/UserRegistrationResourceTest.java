package jp.he23inw3.asset.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jp.he23inw3.asset.adapter.context.ApiContext;
import jp.he23inw3.asset.adapter.dto.UserRegistrationRequest;
import jp.he23inw3.asset.adapter.dto.UserSettingResponse;
import jp.he23inw3.asset.adapter.mapper.UserSettingMapper;
import jp.he23inw3.asset.domain.exception.AlreadyExistsException;
import jp.he23inw3.asset.domain.exception.InvalidRegistrationException;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.usecase.SlackLinkageUseCase;
import jp.he23inw3.asset.usecase.UserSettingUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRegistrationResourceTest {

    @Mock
    ApiContext apiContext;

    @Mock
    UserSettingUseCase userSettingUseCase;

    @Mock
    UserSettingMapper userSettingMapper;

    @Mock
    SlackLinkageUseCase slackLinkageUseCase;

    @InjectMocks
    UserRegistrationResource target;

    // =========================================================================
    // 利用者初回登録 (BE-API102)
    // =========================================================================
    @Nested
    @DisplayName("利用者初回登録")
    class Register {

        @Test
        @DisplayName("利用者の初回登録を正常に実行できること")
        void register_Success() {
            UserRegistrationRequest request = new UserRegistrationRequest();
            request.setSlackUserId("U123456");
            request.setUserName("Test User");
            request.setRemindTime("19:00");
            request.setGoogleCalendarId("cal@example.com");
            request.setActive(true);

            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId("U123456")
                    .userName("Test User")
                    .remindTime("19:00")
                    .googleCalendarId("cal@example.com")
                    .active(true)
                    .build();
            UserSetting mockSaved = mockSetting.toBuilder().build();
            UserSettingResponse mockResponse = UserSettingResponse.builder()
                    .slackUserId("U123456")
                    .userName("Test User")
                    .build();

            when(apiContext.getAuthenticatedUserId()).thenReturn("test@example.com");
            when(userSettingMapper.toDomain(request, "test@example.com")).thenReturn(mockSetting);
            when(userSettingUseCase.registerUser(mockSetting)).thenReturn(mockSaved);
            when(userSettingMapper.toResponse(mockSaved)).thenReturn(mockResponse);

            UserSettingResponse response = target.register(request);

            assertThat(response).isNotNull();
            assertThat(response.getSlackUserId()).isEqualTo("U123456");
            assertThat(response.getUserName()).isEqualTo("Test User");
            verify(apiContext).getAuthenticatedUserId();
            verify(userSettingMapper).toDomain(request, "test@example.com");
            verify(userSettingUseCase).registerUser(mockSetting);
            verify(userSettingMapper).toResponse(mockSaved);
        }

        @Test
        @DisplayName("メールアドレスが既に登録されている場合はConflictエラーになること")
        void register_Fail_DuplicateEmail() {
            UserRegistrationRequest request = new UserRegistrationRequest();
            request.setSlackUserId("U123456");
            request.setUserName("Test User");
            request.setRemindTime("19:00");
            request.setGoogleCalendarId("cal@example.com");

            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId("U123456")
                    .userName("Test User")
                    .build();

            when(apiContext.getAuthenticatedUserId()).thenReturn("test@example.com");
            when(userSettingMapper.toDomain(request, "test@example.com")).thenReturn(mockSetting);
            when(userSettingUseCase.registerUser(mockSetting))
                    .thenThrow(new AlreadyExistsException("このメールアドレスは既に登録されています。"));

            assertThatThrownBy(() -> target.register(request))
                    .isInstanceOf(AlreadyExistsException.class)
                    .hasMessage("このメールアドレスは既に登録されています。");
        }

        @Test
        @DisplayName("SlackユーザーIDが既に登録されている場合はConflictエラーになること")
        void register_Fail_DuplicateSlackUserId() {
            UserRegistrationRequest request = new UserRegistrationRequest();
            request.setSlackUserId("U123456");
            request.setUserName("Test User");
            request.setRemindTime("19:00");
            request.setGoogleCalendarId("cal@example.com");

            UserSetting mockSetting = UserSetting.builder()
                    .slackUserId("U123456")
                    .userName("Test User")
                    .build();

            when(apiContext.getAuthenticatedUserId()).thenReturn("test@example.com");
            when(userSettingMapper.toDomain(request, "test@example.com")).thenReturn(mockSetting);
            when(userSettingUseCase.registerUser(mockSetting))
                    .thenThrow(new AlreadyExistsException("この Slack ユーザー ID は既に登録されています。"));

            assertThatThrownBy(() -> target.register(request))
                    .isInstanceOf(AlreadyExistsException.class)
                    .hasMessage("この Slack ユーザー ID は既に登録されています。");
        }

        @Test
        @DisplayName("SlackユーザーIDとSlackトークンが共に空の場合はエラーになること")
        void register_Fail_EmptySlackUserId() {
            UserRegistrationRequest request = new UserRegistrationRequest();
            request.setSlackUserId("");
            request.setSlackToken("");
            request.setUserName("Test User");
            request.setRemindTime("19:00");
            request.setGoogleCalendarId("cal@example.com");

            when(apiContext.getAuthenticatedUserId()).thenReturn("test@example.com");

            assertThatThrownBy(() -> target.register(request))
                    .isInstanceOf(InvalidRegistrationException.class)
                    .hasMessage("Slack ユーザー ID または Slack 連携トークンは必須です。");
            verify(userSettingUseCase, never()).registerUser(any());
        }
    }
}
