// AttendanceHistoryRecord.java
package com.example.attendify;

import com.example.attendify.models.Subject;

public class AttendanceHistoryRecord {
    private String date;
    private boolean present;
    private Subject subject;
    private String status;

    public AttendanceHistoryRecord(String date, boolean present, Subject subject) {
        this.date = date;
        this.present = present;
        this.subject = subject;
        this.status = present ? "PRESENT" : "ABSENT";
    }

    public String getDate() { return date; }
    public boolean isPresent() { return present; }
    public Subject getSubject() { return subject; }
    public String getStatus() { return status; }
}
