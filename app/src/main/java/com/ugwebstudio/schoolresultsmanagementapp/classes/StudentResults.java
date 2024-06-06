package com.ugwebstudio.schoolresultsmanagementapp.classes;

public class StudentResults {
    private String student;
    private String studentClass;
    private String resultType;
    private String term;
    private String class_name;
    private String studentId;
    private String subject;
    private int marks;
    private String studentName;
    private String name;

    public StudentResults() {
    }



    public StudentResults(String student, String studentClass, String resultType, String term, String class_name, String studentId, String subject, int marks) {
        this.student = student;
        this.studentClass = studentClass;
        this.resultType = resultType;
        this.term = term;
        this.class_name = class_name;
        this.studentId = studentId;
        this.subject = subject;
        this.marks = marks;
    }

    public StudentResults(String student, String studentClass, String resultType, String term, String class_name, String studentId, String subject, int marks, String name) {
        this.student = student;
        this.studentClass = studentClass;
        this.resultType = resultType;
        this.term = term;
        this.class_name = class_name;
        this.studentId = studentId;
        this.subject = subject;
        this.marks = marks;
        this.name = name;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public String getStudentClass() {
        return studentClass;
    }



    public void setStudentClass(String studentClass) {
        this.studentClass = studentClass;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }
}
