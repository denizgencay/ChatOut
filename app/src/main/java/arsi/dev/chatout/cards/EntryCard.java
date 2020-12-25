package arsi.dev.chatout.cards;

import com.google.firebase.Timestamp;

public class EntryCard {

    private String title;
    private String commentNumber;
    private String entryId;
    private Timestamp time;

    public EntryCard(String title, String commentNumber, String entryId, Timestamp time) {
        this.title = title;
        this.commentNumber = commentNumber;
        this.entryId = entryId;
        this.time = time;
    }

    public EntryCard() {

    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getCommentNumber() {
        return commentNumber;
    }

    public void setCommentNumber(String commentNumber) {
        this.commentNumber = commentNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
