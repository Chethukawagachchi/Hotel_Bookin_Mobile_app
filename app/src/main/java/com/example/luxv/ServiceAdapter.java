package com.example.luxv;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<Service> services;
    private Context context;

    public ServiceAdapter(Context context, List<Service> services) {
        this.context = context;
        this.services = services;
    }

    public void updateServiceList(List<Service> newServices) {
        this.services.clear();
        this.services.addAll(newServices);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = services.get(position);
        holder.serviceName.setText(service.getServiceName());
        holder.serviceID.setText("Service ID: " + service.getServiceID());
        holder.servicePrice.setText("Price Per Person: Rs." + service.getPrice());
        holder.serviceDescription.setText("Description: " + service.getDescription());

        if ("Available".equalsIgnoreCase(service.getAvailability())) {
            holder.serviceAvailability.setText("Availability: Available");
            holder.serviceAvailability.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.serviceAvailability.setText("Availability: Not Available");
            holder.serviceAvailability.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }

        holder.buttonReserveService.setOnClickListener(v -> {
            if ("Available".equalsIgnoreCase(service.getAvailability())) {
                Intent intent = new Intent(context, ReservationFormActivity.class);
                intent.putExtra("serviceId", String.valueOf(service.getServiceID()));
                intent.putExtra("serviceName", service.getServiceName());
                intent.putExtra("servicePrice", String.valueOf(service.getPrice()));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "This service is currently unavailable.", Toast.LENGTH_SHORT).show();
            }
        });

        byte[] imageBytes = service.getImage();
        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.serviceImage.setImageBitmap(bitmap);
        } else {
            holder.serviceImage.setImageResource(R.drawable.icon);
        }
    }


    @Override
    public int getItemCount() {
        return services.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName,serviceID, servicePrice, serviceDescription, serviceAvailability;
        ImageView serviceImage;
        Button buttonReserveService;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.serviceName);
            serviceID = itemView.findViewById(R.id.serviceID);
            servicePrice = itemView.findViewById(R.id.servicePrice);
            serviceDescription = itemView.findViewById(R.id.serviceDescription);
            serviceAvailability = itemView.findViewById(R.id.serviceAvailability);
            serviceImage = itemView.findViewById(R.id.serviceImage);
            buttonReserveService = itemView.findViewById(R.id.buttonReserveService);
        }
    }
}
