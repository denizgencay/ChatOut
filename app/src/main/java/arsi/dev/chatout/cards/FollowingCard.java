package arsi.dev.chatout.cards;

public class FollowingCard {
    private String username;
    private String followingId;
    private String photoUri;
    private int priority;

    public FollowingCard(String username, String followingId, String photoUri, int priority) {
        this.username = username;
        this.followingId = followingId;
        this.photoUri = photoUri;
        this.priority = priority;
    }

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

    public String getFollowingId() {
        return followingId;
    }

    public void setFollowingId(String followingId) {
        this.followingId = followingId;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

}
