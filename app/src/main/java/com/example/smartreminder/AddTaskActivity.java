package com.example.smartreminder;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {
    private static final String TAG = "AddTaskActivity";
    private EditText etTitle, etDescription, etDeadline, etTime;
    private Button btnSave;
    private DatabaseHelper dbHelper;
    private int editingTaskId = -1;
    private Calendar pickerCalendar = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        requestPermissions();
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDeadline = findViewById(R.id.etDeadline);
        etTime = findViewById(R.id.etTime);
        btnSave = findViewById(R.id.btnSave);
        dbHelper = new DatabaseHelper(this);
        Intent i = getIntent();
        if (i != null && i.hasExtra("taskId")) {
            editingTaskId = i.getIntExtra("taskId", -1);
            etTitle.setText(i.getStringExtra("title"));
            etDescription.setText(i.getStringExtra("description"));
            etDeadline.setText(i.getStringExtra("deadline"));
            etTime.setText(i.getStringExtra("time"));
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                pickerCalendar.setTime(sdf.parse(etDeadline.getText().toString().trim() + " " + etTime.getText().toString().trim()));
            } catch (Exception ignored) {}
        }
        etDeadline.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveTask());
    }
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }
    private void showDatePicker() {
        int year = pickerCalendar.get(Calendar.YEAR);
        int month = pickerCalendar.get(Calendar.MONTH);
        int day = pickerCalendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, y, m, d) -> {
                    pickerCalendar.set(Calendar.YEAR, y);
                    pickerCalendar.set(Calendar.MONTH, m);
                    pickerCalendar.set(Calendar.DAY_OF_MONTH, d);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etDeadline.setText(sdf.format(pickerCalendar.getTime()));
                }, year, month, day);

        datePicker.show();
    }
    private void showTimePicker() {
        int hour = pickerCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = pickerCalendar.get(Calendar.MINUTE);
        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, h, m) -> {
                    pickerCalendar.set(Calendar.HOUR_OF_DAY, h);
                    pickerCalendar.set(Calendar.MINUTE, m);
                    pickerCalendar.set(Calendar.SECOND, 0);
                    pickerCalendar.set(Calendar.MILLISECOND, 0);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    etTime.setText(sdf.format(pickerCalendar.getTime()));
                }, hour, minute, true);

        timePicker.show();
    }
    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String date = etDeadline.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please fill title, date and time", Toast.LENGTH_SHORT).show();
            return;
        }
        long rowId;
        if (editingTaskId == -1) {
            rowId = dbHelper.addTask(title, desc, date, time);
        } else {
            dbHelper.updateTask(editingTaskId, title, desc, date, time);
            rowId = editingTaskId;
        }
        boolean scheduled = scheduleAlarmForTask((int) rowId, title, desc, date, time);
        if (scheduled) {
            Toast.makeText(this, "Reminder scheduled", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to schedule reminder", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
    @SuppressLint("ScheduleExactAlarm")
    private boolean scheduleAlarmForTask(int taskId, String title, String description, String date, String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(date + " " + time));
        } catch (ParseException e) {
            cal = (Calendar) pickerCalendar.clone();
        }
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        Intent intent = new Intent(this, ReminderBroadcast.class);
        intent.putExtra("taskId", taskId);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("date", date);
        intent.putExtra("time", time);
        PendingIntent pi;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pi = PendingIntent.getBroadcast(this, taskId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pi = PendingIntent.getBroadcast(this, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am == null) return false;
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        Log.d(TAG, "Alarm scheduled for taskId=" + taskId + " at " + cal.getTime());
        return true;
    }
}