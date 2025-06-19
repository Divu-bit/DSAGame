package com.example.dsagame.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class UserProgress {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int questionId;
    public String solvedDate;
}

