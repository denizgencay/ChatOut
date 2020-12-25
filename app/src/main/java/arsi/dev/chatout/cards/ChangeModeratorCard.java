package arsi.dev.chatout.cards;

public class ChangeModeratorCard {

    private String photoUri,username,userId;
    private int priority;

    public ChangeModeratorCard(String photoUri, String username, String userId, int priority) {
        this.photoUri = photoUri;
        this.username = username;
        this.userId = userId;
        this.priority = priority;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
