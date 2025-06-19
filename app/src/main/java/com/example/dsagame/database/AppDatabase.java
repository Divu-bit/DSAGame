package com.example.dsagame.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.dsagame.database.entities.Question;
import com.example.dsagame.database.entities.User;

@Database(entities = {User.class, Question.class}, version = 3) // Increment to version 3
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract QuestionDao questionDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "dsa_game_db")
                    .fallbackToDestructiveMigration() // Will wipe DB on version change
                    // For production, use proper migration instead:
                    // .addMigrations(MIGRATION_2_3)
                    .allowMainThreadQueries() // Only for demo!
                    .build();
        }
        return instance;
    }

    // Migration for production (optional)
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add new testCases column
            database.execSQL("ALTER TABLE Question ADD COLUMN testCases TEXT DEFAULT ''");
        }
    };
}
