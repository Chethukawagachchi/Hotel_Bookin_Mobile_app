package com.example.luxv;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import android.text.TextWatcher;
import android.text.Editable;
import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {
    private RecyclerView usersRecyclerView;
    private TextInputEditText searchEditText;
    private DB_Operations dbOperations;
    private List<User> allUsers;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Registered Users");

        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        // Initialize views
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        MaterialButton searchButton = findViewById(R.id.searchButton);

        // Initialize RecyclerView
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter();
        usersRecyclerView.setAdapter(adapter);

        dbOperations = new DB_Operations(this);

        // Load all users initially
        loadUsers();

        // Search button click listener
        searchButton.setOnClickListener(v -> performSearch());

        // Real-time search as user types
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        allUsers = dbOperations.getAllUsers();
        adapter.setUsers(allUsers);
    }

    private void performSearch() {
        String searchTerm = searchEditText.getText().toString().toLowerCase().trim();
        List<User> filteredUsers = new ArrayList<>();

        for (User user : allUsers) {
            if (user.getEmail().toLowerCase().contains(searchTerm)) {
                filteredUsers.add(user);
            }
        }

        adapter.setUsers(filteredUsers);
    }
}