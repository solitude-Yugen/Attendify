package com.example.attendify;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.example.attendify.StudentAttendance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.*;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import android.Manifest;
import android.content.pm.PackageManager;

public class ClassSummariesActivity extends AppCompatActivity {
    private TextView startDateTV, endDateTV;
    private Spinner typeSpinner, subjectSpinner, batchSpinner;
    private TableLayout summaryTable;
    private DatabaseReference databaseRef;
    private String selectedYear = "TY";
    private String currentAcademicYear = "2024_2025";
    private Map<String, StudentAttendance> studentAttendanceMap = new HashMap<>();

    private Button generateReportBtn;
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_summaries);

        initializeViews();
        setupDatabase();
        setupDatePickers();
        setupSpinners();
    }

    private void initializeViews() {
        startDateTV = findViewById(R.id.startDateTV);
        endDateTV = findViewById(R.id.endDateTV);
        typeSpinner = findViewById(R.id.typeSpinner);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        batchSpinner = findViewById(R.id.batchSpinner);
        summaryTable = findViewById(R.id.summaryTable);
        generateReportBtn = findViewById(R.id.generateReportBtn);
        generateReportBtn.setOnClickListener(v -> generateReport());
    }
    private void generateReport() {
        // Check for write permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
            return;
        }

        // Validate required data
        if (studentAttendanceMap.isEmpty()) {
            Toast.makeText(this, "No data available to generate report", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Get the selected parameters for the filename
            String subject = subjectSpinner.getSelectedItem().toString();
            String type = typeSpinner.getSelectedItem().toString();
            String batch = type.equals("Practical") ?
                    "_" + batchSpinner.getSelectedItem().toString() : "";
            String startDate = startDateTV.getText().toString();
            String endDate = endDateTV.getText().toString();

            // Create filename
            String filename = String.format("attendance_report_%s_%s%s_%s_to_%s.csv",
                            subject, type, batch, startDate, endDate)
                    .replace(" ", "_")
                    .replace("/", "_");

            // Create reports directory if it doesn't exist
            File reportsDir = new File(getExternalFilesDir(null), "reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }

            // Create the report file
            File reportFile = new File(reportsDir, filename);

            // Create CSV content
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("Sr.No.,Name,Total Classes,Present Classes,Attendance Percentage,Status\n");

            // Sort students by name
            List<Map.Entry<String, StudentAttendance>> sortedEntries =
                    new ArrayList<>(studentAttendanceMap.entrySet());
            Collections.sort(sortedEntries, (a, b) -> a.getKey().compareTo(b.getKey()));

            // Add data rows
            int srNo = 1;
            for (Map.Entry<String, StudentAttendance> entry : sortedEntries) {
                StudentAttendance attendance = entry.getValue();
                float percentage = attendance.totalClasses == 0 ? 0 :
                        (attendance.presentClasses * 100.0f) / attendance.totalClasses;
                String status = percentage >= 75 ? "Regular" : "Defaulter";

                csvContent.append(String.format("%d,%s,%d,%d,%.2f,%s\n",
                        srNo++,
                        attendance.name,
                        attendance.totalClasses,
                        attendance.presentClasses,
                        percentage,
                        status));
            }

            // Write CSV content to file
            try (FileWriter writer = new FileWriter(reportFile)) {
                writer.write(csvContent.toString());
            }

            // Save report metadata to Firebase
            DatabaseReference reportsRef = databaseRef.child("reports");
            String reportId = reportsRef.push().getKey();

            if (reportId != null) {
                Map<String, Object> reportData = new HashMap<>();
                reportData.put("fileName", filename);
                reportData.put("filePath", reportFile.getAbsolutePath());
                reportData.put("dateGenerated", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()).format(new Date()));

                reportsRef.child(reportId).setValue(reportData)
                        .addOnSuccessListener(aVoid -> {
                            // Show success message
                            Toast.makeText(this, "Report saved successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            // Show error message
                            Toast.makeText(this, "Failed to save report metadata", Toast.LENGTH_SHORT).show();
                        });
            }

            // Create sharing intent with a copy of the file
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            Uri fileUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider",
                    reportFile);
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Start sharing activity
            startActivity(Intent.createChooser(shareIntent, "Share Attendance Report"));

        } catch (Exception e) {
            Log.e(TAG, "Error generating report", e);
            Toast.makeText(this, "Error generating report: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }



    private void setupDatabase() {
        databaseRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://attendify-34e8f-default-rtdb.firebaseio.com/");
    }

    private void setupDatePickers() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener startDateListener = (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateDateText(startDateTV, calendar.getTime());
            fetchAttendanceData();
        };

        DatePickerDialog.OnDateSetListener endDateListener = (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateDateText(endDateTV, calendar.getTime());
            fetchAttendanceData();
        };

        startDateTV.setOnClickListener(v -> showDatePicker(startDateListener));
        endDateTV.setOnClickListener(v -> showDatePicker(endDateListener));
    }

    private void setupSpinners() {
        // Type Spinner
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Theory", "Practical"));
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        // Batch Spinner
        ArrayAdapter<String> batchAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Batch 1", "Batch 2", "Batch 3"));
        batchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        batchSpinner.setAdapter(batchAdapter);

        // Load subjects from Firebase
        loadSubjects();

        // Set up listeners
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = typeSpinner.getSelectedItem().toString();
                batchSpinner.setEnabled(selectedType.equals("Practical"));
                if (!startDateTV.getText().toString().isEmpty() &&
                        !endDateTV.getText().toString().isEmpty()) {
                    fetchAttendanceData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!startDateTV.getText().toString().isEmpty() &&
                        !endDateTV.getText().toString().isEmpty()) {
                    fetchAttendanceData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        batchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!startDateTV.getText().toString().isEmpty() &&
                        !endDateTV.getText().toString().isEmpty()) {
                    fetchAttendanceData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadSubjects() {
        databaseRef.child("subjects").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    List<String> subjects = new ArrayList<>();
                    for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                        String subjectName = subjectSnapshot.child("Subject").getValue(String.class);
                        if (subjectName != null) {
                            subjects.add(subjectName);
                        }
                    }

                    ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(
                            ClassSummariesActivity.this,
                            android.R.layout.simple_spinner_item,
                            subjects
                    );
                    subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    subjectSpinner.setAdapter(subjectAdapter);
                } catch (Exception e) {
                    Log.e(TAG, "Error processing subjects", e);
                    Toast.makeText(ClassSummariesActivity.this,
                            "Error loading subjects", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading subjects: " + databaseError.getMessage());
                Toast.makeText(ClassSummariesActivity.this,
                        "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAttendanceData() {
        String startDate = startDateTV.getText().toString().replace("-", "");
        String endDate = endDateTV.getText().toString().replace("-", "");

        Log.d(TAG, "fetchAttendanceData called - Start Date: " + startDate + ", End Date: " + endDate);

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = typeSpinner.getSelectedItem() != null ? typeSpinner.getSelectedItem().toString() : null;
        String subject = subjectSpinner.getSelectedItem() != null ? subjectSpinner.getSelectedItem().toString() : null;

        if (type == null || subject == null) {
            Toast.makeText(this, "Please ensure all fields are selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedBatch = null;
        if (type.equals("Practical")) {
            selectedBatch = batchSpinner.getSelectedItem() != null ?
                    batchSpinner.getSelectedItem().toString() : null;
            if (selectedBatch == null) {
                Toast.makeText(this, "Please select a batch", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        processStudentsFromAttendance(startDate, endDate, type, subject, selectedBatch);
    }

    private void processStudentsFromAttendance(String startDate, String endDate, String type,
                                               String subject, String selectedBatch) {
        DatabaseReference attendanceRef = databaseRef.child("attendance")
                .child(currentAcademicYear)
                .child(selectedYear);

        attendanceRef.orderByKey()
                .startAt(startDate)
                .endAt(endDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        studentAttendanceMap.clear();
                        boolean foundMatchingRecord = false;

                        for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                            for (DataSnapshot recordSnapshot : dateSnapshot.getChildren()) {
                                String recordType = recordSnapshot.child("type").getValue(String.class);
                                String recordSubject = recordSnapshot.child("subjectName").getValue(String.class);

                                if (recordType != null && recordType.equals(type) &&
                                        recordSubject != null && recordSubject.equals(subject)) {

                                    foundMatchingRecord = true;
                                    DataSnapshot studentsNode = recordSnapshot.child("students");
                                    for (DataSnapshot studentSnapshot : studentsNode.getChildren()) {
                                        String name = studentSnapshot.child("name").getValue(String.class);
                                        String batch = studentSnapshot.child("batch").getValue(String.class);

                                        if (type.equals("Theory") || selectedBatch == null ||
                                                (batch != null && batch.equals(selectedBatch))) {
                                            if (name != null && !studentAttendanceMap.containsKey(name)) {
                                                StudentAttendance attendance = new StudentAttendance(name);
                                                studentAttendanceMap.put(name, attendance);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (foundMatchingRecord) {
                            fetchAttendanceRecordsNew(startDate, endDate, type, subject, selectedBatch);
                        } else {
                            Toast.makeText(ClassSummariesActivity.this,
                                    "No matching attendance records found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error fetching attendance record: " + databaseError.getMessage());
                        Toast.makeText(ClassSummariesActivity.this,
                                "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchAttendanceRecordsNew(String startDate, String endDate, String type,
                                           String subject, String selectedBatch) {
        DatabaseReference attendanceRef = databaseRef.child("attendance")
                .child(currentAcademicYear)
                .child(selectedYear);

        // Reset attendance counters
        for (StudentAttendance attendance : studentAttendanceMap.values()) {
            attendance.totalClasses = 0;
            attendance.presentClasses = 0;
        }

        attendanceRef.orderByKey()
                .startAt(startDate)
                .endAt(endDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                            for (DataSnapshot recordSnapshot : dateSnapshot.getChildren()) {
                                String recordType = recordSnapshot.child("type").getValue(String.class);
                                String recordSubject = recordSnapshot.child("subjectName").getValue(String.class);

                                if (recordType != null && recordType.equals(type) &&
                                        recordSubject != null && recordSubject.equals(subject)) {
                                    processAttendanceRecord(recordSnapshot, type, selectedBatch);
                                }
                            }
                        }
                        updateTable();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error fetching attendance: " + databaseError.getMessage());
                    }
                });
    }

    private void processAttendanceRecord(DataSnapshot recordSnapshot, String type, String selectedBatch) {
        DataSnapshot studentsNode = recordSnapshot.child("students");
        if (!studentsNode.exists()) {
            return;
        }

        for (DataSnapshot studentSnapshot : studentsNode.getChildren()) {
            String studentName = studentSnapshot.child("name").getValue(String.class);
            Boolean isPresent = studentSnapshot.child("present").getValue(Boolean.class);
            String studentBatch = studentSnapshot.child("batch").getValue(String.class);

            boolean batchMatches = type.equals("Theory") || selectedBatch == null ||
                    (studentBatch != null && studentBatch.equals(selectedBatch));

            if (!batchMatches) {
                continue;
            }

            StudentAttendance attendance = studentAttendanceMap.get(studentName);
            if (attendance != null) {
                attendance.setTotalClasses(attendance.getTotalClasses() + 1);

                if (isPresent != null && isPresent) {
                    attendance.setPresentClasses(attendance.getPresentClasses() + 1);


                }
            }
        }
    }

    private void updateTable() {
        runOnUiThread(() -> {
            summaryTable.removeAllViews();
            addHeaderRow();

            List<Map.Entry<String, StudentAttendance>> sortedEntries =
                    new ArrayList<>(studentAttendanceMap.entrySet());

            Collections.sort(sortedEntries, (a, b) -> a.getKey().compareTo(b.getKey()));

            int srNo = 1;
            for (Map.Entry<String, StudentAttendance> entry : sortedEntries) {
                StudentAttendance attendance = entry.getValue();
                float percentage = attendance.totalClasses == 0 ? 0 :
                        (attendance.presentClasses * 100.0f) / attendance.totalClasses;
                addStudentRow(srNo++, attendance.name, percentage);
            }
        });
    }

    private void addHeaderRow() {
        TableRow headerRow = new TableRow(this);
        headerRow.addView(createHeaderTextView("Sr.No."));
        headerRow.addView(createHeaderTextView("Name"));
        headerRow.addView(createHeaderTextView("Present %"));
        headerRow.addView(createHeaderTextView("Status"));
        summaryTable.addView(headerRow);
    }

    private void addStudentRow(int srNo, String name, float percentage) {
        TableRow row = new TableRow(this);
        row.addView(createTextView(String.valueOf(srNo)));
        row.addView(createTextView(name));
        row.addView(createTextView(String.format("%.2f%%", percentage)));

        TextView statusBox = createTextView("");
        statusBox.setBackgroundColor(percentage >= 75 ?
                getResources().getColor(R.color.green) :
                getResources().getColor(R.color.red));
        row.addView(statusBox);

        summaryTable.addView(row);
    }

    private TextView createHeaderTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(16, 16, 16, 16);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        return tv;
    }

    private TextView createTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(16, 16, 16, 16);
        return tv;
    }

    private void showDatePicker(DatePickerDialog.OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateText(TextView textView, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        textView.setText(sdf.format(date));
    }

}