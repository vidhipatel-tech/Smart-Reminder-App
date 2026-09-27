package com.example.smartreminder;

public class Task {
    private final int id;
    private String title;
    private String description;
    private String date; // dd/MM/yyyy
    private String time; // HH:mm

    public Task(int id, String title, String description, String date, String time) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getTime() { return time; }

    public void setTitle(String t) { this.title = t; }
    public void setDescription(String d) { this.description = d; }
    public void setDate(String d) { this.date = d; }
    public void setTime(String t) { this.time = t; }

    // combined deadline for legacy support: "dd/MM/yyyy HH:mm"
    public String getDeadline() {
        if (date == null) return "";
        if (time == null || time.isEmpty()) return date;
        return date + " " + time;
    }
}
