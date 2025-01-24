package com.example.luxv;

import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.ParseException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CalenderActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView dateInfoText;
    private Set<String> bookedDates;
    private Set<String> reservedDates;
    private Map<String, String> dateNotes;
    private DB_Operations dbOperations;
    private String userEmail;
    private FloatingActionButton fabAddNote;
    private static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calender);

        initializeViews();
        setupWindowInsets();
        setupToolbar();
        initializeData();
        setupCalendarListeners();
        setupFab();
    }

    private void initializeViews() {
        calendarView = findViewById(R.id.calendarView);
        dateInfoText = findViewById(R.id.dateInfoText);
        fabAddNote = findViewById(R.id.fabAddNote);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Bookings Calendar");
        }
    }

    private void initializeData() {
        bookedDates = new HashSet<>();
        reservedDates = new HashSet<>();
        dateNotes = new HashMap<>();
        dbOperations = new DB_Operations(this);

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail != null) {
            loadBookedDates(userEmail);
            loadReservedDates(userEmail);
            loadSavedNotes();
        } else {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupCalendarListeners() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = formatDate(year, month + 1, dayOfMonth);
            showDateDetails(selectedDate);
        });

        // Disable past dates
        Calendar today = Calendar.getInstance();
        calendarView.setMinDate(today.getTimeInMillis());

        // Set maximum date to 1 year from now
        today.add(Calendar.YEAR, 1);
        calendarView.setMaxDate(today.getTimeInMillis());
    }

    private void setupFab() {
        fabAddNote.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(calendarView.getDate());
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(calendar.getTime());
            showAddNoteDialog(currentDate);
        });
    }

    private void showDateDetails(String selectedDate) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        bottomSheet.setContentView(R.layout.bottom_sheet_date_details);

        TextView statusText = bottomSheet.findViewById(R.id.statusText);
        TextView noteText = bottomSheet.findViewById(R.id.noteText);
        ChipGroup chipGroup = bottomSheet.findViewById(R.id.chipGroup);

        // Add status chips
        if (bookedDates.contains(selectedDate)) {
            addChip(chipGroup, "Booked", Color.RED);
        }
        if (reservedDates.contains(selectedDate)) {
            addChip(chipGroup, "Service Reserved", Color.BLUE);
        }
        if (!bookedDates.contains(selectedDate) && !reservedDates.contains(selectedDate)) {
            addChip(chipGroup, "Available", Color.GREEN);
        }

        // Show note if exists
        String note = dateNotes.get(selectedDate);
        if (note != null && !note.isEmpty()) {
            noteText.setText("Note: " + note);
            noteText.setVisibility(android.view.View.VISIBLE);
        } else {
            noteText.setVisibility(android.view.View.GONE);
        }

        bottomSheet.show();
    }

    private void addChip(ChipGroup chipGroup, String text, int color) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setChipBackgroundColorResource(android.R.color.white);
        chip.setTextColor(color);
        chip.setClickable(false);
        chipGroup.addView(chip);
    }

    private void showAddNoteDialog(String date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Note for " + date);

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setText(dateNotes.getOrDefault(date, ""));
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String note = input.getText().toString().trim();
            if (!note.isEmpty()) {
                dateNotes.put(date, note);
                saveNoteToDatabase(date, note);
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
            } else {
                dateNotes.remove(date);
                deleteNoteFromDatabase(date);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calendar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_filter) {
            showFilterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        String[] options = {"All", "Booked Only", "Services Only", "Available Only"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Calendar View")
                .setItems(options, (dialog, which) -> {
                    // Handle filter selection
                    filterCalendarView(which);
                });
        builder.show();
    }

    private void filterCalendarView(int filterOption) {
        // Implement calendar filtering based on selection
        // This would require custom calendar view implementation
        // For now, just show what's being filtered
        String[] filterNames = {"All Dates", "Booked Dates", "Service Dates", "Available Dates"};
        Toast.makeText(this, "Showing " + filterNames[filterOption], Toast.LENGTH_SHORT).show();
    }

    private String formatDate(int year, int month, int day) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
    }

    private void loadBookedDates(String email) {
        ArrayList<Reservation> bookings = dbOperations.getBookingsByEmail(email);
        for (Reservation booking : bookings) {
            addDateRange(booking.getCheckInDate(), booking.getCheckOutDate(), bookedDates);
        }
    }

    private void loadReservedDates(String email) {
        ArrayList<ServiceReservation> reservations = dbOperations.getServiceReservationsByEmail(email);
        for (ServiceReservation reservation : reservations) {
            reservedDates.add(reservation.getReservationDate());
        }
    }

    private void addDateRange(String startDate, String endDate, Set<String> dateSet) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);

            while (calendar.getTime().before(end) || calendar.getTime().equals(end)) {
                dateSet.add(sdf.format(calendar.getTime()));
                calendar.add(Calendar.DATE, 1);
            }
        } catch (ParseException | java.text.ParseException e) {
            e.printStackTrace();
        }
    }

    // Methods for handling notes in database
    private void loadSavedNotes() {
        // Implement loading notes from database
    }

    private void saveNoteToDatabase(String date, String note) {
        // Implement saving note to database
    }

    private void deleteNoteFromDatabase(String date) {
        // Implement deleting note from database
    }
}