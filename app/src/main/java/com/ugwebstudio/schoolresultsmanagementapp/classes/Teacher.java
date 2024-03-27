package com.ugwebstudio.schoolresultsmanagementapp.classes;

public class Teacher {
    private String name;
    private String subject;

    private String phone;
    private String teacherClass;
    private String email;

    private String role= "teacher";

    public Teacher() {
    }

    public Teacher(String name, String email, String phone, String subject, String teacherClass, String role) {
        this.name=name;
        this.email=email;
        this.phone=phone;
        this.subject=subject;
        this.teacherClass=teacherClass;
        this.role = role;

    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getSubject() {
        return subject;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTeacherClass() {
        return teacherClass;
    }

    public void setTeacherClass(String teacherClass) {
        this.teacherClass = teacherClass;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

