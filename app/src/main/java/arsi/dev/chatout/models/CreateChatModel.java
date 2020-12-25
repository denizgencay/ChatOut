package arsi.dev.chatout.models;

import java.util.HashMap;

public class CreateChatModel {

    String pushToken;
    HashMap<String,Object> pushTokens;

    public HashMap<String, Object> getPushTokens() {
        return pushTokens;
    }

    public void setPushTokens(HashMap<String, Object> pushTokens) {
        this.pushTokens = pushTokens;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public CreateChatModel() {
        pushTokens = new HashMap<>();
    }
}
