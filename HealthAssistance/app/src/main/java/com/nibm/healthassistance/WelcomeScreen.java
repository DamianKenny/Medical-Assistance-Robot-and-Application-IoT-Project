package com.nibm.healthassistance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen); // Replace with your welcome screen XML file

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();

            Button loginButton = findViewById(R.id.btnLogin);
            Button signupButton = findViewById(R.id.btnSignup);


            // Login button click handler
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(WelcomeScreen.this, MainActivity.class));
                    // Optional: finish() if you don't want to come back to welcome screen
                }
            });

            // Sign Up button click handler
            signupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(WelcomeScreen.this, SignUp.class));
                    // Optional: finish() if you don't want to come back to welcome screen
                }
            });

        }
    }
}