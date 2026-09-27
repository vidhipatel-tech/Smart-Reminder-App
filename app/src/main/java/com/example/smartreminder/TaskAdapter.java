package com.example.smartreminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {

    public interface OnTaskMenuClickListener {
        void onMenuClick(View anchor, Task task);
    }

    private final ArrayList<Task> tasks;
    private final OnTaskMenuClickListener listener;

    public TaskAdapter(ArrayList<Task> tasks, OnTaskMenuClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Task t = tasks.get(position);
        holder.title.setText(t.getTitle());
        holder.desc.setText(t.getDescription());
        holder.deadline.setText("Due: " + t.getDate());
        holder.time.setText("Time: " + t.getTime());

        holder.itemView.setOnClickListener(v -> {
            // open edit
            v.getContext().startActivity(
                    new android.content.Intent(v.getContext(), AddTaskActivity.class)
                            .putExtra("taskId", t.getId())
                            .putExtra("title", t.getTitle())
                            .putExtra("description", t.getDescription())
                            .putExtra("deadline", t.getDate())
                            .putExtra("time", t.getTime())
            );
        });

        holder.menuBtn.setOnClickListener(v -> {
            if (listener != null) listener.onMenuClick(v, t);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, desc, deadline, time;
        ImageButton menuBtn;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewTitle);
            desc = itemView.findViewById(R.id.textViewDescription);
            deadline = itemView.findViewById(R.id.textViewDeadline);
            time = itemView.findViewById(R.id.textViewTime);
            menuBtn = itemView.findViewById(R.id.buttonMenu);
        }
    }
}
