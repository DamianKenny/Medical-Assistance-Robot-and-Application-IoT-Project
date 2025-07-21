package com.nibm.healthassistance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SignUp extends AppCompatActivity {

    EditText emailInput, usernameInput, passwordInput;
    Button signupButton;
    DatabaseHelper dbHelper;
    TextView loginRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up); // make sure this is your XML file name

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Bind UI elements
        emailInput = findViewById(R.id.email);
        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        signupButton = findViewById(R.id.loginButton); // even though it's "loginButton", it says "Sign Up" so it's fine
        loginRedirectText = findViewById(R.id.loginRedirectText);

        dbHelper = new DatabaseHelper(this);

        signupButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                boolean success = dbHelper.insertUser(username, email, password);
                if (success) {
                    Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUp.this, MainActivity.class)); // go to login
                    finish();
                } else {
                    Toast.makeText(this, "User already exists or error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginRedirectText.setOnClickListener(v -> {
            startActivity(new Intent(SignUp.this, MainActivity.class));
            finish();
        });
    }
}
