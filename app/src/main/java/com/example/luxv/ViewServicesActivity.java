package com.example.luxv;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ViewServicesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewServices;
    private ServiceAdapter serviceAdapter;
    private List<Service> originalServiceList;
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

        initializeViews();
        setupListeners();
        loadServices();
    }

    private void initializeViews() {
        recyclerViewServices = findViewById(R.id.recyclerViewServices);
        recyclerViewServices.setLayoutManager(new LinearLayoutManager(this));

        editTextMinPrice = findViewById(R.id.editTextMinPrice);
        editTextMaxPrice = findViewById(R.id.editTextMaxPrice);
        checkBoxAvailability = findViewById(R.id.checkBoxAvailability);
        buttonFilter = findViewById(R.id.buttonFilter);
        filterOptionsContainer = findViewById(R.id.filterOptionsContainer);
        filterToggleIcon = findViewById(R.id.filterToggleIcon);
    }

    private void setupListeners() {
        buttonFilter.setOnClickListener(v -> applyFilter());

        findViewById(R.id.buttonAdmin).setOnClickListener(v -> {
            Intent intent = new Intent(ViewServicesActivity.this, MainActivity.class);
            startActivity(intent);
        });

        filterToggleIcon.setOnClickListener(v ->
                filterOptionsContainer.setVisibility(
                        filterOptionsContainer.getVisibility() == View.GONE ?
                                View.VISIBLE : View.GONE
                )
        );
    }

    private void loadServices() {
        dbOperations = new DB_Operations(this);
        originalServiceList = dbOperations.viewAllServices();

        serviceAdapter = new ServiceAdapter(this, originalServiceList);
        recyclerViewServices.setAdapter(serviceAdapter);
    }

    private void applyFilter() {
        String minPriceStr = editTextMinPrice.getText().toString();
        String maxPriceStr = editTextMaxPrice.getText().toString();
        boolean onlyAvailable = checkBoxAvailability.isChecked();

        // Implement advanced filtering with stream API
        List<Service> filteredServices = originalServiceList.stream()
                .filter(service -> {
                    boolean priceCheck = isPriceInRange(service, minPriceStr, maxPriceStr);
                    boolean availabilityCheck = !onlyAvailable || isServiceAvailable(service);
                    return priceCheck && availabilityCheck;
                })
                .collect(Collectors.toList());

        serviceAdapter.updateServiceList(filteredServices);
    }

    private boolean isPriceInRange(Service service, String minPriceStr, String maxPriceStr) {
        double minPrice = TextUtils.isEmpty(minPriceStr) ? 0 : Double.parseDouble(minPriceStr);
        double maxPrice = TextUtils.isEmpty(maxPriceStr) ? Double.MAX_VALUE : Double.parseDouble(maxPriceStr);
        return service.getPrice() >= minPrice && service.getPrice() <= maxPrice;
    }

    private boolean isServiceAvailable(Service service) {
        return "Available".equalsIgnoreCase(service.getAvailability());
    }
}