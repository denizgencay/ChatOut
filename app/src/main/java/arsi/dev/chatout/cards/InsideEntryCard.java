package arsi.dev.chatout.cards;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class InsideEntryCard {
    private String comment;
    private ArrayList<String> likes;
    private ArrayList<String> dislikes;
    private String username;
    private Timestamp timestamp;

    public ArrayList<String> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }

    public ArrayList<String> getDislikes() {
        return dislikes;
    }

    public void setDislikes(ArrayList<String> dislikes) {
        this.dislikes = dislikes;
    }

    private String commentId;

    public String getcommentId() {
        return commentId;
    }

    public void setcommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public InsideEntryCard(String comment, ArrayList<String> likes, ArrayList<String> dislikes, String username, Timestamp timestamp,String commentId) {
        this.comment = comment;
        this.likes = likes;
        this.dislikes = dislikes;
        this.username = username;
        this.timestamp = timestamp;
        this.commentId = commentId;
    }
}
