package com.example.luxv;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Handler;
import java.util.concurrent.TimeUnit;

public class Customer_Logins extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView attemptsTextView;
    private ImageButton showPasswordButton;
    private DB_Operations dbOperations;

    private int loginAttempts = 0;  // Counter for failed attempts
    private boolean isLoginBlocked = false;  // Flag to block login temporarily

    private final int MAX_ATTEMPTS = 3;
    private final int BLOCK_TIME = 30000;  // Block time in milliseconds (30 seconds)
    private Handler handler = new Handler();  // Handler for post-delayed operations
    Button buttonRegister;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_logins);

        View mainView = findViewById(R.id.main);
        if (mainView == null) {
            throw new NullPointerException("View with ID 'main' not found in the layout.");
        }

        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        ImageButton showPasswordButton = findViewById(R.id.showPasswordButton);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        buttonRegister = findViewById(R.id.buttonRegister);
        attemptsTextView = findViewById(R.id.attemptsTextView);  // Initialize the attempts TextVieww

        dbOperations = new DB_Operations(this);

        updateAttemptsText();  // Initialize attempts TextView with remaining attempts

        showPasswordButton.setOnClickListener(new View.OnClickListener() {
            private boolean isPasswordVisible = false;

            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide password
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    showPasswordButton.setImageResource(R.drawable.visible);
                } else {
                    // Show password
                    passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    showPasswordButton.setImageResource(R.drawable.witness);
                }
                isPasswordVisible = !isPasswordVisible;

                // Move the cursor to the end of the text in the password field
                passwordEditText.setSelection(passwordEditText.getText().length());
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoginBlocked) {
                    Toast.makeText(Customer_Logins.this, "Please wait 30 seconds before trying again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (validateInputs(email, password)) {
                    if (dbOperations.loginUserWithEmail(email, password)) {
                        Toast.makeText(Customer_Logins.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Customer_Logins.this, MainActivity.class);
                        intent.putExtra("USER_EMAIL", email); // Pass the email to MainActivity
                        startActivity(intent);
                        finish();
                    } else {
                        loginAttempts++;
                        updateAttemptsText();  // Update attempts after a failed login

                        if (loginAttempts >= MAX_ATTEMPTS) {
                            blockLoginFor30Seconds();
                        } else {
                            Toast.makeText(Customer_Logins.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        // Register button onClick
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Customer_Logins.this, Register.class);
                startActivity(intent);
            }
        });


        // Initialize and set listener for Cancel button
        Button cancelButton = findViewById(R.id.CancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();  // Close the app
            }
        });

        TextView adminLoginLink = findViewById(R.id.adminLoginLink);
        adminLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Customer_Logins.this, Admin_Logins.class);
                startActivity(intent);
            }
        });

    }

    private void blockLoginFor30Seconds() {
        isLoginBlocked = true;
        loginButton.setEnabled(false);
        attemptsTextView.setText("Too many attempts. Please wait 30 seconds.");  // Update TextView to indicate block time
        Toast.makeText(this, "Too many attempts. Please wait 30 seconds.", Toast.LENGTH_SHORT).show();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isLoginBlocked = false;
                loginButton.setEnabled(true);
                loginAttempts = 0;  // Reset attempts after block time
                updateAttemptsText();  // Reset attempts TextView
            }
        }, BLOCK_TIME);
    }

    private void updateAttemptsText() {
        int remainingAttempts = MAX_ATTEMPTS - loginAttempts;
        attemptsTextView.setText("Attempts remaining: " + remainingAttempts);
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 4) {
            Toast.makeText(this, "Password must be at least 4 characters long", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}
