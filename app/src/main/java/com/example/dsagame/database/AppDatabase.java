package com.example.dsagame.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.dsagame.database.entities.Question;
import com.example.dsagame.database.entities.User;

@Database(entities = {User.class, Question.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract QuestionDao questionDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "dsa_game_db")
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    // Migration from version 2 to 3 (existing)
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add new testCases column
            database.execSQL("ALTER TABLE Question ADD COLUMN testCases TEXT DEFAULT ''");
        }
    };

    // Migration from version 3 to 4 (new)
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add firebaseUid column to User table
            database.execSQL("ALTER TABLE User ADD COLUMN firebaseUid TEXT DEFAULT ''");
        }
    };
}