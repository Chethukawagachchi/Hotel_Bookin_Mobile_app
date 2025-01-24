package com.example.luxv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Reservation> reservations;
    private Context context;

    public BookingAdapter(Context context, List<Reservation> reservations) {
        this.context = context;
        this.reservations = reservations;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        holder.txtBookingEmail.setText("Email: " + reservation.getEmail());
        holder.txtRoomID.setText("Room Number: " + reservation.getRoomID());
        holder.txtCheckInDate.setText("Check-In Date: " + reservation.getCheckInDate());
        holder.txtCheckOutDate.setText("Check-Out Date: " + reservation.getCheckOutDate());
        holder.txtTotalPrice.setText("Total Price: Rs." + reservation.getTotalPrice());
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView txtBookingEmail, txtRoomID, txtCheckInDate, txtCheckOutDate, txtTotalPrice;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBookingEmail = itemView.findViewById(R.id.txtBookingEmail);
            txtRoomID = itemView.findViewById(R.id.txtRoomID);
            txtCheckInDate = itemView.findViewById(R.id.txtCheckInDate);
            txtCheckOutDate = itemView.findViewById(R.id.txtCheckOutDate);
            txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);
        }
    }
}
