package com.example.attendify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView greetingText;
    private CircleImageView profileImage;
    private Spinner diplomaSpinner, yearSpinner, courseSpinner, semesterSpinner;
    private ValueEventListener facultyListener, studentsListener, subjectsListener;
    private Map<String, Object> facultyData = new HashMap<>();
    private Map<String, Object> studentsData = new HashMap<>();
    private Map<String, Object> subjectsData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        initializeViews();

        // Set up user profile
        setupUserProfile();

        // Set up spinners
        setupSpinners();

        // Set up click listeners
        setupClickListeners();

        // Set up bottom navigation
        setupBottomNavigation();

        // Initialize database listeners
        setupDatabaseListeners();
    }

    private void initializeViews() {
        greetingText = findViewById(R.id.greetingText);
        profileImage = findViewById(R.id.profileImage);
        diplomaSpinner = findViewById(R.id.diplomaSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        courseSpinner = findViewById(R.id.courseSpinner);
      //  semesterSpinner = findViewById(R.id.semesterSpinner);
    }

    private void setupUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String fullName = user.getDisplayName();
            String firstName = fullName != null ? fullName.split(" ")[0] : "";

            Calendar c = Calendar.getInstance();
            int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
            String greeting;
            if (timeOfDay >= 0 && timeOfDay < 12) {
                greeting = "Good Morning";
            } else if (timeOfDay >= 12 && timeOfDay < 16) {
                greeting = "Good Afternoon";
            } else if (timeOfDay >= 16 && timeOfDay < 20){
                greeting = "Good Evening";
            }
            else {
                greeting = "Good Night";
            }

            greetingText.setText(greeting + " " + firstName);

            if (user.getPhotoUrl() != null) {
                Picasso.get().load(user.getPhotoUrl()).into(profileImage);
            }
        }
    }

    private void setupSpinners() {
        // Setup Diploma/Degree Spinner
        ArrayList<String> diplomaOptions = new ArrayList<>();
        diplomaOptions.add("Type");
        diplomaOptions.add("Diploma");
        diplomaOptions.add("Degree");
        ArrayAdapter<String> diplomaAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, diplomaOptions);
        diplomaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        diplomaSpinner.setAdapter(diplomaAdapter);

        // Setup Year Spinner
        ArrayList<String> yearOptions = new ArrayList<>();
        yearOptions.add("Year");
        yearOptions.add("FY");
        yearOptions.add("SY");
        yearOptions.add("TY");
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, yearOptions);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        // Setup Course Spinner
        ArrayList<String> courseOptions = new ArrayList<>();
        courseOptions.add("Course");
        courseOptions.add("CO");
        courseOptions.add("IT");
        courseOptions.add("IF");
        courseOptions.add("IS");
        courseOptions.add("EP");
        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, courseOptions);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(courseAdapter);


        // Setup Spinner Listeners
        setupSpinnerListeners();
    }

    private void setupSpinnerListeners() {
        diplomaSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position > 0) {
                    updateFilters();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        yearSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position > 0) {
                    updateFilters();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        courseSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position > 0) {
                    updateFilters();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

    }

    private void setupDatabaseListeners() {
        // Faculty Listener
        facultyListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                facultyData.clear();
                for (DataSnapshot facultySnapshot : snapshot.getChildren()) {
                    facultyData.put(facultySnapshot.getKey(), facultySnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Failed to load faculty data", Toast.LENGTH_SHORT).show();
            }
        };
        mDatabase.child("faculty").addValueEventListener(facultyListener);

        // Students Listener
        studentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentsData.clear();
                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    studentsData.put(studentSnapshot.getKey(), studentSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Failed to load student data", Toast.LENGTH_SHORT).show();
            }
        };
        mDatabase.child("students").addValueEventListener(studentsListener);

        // Subjects Listener
        subjectsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectsData.clear();
                for (DataSnapshot subjectSnapshot : snapshot.getChildren()) {
                    subjectsData.put(subjectSnapshot.getKey(), subjectSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Failed to load subject data", Toast.LENGTH_SHORT).show();
            }
        };
        mDatabase.child("subjects").addValueEventListener(subjectsListener);
    }

    private void updateFilters() {
        String selectedType = diplomaSpinner.getSelectedItem().toString();
        String selectedYear = yearSpinner.getSelectedItem().toString();
        String selectedCourse = courseSpinner.getSelectedItem().toString();
     //   String selectedSemester = semesterSpinner.getSelectedItem().toString();

        // Filter data based on selections
        filterData(selectedType, selectedYear, selectedCourse);
    }

    private void filterData(String type, String year, String course) {
        // TODO: Implement filtering logic based on selected criteria
        // This will depend on your specific requirements and data structure
    }

    // Update the setupClickListeners() method in DashboardActivity.java

    private void setupClickListeners() {
        findViewById(R.id.markAttendanceCard).setOnClickListener(v -> {
            if (validateSelections()) {
                Intent intent = new Intent(DashboardActivity.this, MarkAttendanceActivity.class);
                // Pass the selected filter data
                intent.putExtra("type", diplomaSpinner.getSelectedItem().toString());
                intent.putExtra("year", yearSpinner.getSelectedItem().toString());
                intent.putExtra("course", courseSpinner.getSelectedItem().toString());
         //       intent.putExtra("semester", semesterSpinner.getSelectedItem().toString());
                startActivity(intent);
            }
        });

        findViewById(R.id.classSummaryCard).setOnClickListener(v -> {
            if (validateSelections()) {
                Intent intent = new Intent(DashboardActivity.this, ClassSummariesActivity.class);
                intent.putExtra("type", diplomaSpinner.getSelectedItem().toString());
                intent.putExtra("year", yearSpinner.getSelectedItem().toString());
                intent.putExtra("course", courseSpinner.getSelectedItem().toString());
          //      intent.putExtra("semester", semesterSpinner.getSelectedItem().toString());
                startActivity(intent);
            }
        });

        findViewById(R.id.studentReportCard).setOnClickListener(v -> {
            if (validateSelections()) {
                Intent intent = new Intent(DashboardActivity.this, StudentSummariesActivity.class);
                intent.putExtra("type", diplomaSpinner.getSelectedItem().toString());
                intent.putExtra("year", yearSpinner.getSelectedItem().toString());
                intent.putExtra("course", courseSpinner.getSelectedItem().toString());
         //       intent.putExtra("semester", semesterSpinner.getSelectedItem().toString());
                startActivity(intent);
            }
        });

        findViewById(R.id.generateReportsCard).setOnClickListener(v -> {
            if (validateSelections()) {
                Intent intent = new Intent(DashboardActivity.this, GenerateReportsActivity.class);
                intent.putExtra("type", diplomaSpinner.getSelectedItem().toString());
                intent.putExtra("year", yearSpinner.getSelectedItem().toString());
                intent.putExtra("course", courseSpinner.getSelectedItem().toString());
        //        intent.putExtra("semester", semesterSpinner.getSelectedItem().toString());
                startActivity(intent);
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Navigate to profile activity
                Toast.makeText(this, "Opening Profile", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }




    private boolean validateSelections() {
        if (diplomaSpinner.getSelectedItemPosition() == 0 ||
                yearSpinner.getSelectedItemPosition() == 0 ||
                courseSpinner.getSelectedItemPosition() == 0 )
               // semesterSpinner.getSelectedItemPosition() == 0)
        {
            Toast.makeText(this, "Please select all filters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove database listeners
        if (facultyListener != null) {
            mDatabase.child("faculty").removeEventListener(facultyListener);
        }
        if (studentsListener != null) {
            mDatabase.child("students").removeEventListener(studentsListener);
        }
        if (subjectsListener != null) {
            mDatabase.child("subjects").removeEventListener(subjectsListener);
        }
    }

}