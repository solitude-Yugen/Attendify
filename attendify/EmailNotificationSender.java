package com.example.attendify;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.google.firebase.database.*;
import java.util.*;

public class EmailNotificationSender {
    private static final String TAG = "EmailNotificationSender";
    private final DatabaseReference databaseRef;
    private final Context context;

    public EmailNotificationSender(Context context, DatabaseReference databaseRef) {
        this.context = context;
        this.databaseRef = databaseRef;
    }

    public void sendDefaulterNotifications(Map<String, StudentAttendance> attendanceData,
                                           String subject,
                                           String startDate,
                                           String endDate) {
        // Get list of defaulters (below 75% attendance)
        List<String> defaulterNames = new ArrayList<>();
        for (Map.Entry<String, StudentAttendance> entry : attendanceData.entrySet()) {
            StudentAttendance attendance = entry.getValue();
            float percentage = attendance.getTotalClasses() == 0 ? 0 :
                    (attendance.getPresentClasses() * 100.0f) / attendance.getTotalClasses();
            if (percentage < 75) {
                defaulterNames.add(entry.getKey());
            }
        }

        if (defaulterNames.isEmpty()) {
            Log.d(TAG, "No defaulters found");
            return;
        }

        // Query student database to get email addresses
        databaseRef.child("students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> recipientEmails = new ArrayList<>();
                StringBuilder emailBody = new StringBuilder();

                // Prepare email content
                emailBody.append("Dear Student,\n\n");
                emailBody.append("This is to inform you that your attendance in ").append(subject)
                        .append(" for the period ").append(startDate).append(" to ")
                        .append(endDate).append(" is below the required 75%.\n\n");
                emailBody.append("Please ensure regular attendance in upcoming classes. ")
                        .append("If you have any concerns, please contact your subject teacher.\n\n");
                emailBody.append("Best regards,\nAttendify System");

                // Find matching student records and collect email addresses
                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    String studentName = studentSnapshot.child("Name").getValue(String.class);
                    if (studentName != null && defaulterNames.contains(studentName)) {
                        String email = studentSnapshot.child("Email").getValue(String.class);
                        if (email != null && !email.isEmpty()) {
                            recipientEmails.add(email);
                        }
                    }
                }

                if (!recipientEmails.isEmpty()) {
                    sendEmail(recipientEmails,
                            "Low Attendance Alert - " + subject,
                            emailBody.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error fetching student data: " + databaseError.getMessage());
            }
        });
    }

    private void sendEmail(List<String> recipients, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this

        intent.putExtra(Intent.EXTRA_EMAIL, recipients.toArray(new String[0]));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            context.startActivity(Intent.createChooser(intent, "Send email using..."));
        } catch (Exception e) {
            Log.e(TAG, "Error sending email: " + e.getMessage());
        }
    }
}