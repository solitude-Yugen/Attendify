package com.example.attendify;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class PDFReportGenerator {
    private static final int PAGE_WIDTH = 595; // A4 width in points
    private static final int PAGE_HEIGHT = 842; // A4 height in points
    private static final int MARGIN = 50;
    private static final float TITLE_TEXT_SIZE = 18f;
    private static final float HEADER_TEXT_SIZE = 14f;
    private static final float CONTENT_TEXT_SIZE = 12f;


    public static File generateAttendanceReport(Context context,
                                                Map<String, StudentAttendance> studentData,
                                                String startDate,
                                                String endDate,
                                                String type,
                                                String subject,
                                                String batch) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Draw title
        paint.setTextSize(TITLE_TEXT_SIZE);
        canvas.drawText("Attendance Report", MARGIN, MARGIN, paint);

        // Draw report details
        paint.setTextSize(CONTENT_TEXT_SIZE);
        int y = MARGIN + 40;
        canvas.drawText("Start Date: " + startDate, MARGIN, y, paint);
        y += 20;
        canvas.drawText("End Date: " + endDate, MARGIN, y, paint);
        y += 20;
        canvas.drawText("Type: " + type, MARGIN, y, paint);
        y += 20;
        canvas.drawText("Subject: " + subject, MARGIN, y, paint);
        if (batch != null && !batch.isEmpty()) {
            y += 20;
            canvas.drawText("Batch: " + batch, MARGIN, y, paint);
        }

        // Draw table headers
        y += 40;
        paint.setTextSize(HEADER_TEXT_SIZE);
        int x = MARGIN;
        canvas.drawText("Sr.No.", x, y, paint);
        x += 60;
        canvas.drawText("Name", x, y, paint);
        x += 200;
        canvas.drawText("Present %", x, y, paint);
        x += 100;
        canvas.drawText("Status", x, y, paint);

        // Draw table content
        paint.setTextSize(CONTENT_TEXT_SIZE);
        int srNo = 1;
        for (Map.Entry<String, StudentAttendance> entry : studentData.entrySet()) {
            y += 25;
            if (y > PAGE_HEIGHT - MARGIN) {
                // Start new page if content exceeds current page
                document.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, document.getPages().size() + 1).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = MARGIN + 40;
            }

            StudentAttendance attendance = entry.getValue();
            float percentage = attendance.getTotalClasses() == 0 ? 0 :
                    (attendance.getPresentClasses() * 100.0f) / attendance.getTotalClasses();

            x = MARGIN;
            canvas.drawText(String.valueOf(srNo++), x, y, paint);
            x += 60;
            canvas.drawText(attendance.name, x, y, paint);
            x += 200;
            canvas.drawText(String.format("%.2f%%", percentage), x, y, paint);
            x += 100;
            canvas.drawText(percentage >= 75 ? "Regular" : "Defaulter", x, y, paint);
        }

        document.finishPage(page);

        // Save the document
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "Attendance_Report_" + timestamp + ".pdf";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

        try {
            document.writeTo(new FileOutputStream(file));
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            document.close();
        }
    }
}