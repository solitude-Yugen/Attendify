package com.example.attendify.models;

public class Subject {
    private String year;
    private String course;
    private String semester;
    private String subjectName;
    private String subjectId;

    public Subject() {
        // Required empty constructor for Firebase
    }

    public Subject(String year, String course, String semester, String subjectName, String subjectId) {
        this.year = year;
        this.course = course;
        this.semester = semester;
        this.subjectName = subjectName;
        this.subjectId = subjectId;
    }

    // Getters and setters
    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }
}
