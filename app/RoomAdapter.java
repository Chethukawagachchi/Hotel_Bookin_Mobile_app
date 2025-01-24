import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.luxv.R;

public class RoomAdapter extends BaseAdapter {

    private Context context;
    private List<Room> rooms;

    public RoomAdapter(Context context, List<Room> rooms) {
        this.context = context;
        this.rooms = rooms;
    }

    @Override
    public int getCount() {
        return rooms.size();
    }

    @Override
    public Object getItem(int position) {
        return rooms.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.room_item, null);  // Layout for each item
        }

        // Initialize the TextViews by finding them by their ID
        TextView roomNumberText = convertView.findViewById(R.id.roomNumberText);
        TextView roomTypeText = convertView.findViewById(R.id.roomTypeText);
        TextView pricePerNightText = convertView.findViewById(R.id.pricePerNightText);

        // Get the room data for the current position
        Room room = rooms.get(position);

        // Set the data to the TextViews
        roomNumberText.setText(room.getRoomNumber());
        roomTypeText.setText(room.getRoomType());
        pricePerNightText.setText("$" + room.getPricePerNight());

        return convertView;
    }
}
