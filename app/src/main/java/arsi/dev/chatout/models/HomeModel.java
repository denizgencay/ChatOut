package arsi.dev.chatout.models;

import java.util.ArrayList;

public class HomeModel {
    private ArrayList<String> blockedUsers,blockedByUsers;

    public HomeModel() {
        this.blockedByUsers = new ArrayList<>();
        this.blockedUsers = new ArrayList<>();
    }

    public ArrayList<String> getBlockedUsers() {
        return blockedUsers;
    }

    public void setBlockedUsers(ArrayList<String> blockedUsers) {
        this.blockedUsers = blockedUsers;
    }

    public ArrayList<String> getBlockedByUsers() {
        return blockedByUsers;
    }

    public void setBlockedByUsers(ArrayList<String> blockedByUsers) {
        this.blockedByUsers = blockedByUsers;
    }
}
