package com.example.luxv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DB_Operations extends SQLiteOpenHelper {

    private String username;

    private static final String DATABASE_NAME = "Hotel_Booking";
    private static final int DATABASE_VERSION = 11;

    // Constructor for DB_Operations without username
    public DB_Operations(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Constructor for DB_Operations with username
    public DB_Operations(Context context, String username) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.username = username;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create user table
        sqLiteDatabase.execSQL("CREATE TABLE tblUser(Username VARCHAR(20), Mobile INTEGER, Address VARCHAR, Email VARCHAR(50) PRIMARY KEY, Password VARCHAR(20))");
        // Create room table
        sqLiteDatabase.execSQL("CREATE TABLE tblRoom(RoomID INTEGER PRIMARY KEY, RoomNumber VARCHAR(10), RoomType VARCHAR(20), PricePerNight DOUBLE, Image BLOB, Description VARCHAR(100), Availability VARCHAR(20))");
        // Create reservation table
        sqLiteDatabase.execSQL("CREATE TABLE tblReservation(ReservationID INTEGER PRIMARY KEY AUTOINCREMENT, Email VARCHAR(50), RoomID INTEGER, CheckInDate TEXT, CheckOutDate TEXT, TotalPrice DOUBLE, FOREIGN KEY(Email) REFERENCES tblUser(Email), FOREIGN KEY(RoomID) REFERENCES tblRoom(RoomID))");
        // Create service table
        sqLiteDatabase.execSQL("CREATE TABLE tblService(ServiceID INTEGER PRIMARY KEY, ServiceName VARCHAR(30), Description VARCHAR(100), Price DOUBLE, Availability VARCHAR(20), Image BLOB)");
        // Create service reservation table
        sqLiteDatabase.execSQL("CREATE TABLE tblServiceReservation(ReservationID INTEGER PRIMARY KEY AUTOINCREMENT, Email VARCHAR(50), ServiceID INTEGER, ReservationDate TEXT, ReservationTime TEXT, TotalPrice DOUBLE, FOREIGN KEY(Email) REFERENCES tblUser(Email), FOREIGN KEY(ServiceID) REFERENCES tblService(ServiceID))");

        sqLiteDatabase.execSQL("CREATE TABLE tblAdmin(AdminID INTEGER PRIMARY KEY AUTOINCREMENT, Username VARCHAR(20), Password VARCHAR(20))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Drop tables if exist
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS tblUser");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS tblRoom");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS tblReservation");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS tblService");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS tblServiceReservation");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS tblDiscount");

        // Recreate tables
        onCreate(sqLiteDatabase);
    }

    // User creation method
    public void createUser(User user) throws Exception {
        SQLiteDatabase database = this.getWritableDatabase();

        String query = "SELECT * FROM tblUser WHERE Email = ?";
        Cursor cursor = database.rawQuery(query, new String[]{user.getEmail()});

        if (cursor.getCount() > 0) {
            cursor.close();
            database.close();
            throw new Exception("Email already exists");
        }

        cursor.close();

        ContentValues values = new ContentValues();
        values.put("Username", user.getUsername());
        values.put("Mobile", user.getMobile());
        values.put("Address", user.getAddress());
        values.put("Email", user.getEmail());
        values.put("Password", user.getPassword());

        database.insert("tblUser", null, values);
        database.close();
    }

    public boolean loginUserWithEmail(String email, String password) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM tblUser WHERE Email = ? AND Password = ?", new String[]{email, password});
        boolean isLoggedIn = cursor.getCount() > 0;
        cursor.close();
        database.close();
        return isLoggedIn;
    }


    // Method to get a user by email
    public User getUserByEmail(String email) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM tblUser WHERE Email = ?", new String[]{email});

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("Username")));
            user.setMobile(cursor.getInt(cursor.getColumnIndexOrThrow("Mobile")));
            user.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("Address")));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("Email")));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("Password")));
        }
        cursor.close();
        database.close();
        return user;
    }

    // Method to update an existing user record
    public void updateUser(User user) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Username", user.getUsername());
        values.put("Mobile", user.getMobile());
        values.put("Address", user.getAddress());
        values.put("Password", user.getPassword());

        database.update("tblUser", values, "Email = ?", new String[]{user.getEmail()});
        database.close();
    }

    public boolean updateUsers(User user) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Username", user.getUsername());
        values.put("Mobile", user.getMobile());
        values.put("Address", user.getAddress());

        int rowsAffected = database.update("tblUser", values, "Email = ?", new String[]{user.getEmail()});
        database.close();
        return rowsAffected > 0;
    }


    // Method to delete a user by email
    public void deleteUser(String email) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete("tblUser", "Email = ?", new String[]{email});
        database.close();
    }


    public ArrayList<User> getAllUsers() {
        ArrayList<User> userList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM tblUser", null);

        if (cursor.moveToFirst()) {
            do {
                User user = new User(
                        cursor.getString(cursor.getColumnIndexOrThrow("Username")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("Mobile")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Address")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Password"))
                );
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return userList;
    }


    // Method to add a new room
    public void addRoom(Room room) throws Exception {
        SQLiteDatabase database = this.getWritableDatabase();

        String query = "SELECT * FROM tblRoom WHERE RoomID = ?";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(room.getRoomID())});

        if (cursor.getCount() > 0) {
            cursor.close();
            database.close();
            throw new Exception("Room ID already exists");
        }

        cursor.close();

        ContentValues values = new ContentValues();
        values.put("RoomID", room.getRoomID());
        values.put("RoomType", room.getRoomType().toLowerCase());
        values.put("PricePerNight", room.getPricePerNight());
        values.put("Image", room.getImage());
        values.put("Description", room.getDescription());
        values.put("Availability", room.getAvailability());

        database.insert("tblRoom", null, values);
        database.close();
    }


    // Method to get a room by RoomID
    public Room getRoomById(int roomID) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM tblRoom WHERE RoomID = ?", new String[]{String.valueOf(roomID)});

        Room room = null;
        if (cursor.moveToFirst()) {
            room = new Room();
            room.setRoomID(cursor.getInt(cursor.getColumnIndexOrThrow("RoomID")));
            room.setRoomType(cursor.getString(cursor.getColumnIndexOrThrow("RoomType")));
            room.setPricePerNight(cursor.getDouble(cursor.getColumnIndexOrThrow("PricePerNight")));
            room.setImage(cursor.getBlob(cursor.getColumnIndexOrThrow("Image")));
            room.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("Description")));
            room.setAvailability(cursor.getString(cursor.getColumnIndexOrThrow("Availability")));
        }
        cursor.close();
        database.close();
        return room;
    }


    // Method to add a new service
    public void addService(Service service) throws Exception {
        SQLiteDatabase database = this.getWritableDatabase();

        // Check if the ServiceID already exists
        String query = "SELECT * FROM tblService WHERE ServiceID = ?";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(service.getServiceID())});

        if (cursor.getCount() > 0) {
            cursor.close();
            database.close();
            throw new Exception("Service ID already exists");
        }

        cursor.close();

        ContentValues values = new ContentValues();
        values.put("ServiceID", service.getServiceID());
        values.put("ServiceName", service.getServiceName());
        values.put("Description", service.getDescription());
        values.put("Price", service.getPrice());
        values.put("Availability", service.getAvailability());
        values.put("Image", service.getImage());

        database.insert("tblService", null, values);
        database.close();
    }


    // Method to get a service by ServiceID
    public Service getServiceById(int serviceID) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM tblService WHERE ServiceID = ?", new String[]{String.valueOf(serviceID)});

        Service service = null;
        if (cursor.moveToFirst()) {
            service = new Service();
            service.setServiceID(cursor.getInt(cursor.getColumnIndexOrThrow("ServiceID")));
            service.setServiceName(cursor.getString(cursor.getColumnIndexOrThrow("ServiceName")));
            service.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("Description")));
            service.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("Price")));
            service.setAvailability(cursor.getString(cursor.getColumnIndexOrThrow("Availability")));
            service.setImage(cursor.getBlob(cursor.getColumnIndexOrThrow("Image")));
        }
        cursor.close();
        database.close();
        return service;
    }

    // Method to update an existing service record
    public void updateService(Service service) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ServiceName", service.getServiceName());
        values.put("Description", service.getDescription());
        values.put("Price", service.getPrice());
        values.put("Availability", service.getAvailability());
        values.put("Image", service.getImage());

        database.update("tblService", values, "ServiceID = ?", new String[]{String.valueOf(service.getServiceID())});
        database.close();
    }

    // Method to delete a service by ServiceID
    public void deleteService(int serviceID) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete("tblService", "ServiceID = ?", new String[]{String.valueOf(serviceID)});
        database.close();
    }

    // Method to view all services
    public ArrayList<Service> viewAllServices() {
        ArrayList<Service> servicesList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM tblService", null);

        if (cursor.moveToFirst()) {
            do {
                Service service = new Service();
                service.setServiceID(cursor.getInt(0));
                service.setServiceName(cursor.getString(1));
                service.setDescription(cursor.getString(2));
                service.setPrice(cursor.getDouble(3));
                service.setAvailability(cursor.getString(4));
                service.setImage(cursor.getBlob(5));
                servicesList.add(service);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return servicesList;
    }

    // Method to update an existing room record
    public void updateRoom(Room room) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("RoomType", room.getRoomType());
        values.put("PricePerNight", room.getPricePerNight());
        values.put("Image", room.getImage());
        values.put("Description", room.getDescription());
        values.put("Availability", room.getAvailability());

        database.update("tblRoom", values, "RoomID = ?", new String[]{String.valueOf(room.getRoomID())});
        database.close();
    }

    // Method to delete a room by Room ID
    public void deleteRoom(int roomID) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete("tblRoom", "RoomID = ?", new String[]{String.valueOf(roomID)});
        database.close();
    }

    // Method to view all room types
    public ArrayList<Room> getAllRooms() {
        ArrayList<Room> roomList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM tblRoom", null);

        if (cursor.moveToFirst()) {
            do {
                Room room = new Room();
                room.setRoomID(cursor.getInt(cursor.getColumnIndexOrThrow("RoomID")));
                room.setRoomType(cursor.getString(cursor.getColumnIndexOrThrow("RoomType")));
                room.setPricePerNight(cursor.getDouble(cursor.getColumnIndexOrThrow("PricePerNight")));
                room.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("Description")));
                room.setAvailability(cursor.getString(cursor.getColumnIndexOrThrow("Availability")));
                room.setImage(cursor.getBlob(cursor.getColumnIndexOrThrow("Image"))); // Correctly retrieve as BLOB
                roomList.add(room);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return roomList;
    }


    // Method to reserve a room
    public void reserveRoom(Reservation reservation) {
        ContentValues values = new ContentValues();
        values.put("Email", reservation.getEmail());
        values.put("RoomID", reservation.getRoomID());
        values.put("CheckInDate", reservation.getCheckInDate());
        values.put("CheckOutDate", reservation.getCheckOutDate());
        values.put("TotalPrice", reservation.getTotalPrice());
        SQLiteDatabase database = this.getWritableDatabase();
        database.insert("tblReservation", null, values);
        database.close();
    }

    public boolean isRoomAvailable(int roomID, String checkInDate, String checkOutDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM tblReservation WHERE RoomID = ? " +
                "AND ((CheckInDate <= ? AND CheckOutDate > ?) OR " +
                "(CheckInDate < ? AND CheckOutDate >= ?) OR " +
                "(CheckInDate >= ? AND CheckOutDate < ?))";
        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(roomID),
                checkInDate, checkInDate,
                checkOutDate, checkOutDate,
                checkInDate, checkOutDate
        });

        boolean isAvailable = cursor.getCount() == 0;
        cursor.close();
        db.close();
        return isAvailable;
    }

    public void updateRoomAvailability(int roomID, String availability) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Availability", availability);

        database.update("tblRoom", values, "RoomID = ?", new String[]{String.valueOf(roomID)});
        database.close();
    }

    public ArrayList<Reservation> getBookingsByEmail(String email) {
        ArrayList<Reservation> reservationsList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query("tblReservation",
                null, "Email = ?", new String[]{email}, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String emailFromDb = cursor.getString(cursor.getColumnIndexOrThrow("Email"));
                int roomID = cursor.getInt(cursor.getColumnIndexOrThrow("RoomID"));
                String checkInDate = cursor.getString(cursor.getColumnIndexOrThrow("CheckInDate"));
                String checkOutDate = cursor.getString(cursor.getColumnIndexOrThrow("CheckOutDate"));
                double totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("TotalPrice"));

                Reservation reservation = new Reservation(emailFromDb, roomID, checkInDate, checkOutDate, totalPrice);
                reservationsList.add(reservation);
            }
            cursor.close();
        }

        database.close();
        return reservationsList;
    }

    // Method to reserve a service
    public boolean reserveService(ServiceReservation serviceReservation) {
        ContentValues values = new ContentValues();
        values.put("Email", serviceReservation.getEmail());
        values.put("ServiceID", serviceReservation.getServiceID());
        values.put("ReservationDate", serviceReservation.getReservationDate());
        values.put("ReservationTime", serviceReservation.getReservationTime());
        values.put("TotalPrice", serviceReservation.getPrice());

        SQLiteDatabase database = this.getWritableDatabase();
        long result = database.insert("tblServiceReservation", null, values);
        database.close();

        return result != -1;
    }

    public List<ServiceReservation> getAllServiceReservations() {
        List<ServiceReservation> serviceReservations = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();


        Cursor cursor = database.query("tblServiceReservation", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int reservationID = cursor.getInt(cursor.getColumnIndexOrThrow("ReservationID"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("Email"));
                int serviceID = cursor.getInt(cursor.getColumnIndexOrThrow("ServiceID"));
                String reservationDate = cursor.getString(cursor.getColumnIndexOrThrow("ReservationDate"));
                String reservationTime = cursor.getString(cursor.getColumnIndexOrThrow("ReservationTime"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("TotalPrice"));

                ServiceReservation reservation = new ServiceReservation(email, serviceID, reservationDate, reservationTime, price);
                reservation.setReservationID(reservationID);
                serviceReservations.add(reservation);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();

        return serviceReservations;
    }

    public ArrayList<ServiceReservation> getServiceReservationsByEmail(String email) {
        ArrayList<ServiceReservation> reservations = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query("tblServiceReservation", null,
                "Email = ?", new String[]{email}, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int reservationID = cursor.getInt(cursor.getColumnIndexOrThrow("ReservationID"));
                String emailFromDb = cursor.getString(cursor.getColumnIndexOrThrow("Email"));
                int serviceID = cursor.getInt(cursor.getColumnIndexOrThrow("ServiceID"));
                String reservationDate = cursor.getString(cursor.getColumnIndexOrThrow("ReservationDate"));
                String reservationTime = cursor.getString(cursor.getColumnIndexOrThrow("ReservationTime"));
                double totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("TotalPrice"));

                ServiceReservation reservation = new ServiceReservation(
                        emailFromDb,
                        serviceID,
                        reservationDate,
                        reservationTime,
                        totalPrice
                );
                reservation.setReservationID(reservationID);
                reservations.add(reservation);
            }
            cursor.close();
        }
        database.close();
        return reservations;
    }



}



