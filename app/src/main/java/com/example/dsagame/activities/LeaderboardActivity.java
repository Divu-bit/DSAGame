// app/java/com/example/dsagame/activities/LeaderboardActivity.java
package com.example.dsagame.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.dsagame.R;
import com.example.dsagame.adapter.LeaderboardAdapter;
import com.example.dsagame.database.AppDatabase;
import com.example.dsagame.database.entities.User;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyStateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        recyclerView = findViewById(R.id.leaderboardRecycler);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        emptyStateView = findViewById(R.id.emptyStateView);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter();
        recyclerView.setAdapter(adapter);

        // Setup pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadLeaderboard);

        // Load initial data
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        swipeRefreshLayout.setRefreshing(true);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(LeaderboardActivity.this);
            List<User> users = db.userDao().getLeaderboard();

            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);

                if (users == null || users.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyStateView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyStateView.setVisibility(View.GONE);
                    adapter.setUsers(users);
                }
            });
        }).start();
    }
}