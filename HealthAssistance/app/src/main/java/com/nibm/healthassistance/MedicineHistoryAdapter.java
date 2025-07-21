package com.nibm.healthassistance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MedicineHistoryAdapter extends RecyclerView.Adapter<MedicineHistoryAdapter.MedicineViewHolder> {

    private List<Medicine> medicineList;

    public MedicineHistoryAdapter(List<Medicine> medicineList) {
        this.medicineList = medicineList;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine_history, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);

        // Safely handle medicine name
        String name = medicine.getName() != null ? medicine.getName() : "";
        if (!name.isEmpty()) {
            holder.textMedicineName.setText(name.substring(0, 1).toUpperCase() +
                    name.substring(1).toLowerCase());
        } else {
            holder.textMedicineName.setText("Unknown Medicine");
        }

        // Safely handle dosage and unit
        String dosage = medicine.getDosage() != null ? medicine.getDosage() : "";
        String unit = medicine.getUnit() != null ? medicine.getUnit() : "";
        holder.textDosage.setText(String.format("%s %s", dosage, unit).trim());

        // Safely handle pills
        holder.textPills.setText(medicine.getPills() != null ? medicine.getPills() : "");

        // Safely handle time
        holder.textTime.setText(medicine.getTime() != null ? medicine.getTime() : "");

        // Safely handle timing
        holder.textTiming.setText(medicine.getTiming() != null ? medicine.getTiming() : "");

        // Safely handle date
        holder.textDate.setText(medicine.getFormattedDate() != null ?
                medicine.getFormattedDate() : "");

        // Safely handle status
        String statusText = medicine.isTaken() ? "Taken" : "Missed";
        holder.textStatus.setText(statusText);

        // Set status color
        int statusColor;
        int indicatorColor;
        if (medicine.isTaken()) {
            statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.green_400);
            indicatorColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.green_500);
        } else {
            statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.red_400);
            indicatorColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.red_500);
        }
        holder.textStatus.setTextColor(statusColor);
        holder.statusIndicator.setBackgroundColor(indicatorColor);
    }

    @Override
    public int getItemCount() {
        return medicineList != null ? medicineList.size() : 0;
    }

    public static class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView textMedicineName;
        TextView textDosage;
        TextView textPills;
        TextView textTime;
        TextView textTiming;
        TextView textDate;
        TextView textStatus;
        View statusIndicator;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            textMedicineName = itemView.findViewById(R.id.textMedicineName);
            textDosage = itemView.findViewById(R.id.textDosage);
            textPills = itemView.findViewById(R.id.textPills);
            textTime = itemView.findViewById(R.id.textTime);
            textTiming = itemView.findViewById(R.id.textTiming);
            textDate = itemView.findViewById(R.id.textDate);
            textStatus = itemView.findViewById(R.id.textStatus);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }
    }

    // Helper method to update data
    public void updateData(List<Medicine> newList) {
        medicineList = newList;
        notifyDataSetChanged();
    }
}