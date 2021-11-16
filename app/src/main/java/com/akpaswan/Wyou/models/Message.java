package com.akpaswan.Wyou.models;

import com.google.firebase.database.Exclude;

import java.util.Date;

public class Message {
    private String id;
    private String from;
    private String to;
    private String content;
    private Date dateTime;

    public void setId (String id) {
        this.id = id;
    }

    @Exclude
    public String getId () {
        return id;
    }

    public String getFrom () {
        return from;
    }

    public void setFrom (String from) {
        this.from = from;
    }

    public String getTo () {
        return to;
    }

    public void setTo (String to) {
        this.to = to;
    }

    public String getContent () {
        return content;
    }

    public void setContent (String content) {
        this.content = content;
    }

    public Date getDateTime () {
        return dateTime;
    }

    public void setDateTime (Date dateTime) {
        this.dateTime = dateTime;
    }


}
