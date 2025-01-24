package com.example.luxv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BookServices extends AppCompatActivity {
    private RecyclerView recyclerViewServices;
    private ServiceAdapter serviceAdapter;
    private List<Service> serviceList;
    private DB_Operations dbOperations;
    private LinearLayout filterOptionsContainer;
    private ImageView filterToggleIcon;

    private EditText editTextMinPrice, editTextMaxPrice;
    private CheckBox checkBoxAvailability;
    private Button buttonFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_services);

        recyclerViewServices = findViewById(R.id.recyclerViewServices);
        recyclerViewServices.setLayoutManager(new LinearLayoutManager(this));

        editTextMinPrice = findViewById(R.id.editTextMinPrice);
        editTextMaxPrice = findViewById(R.id.editTextMaxPrice);
        checkBoxAvailability = findViewById(R.id.checkBoxAvailability);
        buttonFilter = findViewById(R.id.buttonFilter);

        serviceList = new ArrayList<>();
        dbOperations = new DB_Operations(this);

        serviceList.addAll(dbOperations.viewAllServices());

        serviceAdapter = new ServiceAdapter(this, serviceList);
        recyclerViewServices.setAdapter(serviceAdapter);

        buttonFilter.setOnClickListener(v -> applyFilter());

        Button buttonAdmin = findViewById(R.id.buttonAdmin);
        buttonAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookServices.this, MainActivity.class);
                startActivity(intent);
            }
        });

        filterOptionsContainer = findViewById(R.id.filterOptionsContainer);
        filterToggleIcon = findViewById(R.id.filterToggleIcon);

        filterToggleIcon.setOnClickListener(v -> {
            if (filterOptionsContainer.getVisibility() == View.GONE) {
                filterOptionsContainer.setVisibility(View.VISIBLE);
            } else {
                filterOptionsContainer.setVisibility(View.GONE);
            }
        });
    }

    private void applyFilter() {
        String minPriceStr = editTextMinPrice.getText().toString();
        String maxPriceStr = editTextMaxPrice.getText().toString();
        boolean onlyAvailable = checkBoxAvailability.isChecked();

        double minPrice = minPriceStr.isEmpty() ? 0 : Double.parseDouble(minPriceStr);
        double maxPrice = maxPriceStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxPriceStr);

        List<Service> filteredServices = new ArrayList<>();
        for (Service service : dbOperations.viewAllServices()) {
            boolean matchesPrice = service.getPrice() >= minPrice && service.getPrice() <= maxPrice;
            boolean matchesAvailability = !onlyAvailable || "Available".equalsIgnoreCase(service.getAvailability());

            if (matchesPrice && matchesAvailability) {
                filteredServices.add(service);
            }
        }

        serviceAdapter.updateServiceList(filteredServices);
    }
}
