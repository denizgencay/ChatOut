package arsi.dev.chatout.cards;

public class SearchCard {

    private String username,searchId,profilePhoto,creatorUid,pushToken;
    private int priority;

    public SearchCard(String username, String searchId, String profilePhoto, String creatorUid, String pushToken, int priority) {
        this.username = username;
        this.searchId = searchId;
        this.profilePhoto = profilePhoto;
        this.creatorUid = creatorUid;
        this.pushToken = pushToken;
        this.priority = priority;
    }

    public SearchCard(String username, String searchId, String profilePhoto, String creatorUid, int priority) {
        this.username = username;
        this.searchId = searchId;
        this.profilePhoto = profilePhoto;
        this.creatorUid = creatorUid;
        this.priority = priority;
    }

    public SearchCard(String username, String searchIdUsername, String profilePhoto) {
        this.username = username;
        this.searchId = searchIdUsername;
        this.profilePhoto = profilePhoto;
    }

    public SearchCard(String username, String searchId, String profilePhoto, int priority) {
        this.username = username;
        this.searchId = searchId;
        this.profilePhoto = profilePhoto;
        this.priority = priority;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getCreatorUid() {
        return creatorUid;
    }

    public void setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getSearchIdUsername() {
        return searchId;
    }

    public void setSearchIdUsername(String searchIdUsername) {
        this.searchId = getSearchIdUsername();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }
}
