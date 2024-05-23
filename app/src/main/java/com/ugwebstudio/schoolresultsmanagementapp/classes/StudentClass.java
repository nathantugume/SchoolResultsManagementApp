package com.ugwebstudio.schoolresultsmanagementapp.classes;

public class StudentClass {

    public String email;
    private String className;
    private String Streams;
    private String level;
    private String Subjects;

    public StudentClass() {
    }

    public StudentClass(String className, String streams, String level, String subjects) {
        this.className = className;
        Streams = streams;
        this.level = level;
        Subjects = subjects;
    }

    public StudentClass(String email, String className, String streams, String level, String subjects) {
        this.email = email;
        this.className = className;
        Streams = streams;
        this.level = level;
        Subjects = subjects;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getStreams() {
        return Streams;
    }

    public void setStreams(String streams) {
        Streams = streams;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSubjects() {
        return Subjects;
    }

    public void setSubjects(String subjects) {
        Subjects = subjects;
    }
}
