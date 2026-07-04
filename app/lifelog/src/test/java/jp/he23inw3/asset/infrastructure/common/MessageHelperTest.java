package jp.he23inw3.asset.infrastructure.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MessageHelperTest {

    @Test
    @DisplayName("存在するキーのメッセージが正常に取得できること")
    void testGetMessage_Exist() {
        // messages.propertiesに定義されている実在するキーを使用
        String message = MessageHelper.getMessage("infra.slack.sent", "U123");
        assertThat(message).contains("U123");
    }

    @Test
    @DisplayName("存在しないキーの場合、!key!形式でメッセージが返ること")
    void testGetMessage_NotExist() {
        String message = MessageHelper.getMessage("non.existent.key");
        assertThat(message).isEqualTo("!non.existent.key!");
    }

    @Test
    @DisplayName("引数がない場合、プレースホルダーが埋め込まれずパターンそのものが返ること")
    void testGetMessage_NoArgs() {
        String message = MessageHelper.getMessage("infra.slack.sent");
        assertThat(message).contains("{0}");
    }
}
