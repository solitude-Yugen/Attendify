package com.example.attendify.models;

import java.util.Objects;

public class Student {
    private String name;
    private String studentId;
    private String enrollmentNo;
    private String address;
    private String email;
    private String contact;
    private String year;
    private String batch;
    private String course;
    private String status;
    private String rollNo;
    private boolean isPresent;

    public Student() {
        // Required empty constructor for Firebase
        this.status = "PENDING";
    }

    // Getter and setter for isPresent
    public boolean isPresent() {
        return isPresent;
    }

    public void setPresent(boolean present) {
        isPresent = present;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getEnrollmentNo() {
        return enrollmentNo;
    }

    public void setEnrollmentNo(String enrollmentNo) {
        this.enrollmentNo = enrollmentNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Student student = (Student) o;

        // Safe null checks for each field using Objects.equals
        return Objects.equals(studentId, student.studentId) &&
                Objects.equals(name, student.name) &&
                Objects.equals(rollNo, student.rollNo) &&
                Objects.equals(batch, student.batch) &&
                Objects.equals(status, student.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, name, rollNo, batch, status);
    }
}
