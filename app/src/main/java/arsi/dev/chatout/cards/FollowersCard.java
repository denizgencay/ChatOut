package arsi.dev.chatout.cards;

public class FollowersCard {
    private String username;
    private String followersId;
    private String photoUri;
    private int priority;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFollowersId() {
        return followersId;
    }

    public void setFollowersId(String followersId) {
        this.followersId = followersId;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public FollowersCard(String username, String followersId, String photoUri, int priority) {
        this.username = username;
        this.followersId = followersId;
        this.photoUri = photoUri;
        this.priority = priority;
    }
}
