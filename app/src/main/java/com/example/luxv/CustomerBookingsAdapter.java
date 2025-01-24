package com.example.luxv;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CustomerBookingsAdapter extends RecyclerView.Adapter<CustomerBookingsAdapter.BookingViewHolder> {

    private final ArrayList<Reservation> bookings;

    public CustomerBookingsAdapter(ArrayList<Reservation> bookings) {
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customerbooking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Reservation reservation = bookings.get(position);
        holder.roomId.setText("Room Number: " + reservation.getRoomID());
        holder.checkInDate.setText("Check-In: " + reservation.getCheckInDate());
        holder.checkOutDate.setText("Check-Out: " + reservation.getCheckOutDate());
        holder.totalPrice.setText("Total Price: Rs." + reservation.getTotalPrice());

        // Parse the check-out date to determine status
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date checkOut = dateFormat.parse(reservation.getCheckOutDate());
            Date currentDate = new Date();

            if (checkOut != null && checkOut.before(currentDate)) {
                // Booking is expired
                reservation.setStatus("Expired");
                holder.status.setText("Expired");
                holder.status.setTextColor(Color.RED);
            } else {
                // Booking is pending
                reservation.setStatus("Pending");
                holder.status.setText("Pending");
                holder.status.setTextColor(Color.GREEN);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }



    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView roomId, checkInDate, checkOutDate, totalPrice, status;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            roomId = itemView.findViewById(R.id.room_id);
            checkInDate = itemView.findViewById(R.id.check_in_date);
            checkOutDate = itemView.findViewById(R.id.check_out_date);
            totalPrice = itemView.findViewById(R.id.total_price);
            status = itemView.findViewById(R.id.status);
        }
    }
}
