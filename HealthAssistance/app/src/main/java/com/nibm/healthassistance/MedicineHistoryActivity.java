package com.nibm.healthassistance;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MedicineHistoryActivity extends AppCompatActivity {

    private static final String TAG = "MedicineHistory";
    private RecyclerView recyclerView;
    private MedicineHistoryAdapter adapter;
    private List<Medicine> medicineList;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;  // Matches XML id
    private TextView totalMedicinesText;
    private TextView takenMedicinesText;
    private TextView missedMedicinesText;
    private TextView emptyStateTextView;    // Text view inside empty state layout

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        initViews();
        setupRecyclerView();
        setupFirestore();
        loadMedicineData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewMedicineHistory);
        progressBar = findViewById(R.id.progressBarHistory);

        // Initialize empty state container (LinearLayout)
        emptyStateLayout = findViewById(R.id.textEmptyState);

        // Initialize text views for statistics
        totalMedicinesText = findViewById(R.id.textTotalMedicines);
        takenMedicinesText = findViewById(R.id.textTakenMedicines);
        missedMedicinesText = findViewById(R.id.textMissedMedicines);

        // Initialize the text view inside empty state layout
        emptyStateTextView = emptyStateLayout.findViewById(R.id.textEmptyStateMessage);

        medicineList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new MedicineHistoryAdapter(medicineList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void loadMedicineData() {
        // Show progress, hide other views
        progressBar.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        db.collection("medicine")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    medicineList.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Medicine medicine = document.toObject(Medicine.class);
                        if (medicine != null) {
                            medicine.setId(document.getId());
                            medicineList.add(medicine);
                        }
                    }

                    updateUI();
                    updateStats();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore error: ", e);
                    Toast.makeText(this, "Error loading medicine history", Toast.LENGTH_SHORT).show();

                    // Hide progress, show empty state with error message
                    progressBar.setVisibility(View.GONE);
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    emptyStateTextView.setText("Error loading history. Please try again.");
                });
    }

    private void updateUI() {
        progressBar.setVisibility(View.GONE);

        if (medicineList.isEmpty()) {
            // Show empty state with default message
            emptyStateLayout.setVisibility(View.VISIBLE);
            emptyStateTextView.setText("No medicine history found");
            recyclerView.setVisibility(View.GONE);
        } else {
            // Show data
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void updateStats() {
        int totalMedicines = medicineList.size();
        int takenMedicines = 0;
        int missedMedicines = 0;

        for (Medicine medicine : medicineList) {
            if (medicine.isTaken()) {
                takenMedicines++;
            } else {
                missedMedicines++;
            }
        }

        totalMedicinesText.setText(String.valueOf(totalMedicines));
        takenMedicinesText.setText(String.valueOf(takenMedicines));
        missedMedicinesText.setText(String.valueOf(missedMedicines));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedicineData(); // Refresh data when returning to activity
    }
}