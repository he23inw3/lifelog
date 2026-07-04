package jp.he23inw3.asset.adapter.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 利用者設定一覧レスポンス DTO。
 */
@Data
@Builder
public class UserSettingListResponse {

    /** 利用者総数 */
    private int totalSize;

    /** 利用者設定一覧 */
    private List<UserSettingResponse> userSettings;
}
