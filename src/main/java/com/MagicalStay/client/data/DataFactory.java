package com.MagicalStay.client.data;

import com.MagicalStay.shared.data.GuestData;
import com.MagicalStay.shared.data.HotelData;
import com.MagicalStay.shared.data.RoomData;
import java.io.IOException;

public class DataFactory {
    private static GuestData guestData;
    private static HotelData hotelData;
    private static RoomData roomData;

    public static GuestData getGuestData() throws IOException {
        if (guestData == null) {
            guestData = new GuestData();
        }
        return guestData;
    }

    public static HotelData getHotelData() throws IOException {
        if (hotelData == null) {
            hotelData = new HotelData();
        }
        return hotelData;
    }

    public static RoomData getRoomData() throws IOException {
        if (roomData == null) {
            roomData = new RoomData();
        }
        return roomData;
    }

    public static void closeAll() {
        if (guestData != null) {
            // Cerrar conexión de guestData si es necesario
            guestData = null;
        }
        if (hotelData != null) {
            // Cerrar conexión de hotelData si es necesario
            hotelData = null;
        }
        if (roomData != null) {
            // Cerrar conexión de roomData si es necesario
            roomData = null;
        }
    }
}