package com.example.luxv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ServiceReservationAdapter extends RecyclerView.Adapter<ServiceReservationAdapter.ViewHolder> {

    private List<ServiceReservation> serviceReservations;
    private Context context;

    public ServiceReservationAdapter(Context context, List<ServiceReservation> serviceReservations) {
        this.context = context;
        this.serviceReservations = serviceReservations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_servicereservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceReservation reservation = serviceReservations.get(position);

        holder.txtReservationID.setText("Reservation ID: " + reservation.getReservationID());
        holder.txtEmail.setText("Email: " + reservation.getEmail());
        holder.txtServiceID.setText("Service ID: " + reservation.getServiceID());
        holder.txtReservationDate.setText("Date: " + reservation.getReservationDate());
        holder.txtReservationTime.setText("Time: " + reservation.getReservationTime());
        holder.txtPrice.setText("Price: " + reservation.getPrice());
    }

    @Override
    public int getItemCount() {
        return serviceReservations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtReservationID;
        TextView txtEmail;
        TextView txtServiceID;
        TextView txtReservationDate;
        TextView txtReservationTime;
        TextView txtPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReservationID = itemView.findViewById(R.id.txtReservationID);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtServiceID = itemView.findViewById(R.id.txtServiceID);
            txtReservationDate = itemView.findViewById(R.id.txtReservationDate);
            txtReservationTime = itemView.findViewById(R.id.txtReservationTime);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }
    }
}
