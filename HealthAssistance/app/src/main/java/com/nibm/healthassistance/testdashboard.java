package com.nibm.healthassistance;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class testdashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private TextView supplementScoreText, heartRateText, temperatureText;
    private LineChart heartRateChart;
    private List<Entry> heartRateEntries = new ArrayList<>();
    private String[] weekDays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initViews();
        findViewById(R.id.menuButton).setOnClickListener(v -> toggleDrawer());
        setupHeader();
        setupHeartRateChart();
        loadHeartRateChartSampleData();
        readSensorDataFromFirebase();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void initViews() {
        try {
            supplementScoreText = findViewById(R.id.supplementScoreText);
            heartRateText = findViewById(R.id.heart_rate_value);
            temperatureText = findViewById(R.id.temperature_value);
            heartRateChart = findViewById(R.id.heart_rate_chart);
        } catch (Exception e) {
            Toast.makeText(this, "View init error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView userEmailText = headerView.findViewById(R.id.userEmail);
        String email = getIntent().getStringExtra("email");
        if (email != null) userEmailText.setText(email);
        headerView.findViewById(R.id.closeButton).setOnClickListener(v ->
                drawerLayout.closeDrawer(GravityCompat.START));
    }

    private void toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void setupHeartRateChart() {
        heartRateChart.setBackgroundColor(Color.parseColor("#2A2A2A"));
        heartRateChart.setDrawGridBackground(false);
        heartRateChart.setDrawBorders(false);
        Description description = new Description();
        description.setText("");
        heartRateChart.setDescription(description);
        heartRateChart.getLegend().setEnabled(false);

        XAxis xAxis = heartRateChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(weekDays));
        xAxis.setTextSize(12f);

        YAxis leftAxis = heartRateChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#404040"));
        leftAxis.setAxisMinimum(50f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setTextSize(12f);

        heartRateChart.getAxisRight().setEnabled(false);
        heartRateChart.setTouchEnabled(true);
        heartRateChart.setDragEnabled(true);
        heartRateChart.setScaleEnabled(false);
        heartRateChart.setPinchZoom(false);
    }

    private void loadHeartRateChartSampleData() {
        float[] data = {72f, 68f, 75f, 70f, 73f, 69f, 71f};
        updateHeartRateData(data);
    }

    private void updateHeartRateData(float[] weeklyData) {
        heartRateEntries.clear();
        for (int i = 0; i < weeklyData.length; i++) {
            heartRateEntries.add(new Entry(i, weeklyData[i]));
        }

        LineDataSet dataSet = new LineDataSet(heartRateEntries, "Heart Rate");
        dataSet.setColor(Color.parseColor("#9C27B0"));
        dataSet.setLineWidth(3f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.parseColor("#9C27B0"));
        dataSet.setCircleRadius(6f);
        dataSet.setCircleHoleRadius(3f);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setDrawFilled(true);

        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#AA9C27B0"), Color.parseColor("#209C27B0")}
        );
        dataSet.setFillDrawable(gradientDrawable);

        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        heartRateChart.setData(lineData);
        heartRateChart.invalidate();
    }

    private void readSensorDataFromFirebase() {
//        DatabaseReference sensorsRef = FirebaseDatabase.getInstance()
//                .getReference("sensors");
//
//        sensorsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    Float heartRate = snapshot.child("heartRate").getValue(Float.class);
//                    Float temperature = snapshot.child("temperature").getValue(Float.class);
//
//                    if (heartRate != null) {
//                        heartRateText.setText(heartRate + " bpm");
//                    }
//                    if (temperature != null) {
//                        temperatureText.setText(temperature + " °C");
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("Firebase", "Error: " + error.getMessage());
//            }

        DatabaseReference heartRateRef = FirebaseDatabase.getInstance()
                .getReference("heartRate");

        heartRateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Get heart rate value
                    if (snapshot.hasChild("HeartRate")) {
                        Long heartRate = snapshot.child("HeartRate").getValue(Long.class);
                        if (heartRate != null) {
                            heartRateText.setText(String.valueOf(heartRate));
                        }
                    }

                    // Get temperature value
                    if (snapshot.hasChild("Temperature")) {
                        Double temperature = snapshot.child("Temperature").getValue(Double.class);
                        if (temperature != null) {
                            temperatureText.setText(String.format("%.1f", temperature));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(this, testdashboard.class));
        } else if (id == R.id.nav_medicine) {
            startActivity(new Intent(this, MedicineActivity.class));
        } else if (id == R.id.nav_history) {
            startActivity(new Intent(this, Medicine.class));
        } else if (id == R.id.nav_notifications){

        } else if (id == R.id.nav_help){

        } else if (id == R.id.nav_settings){

        } else if (id == R.id.nav_logout) {

        }

        drawerLayout.closeDrawer(GravityCompat.START);
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
