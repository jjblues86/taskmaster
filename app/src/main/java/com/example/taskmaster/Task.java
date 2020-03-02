package com.example.taskmaster;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Task {

    @PrimaryKey(autoGenerate = true)
    long id;

    String title;
    String body;
    String state;
    String image;

    public Task(String title, String body, String state, String image) {
        this.title = title;
        this.body = body;
        this.state = state;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getState() {
        return state;
    }

    public String getImage() {
        return image;
    }

}
