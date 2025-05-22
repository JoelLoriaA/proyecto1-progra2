package com.MagicalStay.data;

import com.MagicalStay.config.DataConfig;
import java.io.IOException;

/**
 * Factory para crear instancias de las clases de datos
 * Maneja automáticamente las rutas de archivos y dependencias
 */
public class DataFactory {

    // Instancias singleton de las clases de datos
    private static HotelData hotelData;
    private static RoomData roomData;
    private static GuestData guestData;
    private static BookingData bookingData;
    private static FrontDeskData frontDeskData;

    /**
     * Obtiene la instancia de HotelData
     */
    public static synchronized HotelData getHotelData() throws IOException {
        if (hotelData == null) {
            String filepath = DataConfig.getDataFilePath(DataConfig.HOTELS_FILE);
            hotelData = new HotelData(filepath);
        }
        return hotelData;
    }

    /**
     * Obtiene la instancia de RoomData
     */
    public static synchronized RoomData getRoomData() throws IOException {
        if (roomData == null) {
            String filepath = DataConfig.getDataFilePath(DataConfig.ROOMS_FILE);
            // RoomData necesita HotelData como dependencia
            roomData = new RoomData(filepath, getHotelData());
        }
        return roomData;
    }

    /**
     * Obtiene la instancia de GuestData
     */
    public static synchronized GuestData getGuestData() throws IOException {
        if (guestData == null) {
            String filepath = DataConfig.getDataFilePath(DataConfig.GUESTS_FILE);
            guestData = new GuestData(filepath);
        }
        return guestData;
    }

    /**
     * Obtiene la instancia de BookingData
     */
    public static synchronized BookingData getBookingData() throws IOException {
        if (bookingData == null) {
            String filepath = DataConfig.getDataFilePath(DataConfig.BOOKINGS_FILE);
            bookingData = new BookingData(filepath);
        }
        return bookingData;
    }

    /**
     * Obtiene la instancia de FrontDeskData
     */
    public static synchronized FrontDeskData getFrontDeskData() throws IOException {
        if (frontDeskData == null) {
            String filepath = DataConfig.getDataFilePath(DataConfig.FRONT_DESK_FILE);
            frontDeskData = new FrontDeskData(filepath);
        }
        return frontDeskData;
    }

    /**
     * Cierra todas las conexiones de archivos
     */
    public static void closeAll() {
        try {
            if (hotelData != null) {
                hotelData.close();
                hotelData = null;
            }
            if (roomData != null) {
                roomData.close();
                roomData = null;
            }
            if (guestData != null) {
                guestData.close();
                guestData = null;
            }
            if (bookingData != null) {
                bookingData.close();
                bookingData = null;
            }
            if (frontDeskData != null) {
                frontDeskData.close();
                frontDeskData = null;
            }
        } catch (IOException e) {
            System.err.println("Error al cerrar archivos de datos: " + e.getMessage());
        }
    }

    /**
     * Reinicia todas las instancias (útil para testing)
     */
    public static void reset() {
        closeAll();
        // Las instancias se recrearán la próxima vez que se soliciten
    }
}