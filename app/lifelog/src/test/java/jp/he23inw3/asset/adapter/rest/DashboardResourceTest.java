package jp.he23inw3.asset.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import jp.he23inw3.asset.adapter.dto.DashboardResponse;
import jp.he23inw3.asset.adapter.dto.UserSettingListResponse;
import jp.he23inw3.asset.adapter.dto.UserSettingResponse;
import jp.he23inw3.asset.adapter.mapper.DashboardMapper;
import jp.he23inw3.asset.adapter.mapper.UserSettingMapper;
import jp.he23inw3.asset.domain.model.DashboardStats;
import jp.he23inw3.asset.domain.model.UserSetting;
import jp.he23inw3.asset.usecase.AdminDashboardUseCase;
import jp.he23inw3.asset.usecase.UserSettingUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardResourceTest {

    @Mock
    AdminDashboardUseCase adminDashboardUseCase;

    @Mock
    DashboardMapper dashboardMapper;

    @Mock
    UserSettingUseCase userSettingUseCase;

    @Mock
    UserSettingMapper userSettingMapper;

    @InjectMocks
    DashboardResource target;

    // =========================================================================
    // ダッシュボード(BE-API404)
    // =========================================================================

    @Test
    @DisplayName("管理ダッシュボード情報が正常に取得できること")
    void getDashboard_Success() {
        DashboardStats mockStats = DashboardStats.builder()
                .activeUserCount(1)
                .todayLogCount(5)
                .activeSessionCount(1)
                .todayBatchErrorCount(0)
                .build();
        DashboardResponse mockResponse = DashboardResponse.builder().activeUserCount(1).todayLogCount(5)
                .activeSessionCount(1).todayBatchErrorCount(0).build();

        when(adminDashboardUseCase.getDashboardStats()).thenReturn(mockStats);
        when(dashboardMapper.toResponse(mockStats)).thenReturn(mockResponse);

        DashboardResponse response = target.getDashboard();

        assertThat(response).isNotNull();
        assertThat(response.getActiveUserCount()).isEqualTo(1);
        assertThat(response.getTodayLogCount()).isEqualTo(5);
        verify(adminDashboardUseCase).getDashboardStats();
        verify(dashboardMapper).toResponse(mockStats);
    }

    // =========================================================================
    // 利用者一覧 (BE-API405)
    // =========================================================================

    @Test
    @DisplayName("利用者一覧が正常に取得できること")
    void getUsers_Success() {
        List<UserSetting> settings = Collections.singletonList(UserSetting.builder().slackUserId("U123").build());
        List<UserSettingResponse> responses = Collections
                .singletonList(UserSettingResponse.builder().slackUserId("U123").build());

        when(userSettingUseCase.getAllSettings()).thenReturn(settings);
        when(userSettingMapper.toResponseList(settings)).thenReturn(responses);

        UserSettingListResponse result = target.getUsers();

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(result.getUserSettings()).hasSize(1);
        assertThat(result.getUserSettings().get(0).getSlackUserId()).isEqualTo("U123");
        verify(userSettingUseCase).getAllSettings();
        verify(userSettingMapper).toResponseList(settings);
    }
}
