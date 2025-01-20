package com.example.attendify.models;

public class AttendanceSummary {
    private String studentName;
    private String studentId;
    private String batch;
    private double overallPercentage;
    private double theoryPercentage;
    private double practicalPercentage;

    public AttendanceSummary(String studentId, double theoryPercentage, double practicalPercentage) {
        this.studentId = studentId;
        this.theoryPercentage = theoryPercentage;
        this.practicalPercentage = practicalPercentage;
        calculateOverallPercentage();
    }

    private void calculateOverallPercentage() {
        // Overall percentage is average of theory and practical
        this.overallPercentage = (theoryPercentage + practicalPercentage) / 2;
    }

    // Getters and setters
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }

    public double getOverallPercentage() { return overallPercentage; }

    public double getTheoryPercentage() { return theoryPercentage; }
    public void setTheoryPercentage(double theoryPercentage) {
        this.theoryPercentage = Math.round(theoryPercentage * 100.0) / 100.0;
        calculateOverallPercentage();
    }

    public double getPracticalPercentage() { return practicalPercentage; }
    public void setPracticalPercentage(double practicalPercentage) {
        this.practicalPercentage = Math.round(practicalPercentage * 100.0) / 100.0;
        calculateOverallPercentage();
    }
}
