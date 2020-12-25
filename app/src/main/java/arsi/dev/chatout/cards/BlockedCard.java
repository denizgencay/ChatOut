package arsi.dev.chatout.cards;

import java.util.ArrayList;

public class BlockedCard {
    private String username,userId;
    private ArrayList<String> myBlockedUsers,UserBlockedByUsers;
    int priority;

    public BlockedCard(String username, String userId, ArrayList<String> myBlockedUsers, ArrayList<String> userBlockedByUsers, int priority) {
        this.username = username;
        this.userId = userId;
        this.myBlockedUsers = myBlockedUsers;
        UserBlockedByUsers = userBlockedByUsers;
        this.priority = priority;
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

    public ArrayList<String> getMyBlockedUsers() {
        return myBlockedUsers;
    }

    public void setMyBlockedUsers(ArrayList<String> myBlockedUsers) {
        this.myBlockedUsers = myBlockedUsers;
    }

    public ArrayList<String> getUserBlockedByUsers() {
        return UserBlockedByUsers;
    }

    public void setUserBlockedByUsers(ArrayList<String> userBlockedByUsers) {
        UserBlockedByUsers = userBlockedByUsers;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
