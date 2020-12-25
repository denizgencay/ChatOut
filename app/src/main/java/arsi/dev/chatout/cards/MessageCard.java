package arsi.dev.chatout.cards;

import com.google.firebase.Timestamp;

public class MessageCard {
    private String username, message, senderUid,usernameColor;
    private Timestamp time;

    public MessageCard(String message, String username, Timestamp time, String senderUid, String usernameColor) {
        this.message = message;
        this.username = username;
        this.time = time;
        this.senderUid = senderUid;
        this.usernameColor = usernameColor;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getUsernameColor() {
        return usernameColor;
    }

    public void setUsernameColor(String usernameColor) {
        this.usernameColor = usernameColor;
    }
}
