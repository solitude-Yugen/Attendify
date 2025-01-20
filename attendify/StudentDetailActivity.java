package com.example.attendify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import com.example.attendify.models.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class StudentDetailActivity extends AppCompatActivity {
    private TextView studentName, studentRollNo, studentBatch, studentCourse, studentYear;
    private TextView totalClasses, classesAttended, attendancePercentage;
    private RecyclerView attendanceHistoryRecyclerView;
    private DatabaseReference databaseRef;
    private String studentId;
    private AttendanceHistoryAdapter adapter;
    private List<AttendanceHistoryRecord> attendanceRecords;
    private Student currentStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        // Get data from intent
        studentId = getIntent().getStringExtra("studentId");
        String year = getIntent().getStringExtra("year");
        String batch = getIntent().getStringExtra("batch");
        String rollNo = getIntent().getStringExtra("rollNo");

        // Create current student object
        currentStudent = new Student();
        currentStudent.setName(studentId);
        currentStudent.setBatch(batch);
        currentStudent.setRollNo(rollNo);
        currentStudent.setYear(year);

        initializeViews();
        setupInitialData();

        // Initialize Firebase reference
        databaseRef = FirebaseDatabase.getInstance().getReference()
                .child("attendance")
                .child("2024_2025")
                .child(year);

        loadStudentData();
    }

    private void initializeViews() {
        studentName = findViewById(R.id.studentName);
        studentRollNo = findViewById(R.id.studentRollNo);
        studentBatch = findViewById(R.id.studentBatch);
        studentCourse = findViewById(R.id.studentCourse);
        studentYear = findViewById(R.id.studentYear);
        totalClasses = findViewById(R.id.totalClasses);
        classesAttended = findViewById(R.id.classesAttended);
        attendancePercentage = findViewById(R.id.attendancePercentage);
        attendanceHistoryRecyclerView = findViewById(R.id.attendanceHistoryRecyclerView);
    }

    private void setupInitialData() {
        studentName.setText(currentStudent.getName());
        studentBatch.setText(currentStudent.getBatch());
        studentRollNo.setText(currentStudent.getRollNo());
        studentYear.setText(currentStudent.getYear());

        attendanceRecords = new ArrayList<>();
        adapter = new AttendanceHistoryAdapter(attendanceRecords);
        attendanceHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        attendanceHistoryRecyclerView.setAdapter(adapter);
    }

    private void loadStudentData() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalClassCount = 0;
                int attendedClassCount = 0;
                attendanceRecords.clear();

                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot attendanceSnapshot : dateSnapshot.getChildren()) {
                        DataSnapshot studentsNode = attendanceSnapshot.child("students");
                        String subjectName = attendanceSnapshot.child("subjectName").getValue(String.class);

                        // Create subject object with direct information from attendance record
                        Subject subject = new Subject();
                        subject.setSubjectName(subjectName != null ? subjectName : "Unknown Subject");
                        subject.setCourse(attendanceSnapshot.child("course").getValue(String.class));
                        subject.setYear(attendanceSnapshot.child("year").getValue(String.class));
                        subject.setSemester(attendanceSnapshot.child("semester").getValue(String.class));

                        // Update course information if available
                        String courseFullName = attendanceSnapshot.child("courseFullName").getValue(String.class);
                        if (courseFullName != null) {
                            currentStudent.setCourse(courseFullName);
                            studentCourse.setText(courseFullName);
                        }

                        // Find the specific student
                        for (DataSnapshot studentSnapshot : studentsNode.getChildren()) {
                            if (studentSnapshot.child("name").getValue(String.class).equals(studentId)) {
                                totalClassCount++;
                                boolean isPresent = studentSnapshot.child("present").getValue(Boolean.class);
                                if (isPresent) {
                                    attendedClassCount++;
                                }

                                String date = dateSnapshot.getKey();
                                AttendanceHistoryRecord record = new AttendanceHistoryRecord(date, isPresent, subject);
                                attendanceRecords.add(record);
                            }
                        }
                    }
                }

                updateAttendanceSummary(totalClassCount, attendedClassCount);
                sortAndUpdateRecords();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void updateAttendanceSummary(int totalClassCount, int attendedClassCount) {
        totalClasses.setText(String.valueOf(totalClassCount));
        classesAttended.setText(String.valueOf(attendedClassCount));

        float percentage = totalClassCount > 0 ?
                (float) attendedClassCount / totalClassCount * 100 : 0;
        attendancePercentage.setText(String.format("%.1f%%", percentage));
    }

    private void sortAndUpdateRecords() {
        Collections.sort(attendanceRecords, (a, b) -> b.getDate().compareTo(a.getDate()));
        adapter.notifyDataSetChanged();
    }

    private class AttendanceHistoryAdapter extends RecyclerView.Adapter<AttendanceHistoryAdapter.ViewHolder> {
        private List<AttendanceHistoryRecord> records;
        private SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        private SimpleDateFormat parseFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        public AttendanceHistoryAdapter(List<AttendanceHistoryRecord> records) {
            this.records = records;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_attendance_history_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AttendanceHistoryRecord record = records.get(position);

            try {
                Date date = parseFormat.parse(record.getDate());
                holder.dateText.setText(displayFormat.format(date));
            } catch (Exception e) {
                holder.dateText.setText(record.getDate());
            }

            holder.statusText.setText(record.getStatus());
            holder.statusText.setBackgroundResource(record.isPresent() ?
                    R.drawable.status_background_present : R.drawable.status_background_absent);

            holder.subjectText.setText(record.getSubject().getSubjectName());
        }

        @Override
        public int getItemCount() {
            return records.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView dateText;
            TextView statusText;
            TextView subjectText;

            ViewHolder(View itemView) {
                super(itemView);
                dateText = itemView.findViewById(R.id.dateText);
                statusText = itemView.findViewById(R.id.statusText);
                subjectText = itemView.findViewById(R.id.subjectText);
            }
        }
    }
}