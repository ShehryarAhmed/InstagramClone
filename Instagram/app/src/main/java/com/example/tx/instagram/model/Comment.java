package com.example.tx.instagram.model;

import com.example.tx.instagram.model.Like;

import java.util.List;

public class Comment {

    private String comment;
    private String user_id;
    private List<Like> like;
    private String date_created;

    public Comment(){}

    public Comment(String comment, String user_id, List<Like> like, String date_created) {
        this.comment = comment;
        this.user_id = user_id;
        this.like = like;
        this.date_created = date_created;
    }



    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public List<Like> getLike() {
        return like;
    }

    public void setLike(List<Like> like) {
        this.like = like;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "comment='" + comment + '\'' +
                ", user_id='" + user_id + '\'' +
                ", like=" + like +
                ", date_created='" + date_created + '\'' +
                '}';
    }
}
