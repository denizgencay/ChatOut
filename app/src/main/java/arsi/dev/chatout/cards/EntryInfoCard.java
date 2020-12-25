package arsi.dev.chatout.cards;

import com.google.firebase.Timestamp;

public class EntryInfoCard {

    private String title;
    private String entryId;
    private Timestamp time;
    private int priority;

    public EntryInfoCard(String title, String entryId, Timestamp time,int priority) {
        this.title = title;
        this.entryId = entryId;
        this.time = time;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

}
