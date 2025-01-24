package com.example.luxv;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BookRooms extends AppCompatActivity {

    ListView listViewRooms;
    ArrayList<Room> roomList;
    RoomListAdapter roomListAdapter;
    DB_Operations dbOperations;
    EditText editTextMinPrice, editTextMaxPrice;
    CheckBox checkBoxAvailability;
    LinearLayout filterOptionsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rooms);

        dbOperations = new DB_Operations(this);
        roomList = dbOperations.getAllRooms();

        if (roomList == null || roomList.isEmpty()) {
            roomList = new ArrayList<>();
            Toast.makeText(this, "No rooms available in the database.", Toast.LENGTH_SHORT).show();
        }

        // Initialize the custom adapter and set it to the ListView
        roomListAdapter = new RoomListAdapter(this, roomList);
        listViewRooms = findViewById(R.id.listView);
        listViewRooms.setAdapter(roomListAdapter);

        Spinner spinnerRoomType = findViewById(R.id.spinnerRoomType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.room_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoomType.setAdapter(adapter);

        editTextMinPrice = findViewById(R.id.editTextMinPrice);
        editTextMaxPrice = findViewById(R.id.editTextMaxPrice);

        checkBoxAvailability = findViewById(R.id.checkBoxAvailability);

        filterOptionsContainer = findViewById(R.id.filterOptionsContainer);

        Button buttonFilter = findViewById(R.id.buttonFilter);
        buttonFilter.setOnClickListener(v -> applyFilters(spinnerRoomType.getSelectedItem().toString()));

        Button filterToggleButton = new Button(this);
        filterToggleButton.setText("Toggle Filter Options");
        filterToggleButton.setOnClickListener(v -> {
            if (filterOptionsContainer.getVisibility() == View.GONE) {
                filterOptionsContainer.setVisibility(View.VISIBLE);
            } else {
                filterOptionsContainer.setVisibility(View.GONE);
            }
        });

        spinnerRoomType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyFilters("All");
            }
        });

        Button buttonAdmin = findViewById(R.id.buttonAdmin);
        buttonAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(BookRooms.this, MainActivity.class);
            startActivity(intent);
        });

        ImageView filterToggleIcon = findViewById(R.id.filterToggleIcon);
        filterToggleIcon.setOnClickListener(v -> {
            if (filterOptionsContainer.getVisibility() == View.GONE) {
                filterOptionsContainer.setVisibility(View.VISIBLE);
            } else {
                filterOptionsContainer.setVisibility(View.GONE);
            }
        });

    }

    private void applyFilters(String selectedRoomType) {
        String minPriceStr = editTextMinPrice.getText().toString();
        String maxPriceStr = editTextMaxPrice.getText().toString();
        Double minPrice = minPriceStr.isEmpty() ? null : Double.parseDouble(minPriceStr);
        Double maxPrice = maxPriceStr.isEmpty() ? null : Double.parseDouble(maxPriceStr);
        boolean onlyAvailable = checkBoxAvailability.isChecked();

        roomListAdapter.filterByRoomTypePriceAndAvailability(selectedRoomType, minPrice, maxPrice, onlyAvailable);
    }
}
