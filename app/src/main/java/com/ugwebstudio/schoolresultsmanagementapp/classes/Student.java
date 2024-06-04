package com.ugwebstudio.schoolresultsmanagementapp.classes;

public class Student {
    private String name;
   private String id;
    private String phone;
    private String studentClass;
    private String studentParent;
    private String studentDOB;
    private String email;

    private String address;
    private String parentPhone;

    private String academicYear;
    private  String imageUrl;

    public Student() {
    }

    public Student(String name, String id, String phone, String studentClass, String studentParent, String studentDOB, String email, String address, String parentPhone, String academicYear, String imageUrl) {
        this.name = name;
        this.id = id;
        this.phone = phone;
        this.studentClass = studentClass;
        this.studentParent = studentParent;
        this.studentDOB = studentDOB;
        this.email = email;
        this.address = address;
        this.parentPhone = parentPhone;
        this.academicYear = academicYear;
        this.imageUrl = imageUrl;
    }

    public Student(String name, String id, String phone, String studentClass, String studentParent, String studentDOB, String email, String address, String parentPhone, String academicYear) {
        this.name = name;
        this.id = id;
        this.phone = phone;
        this.studentClass = studentClass;
        this.studentParent = studentParent;
        this.studentDOB = studentDOB;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
        return studentClass;
    }

    public void setStudentClass(String studentClass) {
        this.studentClass = studentClass;
    }

    public String getStudentParent() {
        return studentParent;
    }

    public void setStudentParent(String studentParent) {
        this.studentParent = studentParent;
    }

    public String getStudentDOB() {
        return studentDOB;
    }

    public void setStudentDOB(String studentDOB) {
        this.studentDOB = studentDOB;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

