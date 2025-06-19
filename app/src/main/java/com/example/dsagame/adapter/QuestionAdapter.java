package com.example.dsagame.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dsagame.R;
import com.example.dsagame.database.entities.Question;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.VH> {

    public interface OnSolve {
        void solve(Question q);
    }

    private List<Question> questionList;
    private final OnSolve listener;

    public QuestionAdapter(List<Question> questionList, OnSolve listener) {
        this.questionList = questionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Question q = questionList.get(position);
        holder.title.setText(q.title);
        holder.topic.setText(q.topic);
        holder.difficulty.setText(q.difficulty);
        holder.solveBtn.setText(q.isSolved ? "Solved" : "Solve");
        holder.solveBtn.setEnabled(!q.isSolved);

        // Set difficulty-specific styling with drawable backgrounds
        setDifficultyStyle(holder, q.difficulty);

        holder.solveBtn.setOnClickListener(view -> {
            if (!q.isSolved) {
                listener.solve(q);
            }
        });
    }

    private void setDifficultyStyle(VH holder, String difficulty) {
        int bgResId;
        int textColorResId;

        switch (difficulty) {
            case "Medium":
                bgResId = R.drawable.difficulty_medium;
                textColorResId = R.color.medium_text;
                break;
            case "Hard":
                bgResId = R.drawable.difficulty_hard;
                textColorResId = R.color.hard_text;
                break;
            default: // Easy
                bgResId = R.drawable.difficulty_easy;
                textColorResId = R.color.easy_text;
        }

        // Set background and text color
        holder.difficulty.setBackgroundResource(bgResId);
        holder.difficulty.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), textColorResId)
        );
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public void setQuestions(List<Question> newQuestions) {
        this.questionList = newQuestions;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, topic, difficulty;
        Button solveBtn;

        public VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            topic = itemView.findViewById(R.id.topic);
            difficulty = itemView.findViewById(R.id.difficulty);
            solveBtn = itemView.findViewById(R.id.solveBtn);
        }
    }
}