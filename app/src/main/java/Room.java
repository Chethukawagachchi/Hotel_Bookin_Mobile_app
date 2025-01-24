public class Room {

    private String roomName;
    private double price;
    private boolean isAvailable;  // Add a boolean for availability

    // Constructor
    public Room(String roomName, double price, boolean isAvailable) {
        this.roomName = roomName;
        this.price = price;
        this.isAvailable = isAvailable;
    }

    // Getters and Setters
    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return isAvailable;  // Return the availability status
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
