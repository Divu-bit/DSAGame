package com.example.dsagame.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int xp;
    public int level;
    public int streak;
    public String lastActiveDate;
}

