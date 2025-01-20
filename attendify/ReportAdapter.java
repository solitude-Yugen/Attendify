package com.example.attendify;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.FileProvider;
import java.io.File;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
    private List<ReportItem> reports;
    private Context context;

    public ReportAdapter(Context context, List<ReportItem> reports) {
        this.context = context;
        this.reports = reports;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ReportItem report = reports.get(position);
        holder.fileNameText.setText(report.getFileName());
        holder.dateText.setText(report.getDateGenerated());

        holder.shareButton.setOnClickListener(v -> shareReport(report));
        holder.viewButton.setOnClickListener(v -> viewReport(report));
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    private void shareReport(ReportItem report) {
        File reportFile = new File(report.getFilePath());
        Uri fileUri = FileProvider.getUriForFile(context,
                context.getApplicationContext().getPackageName() + ".provider",
                reportFile);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, "Share Report"));
    }

    private void viewReport(ReportItem report) {
        File reportFile = new File(report.getFilePath());
        Uri fileUri = FileProvider.getUriForFile(context,
                context.getApplicationContext().getPackageName() + ".provider",
                reportFile);

        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setDataAndType(fileUri, "text/csv");
        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(viewIntent);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameText;
        TextView dateText;
        ImageButton shareButton;
        ImageButton viewButton;

        public ViewHolder(View view) {
            super(view);
            fileNameText = view.findViewById(R.id.fileNameText);
            dateText = view.findViewById(R.id.dateText);
            shareButton = view.findViewById(R.id.shareButton);
            viewButton = view.findViewById(R.id.viewButton);
        }
    }
}