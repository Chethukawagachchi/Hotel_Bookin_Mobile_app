package com.example.luxv;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import android.view.MenuItem;

public class ClientDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_dashboard);

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        // Set up toolbar with "Client Dashboard" title
        setSupportActionBar(toolbar);
        toolbar.setTitle("Client Dashboard");

        // Set up the navigation drawer toggle
        toolbar.setNavigationIcon(R.drawable.menu); // Hamburger icon

        // Set up the navigation item selection listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(ClientDashboardActivity.this, ProfileActivity.class)); // Client profile
                } else if (itemId == R.id.nav_rooms) {
                    startActivity(new Intent(ClientDashboardActivity.this, ViewRoomsActivity.class)); // View rooms
                } else if (itemId == R.id.nav_services) {
                    // startActivity(new Intent(ClientDashboardActivity.this, ViewServicesActivity.class)); // View services
                } else if (itemId == R.id.nav_bookings) {
                    startActivity(new Intent(ClientDashboardActivity.this, CustomerBookings.class)); // View bookings
                } else if (itemId == R.id.nav_reservations) {
                    startActivity(new Intent(ClientDashboardActivity.this, ReservationFormActivity.class)); // View reservations
                } else if (itemId == R.id.nav_calendar) {
                    // startActivity(new Intent(ClientDashboardActivity.this, ClientCalendarActivity.class)); // Client calendar
                } else if (itemId == R.id.nav_logout) {
                    // Perform logout actions, e.g., clear session or token
                    finish(); // Close current activity (log out)
                }

                drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer after selection
                return true;
            }
        });

        // Set up the navigation drawer toggle for toolbar icon click
        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }
}
