package arsi.dev.chatout.cards;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class SearchTitleCard {

    private String title, searchId, personNumber, activeChat;
    private Timestamp finishTime;
    private ArrayList<String> peopleInChat, blockedUsers;

    public SearchTitleCard(String title, String searchId, String personNumber, Timestamp finishTime, ArrayList<String> peopleInChat, ArrayList<String> blockedUsers, String activeChat) {
        this.title = title;
        this.searchId = searchId;
        this.personNumber = personNumber;
        this.finishTime = finishTime;
        this.peopleInChat = peopleInChat;
        this.blockedUsers = blockedUsers;
        this.activeChat = activeChat;
    }


    public String getActiveChat() {
        return activeChat;
    }

    public void setActiveChat(String activeChat) {
        this.activeChat = activeChat;
    }

    public ArrayList<String> getBlockedUsers() {
        return blockedUsers;
    }

    public void setBlockedUsers(ArrayList<String> blockedUsers) {
        this.blockedUsers = blockedUsers;
    }

    public String getPersonNumber() {
        return personNumber;
    }

    public void setPersonNumber(String personNumber) {
        this.personNumber = personNumber;
    }

    public Timestamp getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Timestamp finishTime) {
        this.finishTime = finishTime;
    }

    public ArrayList<String> getPeopleInChat() {
        return peopleInChat;
    }

    public void setPeopleInChat(ArrayList<String> peopleInChat) {
        this.peopleInChat = peopleInChat;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }


}
