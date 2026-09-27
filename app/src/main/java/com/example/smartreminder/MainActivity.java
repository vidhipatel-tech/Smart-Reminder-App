package com.example.smartreminder;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskMenuClickListener {
    private static final String TAG = "MainActivity";
    private static final int REQ_POST_NOTIFICATIONS = 1001;

    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private TaskAdapter taskAdapter;
    private DatabaseHelper dbHelper;
    private ArrayList<Task> taskList;
    private FloatingActionButton fabAdd;

    private TextView taskToDoHeading;
    private TextView logoText; // title inside toolbar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewTasks);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        fabAdd = findViewById(R.id.fabAdd);
        taskToDoHeading = findViewById(R.id.taskToDoHeading);
        logoText = findViewById(R.id.logoText); // from custom_toolbar.xml

        dbHelper = new DatabaseHelper(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestNotificationPermissionIfNeeded();
        loadTasks();

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });
        Log.d(TAG, "onCreate done");
    }
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_POST_NOTIFICATIONS);
            }
        }
    }
    private void loadTasks() {
        taskList = dbHelper.getAllTasks();

        if (taskList == null || taskList.isEmpty()) {
            // show empty screen
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            taskToDoHeading.setVisibility(View.GONE);
            if (logoText != null) logoText.setVisibility(View.VISIBLE);
        } else {
            // show list
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            taskToDoHeading.setVisibility(View.VISIBLE);

            taskAdapter = new TaskAdapter(taskList, this);
            recyclerView.setAdapter(taskAdapter);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }
    @Override
    public void onMenuClick(View view, Task task) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.task_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> handleMenuClick(item, task));
        popupMenu.show();
    }
    private boolean handleMenuClick(MenuItem item, Task task) {
        if (item.getItemId() == R.id.action_update) {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            intent.putExtra("taskId", task.getId());
            intent.putExtra("title", task.getTitle());
            intent.putExtra("description", task.getDescription());
            intent.putExtra("deadline", task.getDeadline());
            intent.putExtra("time", task.getTime());
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            dbHelper.deleteTask(task.getId());
            loadTasks();
            return true;
        }
        return false;
    }
    // handle permission result (optional logging)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "POST_NOTIFICATIONS granted");
            } else {
                Log.d(TAG, "POST_NOTIFICATIONS denied - notifications may not show on Android 13+");
            }
        }
    }
}
