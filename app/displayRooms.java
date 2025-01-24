import java.util.ArrayList;
import java.util.List;

private void displayRooms() {
    List<String> rooms = new ArrayList<>();
    List<byte[]> roomImages = new ArrayList<>();

    // Fetch rooms and images from the database
    List<Room> allRooms = dbOperations.getAllRoomsWithImages(); // Fetch both room details and images

    for (Room room : allRooms) {
        rooms.add(room.getRoomDetails()); // e.g., "RoomNumber - RoomType - $Price"
        roomImages.add(room.getImage()); // Room image in byte[] format
    }

    // Initialize the RoomAdapter with both room details and images
    roomAdapter = new RoomAdapter(this, rooms, roomImages);
    lstRooms.setAdapter(roomAdapter);
}
