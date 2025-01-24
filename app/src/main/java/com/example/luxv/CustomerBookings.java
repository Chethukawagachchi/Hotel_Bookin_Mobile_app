package com.example.luxv;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CustomerBookings extends AppCompatActivity {

    private RecyclerView bookingsRecyclerView;
    private CustomerBookingsAdapter bookingsAdapter;
    private ArrayList<Reservation> allBookings, filteredBookings;
    private String selectedStatus = "All";
    private Date selectedCheckInDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_bookings);

        // Set up toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Retrieve and display bookings
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null) {
            Toast.makeText(this, "User email not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bookingsRecyclerView = findViewById(R.id.bookings_recycler_view);
        bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        DB_Operations dbOperations = new DB_Operations(this);
        allBookings = dbOperations.getBookingsByEmail(userEmail);
        filteredBookings = new ArrayList<>(allBookings);
        bookingsAdapter = new CustomerBookingsAdapter(filteredBookings);
        bookingsRecyclerView.setAdapter(bookingsAdapter);

        // Initialize filters
        setupStatusFilter();
        setupDateFilter();

        // Notify user of upcoming bookings
        notifyUpcomingBookings();
    }

    private void setupStatusFilter() {
        Spinner statusFilter = findViewById(R.id.status_filter);
        statusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus = parent.getItemAtPosition(position).toString();
                filterBookings();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupDateFilter() {
        Button dateFilterButton = findViewById(R.id.date_filter_button);
        dateFilterButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(CustomerBookings.this,
                    (DatePicker view, int year, int month, int dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        selectedCheckInDate = calendar.getTime();
                        dateFilterButton.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCheckInDate));
                        filterBookings();
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
    }

    private void filterBookings() {
        filteredBookings.clear();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (Reservation booking : allBookings) {
            boolean matchesStatus = true;  // Default to true
            boolean matchesDate = selectedCheckInDate == null;

            // Determine status based on the check-out date
            String status;
            try {
                Date checkOutDate = dateFormat.parse(booking.getCheckOutDate());
                Date currentDate = new Date();

                if (checkOutDate != null && checkOutDate.before(currentDate)) {
                    status = "Expired";
                } else {
                    status = "Pending";
                }

                // Match status with selected filter
                matchesStatus = selectedStatus.equals("All") || status.equals(selectedStatus);

                if (selectedCheckInDate != null) {
                    Date bookingCheckInDate = dateFormat.parse(booking.getCheckInDate());

                    // Compare only the date portions (ignoring time)
                    Calendar selectedCal = Calendar.getInstance();
                    Calendar bookingCal = Calendar.getInstance();
                    selectedCal.setTime(selectedCheckInDate);
                    bookingCal.setTime(bookingCheckInDate);

                    matchesDate = (selectedCal.get(Calendar.YEAR) == bookingCal.get(Calendar.YEAR)) &&
                            (selectedCal.get(Calendar.MONTH) == bookingCal.get(Calendar.MONTH)) &&
                            (selectedCal.get(Calendar.DAY_OF_MONTH) == bookingCal.get(Calendar.DAY_OF_MONTH));
                }
            } catch (ParseException e) {
                e.printStackTrace();
                matchesStatus = false;  // Error case
            }

            if (matchesStatus && matchesDate) {
                filteredBookings.add(booking);
            }
        }
        bookingsAdapter.notifyDataSetChanged();
    }


    private void notifyUpcomingBookings() {
        for (Reservation reservation : allBookings) {
            if (isUpcoming(reservation)) {
                sendNotification(reservation);
            }
        }
    }

    private boolean isUpcoming(Reservation reservation) {
        Calendar checkIn = Calendar.getInstance();
        String[] dateParts = reservation.getCheckInDate().split("-");
        checkIn.set(Calendar.YEAR, Integer.parseInt(dateParts[0]));
        checkIn.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1); // Months are 0-based
        checkIn.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[2]));

        return checkIn.after(Calendar.getInstance());
    }

    private void sendNotification(Reservation reservation) {
        String channelId = "booking_notifications";
        String channelName = "Booking Notifications";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, CustomerBookings.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        android.app.Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new android.app.Notification.Builder(this, channelId)
                    .setContentTitle("Upcoming Booking")
                    .setContentText("You have an upcoming booking on " + reservation.getCheckInDate() + ".")
                    .setSmallIcon(R.drawable.l) // Ensure you have this drawable
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
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