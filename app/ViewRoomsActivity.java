import android.os.Bundle;
import android.widget.ListView;

import com.example.luxv.R;

import java.util.List;

public class ViewRoomsActivity extends AppCompatActivity {

    private ListView lstRooms;
    private RoomAdapter roomAdapter;
    private DB_Operations dbOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rooms);

        // Initialize the ListView and DB operations
        lstRooms = findViewById(R.id.lstRooms);
        dbOperations = new DB_Operations(this);

        // Display the rooms in the ListView
        displayRooms();
    }

    // Method to fetch all rooms from the database and display them
    private void displayRooms() {
        // Fetch the room data from the database
        List<String> rooms = dbOperations.getAllRooms();

        // Create an adapter with the room data and set it to the ListView
        roomAdapter = new RoomAdapter(this, rooms);
        lstRooms.setAdapter(roomAdapter);
    }
}
