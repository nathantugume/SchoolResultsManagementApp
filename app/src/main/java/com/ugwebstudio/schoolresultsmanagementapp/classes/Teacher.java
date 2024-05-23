package com.ugwebstudio.schoolresultsmanagementapp.classes;

public class Teacher {
    private String id;
    private String name;
    private String subject;

    private String phone;
    private String teacherClass;
    private String email;

    private String role= "teacher";

    public Teacher() {
        //empty for use by firebase
    }



    //teacher constructor

    public Teacher(String id, String name, String subject, String phone, String teacherClass, String email, String role) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.phone = phone;
        this.teacherClass = teacherClass;
        this.email = email;
        this.role = role;
    }

    public Teacher(String name, String email, String phone, String subject, String teacherClass, String teacher) {
        this.name = name;
        this.subject = subject;
        this.phone = phone;
        this.teacherClass = teacherClass;
        this.email = email;
        this.role = role;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

