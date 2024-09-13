package com.example.newtask;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_images")
public class UserImage {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private String imageUri;

    public UserImage(int userId, String imageUri) {
        this.userId = userId;
        this.imageUri = imageUri;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
