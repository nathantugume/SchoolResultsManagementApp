package com.ugwebstudio.schoolresultsmanagementapp.classes;

public class GradingScale {
    private int from;
    private int to;
    private String grade;
    private String level;

    private String id;

    public GradingScale() {
        // Firestore requires an empty constructor
    }

    public GradingScale(int from, int to, String grade, String level, String id) {
        this.from = from;
        this.to = to;
        this.grade = grade;
        this.level = level;
        this.id = id;
    }

    // Getters and Setters
    public int getFrom() { return from; }
    public void setFrom(int from) { this.from = from; }

    public int getTo() { return to; }
    public void setTo(int to) { this.to = to; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
