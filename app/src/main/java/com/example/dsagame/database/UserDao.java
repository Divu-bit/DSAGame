package com.example.dsagame.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dsagame.database.entities.Question;
import com.example.dsagame.database.entities.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM User WHERE firebaseUid = :uid LIMIT 1")
    User getUserByUid(String uid);
    @Query("SELECT * FROM User LIMIT 1")
    User getUser();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Update
    void updateUser(User user);
    @Query("SELECT * FROM User ORDER BY xp DESC, level DESC, streak DESC LIMIT 50")
    List<User> getLeaderboard();
}

