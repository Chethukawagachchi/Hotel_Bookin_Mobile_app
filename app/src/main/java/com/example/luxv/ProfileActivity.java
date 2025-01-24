package com.example.luxv;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProfileActivity extends AppCompatActivity {

    private EditText editUsername, editMobile, editAddress, editEmail;
    private Button btnUpdate;
    private DB_Operations dbOperations;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editUsername = findViewById(R.id.editUsername);
        editMobile = findViewById(R.id.editMobile);
        editAddress = findViewById(R.id.editAddress);
        editEmail = findViewById(R.id.editEmail);
        btnUpdate = findViewById(R.id.btnUpdate);

        dbOperations = new DB_Operations(this);

        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail != null) {
            Log.d("ProfileActivity", "Fetching user with email: " + userEmail);
            currentUser = dbOperations.getUserByEmail(userEmail);
            if (currentUser != null) {
                displayUserData();
            } else {
                Toast.makeText(this, "No user details found", Toast.LENGTH_SHORT).show();
                Log.e("ProfileActivity", "User not found for email: " + userEmail);
            }
        } else {
            Toast.makeText(this, "Invalid user email", Toast.LENGTH_SHORT).show();
            Log.e("ProfileActivity", "USER_EMAIL not provided in intent");
        }

        btnUpdate.setOnClickListener(v -> updateUserDetails());
    }

    private void displayUserData() {
        Log.d("ProfileActivity", "Displaying user data: " + currentUser);
        editUsername.setText(currentUser.getUsername());
        editMobile.setText(String.valueOf(currentUser.getMobile()));
        editAddress.setText(currentUser.getAddress());
        editEmail.setText(currentUser.getEmail());
    }

    private void updateUserDetails() {
        currentUser.setUsername(editUsername.getText().toString());
        currentUser.setMobile(Integer.parseInt(editMobile.getText().toString()));
        currentUser.setAddress(editAddress.getText().toString());

        boolean isUpdated = dbOperations.updateUsers(currentUser);
        if (isUpdated) {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
