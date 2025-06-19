package com.example.dsagame.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity
public class Question implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String topic;
    public String difficulty;
    public boolean isSolved;
    public String testCases = "";

    public int getXpValue() {
        switch (difficulty) {
            case "Medium": return 20;
            case "Hard": return 30;
            default: return 10;
        }
    }
}