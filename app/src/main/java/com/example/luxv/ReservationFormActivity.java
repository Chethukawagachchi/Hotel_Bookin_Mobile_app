package com.example.luxv;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class ReservationFormActivity extends AppCompatActivity {

    private TextView txtServiceId, txtServiceName, txtServicePrice, textTotalPrice;
    private EditText editTxtEmail, editTxtReservationDate, editTxtTime, editTxtParticipants;
    private Button buttonConfirmReservation;
    private DB_Operations dbOperations;
    private static final int SMS_PERMISSION_CODE = 101;

    private static final String CHANNEL_ID = "reservation_notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_form);

        // Initialize views
        txtServiceId = findViewById(R.id.txtServiceId);
        txtServiceName = findViewById(R.id.txtServiceName);
        txtServicePrice = findViewById(R.id.txtServicePrice);
        editTxtEmail = findViewById(R.id.editTxtEmail);
        editTxtReservationDate = findViewById(R.id.editTxtReservationDate);
        editTxtTime = findViewById(R.id.editTxtTime);
        editTxtParticipants = findViewById(R.id.editTxtParticipants);
        textTotalPrice = findViewById(R.id.textTotalPrice);
        buttonConfirmReservation = findViewById(R.id.buttonConfirmReservation);

        dbOperations = new DB_Operations(this);

        // Get intent extras
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail != null) {
            editTxtEmail.setText(userEmail);
        }

        String serviceId = getIntent().getStringExtra("serviceId");
        String serviceName = getIntent().getStringExtra("serviceName");
        String servicePrice = getIntent().getStringExtra("servicePrice");

        txtServiceId.setText("Service ID: " + (serviceId != null ? serviceId : "Unknown"));
        txtServiceName.setText("Service Name: " + (serviceName != null ? serviceName : "Unknown"));
        txtServicePrice.setText("Price: Rs." + (servicePrice != null ? servicePrice : "0.00"));

        // Calculate total price on participants change
        editTxtParticipants.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (!s.toString().isEmpty() && servicePrice != null) {
                        calculateTotalPrice(Double.parseDouble(servicePrice));
                    } else {
                        textTotalPrice.setText("Total Price: Rs.0.00");
                    }
                } catch (NumberFormatException e) {
                    Log.e("ReservationForm", "Invalid number format", e);
                    textTotalPrice.setText("Total Price: Rs.0.00");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Check SMS permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }

        // Set button click listeners
        Button buttonAdmin = findViewById(R.id.buttonBack);
        buttonAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(ReservationFormActivity.this, BookRooms.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        editTxtReservationDate.setOnClickListener(v -> showDatePickerDialog(editTxtReservationDate));
        editTxtTime.setOnClickListener(v -> showTimePickerDialog());
        buttonConfirmReservation.setOnClickListener(v -> confirmReservation());
    }

    private void calculateTotalPrice(double servicePrice) {
        String participantsStr = editTxtParticipants.getText().toString();
        int participants = participantsStr.isEmpty() ? 0 : Integer.parseInt(participantsStr);
        double totalPrice = servicePrice * participants;
        textTotalPrice.setText("Total Price: Rs." + totalPrice);
    }

    private void confirmReservation() {
        String email = editTxtEmail.getText().toString();
        String reservationDate = editTxtReservationDate.getText().toString();
        String time = editTxtTime.getText().toString();
        String participants = editTxtParticipants.getText().toString();

        if (email.isEmpty() || reservationDate.isEmpty() || time.isEmpty() || participants.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int serviceID = Integer.parseInt(txtServiceId.getText().toString().replace("Service ID: ", ""));
            double price = Double.parseDouble(txtServicePrice.getText().toString().replace("Price: Rs.", ""));
            int participantCount = Integer.parseInt(participants);
            double totalPrice = price * participantCount;

            new AlertDialog.Builder(this)
                    .setTitle("Confirm Reservation")
                    .setMessage("Service: " + txtServiceName.getText().toString().replace("Service Name: ", "") +
                            "\nDate: " + reservationDate +
                            "\nTime: " + time +
                            "\nParticipants: " + participants +
                            "\nTotal Price: Rs." + totalPrice)
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        ServiceReservation serviceReservation = new ServiceReservation(email, serviceID, reservationDate, time, totalPrice);

                        if (dbOperations.reserveService(serviceReservation)) {
                            Toast.makeText(this, "Reservation confirmed!", Toast.LENGTH_SHORT).show();

                            User user = dbOperations.getUserByEmail(email);
                            if (user != null && user.getMobile() > 0) { // Ensure valid phone number
                                String message = "Your reservation is confirmed for " + txtServiceName.getText().toString().replace("Service Name: ", "") +
                                        " on " + reservationDate + " at " + time +
                                        ". Total Price: Rs." + totalPrice +
                                        ". Participants: " + participants;
                                sendSMS(String.valueOf(user.getMobile()), message);
                            }

                            sendNotification(txtServiceName.getText().toString().replace("Service Name: ", ""), totalPrice, reservationDate, participantCount);
                            clearFields();
                        } else {
                            Toast.makeText(this, "Failed to confirm reservation", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePickerDialog(EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    if (selectedDate.before(calendar)) {
                        Toast.makeText(this, "Please select a future date", Toast.LENGTH_SHORT).show();
                    } else {
                        String date = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                        editText.setText(date);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (TimePicker view, int selectedHour, int selectedMinute) -> {
                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                    editTxtTime.setText(time);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Toast.makeText(this, "SMS sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String serviceName, double totalPrice, String reservationDate, int participantCount) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Reservation Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for reservation notifications");
            channel.setSound(soundUri, null);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String message = "Service: " + serviceName + "\nTotal Price: Rs." + totalPrice + "\nDate: " + reservationDate + "\nParticipants: " + participantCount;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.l)
                .setContentTitle("Reservation Confirmed")
                .setContentText("Your reservation is confirmed.")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }

    private void clearFields() {
        editTxtEmail.getText().clear();
        editTxtReservationDate.getText().clear();
        editTxtTime.getText().clear();
        editTxtParticipants.getText().clear();
        textTotalPrice.setText("Total Price: Rs.0.00");
    }
}
