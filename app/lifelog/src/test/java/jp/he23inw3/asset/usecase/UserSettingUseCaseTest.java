package jp.he23inw3.asset.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import jp.he23inw3.asset.domain.exception.AlreadyExistsException;
import jp.he23inw3.asset.domain.exception.ResourceNotFoundException;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.domain.repository.UserSettingRepository;
import jp.he23inw3.asset.domain.service.UserSettingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserSettingUseCaseTest {

    @Mock
    UserSettingRepository settingRepository;

    @Mock
    UserSettingService settingService;

    @InjectMocks
    UserSettingUseCase target;

    @Nested
    @DisplayName("ユーザーIDによる設定取得")
    class GetSetting {

        @Test
        @DisplayName("ユーザーIDに紐づく設定を正常に取得すること")
        void getSetting_Success() {
            // Arrange
            String slackUserId = "U123456";
            UserSetting expected = UserSetting.builder().slackUserId(slackUserId).userName("Test User").build();
            when(settingRepository.findById(slackUserId)).thenReturn(Optional.of(expected));

            // Act
            UserSetting actual = target.getSetting(slackUserId);

            // Assert
            assertThat(actual).isEqualTo(expected);
            verify(settingRepository).findById(slackUserId);
        }

        @Test
        @DisplayName("ユーザー設定が存在しない場合、ResourceNotFoundExceptionをスローすること")
        void getSetting_NotFound_ShouldThrowException() {
            // Arrange
            String slackUserId = "U999999";
            when(settingRepository.findById(slackUserId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> target.getSetting(slackUserId)).isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User settings not found for " + slackUserId);
            verify(settingRepository).findById(slackUserId);
        }
    }

    @Nested
    @DisplayName("メールアドレスによる設定取得")
    class GetSettingByEmail {

        @Test
        @DisplayName("メールアドレスに紐づく設定を正常に取得すること")
        void getSettingByEmail_Success() {
            // Arrange
            String email = "test@example.com";
            UserSetting expected = UserSetting.builder().email(email).userName("Test User").build();
            when(settingRepository.findByEmail(email)).thenReturn(Optional.of(expected));

            // Act
            UserSetting actual = target.getSettingByEmail(email);

            // Assert
            assertThat(actual).isEqualTo(expected);
            verify(settingRepository).findByEmail(email);
        }

        @Test
        @DisplayName("指定メールアドレスの設定が存在しない場合、ResourceNotFoundExceptionをスローすること")
        void getSettingByEmail_NotFound_ThrowsResourceNotFoundException() {
            // Arrange
            String email = "missing@example.com";
            when(settingRepository.findByEmail(email)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> target.getSettingByEmail(email)).isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User settings not found for email: " + email);
            verify(settingRepository).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("ユーザー設定保存")
    class SaveSetting {

        @Test
        @DisplayName("ユーザー設定を正常に保存すること")
        void saveSetting_Success() {
            // Arrange
            UserSetting setting = UserSetting.builder().slackUserId("U123456").userName("Test User").build();
            doNothing().when(settingRepository).save(setting);

            // Act
            UserSetting actual = target.saveSetting(setting);

            // Assert
            assertThat(actual).isEqualTo(setting);
            verify(settingRepository).save(setting);
        }
    }

    @Nested
    @DisplayName("新規ユーザー登録")
    class RegisterUser {

        @Test
        @DisplayName("正常登録時にUserSettingServiceの登録処理を呼び出すこと")
        void registerUser_Success() {
            UserSetting setting = UserSetting.builder().slackUserId("U123").email("new@example.com").build();
            when(settingService.registerUser(setting)).thenReturn(setting);

            UserSetting result = target.registerUser(setting);

            assertThat(result).isEqualTo(setting);
            verify(settingService).registerUser(setting);
        }

        @Test
        @DisplayName("UserSettingServiceでAlreadyExistsExceptionが発生した場合、そのままスローされること")
        void registerUser_ServiceThrowsAlreadyExistsException() {
            UserSetting setting = UserSetting.builder().slackUserId("U123").email("existing@example.com").build();
            when(settingService.registerUser(setting)).thenThrow(new AlreadyExistsException("既に存在します"));

            assertThatThrownBy(() -> target.registerUser(setting))
                    .isInstanceOf(AlreadyExistsException.class)
                    .hasMessage("既に存在します");
        }
    }

    @Nested
    @DisplayName("全ユーザー設定の取得")
    class GetAllSettings {

        @Test
        @DisplayName("登録されているすべての設定の一覧を正常に取得すること")
        void getAllSettings_Success() {
            List<UserSetting> expected = List.of(UserSetting.builder().build(), UserSetting.builder().build());
            when(settingRepository.findAll()).thenReturn(expected);

            List<UserSetting> actual = target.getAllSettings();

            assertThat(actual).isEqualTo(expected);
            verify(settingRepository).findAll();
        }
    }
}
