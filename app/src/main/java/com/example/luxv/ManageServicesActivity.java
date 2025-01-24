package com.example.luxv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ManageServicesActivity extends AppCompatActivity {
    EditText txtServiceID, txtServiceName, txtPrice, txtDescription;
    ImageView imgService;
    byte[] imageByte;
    Spinner spinnerAvailability;

    DB_Operations dbOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_services);

        dbOperations = new DB_Operations(this);

        txtServiceID = findViewById(R.id.txtServiceID);
        txtServiceName = findViewById(R.id.txtServiceName);
        txtPrice = findViewById(R.id.txtPrice);
        txtDescription = findViewById(R.id.txtDescription);
        imgService = findViewById(R.id.imgService);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinnerAvailability = findViewById(R.id.spinnerAvailability);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.availability_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAvailability.setAdapter(adapter);
    }

    public void selectImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX",0);
        intent.putExtra("aspectY",0);
        intent.putExtra("outputX",150);
        intent.putExtra("outputX",170);
        intent.putExtra("return-data",true);
        startActivityForResult(Intent.createChooser(intent, "Select Service Image"), 111);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111 && data != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
                imageByte = byteArrayOutputStream.toByteArray();
                imgService.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void insertService(View view) {
        if (validateFields()) {
            Service service = new Service();
            service.setServiceID(Integer.parseInt(txtServiceID.getText().toString()));
            service.setServiceName(txtServiceName.getText().toString());
            service.setPrice(Double.parseDouble(txtPrice.getText().toString()));
            service.setDescription(txtDescription.getText().toString());
            service.setImage(imageByte);
            service.setAvailability(spinnerAvailability.getSelectedItem().toString());

            try {
                dbOperations.addService(service);
                Toast.makeText(this, "Service Record Inserted", Toast.LENGTH_SHORT).show();
                clearFields(null);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Service ID Already Added", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void searchServiceById(View view) {
        if (!txtServiceID.getText().toString().isEmpty()) {
            int serviceID = Integer.parseInt(txtServiceID.getText().toString());
            Service service = dbOperations.getServiceById(serviceID);

            if (service != null) {
                txtServiceName.setText(service.getServiceName());
                txtPrice.setText(String.valueOf(service.getPrice()));
                txtDescription.setText(service.getDescription());
                imgService.setImageBitmap(BitmapFactory.decodeByteArray(service.getImage(), 0, service.getImage().length));

                int spinnerPosition = ((ArrayAdapter) spinnerAvailability.getAdapter())
                        .getPosition(service.getAvailability());
                spinnerAvailability.setSelection(spinnerPosition);
            } else {
                Toast.makeText(this, "Service not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter a Service ID", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateService(View view) {
        if (validateFields()) {
            Service service = new Service();
            service.setServiceID(Integer.parseInt(txtServiceID.getText().toString()));
            service.setServiceName(txtServiceName.getText().toString());
            service.setPrice(Double.parseDouble(txtPrice.getText().toString()));
            service.setDescription(txtDescription.getText().toString());
            service.setImage(imageByte);
            service.setAvailability(spinnerAvailability.getSelectedItem().toString());

            try {
                dbOperations.updateService(service);
                Toast.makeText(this, "Service Record Updated", Toast.LENGTH_SHORT).show();
                clearFields(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteService(View view) {
        if (!txtServiceID.getText().toString().isEmpty()) {
            int serviceID = Integer.parseInt(txtServiceID.getText().toString());
            try {
                dbOperations.deleteService(serviceID);
                clearFields(null);
                Toast.makeText(this, "Service Record Deleted", Toast.LENGTH_SHORT).show();
                clearFields(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Please enter a Service ID", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearFields(View view) {
        txtServiceID.setText(null);
        txtServiceName.setText(null);
        txtPrice.setText(null);
        txtDescription.setText(null);
        imgService.setImageDrawable(null);
        spinnerAvailability.setSelection(0);
        imageByte = null;
    }

    private boolean validateFields() {
        if (txtServiceID.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a Service ID", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtServiceName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a Service Name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtPrice.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a Price", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtDescription.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a Description", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (imageByte == null) {
            Toast.makeText(this, "Please select an Image", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
