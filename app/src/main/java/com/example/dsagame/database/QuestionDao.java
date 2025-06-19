package com.example.dsagame.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dsagame.database.entities.Question;

import java.util.List;

@Dao
public interface QuestionDao {
    @Query("SELECT * FROM Question WHERE topic = :topic")
    List<Question> getQuestionsByTopic(String topic);

    @Query("SELECT * FROM Question")
    List<Question> getAll();

    @Update
    void updateQuestion(Question question);

    @Insert
    void insertAll(List<Question> questions);
}
