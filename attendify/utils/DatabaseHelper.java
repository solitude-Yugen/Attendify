package com.example.attendify.utils;

import com.example.attendify.models.Faculty;
import com.example.attendify.models.Student;
import com.example.attendify.models.Subject;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;



public class DatabaseHelper {
    private final FirebaseDatabase database;
    private final DatabaseReference facultyRef;
    private final DatabaseReference studentRef;
    private final DatabaseReference subjectRef;

    public DatabaseHelper() {
        database = FirebaseDatabase.getInstance();
        facultyRef = database.getReference("faculty");
        studentRef = database.getReference("students");
        subjectRef = database.getReference("subjects");
    }

    // Faculty Methods
    public void getFacultyByDepartment(String department, DataListener<List<Faculty>> listener) {
        facultyRef.orderByChild("department").equalTo(department)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Faculty> facultyList = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Faculty faculty = ds.getValue(Faculty.class);
                            if (faculty != null) {
                                facultyList.add(faculty);
                            }
                        }
                        listener.onDataReceived(facultyList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

    // Student Methods
    public void getStudentsByBranchAndDepartment(String branch, String department, DataListener<List<Student>> listener) {
        studentRef.orderByChild("branch").equalTo(branch)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Student> studentList = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Student student = ds.getValue(Student.class);
                            if (student != null && student.getCourse().equals(department)) {
                                studentList.add(student);
                            }
                        }
                        listener.onDataReceived(studentList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

    // Subject Methods
    public void getSubjectsByBranchAndDepartment(String branch, String department, DataListener<List<Subject>> listener) {
        subjectRef.orderByChild("branch").equalTo(branch)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Subject> subjectList = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Subject subject = ds.getValue(Subject.class);
                            if (subject != null && subject.getCourse().equals(department)) {
                                subjectList.add(subject);
                            }
                        }
                        listener.onDataReceived(subjectList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

    // Callback interface
    public interface DataListener<T> {
        void onDataReceived(T data);
        void onError(String error);
    }
}
