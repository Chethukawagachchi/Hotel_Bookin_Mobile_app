package com.example.luxv;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ViewBookingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookingAdapter bookingAdapter;
    private List<Reservation> reservationList;
    private DB_Operations dbOperations;
    private EditText editTextSearchEmail;
    private Button buttonSearch;
    private Button buttonAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bookings);

        // Initialize database operations
        dbOperations = new DB_Operations(this);

        // Initialize UI components
        initializeViews();

        // Set up RecyclerView
        setupRecyclerView();

        // Set up click listeners
        setupClickListeners();

        // Check and notify upcoming bookings
        notifyUpcomingBookings();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewBookings);
        editTextSearchEmail = findViewById(R.id.editTextSearchEmail);
        buttonSearch = findViewById(R.id.buttonSearch);
        buttonAdmin = findViewById(R.id.buttonAdmin);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reservationList = getBookingsFromDatabase();
        bookingAdapter = new BookingAdapter(this, reservationList);
        recyclerView.setAdapter(bookingAdapter);
    }

    private void setupClickListeners() {
        // Admin button click listener
        if (buttonAdmin != null) {
            buttonAdmin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ViewBookingsActivity.this, AdminDashboardActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Search button click listener
        if (buttonSearch != null) {
            buttonSearch.setOnClickListener(v -> {
                String email = editTextSearchEmail.getText().toString().trim();
                if (!email.isEmpty()) {
                    searchBookingsByEmail(email);
                } else {
                    Toast.makeText(ViewBookingsActivity.this,
                            "Please enter an email to search.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private List<Reservation> getBookingsFromDatabase() {
        List<Reservation> reservations = new ArrayList<>();
        SQLiteDatabase database = dbOperations.getReadableDatabase();

        try {
            Cursor cursor = database.rawQuery("SELECT * FROM tblReservation", null);
            if (cursor.moveToFirst()) {
                do {
                    String email = cursor.getString(cursor.getColumnIndexOrThrow("Email"));
                    int roomID = cursor.getInt(cursor.getColumnIndexOrThrow("RoomID"));
                    String checkInDate = cursor.getString(cursor.getColumnIndexOrThrow("CheckInDate"));
                    String checkOutDate = cursor.getString(cursor.getColumnIndexOrThrow("CheckOutDate"));
                    double totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("TotalPrice"));

                    Reservation reservation = new Reservation(email, roomID, checkInDate,
                            checkOutDate, totalPrice);
                    reservations.add(reservation);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
        return reservations;
    }

    private void searchBookingsByEmail(String email) {
        ArrayList<Reservation> filteredBookings = dbOperations.getBookingsByEmail(email);

        if (filteredBookings != null && !filteredBookings.isEmpty()) {
            bookingAdapter = new BookingAdapter(this, filteredBookings);
            recyclerView.setAdapter(bookingAdapter);
        } else {
            Toast.makeText(this,
                    "No bookings found for the email provided.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyUpcomingBookings() {
        List<Reservation> upcomingReservations = new ArrayList<>();
        Calendar today = Calendar.getInstance();

        for (Reservation reservation : reservationList) {
            try {
                Calendar checkIn = Calendar.getInstance();
                String[] dateParts = reservation.getCheckInDate().split("-");
                checkIn.set(Calendar.YEAR, Integer.parseInt(dateParts[0]));
                checkIn.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
                checkIn.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[2]));

                if (checkIn.after(today)) {
                    upcomingReservations.add(reservation);
                    sendNotification(reservation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendNotification(Reservation reservation) {
        String channelId = "booking_notifications";
        String channelName = "Booking Notifications";
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, ViewBookingsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        android.app.Notification.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new android.app.Notification.Builder(this, channelId)
                    .setContentTitle("Upcoming Booking")
                    .setContentText("You have an upcoming booking on " +
                            reservation.getCheckInDate() + ".")
                    .setSmallIcon(R.drawable.icon)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            if (builder != null) {
                notificationManager.notify((int) System.currentTimeMillis(), builder.build());
            }
        }
    }
}