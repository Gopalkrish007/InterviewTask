package com.example.newtask;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserImageDao {
    @Insert
    void insertUserImage(UserImage userImage);

    @Query("SELECT * FROM user_images WHERE userId = :userId LIMIT 1")
    UserImage getUserImage(int userId);
    // Optionally delete by userId if needed
    @Query("DELETE FROM user_images WHERE userId = :userId")
    void deleteByUserId(int userId);
}
