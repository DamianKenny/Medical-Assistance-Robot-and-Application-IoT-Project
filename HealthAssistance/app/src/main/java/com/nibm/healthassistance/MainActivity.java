package com.nibm.healthassistance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class MainActivity extends AppCompatActivity {

    EditText username, password;
    Button loginButton;
    TextView signupText;

    DatabaseHelper dbHelper; // <-- Add this

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signupText = findViewById(R.id.signupText);
        dbHelper = new DatabaseHelper(this); // Initialize DB helper

        loginButton.setOnClickListener(v -> {
            String inputUsername = username.getText().toString().trim();
            String inputPassword = password.getText().toString().trim();

            if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            } else {
                boolean userExists = dbHelper.checkUser(inputUsername, inputPassword);

                if (userExists) {
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MainActivity.this, testdashboard.class);
                    intent.putExtra("email", inputUsername); // <-- Pass email here
                    startActivity(intent);
                    finish(); // Optional
                }
                else {
                    Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signupText.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SignUp.class));
            finish();
        });
    }
}
