// app/java/com/example/dsagame/adapter/LeaderboardAdapter.java
package com.example.dsagame.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dsagame.R;
import com.example.dsagame.database.entities.User;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<User> users = new ArrayList<>();

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user, position + 1);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView rankView, usernameView, xpView, levelView;

        ViewHolder(View itemView) {
            super(itemView);
            rankView = itemView.findViewById(R.id.rank);
            usernameView = itemView.findViewById(R.id.username);
            xpView = itemView.findViewById(R.id.xp);
            levelView = itemView.findViewById(R.id.level);
        }

        void bind(User user, int rank) {
            // Handle anonymity - don't show user IDs
            usernameView.setText("User " + rank);

            rankView.setText("#" + rank);
            xpView.setText(user.xp + " XP");
            levelView.setText("Level " + user.level);

            // Set medal colors for top 3
            if (rank == 1) {
                rankView.setBackgroundResource(R.drawable.gold_medal);
            } else if (rank == 2) {
                rankView.setBackgroundResource(R.drawable.silver_medal);
            } else if (rank == 3) {
                rankView.setBackgroundResource(R.drawable.bronze_medal);
            } else {
                rankView.setBackground(null);
            }
        }
    }
}