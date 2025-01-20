package com.example.attendify.models;

public class Faculty {
    private String facultyId;
    private String name;
    private String email;
    private String username;
    private String password;
    private String department;
    private String contact;

    public Faculty() {}

    public Faculty(String facultyId, String name, String email, String username,
                   String password, String department, String contact) {
        this.facultyId = facultyId;
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.department = department;
        this.contact = contact;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
