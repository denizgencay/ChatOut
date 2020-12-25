package arsi.dev.chatout.models;

import java.util.ArrayList;

public class SignUpModel {

    private ArrayList<String> usernames;

    public SignUpModel() {
        usernames = new ArrayList<>();
    }

    public ArrayList<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(ArrayList<String> usernames) {
        this.usernames = usernames;
    }
}
