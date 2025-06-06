package com.MagicalStay.client.data;

import com.MagicalStay.shared.data.*;

import java.io.IOException;

public class DataFactory {
    private static BookingData bookingData;
    private static FrontDeskData frontDeskData;
    private static GuestData guestData;
    private static HotelData hotelData;
    private static RoomData roomData;
    private static ImageStorage imageStorage;
    private static final String DATA_DIRECTORY = "C:\\Users\\Admin\\Documents\\ULTIMA FASE\\server\\";
    
    static {
        try {
            // Crear directorio de datos si no existe
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(DATA_DIRECTORY));
        } catch (IOException e) {
            System.err.println("Error al crear directorio de datos: " + e.getMessage());
        }
    }

    public static BookingData getBookingData() throws IOException {
        if (bookingData == null) {
            bookingData = new BookingData(DATA_DIRECTORY + "bookings.dat");
        }
        return bookingData;
    }

    public static FrontDeskData getFrontDeskData() throws IOException {
        if (frontDeskData == null) {
            frontDeskData = new FrontDeskData(DATA_DIRECTORY + "frontdesk.dat");
        }
        return frontDeskData;
    }

    public static GuestData getGuestData() throws IOException {
        if (guestData == null) {
            guestData = new GuestData(DATA_DIRECTORY + "guests.dat");
        }
        return guestData;
    }

    public static HotelData getHotelData() throws IOException {
        if (hotelData == null) {
            hotelData = new HotelData(DATA_DIRECTORY + "hotels.dat");
        }
        return hotelData;
    }

    public static RoomData getRoomData() throws IOException {
        if (roomData == null) {
            roomData = new RoomData(DATA_DIRECTORY + "rooms.dat", getHotelData());
        }
        return roomData;
    }

    public static ImageStorage getImageStorage() {
        if (imageStorage == null) {
            imageStorage = new ImageStorage();
        }
        return imageStorage;
    }

    public static void closeAll() throws IOException {
        if(bookingData != null) {
            bookingData.close();
            bookingData = null;
        }

        if (frontDeskData != null) {
            frontDeskData.close();
            frontDeskData = null;
        }

        if (guestData != null) {
            guestData.close();
            guestData = null;
        }
        if (hotelData != null) {
            hotelData.close();
            hotelData = null;
        }
        if (roomData != null) {
            roomData.close();
            roomData = null;
        }
    }
}