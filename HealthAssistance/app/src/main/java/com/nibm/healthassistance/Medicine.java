package com.nibm.healthassistance;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Medicine {
    private String id;
    private String name;
    private String dosage;
    private String unit;
    private String pills;
    private String time;
    private String timing;
    private boolean taken;
    private Timestamp timestamp;

    // Empty constructor required for Firestore
    public Medicine() {
    }

    public Medicine(String name, String dosage, String unit, String pills,
                    String time, String timing, boolean taken) {
        this.name = name;
        this.dosage = dosage;
        this.unit = unit;
        this.pills = pills;
        this.time = time;
        this.timing = timing;
        this.taken = taken;
        this.timestamp = Timestamp.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getPills() {
        return pills;
    }

    public void setPills(String pills) {
        this.pills = pills;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTiming() {
        return timing;
    }

    public void setTiming(String timing) {
        this.timing = timing;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedDate() {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return dateFormat.format(date);
        }
        return "";
    }

    public String getStatusText() {
        return taken ? "Taken" : "Missed";
    }
}