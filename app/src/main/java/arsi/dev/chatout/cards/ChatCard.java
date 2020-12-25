package arsi.dev.chatout.cards;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class ChatCard {
    private String numberOfPerson;
    private String title;
    private String chatId;
    private Timestamp timestamp;
    private ArrayList<String> blockedUsers;
    private String activeChat1;
    private int priority;

    public ChatCard(Timestamp timestamp, String numberOfPerson, String title, String chatId,ArrayList<String> blockedUsers,ArrayList<String> peopleInChat, String activeChat1, int priority) {
        this.blockedUsers = blockedUsers;
        this.timestamp = timestamp;
        this.numberOfPerson = numberOfPerson;
        this.title = title;
        this.chatId = chatId;
        this.peopleInChat = peopleInChat;
        this.activeChat1 = activeChat1;
        this.priority = priority;
    }

    public ChatCard(Timestamp timestamp, String numberOfPerson, String title, String chatId,ArrayList<String> blockedUsers,ArrayList<String> peopleInChat, String activeChat1) {
        this.blockedUsers = blockedUsers;
        this.timestamp = timestamp;
        this.numberOfPerson = numberOfPerson;
        this.title = title;
        this.chatId = chatId;
        this.peopleInChat = peopleInChat;
        this.activeChat1 = activeChat1;
    }

    public ChatCard() {

    }

    public ChatCard(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getActiveChat1() {
        return activeChat1;
    }

    public void setActiveChat1(String activeChat1) {
        this.activeChat1 = activeChat1;
    }

    public ArrayList<String> getPeopleInChat() {
        return peopleInChat;
    }

    public void setPeopleInChat(ArrayList<String> peopleInChat) {
        this.peopleInChat = peopleInChat;
    }

    private ArrayList<String> peopleInChat;

    public ArrayList<String> getBlockedUsers() {
        return blockedUsers;
    }

    public void setBlockedUsers(ArrayList<String> blockedUsers) {
        this.blockedUsers = blockedUsers;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public Timestamp getTime() {
        return timestamp;
    }

    public void setTime(Timestamp time) {
        this.timestamp = timestamp;
    }

    public String getNumberOfPerson() {
        return numberOfPerson;
    }

    public void setNumberOfPerson(String numberOfPerson) {
        this.numberOfPerson = numberOfPerson;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
