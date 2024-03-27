package com.ugwebstudio.schoolresultsmanagementapp.classes;

public class Student {
    private String name;


    private String phone;
    private String StudentClass;
    private String StudentParent;
    private String StudentDOB;
    private String email;

    private String address;
    private String parentPhone;

    private String academicYear;


    public Student() {
    }


    public Student(String name, String phone, String studentClass, String studentParent, String studentDOB, String email, String address, String parentPhone, String academicYear) {
        this.name = name;
        this.phone = phone;
        StudentClass = studentClass;
        StudentParent = studentParent;
        StudentDOB = studentDOB;
        this.email = email;
        this.address = address;
        this.parentPhone = parentPhone;
        this.academicYear = academicYear;
    }

    public String getAddress() {
        return address;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStudentClass() {
        return StudentClass;
    }

    public void setStudentClass(String studentClass) {
        StudentClass = studentClass;
    }

    public String getStudentParent() {
        return StudentParent;
    }

    public void setStudentParent(String studentParent) {
        StudentParent = studentParent;
    }

    public String getStudentDOB() {
        return StudentDOB;
    }

    public void setStudentDOB(String studentDOB) {
        StudentDOB = studentDOB;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

