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

public class ManageRoomsActivity extends AppCompatActivity {
    EditText txtRoomID, txtRoomType, txtPrice, txtDescription;
    ImageView imgRoom;
    Spinner spinnerAvailability;
    byte[] imageByte;

    DB_Operations dbOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_rooms);

        dbOperations = new DB_Operations(this);

        txtRoomID = findViewById(R.id.txtRoomID);
        txtRoomType = findViewById(R.id.txtRoomType);
        txtPrice = findViewById(R.id.txtPrice);
        txtDescription = findViewById(R.id.txtDescription);
        imgRoom = findViewById(R.id.imgRoom);
        spinnerAvailability = findViewById(R.id.spinnerAvailability);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
        startActivityForResult(Intent.createChooser(intent, "Select Room Image"), 111);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
                imageByte = byteArrayOutputStream.toByteArray();
                imgRoom.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void insertRoom(View view) {
        if (validateFields()) {
            Room room = new Room();
            room.setRoomID(Integer.parseInt(txtRoomID.getText().toString()));
            room.setRoomType(txtRoomType.getText().toString());
            room.setPricePerNight(Double.parseDouble(txtPrice.getText().toString()));
            room.setDescription(txtDescription.getText().toString());
            room.setImage(imageByte);
            room.setAvailability(spinnerAvailability.getSelectedItem().toString());

            try {
                dbOperations.addRoom(room);
                Toast.makeText(this, "Room Record Inserted", Toast.LENGTH_SHORT).show();
                clearFields(null);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Room ID Already Added", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void searchRoomById(View view) {
        if (!txtRoomID.getText().toString().isEmpty()) {
            int roomID = Integer.parseInt(txtRoomID.getText().toString());
            Room room = dbOperations.getRoomById(roomID);

            if (room != null) {
                txtRoomType.setText(room.getRoomType());
                txtPrice.setText(String.valueOf(room.getPricePerNight()));
                txtDescription.setText(room.getDescription());
                imgRoom.setImageBitmap(BitmapFactory.decodeByteArray(room.getImage(), 0, room.getImage().length));

                if (room.getAvailability().equals("Available")) {
                    spinnerAvailability.setSelection(0);
                } else {
                    spinnerAvailability.setSelection(1);
                }
            } else {
                Toast.makeText(this, "Room not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter a Room ID", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateRoom(View view) {
        if (validateFields()) {
            Room room = new Room();
            room.setRoomID(Integer.parseInt(txtRoomID.getText().toString()));
            room.setRoomType(txtRoomType.getText().toString());
            room.setPricePerNight(Double.parseDouble(txtPrice.getText().toString()));
            room.setDescription(txtDescription.getText().toString());
            room.setImage(imageByte);
            room.setAvailability(spinnerAvailability.getSelectedItem().toString());

            try {
                dbOperations.updateRoom(room);
                Toast.makeText(this, "Room Record Updated", Toast.LENGTH_SHORT).show();
                clearFields(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteRoom(View view) {
        if (!txtRoomID.getText().toString().isEmpty()) {
            int roomID = Integer.parseInt(txtRoomID.getText().toString());
            try {
                dbOperations.deleteRoom(roomID);
                clearFields(null);
                Toast.makeText(this, "Room Record Deleted", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Please enter a Room ID", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearFields(View view) {
        txtRoomID.setText(null);
        txtRoomType.setText(null);
        txtPrice.setText(null);
        txtDescription.setText(null);
        imgRoom.setImageDrawable(null);
        imageByte = null;
        spinnerAvailability.setSelection(0);
    }

    private boolean validateFields() {
        if (txtRoomID.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a Room ID", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtRoomType.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a Room Type", Toast.LENGTH_SHORT).show();
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
