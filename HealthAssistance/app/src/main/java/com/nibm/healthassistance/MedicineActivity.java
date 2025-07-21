package com.nibm.healthassistance;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import java.util.*;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;




public class MedicineActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private TextView supplementScoreText, tabletsTakenText, omega3TimeText, nmnTimeText;
    private CardView omega3Card, nmnCard, newSupplementButton;
    private CardView notificationButton;
    private ImageView omega3CheckBox, nmnCheckBox;
    private LinearLayout addSupplementLayout;
    private LinearLayout medicineCardsContainer; // Container for dynamic cards

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean omega3Taken = true;
    private boolean nmnTaken = false;
    private int takenCount = 1;
    private int totalCount = 2; // Start with Omega3 + NMN

    // Medicine data class
    private static class Medicine {
        String name;
        String dosage;
        String unit;
        String pills;
        String timing;
        String time;
        boolean taken;

        Medicine(String name, String dosage, String unit, String pills, String timing, String time) {
            this.name = name;
            this.dosage = dosage;
            this.unit = unit;
            this.pills = pills;
            this.timing = timing;
            this.time = time;
            this.taken = false;
        }
    }

    private final List<Medicine> addedMedicines = new ArrayList<>();

    // Reminder functionality variables
    private static final String CHANNEL_ID = "medicine_reminder_channel";
    private static final int NOTIFICATION_ID = 1001;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicine);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        findViewById(R.id.menuButton).setOnClickListener(v -> toggleDrawer());

        supplementScoreText = findViewById(R.id.supplementScoreText);
        tabletsTakenText = findViewById(R.id.tabletsTakenText);
        omega3Card = findViewById(R.id.omega3Card);
        nmnCard = findViewById(R.id.nmnCard);
        newSupplementButton = findViewById(R.id.newSupplementButton);
        omega3CheckBox = findViewById(R.id.omega3CheckBox);
        nmnCheckBox = findViewById(R.id.nmnCheckBox);
        omega3TimeText = findViewById(R.id.omega3TimeText);
        nmnTimeText = findViewById(R.id.nmnTimeText);
        addSupplementLayout = findViewById(R.id.addSupplementLayout);
        notificationButton = findViewById(R.id.notificationButton);

        // Initialize the medicine cards container
        medicineCardsContainer = findViewById(R.id.medicineCardsContainer);

        // Initialize reminder functionality
        sharedPreferences = getSharedPreferences("MedicineReminder", MODE_PRIVATE);
        createNotificationChannel();

        updateCheckBoxes();
        updateSupplementScore();

        // Set click listeners
        omega3Card.setOnClickListener(v -> toggleSupplement(true));
        nmnCard.setOnClickListener(v -> toggleSupplement(false));
        addSupplementLayout.setOnClickListener(v -> showAddMedicineDialog());
        omega3TimeText.setOnClickListener(v -> showTimePicker(true));
        nmnTimeText.setOnClickListener(v -> showTimePicker(false));
        notificationButton.setOnClickListener(v -> showReminderDialog());
    }

    private void toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void toggleSupplement(boolean isOmega3) {
        if (isOmega3) {
            omega3Taken = !omega3Taken;
        } else {
            nmnTaken = !nmnTaken;
        }
        updateCheckBoxes();
        updateSupplementScore();
    }

    private void updateCheckBoxes() {
        omega3CheckBox.setImageResource(omega3Taken ?
                R.drawable.ic_check_circle_filled : R.drawable.ic_check_circle_outline);
        nmnCheckBox.setImageResource(nmnTaken ?
                R.drawable.ic_check_circle_filled : R.drawable.ic_check_circle_outline);
    }

    private void updateSupplementScore() {
        takenCount = (omega3Taken ? 1 : 0) + (nmnTaken ? 1 : 0);
        totalCount = 2; // Omega3 + NMN

        for (Medicine medicine : addedMedicines) {
            if (medicine.taken) takenCount++;
            totalCount++;
        }

        tabletsTakenText.setText(takenCount + "/" + totalCount + " tablets taken");
    }

    private void showTimePicker(boolean isOmega3) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                R.style.CustomTimePickerTheme,
                (view, hour, minute) -> {
                    String amPm = hour >= 12 ? "PM" : "AM";
                    int displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
                    String time = String.format("%d:%02d %s", displayHour, minute, amPm);

                    if (isOmega3) {
                        omega3TimeText.setText("To take at " + time);
                    } else {
                        nmnTimeText.setText("To take at " + time);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    // Reminder dialog method
    private void showReminderDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_reminder, null);

        EditText medicineNameEdit = dialogView.findViewById(R.id.medicineNameEdit);
        TextView timeDisplay = dialogView.findViewById(R.id.timeDisplay);
        Button selectTimeButton = dialogView.findViewById(R.id.selectTimeButton);

        final int[] selectedHour = {9};
        final int[] selectedMinute = {0};

        timeDisplay.setText(String.format("%02d:%02d", selectedHour[0], selectedMinute[0]));

        selectTimeButton.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    MedicineActivity.this,
                    R.style.CustomTimePickerTheme,
                    (view, hourOfDay, minute) -> {
                        selectedHour[0] = hourOfDay;
                        selectedMinute[0] = minute;
                        timeDisplay.setText(String.format("%02d:%02d", hourOfDay, minute));
                    },
                    selectedHour[0],
                    selectedMinute[0],
                    true
            );
            timePickerDialog.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setView(dialogView)
                .setPositiveButton("Set Reminder", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String medicineName = medicineNameEdit.getText().toString().trim();

            if (medicineName.isEmpty()) {
                medicineNameEdit.setError("Please enter medicine name");
                return;
            }

            saveReminder(medicineName, selectedHour[0], selectedMinute[0]);
            setMedicineAlarm(selectedHour[0], selectedMinute[0], medicineName);

            Toast.makeText(MedicineActivity.this,
                    "Reminder set for " + String.format("%02d:%02d", selectedHour[0], selectedMinute[0]),
                    Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });
    }

    // Reminder functionality method
    private void saveReminder(String medicineName, int hour, int minute) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("medicine_name", medicineName);
        editor.putInt("reminder_hour", hour);
        editor.putInt("reminder_minute", minute);
        editor.putBoolean("reminder_set", true);
        editor.apply();
    }

    private void setMedicineAlarm(int hour, int minute, String medicineName) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MedicineReminderReceiver.class);
        intent.putExtra("medicine_name", medicineName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Medicine Reminder";
            String description = "Channel for medicine reminder notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showAddMedicineDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_medicine, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText medicineName = dialogView.findViewById(R.id.medicineName);
        EditText dosageAmount = dialogView.findViewById(R.id.dosageAmount);
        Spinner dosageUnit = dialogView.findViewById(R.id.dosageUnit);
        EditText pillsCount = dialogView.findViewById(R.id.pillsCount);
        Spinner timingSpinner = dialogView.findViewById(R.id.timingSpinner);
        LinearLayout timeSelector = dialogView.findViewById(R.id.timeSelector);
        TextView selectedTime = dialogView.findViewById(R.id.selectedTime);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button addButton = dialogView.findViewById(R.id.addButton);
        ImageView closeDialog = dialogView.findViewById(R.id.closeDialog);

        setupDosageUnitSpinner(dosageUnit);
        setupTimingSpinner(timingSpinner);

        timeSelector.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    R.style.CustomTimePickerTheme,
                    (view, hourOfDay, minute) -> {
                        String amPm = hourOfDay >= 12 ? "PM" : "AM";
                        int displayHour = hourOfDay == 0 ? 12 : (hourOfDay > 12 ? hourOfDay - 12 : hourOfDay);
                        String time = String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm);
                        selectedTime.setText(time);
                        selectedTime.setTextColor(Color.WHITE);
                    }, 9, 0, false);
            timePickerDialog.show();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        closeDialog.setOnClickListener(v -> dialog.dismiss());

        addButton.setOnClickListener(v -> {
            String name = medicineName.getText().toString().trim();
            String dosage = dosageAmount.getText().toString().trim();
            String unit = dosageUnit.getSelectedItem().toString();
            String pills = pillsCount.getText().toString().trim();
            String timing = timingSpinner.getSelectedItem().toString();
            String time = selectedTime.getText().toString();

            if (validateInputs(name, dosage, pills, time)) {
                addMedicineToList(name, dosage, unit, pills, timing, time);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void setupDosageUnitSpinner(Spinner spinner) {
        String[] units = {"mg", "g", "ml", "tablets", "capsules"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, units) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(Color.WHITE);
                view.setBackgroundColor(Color.parseColor("#3A3A3A"));
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupTimingSpinner(Spinner spinner) {
        String[] timings = {"Before meals", "After meals", "With meals", "On empty stomach", "Before sleep"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, timings) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(Color.WHITE);
                view.setBackgroundColor(Color.parseColor("#3A3A3A"));
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private boolean validateInputs(String name, String dosage, String pills, String time) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter medicine name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (dosage.isEmpty()) {
            Toast.makeText(this, "Please enter dosage amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (pills.isEmpty()) {
            Toast.makeText(this, "Please enter number of pills", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (time.equals("Select time")) {
            Toast.makeText(this, "Please select time", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addMedicineToList(String name, String dosage, String unit, String pills, String timing, String time) {
        // Create medicine object
        Medicine medicine = new Medicine(name, dosage, unit, pills, timing, time);
        addedMedicines.add(medicine);

        // Add to Firestore
        Map<String, Object> medicineMap = new HashMap<>();
        medicineMap.put("name", name);
        medicineMap.put("dosage", dosage);
        medicineMap.put("unit", unit);
        medicineMap.put("pills", pills);
        medicineMap.put("timing", timing);
        medicineMap.put("time", time);
        medicineMap.put("taken", false);

        db.collection("medicine")
                .add(medicineMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Medicine saved to Firestore", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Create and add card
        createMedicineCard(medicine);
        updateSupplementScore();


    }


    private void createMedicineCard(Medicine medicine) {
        // Inflate card layout
        View cardView = LayoutInflater.from(this).inflate(R.layout.medicine_card_template, medicineCardsContainer, false);

        // Get references to views
        TextView nameText = cardView.findViewById(R.id.medicineName);
        TextView doseText = cardView.findViewById(R.id.medicineDose);
        TextView featuresText = cardView.findViewById(R.id.medicineFeatures);
        TextView timeText = cardView.findViewById(R.id.medicineTimeText);
        ImageView checkBox = cardView.findViewById(R.id.medicineCheckBox);

        // Set card content
        nameText.setText(medicine.name);
        doseText.setText("Dose: " + medicine.dosage + medicine.unit + " | " + medicine.pills + " pill(s)");
        featuresText.setText("Features: " + medicine.timing);
        timeText.setText("To take at " + medicine.time);
        checkBox.setImageResource(medicine.taken ?
                R.drawable.ic_check_circle_filled : R.drawable.ic_check_circle_outline);

        // Set click listeners
        cardView.setOnClickListener(v -> {
            medicine.taken = !medicine.taken;
            checkBox.setImageResource(medicine.taken ?
                    R.drawable.ic_check_circle_filled : R.drawable.ic_check_circle_outline);
            updateSupplementScore();
        });

        timeText.setOnClickListener(v -> showMedicineTimePicker(medicine, timeText));

        // Add card to the container
        medicineCardsContainer.addView(cardView);
    }

    private void showMedicineTimePicker(Medicine medicine, TextView timeView) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                R.style.CustomTimePickerTheme,
                (view, hour, minute) -> {
                    String amPm = hour >= 12 ? "PM" : "AM";
                    int displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
                    String newTime = String.format("%d:%02d %s", displayHour, minute, amPm);
                    medicine.time = newTime;
                    timeView.setText("To take at " + newTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            startActivity(new Intent(this, testdashboard.class));
        } else if (id == R.id.nav_medicine) {
            startActivity(new Intent(this, MedicineActivity.class));
        } else if (id == R.id.nav_history) {
            startActivity(new Intent(this, MedicineHistoryActivity.class));
        } else if (id == R.id.nav_notifications){

        } else if (id == R.id.nav_help){

        } else if (id == R.id.nav_settings){

        } else if (id == R.id.nav_logout) {

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}