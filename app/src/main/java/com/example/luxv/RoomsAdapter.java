package com.example.luxv;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RoomsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Room> roomList;
    private ArrayList<Room> originalRoomList;

    public RoomsAdapter(Context context, ArrayList<Room> roomList) {
        this.context = context;
        this.roomList = new ArrayList<>(roomList);
        this.originalRoomList = new ArrayList<>(roomList);
    }

    @Override
    public int getCount() {
        return roomList.size();
    }

    @Override
    public Object getItem(int position) {
        return roomList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.item_room, parent, false);
        }

        TextView txtRoomNumber = rowView.findViewById(R.id.txtRoomNumber);
        TextView txtRoomType = rowView.findViewById(R.id.txtRoomType);
        TextView txtPrice = rowView.findViewById(R.id.txtPrice);
        TextView txtDescription = rowView.findViewById(R.id.txtDescription);
        TextView txtAvailability = rowView.findViewById(R.id.txtAvailability);
        ImageView imgRoom = rowView.findViewById(R.id.imgRoom);
        Button btn = rowView.findViewById(R.id.buttonBook);

        Room room = roomList.get(position);
        txtRoomNumber.setText("Room Number: " + room.getRoomID());
        txtRoomType.setText("Type: " + room.getRoomType());
        txtPrice.setText("Price Per Night: Rs." + String.format("%.2f", room.getPricePerNight()));
        txtDescription.setText("Description: " + room.getDescription());

        if (room.getAvailability().equalsIgnoreCase("Available")) {
            txtAvailability.setText("Availability: Available");
            txtAvailability.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            txtAvailability.setText("Availability: Not Available");
            txtAvailability.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }

        if (room.getImage() != null && room.getImage().length > 0) {
            imgRoom.setImageBitmap(BitmapFactory.decodeByteArray(room.getImage(), 0, room.getImage().length));
        } else {
            imgRoom.setImageResource(R.drawable.logout);
        }

        btn.setOnClickListener(v -> bookRoom(room));

        return rowView;
    }


    public void filterByRoomTypePriceAndAvailability(String roomType, Double minPrice, Double maxPrice, boolean onlyAvailable) {
        ArrayList<Room> filteredRooms = new ArrayList<>();
        String roomTypeLower = roomType.toLowerCase();

        for (Room room : originalRoomList) {
            String roomTypeFromList = room.getRoomType() != null ? room.getRoomType().toLowerCase() : "";

            boolean matchesRoomType = roomTypeLower.equals("all") || roomTypeFromList.equals(roomTypeLower);

            boolean matchesPrice = true;
            if (minPrice != null && room.getPricePerNight() < minPrice) {
                matchesPrice = false;
            }
            if (maxPrice != null && room.getPricePerNight() > maxPrice) {
                matchesPrice = false;
            }

            boolean matchesAvailability = !onlyAvailable || room.getAvailability().equals("Available");

            if (matchesRoomType && matchesPrice && matchesAvailability) {
                filteredRooms.add(room);
            }
        }

        roomList.clear();
        roomList.addAll(filteredRooms);
        notifyDataSetChanged();
    }

    private void bookRoom(Room room) {
        if (room.getAvailability() != null && room.getAvailability().equalsIgnoreCase("Available")) {
            Toast.makeText(context, "Cannot Book.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Sorry, this room is not available for booking.", Toast.LENGTH_SHORT).show();
        }
    }
}