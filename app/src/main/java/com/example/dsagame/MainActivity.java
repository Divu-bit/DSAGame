package com.example.dsagame;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dsagame.activities.CodeEditorActivity;
import com.example.dsagame.activities.LeaderboardActivity;
import com.example.dsagame.adapter.QuestionAdapter;
import com.example.dsagame.database.AppDatabase;
import com.example.dsagame.database.entities.Question;
import com.example.dsagame.database.entities.User;
import com.example.dsagame.models.LevelUtils;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_EDITOR = 1001;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 102;
    private static final int REQUEST_EXACT_ALARM_PERMISSION = 103;

    RecyclerView recyclerView;
    QuestionAdapter adapter;
    List<Question> allQuestions = new ArrayList<>();
    List<Question> filteredQuestions = new ArrayList<>();
    User user;
    AppDatabase db;

    // UI elements
    TextView xpView, levelView, streakView, dailyChallengeTitle;
    ProgressBar progressBar;
    Button dailyChallengeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create notification channel
        createNotificationChannel();

        // Initialize UI elements
        recyclerView = findViewById(R.id.recyclerView);
        xpView = findViewById(R.id.xpView);
        levelView = findViewById(R.id.levelView);
        streakView = findViewById(R.id.streakView);
        progressBar = findViewById(R.id.progressBar);
        dailyChallengeTitle = findViewById(R.id.dailyChallengeTitle);
        dailyChallengeBtn = findViewById(R.id.dailyChallengeBtn);

        Spinner topicSpinner = findViewById(R.id.topicSpinner);
        db = AppDatabase.getInstance(this);

        initUser();
        initQuestions();
        setupTopicSpinner(topicSpinner);
        updateUserDisplay();

        // Setup daily challenge
        setupDailyChallenge();

        // Setup navigation to leaderboard
        setupNavigation();

        // Schedule notifications with permission checks
        scheduleNotificationsWithPermissions();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Daily Challenges";
            String description = "Notifications for daily DSA challenges";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("daily_challenge", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setupNavigation() {
        Button leaderboardBtn = findViewById(R.id.leaderboardBtn);
        leaderboardBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
            startActivity(intent);
        });
    }

    private void scheduleNotificationsWithPermissions() {
        // Check and request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
                return;
            }
        }

        // Check and request exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivityForResult(intent, REQUEST_EXACT_ALARM_PERMISSION);
                return;
            }
        }

        // If all permissions are granted, schedule the notification
        scheduleDailyNotification();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDITOR && resultCode == RESULT_OK) {
            // Question was solved successfully
            Log.d(TAG, "Question solved - refreshing data");
            refreshQuestionData();
            // Refresh daily challenge in case it was solved
            setupDailyChallenge();
        }
        else if (requestCode == REQUEST_EXACT_ALARM_PERMISSION) {
            // Retry scheduling after returning from permission request
            scheduleNotificationsWithPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Notification permission granted, continue with scheduling
                scheduleNotificationsWithPermissions();
            } else {
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void refreshQuestionData() {
        // Refresh from database
        allQuestions = db.questionDao().getAll();
        filteredQuestions = new ArrayList<>(allQuestions);

        // Update adapter
        adapter.setQuestions(filteredQuestions);

        // Update user data
        user = db.userDao().getUser();
        updateUserDisplay();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkStreak();
    }

    private void setupDailyChallenge() {
        Question challenge = getTodaysChallenge();
        if (challenge != null) {
            dailyChallengeTitle.setText("Daily: " + challenge.title);
            dailyChallengeBtn.setOnClickListener(v -> launchChallenge(challenge));
            // Make sure the card is visible
            findViewById(R.id.dailyChallengeCard).setVisibility(View.VISIBLE);
        } else {
            // Hide the daily challenge card if no challenge available
            findViewById(R.id.dailyChallengeCard).setVisibility(View.GONE);
        }
    }

    private Question getTodaysChallenge() {
        SharedPreferences prefs = getSharedPreferences("challenges", MODE_PRIVATE);
        String today = LocalDate.now().toString();
        String lastDate = prefs.getString("last_challenge", "");

        // If we already have a challenge for today
        if (today.equals(lastDate)) {
            int challengeId = prefs.getInt("challenge_id", -1);
            if (challengeId != -1) {
                return findQuestionById(challengeId);
            }
        }

        // Select new challenge - only unsolved medium difficulty questions
        List<Question> candidates = new ArrayList<>();
        for (Question q : allQuestions) {
            if ("Medium".equals(q.difficulty) && !q.isSolved) {
                candidates.add(q);
            }
        }

        if (!candidates.isEmpty()) {
            // Pick a random question from candidates
            Random rand = new Random();
            Question challenge = candidates.get(rand.nextInt(candidates.size()));

            // Save challenge to preferences
            prefs.edit()
                    .putString("last_challenge", today)
                    .putInt("challenge_id", challenge.id)
                    .apply();

            return challenge;
        }
        return null; // No suitable challenge found
    }

    private void scheduleDailyNotification() {
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(this, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Set for 9 AM daily
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // If it's already past 9 AM, schedule for next day
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            // Use setAndAllowWhileIdle for better battery optimization
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                );
            }

            Log.d(TAG, "Daily notification scheduled");
        } catch (SecurityException e) {
            Log.e(TAG, "Alarm scheduling failed", e);
            Toast.makeText(this, "Alarm permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private Question findQuestionById(int id) {
        for (Question q : allQuestions) {
            if (q.id == id) {
                return q;
            }
        }
        return null;
    }

    private void launchChallenge(Question challenge) {
        if (challenge != null) {
            launchCodeEditor(challenge);
        } else {
            Toast.makeText(this, "Challenge not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTopicSpinner(Spinner spinner) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.topics_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTopic = parent.getItemAtPosition(position).toString();
                filterQuestions(selectedTopic);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filterQuestions("All");
            }
        });
    }

    private void filterQuestions(String topic) {
        if ("All".equals(topic)) {
            filteredQuestions = new ArrayList<>(allQuestions);
        } else {
            filteredQuestions = new ArrayList<>();
            for (Question q : allQuestions) {
                if (q.topic.equals(topic)) {
                    filteredQuestions.add(q);
                }
            }
        }
        adapter.setQuestions(filteredQuestions);
    }

    private void initUser() {
        user = db.userDao().getUser();
        if (user == null) {
            user = new User();
            user.xp = 0;
            user.level = 1;
            user.streak = 0;
            user.lastActiveDate = "";
            db.userDao().insertUser(user);
        }
    }

    private void initQuestions() {
        allQuestions = db.questionDao().getAll();
        if (allQuestions.isEmpty()) {
            List<Question> list = Arrays.asList(
                    create("Two Sum", "Arrays", "Easy"),
                    create("Reverse String", "Strings", "Easy"),
                    create("Container With Most Water", "Arrays", "Medium"),
                    create("Longest Substring Without Repeating Characters", "Strings", "Medium"),
                    create("Trapping Rain Water", "Arrays", "Hard")
            );
            db.questionDao().insertAll(list);
            allQuestions = db.questionDao().getAll();

            // Verify test cases were added
            for (Question q : allQuestions) {
                Log.d(TAG, "Question: " + q.title + " | Test Cases: " + q.testCases);
            }
        }
        filteredQuestions = new ArrayList<>(allQuestions);

        adapter = new QuestionAdapter(filteredQuestions, new QuestionAdapter.OnSolve() {
            @Override
            public void solve(Question q) {
                launchCodeEditor(q);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void launchCodeEditor(Question question) {
        Intent intent = new Intent(MainActivity.this, CodeEditorActivity.class);
        // Pass entire question object
        intent.putExtra("question", question);
        startActivityForResult(intent, REQUEST_CODE_EDITOR);
    }

    private Question create(String title, String topic, String difficulty) {
        Question q = new Question();
        q.title = title;
        q.topic = topic;
        q.difficulty = difficulty;
        q.isSolved = false;

        // Add test cases based on question
        q.testCases = generateTestCases(title);
        return q;
    }

    private String generateTestCases(String title) {
        switch (title) {
            case "Two Sum":
                return "{\"inputs\":[[2,7,11,15,9],[3,2,4,6]],\"outputs\":[\"[0,1]\",\"[1,2]\"]}";

            case "Reverse String":
                return "{\"inputs\":[[\"hello\"]],\"outputs\":[\"olleh\"]}";

            case "Container With Most Water":
                return "{\"inputs\":[[1,8,6,2,5,4,8,3,7]],\"outputs\":[\"49\"]}";

            case "Longest Substring Without Repeating Characters":
                return "{\"inputs\":[[\"abcabcbb\"]],\"outputs\":[\"3\"]}";

            case "Trapping Rain Water":
                return "{\"inputs\":[[0,1,0,2,1,0,1,3,2,1,2,1]],\"outputs\":[\"6\"]}";

            default:
                return "{\"inputs\":[[\"sample\"]],\"outputs\":[\"elpmas\"]}";
        }
    }

    private void handleQuestionSolved(Question question) {
        if (!question.isSolved) {
            question.isSolved = true;
            db.questionDao().updateQuestion(question);

            // Award XP based on difficulty
            int xpEarned = question.getXpValue();
            user.xp += xpEarned;
            user.level = LevelUtils.getLevel(user.xp);

            // Update streak
            updateStreak();

            db.userDao().updateUser(user);
            adapter.notifyDataSetChanged();
            updateUserDisplay();

            // TODO: Add XP gain animation here
        }
    }

    private void updateStreak() {
        String today = LocalDate.now().toString();
        if (!today.equals(user.lastActiveDate)) {
            // Check if consecutive day
            if (!user.lastActiveDate.isEmpty()) {
                LocalDate lastDate = LocalDate.parse(user.lastActiveDate);
                if (lastDate.plusDays(1).isEqual(LocalDate.now())) {
                    user.streak++;
                } else if (!lastDate.isEqual(LocalDate.now())) {
                    // Reset streak if not consecutive
                    user.streak = 1;
                }
            } else {
                user.streak = 1;
            }
            user.lastActiveDate = today;
        }
    }

    private void checkStreak() {
        String today = LocalDate.now().toString();
        if (!today.equals(user.lastActiveDate)) {
            if (!user.lastActiveDate.isEmpty()) {
                LocalDate lastDate = LocalDate.parse(user.lastActiveDate);
                if (!lastDate.plusDays(1).isEqual(LocalDate.now())) {
                    user.streak = 0; // Reset streak if missed a day
                }
            }
            user.lastActiveDate = today;
            db.userDao().updateUser(user);
            updateUserDisplay();
        }
    }

    private void updateUserDisplay() {
        xpView.setText("XP: " + user.xp);
        levelView.setText("Level: " + user.level);
        streakView.setText("🔥 " + user.streak + " days");
        progressBar.setProgress(LevelUtils.getCurrentLevelProgress(user.xp));
    }
}