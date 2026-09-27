package com.example.smartreminder;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReminderActivity extends AppCompatActivity {

    private TextView tvTitle, tvDesc, tvDatetime;
    private Button btnClose, btnSnooze;
    private int taskId;
    private String title, description, datetime;

    @SuppressLint("ScheduleExactAlarm")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        tvTitle = findViewById(R.id.rem_title);
        tvDesc = findViewById(R.id.rem_desc);
        tvDatetime = findViewById(R.id.rem_datetime);
        btnClose = findViewById(R.id.btnClose);
        btnSnooze = findViewById(R.id.btnSnooze);

        Intent intent = getIntent();
        if (intent != null) {
            taskId = intent.getIntExtra("taskId", 0);
            title = intent.getStringExtra("title");
            description = intent.getStringExtra("description");
            datetime = intent.getStringExtra("datetime");

            tvTitle.setText(title);
            tvDesc.setText(description);
            tvDatetime.setText(datetime);
        }

        btnClose.setOnClickListener(v -> finish());

        btnSnooze.setOnClickListener(v -> {
            // Snooze for 5 minutes
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, 5);

            Intent i = new Intent(ReminderActivity.this, ReminderBroadcast.class);
            i.putExtra("taskId", taskId);
            i.putExtra("title", title);
            i.putExtra("description", description);
            i.putExtra("datetime", datetime);

            PendingIntent pi;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pi = PendingIntent.getBroadcast(ReminderActivity.this, taskId, i,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            } else {
                pi = PendingIntent.getBroadcast(ReminderActivity.this, taskId, i, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (am != null) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                Toast.makeText(ReminderActivity.this, "Snoozed for 5 minutes", Toast.LENGTH_SHORT).show();
            }

            finish();
        });
    }
}
