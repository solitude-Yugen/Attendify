package com.example.attendify;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.attendify.models.Student;

import java.util.ArrayList;

public class StudentListAdapter extends ArrayAdapter<Student> {
    private Context context;
    private ArrayList<Student> students;

    static class ViewHolder {
        TextView idTextView;
        TextView nameTextView;
        TextView statusTextView;
        RadioButton presentRadio;
        RadioButton absentRadio;
    }

    public StudentListAdapter(Context context, ArrayList<Student> students) {
        super(context, 0, students);
        this.context = context;
        this.students = students;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.student_list_item, parent, false);

            holder = new ViewHolder();
            holder.idTextView = convertView.findViewById(R.id.studentId);
            holder.nameTextView = convertView.findViewById(R.id.studentName);
            holder.statusTextView = convertView.findViewById(R.id.statusText);
            holder.presentRadio = convertView.findViewById(R.id.presentRadio);
            holder.absentRadio = convertView.findViewById(R.id.absentRadio);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Student student = students.get(position);

        // Set basic student info
        holder.idTextView.setText(student.getStudentId());
        holder.nameTextView.setText(student.getName());
        holder.statusTextView.setText(student.getStatus());

        // Remove previous listeners to prevent duplicate calls
        holder.presentRadio.setOnClickListener(null);
        holder.absentRadio.setOnClickListener(null);

        // Set the correct state without triggering listeners
        holder.presentRadio.setChecked(student.getStatus().equals("PRESENT"));
        holder.absentRadio.setChecked(student.getStatus().equals("ABSENT"));

        // Set new listeners
        holder.presentRadio.setOnClickListener(v -> {
            student.setStatus("PRESENT");
            student.setPresent(true);
            holder.statusTextView.setText("PRESENT");
            holder.absentRadio.setChecked(false);
            // Log the status change
            Log.d("StudentListAdapter", "Student " + student.getName() + " marked PRESENT");
        });

        holder.absentRadio.setOnClickListener(v -> {
            student.setStatus("ABSENT");
            student.setPresent(false);
            holder.statusTextView.setText("ABSENT");
            holder.presentRadio.setChecked(false);
            // Log the status change
            Log.d("StudentListAdapter", "Student " + student.getName() + " marked ABSENT");
        });

        return convertView;
    }
}
