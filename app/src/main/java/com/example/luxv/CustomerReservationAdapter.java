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

public class CustomerReservationAdapter extends RecyclerView.Adapter<CustomerReservationAdapter.ViewHolder> {

    private ArrayList<ServiceReservation> reservations;

    public CustomerReservationAdapter(ArrayList<ServiceReservation> reservations) {
        this.reservations = reservations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customerreservations, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceReservation reservation = reservations.get(position);
        holder.reservationDateTextView.setText(reservation.getReservationDate());
        holder.reservationTimeTextView.setText(reservation.getReservationTime());
        holder.totalPriceTextView.setText(String.format("Total: %.2f", reservation.getPrice()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date reservationDate;
        try {
            reservationDate = sdf.parse(reservation.getReservationDate());
            Date currentDate = new Date();
            if (reservationDate.before(currentDate)) {
                holder.status.setTextColor(Color.RED);
                holder.status.setText("Expired");
            } else {
                holder.status.setTextColor(Color.GREEN);
                holder.status.setText("Pending");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView reservationDateTextView;
        TextView reservationTimeTextView;
        TextView totalPriceTextView;
        TextView status;

        ViewHolder(View itemView) {
            super(itemView);
            reservationDateTextView = itemView.findViewById(R.id.reservation_date);
            reservationTimeTextView = itemView.findViewById(R.id.reservation_time);
            totalPriceTextView = itemView.findViewById(R.id.total_price);
            status = itemView.findViewById(R.id.status);
        }
    }
}
