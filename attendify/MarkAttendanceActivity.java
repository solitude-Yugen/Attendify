package com.example.attendify;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.attendify.models.Student;
import com.example.attendify.models.Subject;
import com.google.firebase.database.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.AlertDialog;
import android.content.Intent;
import android.provider.MediaStore;
import android.graphics.Bitmap;

public class MarkAttendanceActivity extends AppCompatActivity {
    private static final String TAG = "MarkAttendanceActivity";
    private Spinner typeSpinner, subjectSpinner, batchSpinner, yearSpinner;
    private ListView studentListView;
    private Button submitButton;
    private DatabaseReference databaseRef;
    private StudentListAdapter adapter;
    private ArrayList<Student> studentList;
    private String selectedCourse;
    private String selectedYear;
    private Map<String, Subject> subjectMap;
    private Map<String, Student> studentMap;
    private String subjectId;
    private DatabaseReference attendanceRef;
    private String year;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private AlertDialog attendanceMethodDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        selectedCourse = getIntent().getStringExtra("course");
        selectedYear = getIntent().getStringExtra("year");

        Log.d(TAG, "Selected Course: " + selectedCourse);
        Log.d(TAG, "Selected Year: " + selectedYear);

        // Add debug listener to check Firebase data
        databaseRef = FirebaseDatabase.getInstance("https://attendify-34e8f-default-rtdb.firebaseio.com/")
                .getReference();

        // Debug: Print all subjects
        databaseRef.child("subjects").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "All subjects in database:");
                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    String course = subjectSnapshot.child("Course").getValue(String.class);
                    String year = subjectSnapshot.child("Year").getValue(String.class);
                    String subject = subjectSnapshot.child("Subject").getValue(String.class);
                    Log.d(TAG, String.format("Subject: %s, Course: %s, Year: %s", subject, course, year));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });

        // Debug: Print all students
        databaseRef.child("students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "All students in database:");
                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    String course = studentSnapshot.child("Course").getValue(String.class);
                    String year = studentSnapshot.child("Year").getValue(String.class);
                    String name = studentSnapshot.child("Name").getValue(String.class);
                    Log.d(TAG, String.format("Student: %s, Course: %s, Year: %s", name, course, year));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });

        // Setup crash handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Uncaught exception", throwable);
            Toast.makeText(this, "An error occurred: " + throwable.getMessage(),
                    Toast.LENGTH_LONG).show();
        });

        // Initialize data structures
        studentList = new ArrayList<>();
        subjectMap = new HashMap<>();
        studentMap = new HashMap<>();

        // Get selected course and year from intent
        selectedCourse = getIntent().getStringExtra("course");
        selectedYear = getIntent().getStringExtra("year");

        if (selectedCourse == null || selectedYear == null) {
            Toast.makeText(this, "Missing required information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        try {
            databaseRef = FirebaseDatabase.getInstance("https://attendify-34e8f-default-rtdb.firebaseio.com/")
                    .getReference();
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase", e);
            Toast.makeText(this, "Failed to initialize Firebase", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupSpinners();
        setupStudentList();
        setupSubmitButton();

        // Handle recognized students if coming from photo recognition
        ArrayList<String> recognizedStudents = getIntent().getStringArrayListExtra("recognized_students");
        if (recognizedStudents != null) {
            handleRecognizedStudents(recognizedStudents);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (attendanceMethodDialog == null) {
            showAttendanceMethodDialog();
        }
    }
    private void showAttendanceMethodDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Attendance Method")
                .setItems(new CharSequence[]{"Manual Attendance", "Take Photo"},
                        (dialog, which) -> {
                            if (which == 0) {
                                // Manual attendance - do nothing as the current view is already set up
                                attendanceMethodDialog = null;
                            } else {
                                // Photo attendance
                                dispatchTakePictureIntent();
                            }
                        })
                .setCancelable(false);
        attendanceMethodDialog = builder.create();
        attendanceMethodDialog.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                processImageForAttendance(imageBitmap);
            }
        }
    }

    private void processImageForAttendance(Bitmap bitmap) {
        // Create instance of ImageProcessor
        ImageProcessor imageProcessor = new ImageProcessor();

        // Process the image
        imageProcessor.processImage(bitmap, new ImageProcessor.ImageProcessCallback() {
            @Override
            public void onSuccess(List<String> recognizedStudents) {
                runOnUiThread(() -> {
                    handleRecognizedStudents(recognizedStudents);
                    Toast.makeText(MarkAttendanceActivity.this,
                            "Successfully processed " + recognizedStudents.size() + " students",
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MarkAttendanceActivity.this,
                            "Error processing image: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    private void handleRecognizedStudents(List<String> recognizedStudents) {
        // Update the attendance status for recognized students
        for (Student student : studentList) {
            boolean isPresent = recognizedStudents.contains(student.getName());
            student.setPresent(isPresent);
            student.setStatus(isPresent ? "PRESENT" : "ABSENT");
        }

        // Notify adapter to refresh the list
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // [Rest of the existing code remains the same...]

    private void initializeViews() {
        try {
            typeSpinner = findViewById(R.id.typeSpinner);
            subjectSpinner = findViewById(R.id.subjectSpinner);
            batchSpinner = findViewById(R.id.batchSpinner);
            yearSpinner = findViewById(R.id.yearSpinner);
            studentListView = findViewById(R.id.studentListView);
            submitButton = findViewById(R.id.submitButton);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize views", e);
            Toast.makeText(this, "Failed to initialize views", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupSpinners() {
        try {
            // Type Spinner
            ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item,
                    new String[]{"Theory", "Practical"});
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(typeAdapter);

            // Batch Spinner
            ArrayAdapter<String> batchAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item,
                    new String[]{"Batch 1", "Batch 2", "Batch 3"});
            batchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            batchSpinner.setAdapter(batchAdapter);
            batchSpinner.setEnabled(false); // Initially disabled for Theory

            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    boolean isTheory = position == 0;
                    batchSpinner.setEnabled(!isTheory);
                    if (adapter != null) {
                        loadStudents();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            loadSubjects();

        } catch (Exception e) {
            Log.e(TAG, "Failed to setup spinners", e);
            Toast.makeText(this, "Failed to setup spinners", Toast.LENGTH_SHORT).show();
        }
    }

    private String getStringValue(DataSnapshot snapshot) {
        if (!snapshot.exists()) {
            return null;
        }

        Object value = snapshot.getValue();
        if (value == null) {
            return null;
        }

        // Handle different types of values
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Long) {
            return String.valueOf(value);
        } else if (value instanceof Integer) {
            return String.valueOf(value);
        } else if (value instanceof Double) {
            return String.valueOf(value);
        }

        return value.toString();
    }

    private void loadSubjects() {
        DatabaseReference subjectsRef = databaseRef.child("subjects");
        subjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    ArrayList<String> subjectNames = new ArrayList<>();
                    subjectMap.clear();

                    for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                        // Add logging to see the raw data
                        Log.d(TAG, "Subject raw data: " + subjectSnapshot.getValue());

                        // Safe conversion for different types
                        String year = getStringValue(subjectSnapshot.child("Year"));
                        String subjectName = getStringValue(subjectSnapshot.child("Subject"));
                        String subjectId = getStringValue(subjectSnapshot.child("SubjectID"));

                        Log.d(TAG, String.format("Processed subject data - Year: %s, Subject: %s, ID: %s",
                                year, subjectName, subjectId));

                        if (year != null && year.equals(selectedYear)) {
                            if (subjectName != null && subjectId != null) {
                                Subject subject = new Subject();
                                subject.setSubjectName(subjectName);
                                subject.setSubjectId(subjectId);
                                subjectMap.put(subjectSnapshot.getKey(), subject);
                                subjectNames.add(subjectName);
                            }
                        }
                    }

                    if (subjectNames.isEmpty()) {
                        Log.d(TAG, "No subjects found for year: " + selectedYear);
                        Toast.makeText(MarkAttendanceActivity.this,
                                "No subjects found for year: " + selectedYear, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAG, "Found subjects: " + subjectNames);
                    ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(MarkAttendanceActivity.this,
                            android.R.layout.simple_spinner_item, subjectNames);
                    subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    subjectSpinner.setAdapter(subjectAdapter);

                } catch (Exception e) {
                    Log.e(TAG, "Error processing subjects", e);
                    Toast.makeText(MarkAttendanceActivity.this,
                            "Error loading subjects: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(MarkAttendanceActivity.this,
                        "Failed to load subjects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupStudentList() {
        studentList = new ArrayList<>();
        adapter = new StudentListAdapter(this, studentList);
        studentListView.setAdapter(adapter);
        loadStudents();
    }

    private void loadStudents() {
        String selectedBatch = batchSpinner.getSelectedItem().toString();
        boolean isTheory = typeSpinner.getSelectedItem().toString().equals("Theory");

        DatabaseReference studentsRef = databaseRef.child("students");
        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    studentList.clear();
                    studentMap.clear();

                    for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                        // Add logging to see the raw data
                        Log.d(TAG, "Student raw data: " + studentSnapshot.getValue());

                        // Safe conversion for different types
                        String year = getStringValue(studentSnapshot.child("Year"));
                        String batch = getStringValue(studentSnapshot.child("Batch"));
                        String name = getStringValue(studentSnapshot.child("Name"));
                        String studentId = getStringValue(studentSnapshot.child("StudentID"));
                        String enrollmentNo = getStringValue(studentSnapshot.child("EnrollmentNo"));
                        String rollNo = getStringValue(studentSnapshot.child("RollNo"));
                        String status = getStringValue(studentSnapshot.child("Status"));
                        Log.d(TAG, String.format("Processed student data - Year: %s, Batch: %s, Name: %s",
                                year, batch, name));

                        if (year != null && year.equals(selectedYear)) {
                            if (isTheory || (batch != null && batch.equals(selectedBatch))) {
                                Student student = new Student();
                                student.setName(name != null ? name : "");
                                student.setStudentId(studentId != null ? studentId : "");
                                student.setRollNo(rollNo != null ? rollNo : "");
                                student.setBatch(batch != null ? batch : "");
                                student.setStatus(status != null ? status : "");

                                studentMap.put(studentSnapshot.getKey(), student);
                                studentList.add(student);
                            }
                        }
                    }

                    if (studentList.isEmpty()) {
                        Log.d(TAG, "No students found for year: " + selectedYear);
                        Toast.makeText(MarkAttendanceActivity.this,
                                "No students found for year: " + selectedYear, Toast.LENGTH_SHORT).show();
                    }

                    Log.d(TAG, "Found students: " + studentList.size());
                    adapter.notifyDataSetChanged();

                } catch (Exception e) {
                    Log.e(TAG, "Error processing students", e);
                    Toast.makeText(MarkAttendanceActivity.this,
                            "Error loading students: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(MarkAttendanceActivity.this,
                        "Failed to load students", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStudentId(Student student) {
        if (student == null || studentMap == null) {
            Log.d(TAG, "getStudentId: Student or studentMap is null");
            return null;
        }

        for (Map.Entry<String, Student> entry : studentMap.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            // Log comparison details for debugging
            Log.d(TAG, "Comparing students - Current: " +
                    entry.getValue().getName() + ", Target: " + student.getName());

            if (entry.getValue().getStudentId() != null &&
                    entry.getValue().getStudentId().equals(student.getStudentId())) {
                return entry.getKey();
            }
        }

        Log.d(TAG, "No matching student found for: " + student.getName());
        return null;
    }

    private void validateFirebaseKey(String key) throws IllegalArgumentException {
        if (key == null || key.isEmpty() || key.contains(".") || key.contains("#") ||
                key.contains("$") || key.contains("[") || key.contains("]") || key.contains("/")) {
            throw new IllegalArgumentException("Invalid Firebase key: " + key);
        }
    }

    private Map<String, Object> sanitizeDataForFirebase(Map<String, Object> data) {
        Map<String, Object> sanitizedData = new HashMap<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            try {
                validateFirebaseKey(key);

                // Recursively sanitize nested maps
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nestedMap = (Map<String, Object>) value;
                    sanitizedData.put(key, sanitizeDataForFirebase(nestedMap));
                } else {
                    // Ensure value is not null
                    sanitizedData.put(key, value != null ? value : "");
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Skipping invalid key: " + key);
            }
        }

        return sanitizedData;
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            try {
                // Validate selections
                if (typeSpinner.getSelectedItem() == null || subjectSpinner.getSelectedItem() == null) {
                    Toast.makeText(this, "Please select all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                String type = typeSpinner.getSelectedItem().toString();
                String batch = null;
                if ("Practical".equals(type)) {
                    if (batchSpinner.getSelectedItem() == null) {
                        Toast.makeText(this, "Please select a batch", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    batch = batchSpinner.getSelectedItem().toString();
                }

                if (studentList == null || studentList.isEmpty()) {
                    Toast.makeText(this, "No students found to mark attendance", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Prepare attendance data
                Map<String, Object> attendanceData = new HashMap<>();
                Map<String, Object> studentAttendance = new HashMap<>();

                int presentCount = 0;
                int absentCount = 0;
                int totalStudents = studentList.size();

                // Process student attendance
                for (Student student : studentList) {
                    if (student.isPresent()) {
                        presentCount++;
                    } else {
                        absentCount++;
                    }

                    // Ensure student ID is valid for Firebase
                    String studentKey = student.getStudentId();
                    if (studentKey == null || studentKey.isEmpty()) {
                        studentKey = "student_" + studentList.indexOf(student);
                    }

                    try {
                        validateFirebaseKey(studentKey);

                        Map<String, Object> studentRecord = new HashMap<>();
                        studentRecord.put("studentId", student.getStudentId());
                        studentRecord.put("name", student.getName() != null ? student.getName() : "");
                        studentRecord.put("rollNo", student.getRollNo() != null ? student.getRollNo() : "");
                        studentRecord.put("status", student.getStatus() != null ? student.getStatus() : "");
                        studentRecord.put("present", student.isPresent());
                        studentRecord.put("batch", student.getBatch() != null ? student.getBatch() : "");
                        studentRecord.put("markedAt", ServerValue.TIMESTAMP);

                        studentAttendance.put(studentKey, studentRecord);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Invalid student key: " + studentKey);
                    }
                }

                // Calculate statistics
                double attendancePercentage = (totalStudents > 0) ?
                        ((double) presentCount / totalStudents) * 100 : 0;

                Map<String, Object> statistics = new HashMap<>();
                statistics.put("totalStudents", totalStudents);
                statistics.put("presentCount", presentCount);
                statistics.put("absentCount", absentCount);
                statistics.put("attendancePercentage", attendancePercentage);

                // Basic attendance info
                String currentDate = LocalDate.now().toString().replace("-", "");
                attendanceData.put("date", currentDate);
                attendanceData.put("timestamp", ServerValue.TIMESTAMP);
                attendanceData.put("academicYear", getCurrentAcademicYear());
                attendanceData.put("semester", getCurrentSemester());
                attendanceData.put("year", selectedYear);
                attendanceData.put("yearFullName", getYearFullName(selectedYear));
                attendanceData.put("course", selectedCourse);
                attendanceData.put("courseFullName", getCourseFullName(selectedCourse));
                attendanceData.put("subjectName", subjectSpinner.getSelectedItem().toString());
                attendanceData.put("type", type);

                if ("Practical".equals(type)) {
                    attendanceData.put("batch", batch);
                }

                attendanceData.put("statistics", statistics);
                attendanceData.put("students", studentAttendance);

                // Sanitize the entire data structure
                Map<String, Object> sanitizedData = sanitizeDataForFirebase(attendanceData);

                // Create database reference path
                String attendancePath = String.format("attendance/%s/%s/%s",
                        getCurrentAcademicYear().replace("-", "_"),
                        selectedYear,
                        currentDate);

                // Generate unique key
                String attendanceKey = databaseRef.child(attendancePath).push().getKey();
                if (attendanceKey == null) {
                    throw new Exception("Failed to generate attendance key");
                }

                DatabaseReference attendanceRef = databaseRef.child(attendancePath).child(attendanceKey);

                // Save sanitized data to Firebase
                attendanceRef.setValue(sanitizedData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MarkAttendanceActivity.this,
                                    "Attendance submitted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Firebase submission error", e);
                            Toast.makeText(MarkAttendanceActivity.this,
                                    "Failed to submit attendance: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });

            } catch (Exception e) {
                Log.e(TAG, "Error submitting attendance", e);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Helper methods for the enhanced structure
    private String getCurrentAcademicYear() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Calendar months are 0-based

        // If we're in the latter part of the year (June-December),
        // the academic year spans current year to next year
        if (currentMonth >= 6) {
            return currentYear + "-" + (currentYear + 1);
        } else {
            // If we're in the early part of the year (January-May),
            // the academic year spans previous year to current year
            return (currentYear - 1) + "-" + currentYear;
        }
    }

    private String getCurrentSemester() {

        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;

        // Use selectedYear directly since we already have it from intent
        if (currentMonth >= 6 && currentMonth <= 12) {
            switch (selectedYear) {
                case "FY": return "Semester 1";
                case "SY": return "Semester 3";
                case "TY": return "Semester 5";
                default: return "Invalid Semester";
            }
        } else if (currentMonth >= 1 && currentMonth <= 5) {
            switch (selectedYear) {
                case "FY": return "Semester 2";
                case "SY": return "Semester 4";
                case "TY": return "Semester 6";
                default: return "Invalid Semester";
            }
        }
        return "Invalid Semester";
    }

    private String getYearFullName(String year) {
        switch (year) {
            case "FY": return "First Year";
            case "SY": return "Second Year";
            case "TY": return "Third Year";
            default: return year;
        }
    }

    private String getCourseFullName(String course) {
        switch (course) {
            case "CO": return "Computer Engineering";
            case "IF": return "Information Technology";
            case "ME": return "Mechanical Engineering";
            // Add other courses as needed
            default: return course;
        }
    }
    // Add this helper method to format the date for Firebase
    private String formatDateForFirebase(String date) {
        // Remove any special characters that are invalid for Firebase keys
        return date.replace("-", "");
    }

}