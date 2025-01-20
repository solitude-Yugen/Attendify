package com.example.attendify;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class GenerateReportsActivity extends AppCompatActivity {
    private static final String TAG = "GenerateReportsActivity";
    private RecyclerView reportsRecyclerView;
    private ReportAdapter reportAdapter;
    private List<ReportItem> reportList;
    private DatabaseReference databaseRef;
    private ProgressBar progressBar;
    private TextView emptyView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_reports);

        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        reportsRecyclerView = findViewById(R.id.reportsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Initialize Firebase
        databaseRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://attendify-34e8f-default-rtdb.firebaseio.com/");

        // Setup RecyclerView
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(this, reportList);
        reportsRecyclerView.setAdapter(reportAdapter);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadReports);

        // Initial load
        loadReports();
    }

    private void loadReports() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference reportsRef = databaseRef.child("reports");

        reportsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                reportList.clear();
                for (DataSnapshot reportSnapshot : dataSnapshot.getChildren()) {
                    String fileName = reportSnapshot.child("fileName").getValue(String.class);
                    String dateGenerated = reportSnapshot.child("dateGenerated").getValue(String.class);
                    String filePath = reportSnapshot.child("filePath").getValue(String.class);

                    if (fileName != null && dateGenerated != null && filePath != null) {
                        reportList.add(new ReportItem(fileName, dateGenerated, filePath));
                    }
                }

                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading reports: " + databaseError.getMessage());
                updateUI();
            }
        });
    }

    private void updateUI() {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        if (reportList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            reportsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            reportsRecyclerView.setVisibility(View.VISIBLE);
            reportAdapter.notifyDataSetChanged();
        }
    }
}