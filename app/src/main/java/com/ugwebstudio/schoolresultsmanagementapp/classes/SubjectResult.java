package com.ugwebstudio.schoolresultsmanagementapp.classes;

import java.util.List;

public class SubjectResult {
    private String subject;
    private List<Double> scores;

    public SubjectResult() {
        // Default constructor required for calls to DataSnapshot.getValue(SubjectResult.class)
    }

    public SubjectResult(String subject, List<Double> scores) {
        this.subject = subject;
        this.scores = scores;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<Double> getScores() {
        return scores;
    }

    public void setScores(List<Double> scores) {
        this.scores = scores;
    }
}