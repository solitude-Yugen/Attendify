package com.example.attendify;

public class StudentAttendance {
    String name;
    int totalClasses;
    int presentClasses;

    public StudentAttendance(String name) {
        this.name = name;
        this.totalClasses = 0;
        this.presentClasses = 0;
    }

    public String getName() {
        return name;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public int getPresentClasses() {
        return presentClasses;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }

    public void setPresentClasses(int presentClasses) {
        this.presentClasses = presentClasses;
    }
}