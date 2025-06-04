package com.MagicalStay.shared.data;

import com.MagicalStay.shared.domain.Guest;
import com.MagicalStay.shared.domain.Hotel;
import com.MagicalStay.shared.domain.Room;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class HotelData extends JsonDataResponse {
    private RandomAccessFile raf;
    // Constantes para rooms
    private static final int ROOM_NUMBER_LENGTH = 8;
    private static final int ROOM_SIZE = ROOM_NUMBER_LENGTH * 2;
    private static final int MAX_ROOMS = 20; // Reducido de 50 a 20
    private static final int ROOMS_LIST_SIZE = MAX_ROOMS * ROOM_SIZE;

    // Constantes para guests
    private static final int GUEST_LENGTH = 30; // Nombre + ID del huésped
    private static final int GUEST_SIZE = GUEST_LENGTH * 2;
    private static final int MAX_GUESTS = 20;
    private static final int GUESTS_LIST_SIZE = MAX_GUESTS * GUEST_SIZE;

    // Constantes básicas del hotel
    private static final int HOTEL_ID_SIZE = (3 + 1) * 2; // ID + espacio
    private static final int NAME_LENGTH = 25;
    private static final int ADDRESS_LENGTH = 40;
    private static final int NAME_SIZE = NAME_LENGTH * 2;
    private static final int ADDRESS_SIZE = ADDRESS_LENGTH * 2;

    // Tamaño total del registro
    private static final int RECORD_SIZE = HOTEL_ID_SIZE + NAME_SIZE + ADDRESS_SIZE + ROOMS_LIST_SIZE + GUESTS_LIST_SIZE;

    public HotelData(String filename) throws IOException {
        this.raf = new RandomAccessFile(filename, "rw");
    }

    public HotelData() {
    }

    public String create(Hotel hotel) throws IOException {
        try {
            if (hotel.getName() != null && hotel.getName().length() > NAME_LENGTH) {
                return createJsonResponse(false, "El nombre no puede exceder " + NAME_LENGTH + " caracteres", null);
            }
            if (hotel.getAddress() != null && hotel.getAddress().length() > ADDRESS_LENGTH) {
                return createJsonResponse(false, "La dirección no puede exceder " + ADDRESS_LENGTH + " caracteres", null);
            }

            raf.seek(raf.length());
            writeHotel(hotel);
            return createJsonResponse(true, "Hotel creado exitosamente", hotel);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al crear hotel: " + e.getMessage(), null);
        }
    }

    public String retrieveById(int hotelId) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                String idStr = readString(3);
                int currentId = Integer.parseInt(idStr.trim());
                if (currentId == hotelId) {
                    raf.seek(pos);
                    Hotel hotel = readHotel();
                    return createJsonResponse(true, "Hotel encontrado", hotel);
                }
            }
            return createJsonResponse(false, "Hotel no encontrado", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al recuperar hotel: " + e.getMessage(), null);
        }
    }

    public String retrieveAll() throws IOException {
        try {
            List<Hotel> hotels = new ArrayList<>();
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                hotels.add(readHotel());
            }
            return createJsonResponse(true, "Hoteles recuperados exitosamente", hotels);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al recuperar hoteles: " + e.getMessage(), null);
        }
    }

    public String update(Hotel hotel) throws IOException {
        try {
            if (hotel.getName() != null && hotel.getName().length() > NAME_LENGTH) {
                return createJsonResponse(false, "El nombre no puede exceder " + NAME_LENGTH + " caracteres", null);
            }
            if (hotel.getAddress() != null && hotel.getAddress().length() > ADDRESS_LENGTH) {
                return createJsonResponse(false, "La dirección no puede exceder " + ADDRESS_LENGTH + " caracteres", null);
            }

            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                String idStr = readString(3);
                int currentId = Integer.parseInt(idStr.trim());
                if (currentId == hotel.getHotelId()) {
                    raf.seek(pos);
                    writeHotel(hotel);
                    return createJsonResponse(true, "Hotel actualizado exitosamente", hotel);
                }
            }
            return createJsonResponse(false, "Hotel no encontrado", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al actualizar hotel: " + e.getMessage(), null);
        }
    }

    public String delete(int hotelId) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                String idStr = readString(3); // Leemos los 3 caracteres del ID
                int currentId = Integer.parseInt(idStr.trim());

                if (currentId == hotelId) {
                    moveRemainingRecords(pos);
                    raf.setLength(raf.length() - RECORD_SIZE);
                    return createJsonResponse(true, "Hotel eliminado exitosamente", null);
                }
            }
            return createJsonResponse(false, "Hotel no encontrado", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al eliminar hotel: " + e.getMessage(), null);
        }
    }

    private void writeHotel(Hotel hotel) throws IOException {
        // ID y espacio
        String hotelIdStr = String.format("%03d", hotel.getHotelId());
        writeString(hotelIdStr, 3);
        writeString(" ", 1);

        // Información básica del hotel
        writeString(padRight(hotel.getName(), NAME_LENGTH), NAME_LENGTH);
        writeString(padRight(hotel.getAddress(), ADDRESS_LENGTH), ADDRESS_LENGTH);

        // Lista de habitaciones
        List<Room> rooms = hotel.getRooms() != null ? hotel.getRooms() : new ArrayList<>();
        for (int i = 0; i < MAX_ROOMS; i++) {
            String roomInfo = "";
            if (i < rooms.size() && rooms.get(i) != null) {
                roomInfo = padRight(rooms.get(i).getRoomNumber(), ROOM_NUMBER_LENGTH);
            }
            writeString(roomInfo, ROOM_NUMBER_LENGTH);
        }

        // Lista de huéspedes
        List<Guest> guests = hotel.getGuests() != null ? hotel.getGuests() : new ArrayList<>();
        for (int i = 0; i < MAX_GUESTS; i++) {
            String guestInfo = "";
            if (i < guests.size() && guests.get(i) != null) {
                guestInfo = padRight(
                        guests.get(i).getName() + ";" + guests.get(i).getId(),
                        GUEST_LENGTH
                );
            }
            writeString(guestInfo, GUEST_LENGTH);
        }
    }

    private Hotel readHotel() throws IOException {
        // Leer ID
        String hotelIdStr = readString(3);
        readString(1); // Saltar espacio
        int hotelId = Integer.parseInt(hotelIdStr.trim());

        // Leer información básica
        String name = readString(NAME_LENGTH).trim();
        String address = readString(ADDRESS_LENGTH).trim();

        // Leer habitaciones
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < MAX_ROOMS; i++) {
            String roomNumber = readString(ROOM_NUMBER_LENGTH).trim();
            if (!roomNumber.isEmpty()) {
                rooms.add(new Room(roomNumber, null, null, null));
            }
        }

        // Leer huéspedes
        List<Guest> guests = new ArrayList<>();
        for (int i = 0; i < MAX_GUESTS; i++) {
            String guestInfo = readString(GUEST_LENGTH).trim();
            if (!guestInfo.isEmpty()) {
                String[] parts = guestInfo.split(";");
                if (parts.length == 2) {
                    guests.add(new Guest(parts[1], parts[0]));
                }
            }
        }

        return new Hotel(hotelId, name, address, rooms, guests);
    }

    private void writeString(String str, int length) throws IOException {
        for (int i = 0; i < length; i++) {
            raf.writeChar(i < str.length() ? str.charAt(i) : '\0');
        }
    }

    private String readString(int length) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = raf.readChar();
            if (c != '\0') sb.append(c);
        }
        return sb.toString().trim();
    }

    private void moveRemainingRecords(long pos) throws IOException {
            byte[] buffer = new byte[RECORD_SIZE];
            long nextPos = pos + RECORD_SIZE;

            while (nextPos < raf.length()) {
                // Leer el siguiente registro
                raf.seek(nextPos);
                raf.readFully(buffer);

                // Escribir en la posición actual
                raf.seek(nextPos - RECORD_SIZE);
                raf.write(buffer);

                nextPos += RECORD_SIZE;
            }
    }

    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }

    public Hotel findById(int id) throws IOException {
        for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
            raf.seek(pos);
            int hotelId = raf.readInt();
            if (hotelId == id) {
                raf.seek(pos);
                return readHotel();
            }
        }
        return null;
    }

    public String retrieveByName(String searchName) throws IOException {
        try {
            List<Hotel> matchingHotels = new ArrayList<>();
            searchName = searchName.toLowerCase();

            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                Hotel hotel = readHotel();
                if (hotel.getName().toLowerCase().contains(searchName)) {
                    matchingHotels.add(hotel);
                }
            }

            if (!matchingHotels.isEmpty()) {
                return createJsonResponse(true, "Hoteles encontrados", matchingHotels);
            }
            return createJsonResponse(false, "No se encontraron hoteles con ese nombre", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al buscar hoteles: " + e.getMessage(), null);
        }
    }

    public String retrieveByAddress(String searchAddress) throws IOException {
        try {
            List<Hotel> matchingHotels = new ArrayList<>();
            searchAddress = searchAddress.toLowerCase();

            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                Hotel hotel = readHotel();
                if (hotel.getAddress().toLowerCase().contains(searchAddress)) {
                    matchingHotels.add(hotel);
                }
            }

            if (!matchingHotels.isEmpty()) {
                return createJsonResponse(true, "Hoteles encontrados", matchingHotels);
            }
            return createJsonResponse(false, "No se encontraron hoteles en esa dirección", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al buscar hoteles: " + e.getMessage(), null);
        }
    }

    private String padRight(String str, int length) {
        if (str == null) {
            str = "";
        }
        str = str.trim();
        return String.format("%-" + length + "s",
                str.length() > length ? str.substring(0, length) : str);
    }


}