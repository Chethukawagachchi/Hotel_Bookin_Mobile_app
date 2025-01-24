package com.example.luxv;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ViewServiceReservationActivity extends AppCompatActivity {

    private RecyclerView recyclerViewServiceReservations;
    private ServiceReservationAdapter adapter;
    private ArrayList<ServiceReservation> serviceReservations;
    private DB_Operations dbOperations;
    private EditText editTextSearchEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_service_reservation);

        dbOperations = new DB_Operations(this);

        recyclerViewServiceReservations = findViewById(R.id.recyclerViewServiceReservations);
        recyclerViewServiceReservations.setLayoutManager(new LinearLayoutManager(this));

        serviceReservations = new ArrayList<>();

        fetchServiceReservations();

        adapter = new ServiceReservationAdapter(this, serviceReservations);
        recyclerViewServiceReservations.setAdapter(adapter);

        editTextSearchEmail = findViewById(R.id.editTextSearchEmail);
        Button buttonSearch = findViewById(R.id.buttonSearch);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextSearchEmail.getText().toString().trim();
                if (!email.isEmpty()) {
                    searchReservationsByEmail(email);
                } else {
                    fetchServiceReservations();
                }
            }
        });

        Button buttonAdmin = findViewById(R.id.buttonAdmin);
        buttonAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewServiceReservationActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        });

        notifyUpcomingServiceReservations();
    }

    private void fetchServiceReservations() {
        serviceReservations.clear();

        serviceReservations.addAll(dbOperations.getAllServiceReservations());

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void searchReservationsByEmail(String email) {
        List<ServiceReservation> filteredReservations = dbOperations.getServiceReservationsByEmail(email);

        if (filteredReservations != null && !filteredReservations.isEmpty()) {
            serviceReservations.clear();
            serviceReservations.addAll(filteredReservations);
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "No reservations found for the email provided.", Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyUpcomingServiceReservations() {
        for (ServiceReservation reservation : serviceReservations) {
            if (isUpcoming(reservation)) {
                sendNotification(reservation);
            }
        }
    }

    private boolean isUpcoming(ServiceReservation reservation) {
        Calendar reservationDate = Calendar.getInstance();
        String[] dateParts = reservation.getReservationDate().split("-");
        reservationDate.set(Calendar.YEAR, Integer.parseInt(dateParts[0]));
        reservationDate.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
        reservationDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[2]));

        return reservationDate.after(Calendar.getInstance());
    }

    private void sendNotification(ServiceReservation reservation) {
        String channelId = "service_reservation_notifications";
        String channelName = "Service Reservation Notifications";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, ViewServiceReservationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        android.app.Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new android.app.Notification.Builder(this, channelId)
                    .setContentTitle("Upcoming Service Reservation")
                    .setContentText("You have an upcoming service reservation on " + reservation.getReservationDate() + ".")
                    .setSmallIcon(R.drawable.icon)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
