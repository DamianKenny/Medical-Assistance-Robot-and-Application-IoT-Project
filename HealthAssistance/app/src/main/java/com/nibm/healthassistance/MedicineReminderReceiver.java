package com.nibm.healthassistance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MedicineReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Retrieve medicine name from the intent
        String medicineName = intent.getStringExtra("medicine_name");
        String time = intent.getStringExtra("time");

        // Create intent to launch the full-screen alarm activity
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.putExtra("medicine_name", medicineName);
        alarmIntent.putExtra("time", time);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        // Start the alarm activity
        context.startActivity(alarmIntent);
    }
}