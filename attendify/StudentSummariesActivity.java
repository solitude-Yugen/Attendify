// StudentSummariesActivity.java
package com.example.attendify;

import android.content.Intent;
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
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

public class StudentSummariesActivity extends AppCompatActivity {
    private RecyclerView studentsRecyclerView;
    private StudentAdapter adapter;
    private DatabaseReference databaseRef;
    private List<Student> studentList;
    private String selectedYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_summaries);

        // Initialize RecyclerView
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        studentList = new ArrayList<>();
        adapter = new StudentAdapter(studentList);
        studentsRecyclerView.setAdapter(adapter);

        // Get filter values from intent
        String type = getIntent().getStringExtra("type");
        selectedYear = getIntent().getStringExtra("year");
        String course = getIntent().getStringExtra("course");

        // If year is not passed, default to "TY"
        if (selectedYear == null) {
            selectedYear = "TY";
        }

        // Initialize Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference()
                .child("attendance")
                .child("2024_2025")
                .child(selectedYear);

        loadStudents();
    }

    private void loadStudents() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot attendanceSnapshot : dateSnapshot.getChildren()) {
                        // Get students node
                        DataSnapshot studentsNode = attendanceSnapshot.child("students");
                        for (DataSnapshot studentSnapshot : studentsNode.getChildren()) {
                            Student student = new Student(
                                    studentSnapshot.child("name").getValue(String.class),
                                    studentSnapshot.child("batch").getValue(String.class),
                                    studentSnapshot.child("rollNo").getValue(String.class),
                                    studentSnapshot.getKey()
                            );
                            if (student.getName() != null && !studentList.contains(student)) {
                                studentList.add(student);
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    // Student model class
    public static class Student {
        private String name;
        private String batch;
        private String rollNo;
        private String studentId;

        public Student() {} // Required for Firebase

        public Student(String name, String batch, String rollNo, String studentId) {
            this.name = name;
            this.batch = batch;
            this.rollNo = rollNo;
            this.studentId = studentId;
        }

        public String getName() { return name; }
        public String getBatch() { return batch; }
        public String getRollNo() { return rollNo; }
        public String getStudentId() { return studentId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Student student = (Student) o;
            return name != null && name.equals(student.name);
        }
    }

    // RecyclerView Adapter
    private class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
        private List<Student> students;

        public StudentAdapter(List<Student> students) {
            this.students = students;
        }

        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_student_item, parent, false);
            return new StudentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
            Student student = students.get(position);
            holder.studentName.setText(student.getName());
            holder.studentBatch.setText(student.getBatch());

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), StudentDetailActivity.class);
                intent.putExtra("studentId", student.getName());
                intent.putExtra("year", selectedYear);
                intent.putExtra("batch", student.getBatch());
                intent.putExtra("rollNo", student.getRollNo());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return students.size();
        }

        class StudentViewHolder extends RecyclerView.ViewHolder {
            CircleImageView studentImage;
            TextView studentName;
            TextView studentBatch;

            StudentViewHolder(View itemView) {
                super(itemView);
                studentImage = itemView.findViewById(R.id.studentImage);
                studentName = itemView.findViewById(R.id.studentName);
                studentBatch = itemView.findViewById(R.id.studentBatch);
            }
        }
    }
}