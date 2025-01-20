package com.example.attendify;

public class ReportItem {
    private String fileName;
    private String dateGenerated;
    private String filePath;

    public ReportItem(String fileName, String dateGenerated, String filePath) {
        this.fileName = fileName;
        this.dateGenerated = dateGenerated;
        this.filePath = filePath;
    }

    public String getFileName() { return fileName; }
    public String getDateGenerated() { return dateGenerated; }
    public String getFilePath() { return filePath; }
}
