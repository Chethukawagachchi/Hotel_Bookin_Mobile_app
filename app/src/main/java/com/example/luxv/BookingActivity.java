package com.example.luxv;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class BookingActivity extends AppCompatActivity {

    private TextView txtRoomDetails, txtTotalPrice;
    private EditText editTextEmail, editTextCheckIn, editTextCheckOut;
    private Button buttonConfirm;
    private DB_Operations dbOperations;
    private Integer roomID;
    private double price;
    private Toolbar toolbar;

    private static final String CHANNEL_ID = "booking_notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        txtRoomDetails = findViewById(R.id.textRoomDetails);
        txtTotalPrice = findViewById(R.id.textTotalPrice);
        editTextEmail = findViewById(R.id.editTxtEmail);
        editTextCheckIn = findViewById(R.id.editTxtCheckIn);
        editTextCheckOut = findViewById(R.id.editTxtCheckOut);
        buttonConfirm = findViewById(R.id.buttonConfirms);

        roomID = getIntent().getIntExtra("roomID", 0);
        price = getIntent().getDoubleExtra("price", 0);

        txtRoomDetails.setText("Room Number: " + roomID + "\nPrice: Rs" + price);

        dbOperations = new DB_Operations(this);

        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail != null) {
            editTextEmail.setText(userEmail);
        } else {
            Log.d("BookingActivity", "User email is null");
        }

        Button buttonAdmin = findViewById(R.id.buttonAdmin);
        buttonAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookingActivity.this, BookRooms.class);
                intent.putExtra("USER_EMAIL", userEmail);
                startActivity(intent);
            }
        });

        createNotificationChannel();

        editTextCheckIn.setOnClickListener(v -> showDatePickerDialog(editTextCheckIn));

        editTextCheckOut.setOnClickListener(v -> showDatePickerDialog(editTextCheckOut));

        buttonConfirm.setOnClickListener(v -> confirmBooking());

        checkSmsPermission();
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkRoomAvailability() {
        String checkIn = editTextCheckIn.getText().toString();
        String checkOut = editTextCheckOut.getText().toString();

        if (checkIn.isEmpty() || checkOut.isEmpty()) {
            return;
        }

        boolean isAvailable = dbOperations.isRoomAvailable(roomID, checkIn, checkOut);
        if (!isAvailable) {
            Toast.makeText(this, "Room is already booked for the selected dates. Please choose different dates.", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(this, "Selected date must be in the future", Toast.LENGTH_SHORT).show();
                    } else {
                        String date = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                        editText.setText(date);

                        if (!editTextCheckIn.getText().toString().isEmpty() && !editTextCheckOut.getText().toString().isEmpty()) {
                            calculateAndDisplayTotalPrice();
                            checkRoomAvailability();
                        }
                    }
                }, year, month, day);
        datePickerDialog.show();
    }


    private void calculateAndDisplayTotalPrice() {
        String checkIn = editTextCheckIn.getText().toString();
        String checkOut = editTextCheckOut.getText().toString();

        String[] checkInParts = checkIn.split("-");
        String[] checkOutParts = checkOut.split("-");

        Calendar selectedCheckInDate = Calendar.getInstance();
        selectedCheckInDate.set(Integer.parseInt(checkInParts[0]), Integer.parseInt(checkInParts[1]) - 1, Integer.parseInt(checkInParts[2]));

        Calendar selectedCheckOutDate = Calendar.getInstance();
        selectedCheckOutDate.set(Integer.parseInt(checkOutParts[0]), Integer.parseInt(checkOutParts[1]) - 1, Integer.parseInt(checkOutParts[2]));

        long differenceInMillis = selectedCheckOutDate.getTimeInMillis() - selectedCheckInDate.getTimeInMillis();
        long differenceInDays = differenceInMillis / (1000 * 60 * 60 * 24);

        double totalPrice = differenceInDays * price;

        txtTotalPrice.setText("Total Price: Rs." + totalPrice);
    }

    private void confirmBooking() {
        String email = editTextEmail.getText().toString();
        String checkIn = editTextCheckIn.getText().toString();
        String checkOut = editTextCheckOut.getText().toString();

        if (email.isEmpty() || checkIn.isEmpty() || checkOut.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar selectedCheckInDate = Calendar.getInstance();
        String[] checkInParts = checkIn.split("-");
        selectedCheckInDate.set(Integer.parseInt(checkInParts[0]), Integer.parseInt(checkInParts[1]) - 1, Integer.parseInt(checkInParts[2]));

        Calendar currentDate = Calendar.getInstance();
        if (selectedCheckInDate.before(currentDate)) {
            Toast.makeText(this, "Check-in date must be in the future", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isAvailable = dbOperations.isRoomAvailable(roomID, checkIn, checkOut);
        if (!isAvailable) {
            Toast.makeText(this, "Room is already booked for the selected dates. Please choose different dates.", Toast.LENGTH_LONG).show();
            return;
        }

        Calendar selectedCheckOutDate = Calendar.getInstance();
        String[] checkOutParts = checkOut.split("-");
        selectedCheckOutDate.set(Integer.parseInt(checkOutParts[0]), Integer.parseInt(checkOutParts[1]) - 1, Integer.parseInt(checkOutParts[2]));

        if (selectedCheckOutDate.before(selectedCheckInDate)) {
            Toast.makeText(this, "Check-out date must be after check-in date", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Booking")
                .setMessage("Are you sure you want to confirm this booking?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String totalPriceText = txtTotalPrice.getText().toString().replace("Total Price: Rs.", "").trim();
                    double totalPrice = Double.parseDouble(totalPriceText);

                    Reservation reservation = new Reservation(email, roomID, checkIn, checkOut, totalPrice);

                    User user = dbOperations.getUserByEmail(email);
                    if (user != null) {
                        dbOperations.reserveRoom(reservation);

                        Toast.makeText(this, "Booking confirmed! Total Price: Rs." + totalPrice, Toast.LENGTH_SHORT).show();

                        sendNotification(email, totalPrice);
                        sendSmsNotification(user.getMobile(), reservation); // Send SMS notification

                        clearFields();
                    } else {
                        Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void clearFields() {
        editTextEmail.setText("");
        editTextCheckIn.setText("");
        editTextCheckOut.setText("");
        txtTotalPrice.setText("Total Price: Rs.0.00");
    }

    private void sendSmsNotification(int mobile, Reservation reservation) {
        String message = "Booking confirmed!\nRoom Number: " + reservation.getRoomID() +
                "\nCheck-in: " + reservation.getCheckInDate() +
                "\nCheck-out: " + reservation.getCheckOutDate() +
                "\nTotal Price: Rs." + reservation.getTotalPrice();

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(String.valueOf(mobile), null, message, null, null);
    }


    private void sendNotification(String email, double totalPrice) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.c)
                .setContentTitle("Booking Confirmation")
                .setContentText("Your booking has been confirmed for room ID: " + roomID + ". Total Price: Rs." + totalPrice + ". Confirmation sent to: " + email)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (notificationManager != null) {
            Log.d("BookingActivity", "Sending notification to: " + email);
            notificationManager.notify(1, builder.build());
        } else {
            Toast.makeText(this, "Notification Manager is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri soundUri;

            try {
                // Attempt to load the custom ringtone
              //  soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ringin);
            } catch (Exception e) {
                // Fallback to default notification sound
                Log.e("BookingActivity", "Error accessing custom ringtone", e);

                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            long[] vibrationPattern = {0, 500, 1000};

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Booking Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for booking notifications");
           // channel.setSound(soundUri, null);
            channel.enableVibration(true);
            channel.setVibrationPattern(vibrationPattern);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}



