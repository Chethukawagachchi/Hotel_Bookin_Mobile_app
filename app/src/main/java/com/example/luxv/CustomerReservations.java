package com.example.luxv;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import Toolbar
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CustomerReservations extends AppCompatActivity {

    private RecyclerView reservationsRecyclerView;
    private CustomerReservationAdapter adapter;
    private ArrayList<ServiceReservation> allReservations;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_reservations);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        reservationsRecyclerView = findViewById(R.id.reservations_recycler_view);
        reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        loadReservations(userEmail);

        Button filterByDateButton = findViewById(R.id.filter_by_date_button);
        TextView selectedDateTextView = findViewById(R.id.selected_date_textview);

        filterByDateButton.setOnClickListener(v -> {
            showDatePicker(selectedDateTextView);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        notifyUpcomingReservations(userEmail);
    }

    private void loadReservations(String email) {
        DB_Operations dbOperations = new DB_Operations(this);
        allReservations = dbOperations.getServiceReservationsByEmail(email);
        adapter = new CustomerReservationAdapter(allReservations);
        reservationsRecyclerView.setAdapter(adapter);
    }

    private void showDatePicker(TextView selectedDateTextView) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
            selectedDateTextView.setText(selectedDate);
            filterReservationsByDate(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void filterReservationsByDate(String date) {
        ArrayList<ServiceReservation> filteredReservations = new ArrayList<>();
        for (ServiceReservation reservation : allReservations) {
            if (reservation.getReservationDate().equals(date)) {
                filteredReservations.add(reservation);
            }
        }
        adapter = new CustomerReservationAdapter(filteredReservations);
        reservationsRecyclerView.setAdapter(adapter);
    }

    private void notifyUpcomingReservations(String userEmail) {
        for (ServiceReservation reservation : allReservations) {
            if (isUpcoming(reservation)) {
                sendNotification(reservation);
            }
        }
    }

    private boolean isUpcoming(ServiceReservation reservation) {
        Calendar reservationDate = Calendar.getInstance();
        String[] dateParts = reservation.getReservationDate().split("-");
        reservationDate.set(Calendar.YEAR, Integer.parseInt(dateParts[0]));
        reservationDate.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1); // Months are 0-based
        reservationDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[2]));

        return reservationDate.after(Calendar.getInstance());
    }

    private void sendNotification(ServiceReservation reservation) {
        String channelId = "customer_reservation_notifications";
        String channelName = "Customer Reservation Notifications";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, CustomerReservations.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        android.app.Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new android.app.Notification.Builder(this, channelId)
                    .setContentTitle("Upcoming Service Reservation")
                    .setContentText("You have an upcoming reservation on " + reservation.getReservationDate() + ".")
                    .setSmallIcon(R.drawable.icon)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
