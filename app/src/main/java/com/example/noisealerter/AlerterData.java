package com.example.noisealerter;

public class AlerterData {
    private String dateAndTime;
    private String localFilePath;

    public AlerterData (String dateAndTime, String localFilePath) {
        this.dateAndTime = dateAndTime;
        this.localFilePath = localFilePath;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }
}
