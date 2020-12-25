package arsi.dev.chatout.cards;

import java.util.HashMap;

public class CommentCard {

    private String commentMessage,commentTitle, commentId;
    private int priority;
    private HashMap<String,Object> writtenComments;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getCommentMessage() {
        return commentMessage;
    }

    public void setCommentMessage(String commentMessage) {
        this.commentMessage = commentMessage;
    }

    public String getCommentTitle() {
        return commentTitle;
    }

    public void setCommentTitle(String commentTitle) {
        this.commentTitle = commentTitle;
    }

    public HashMap<String, Object> getWrittenComments() {
        return writtenComments;
    }

    public void setWrittenComments(HashMap<String, Object> writtenComments) {
        this.writtenComments = writtenComments;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public CommentCard(String commentMessage, String commentTitle, int priority, HashMap<String,Object> writtenComments, String commentId) {
        this.commentMessage = commentMessage;
        this.commentTitle = commentTitle;
        this.priority = priority;
        this.writtenComments = writtenComments;
        this.commentId = commentId;
    }
}