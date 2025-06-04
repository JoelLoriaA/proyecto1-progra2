package com.MagicalStay.shared.data;

import com.MagicalStay.shared.domain.Hotel;
import com.MagicalStay.shared.domain.Room;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class HotelData extends JsonDataResponse {
    private RandomAccessFile raf;
    private static final int HOTEL_ID_SIZE = 4;
    private static final int NAME_LENGTH = 50;
    private static final int ADDRESS_LENGTH = 100;
    private static final int NAME_SIZE = NAME_LENGTH * 2;
    private static final int ADDRESS_SIZE = ADDRESS_LENGTH * 2;
    private static final int MAX_ROOMS = 50;
    private static final int ROOMS_LIST_SIZE = MAX_ROOMS * (12 * 2);
    private static final int RECORD_SIZE = HOTEL_ID_SIZE + NAME_SIZE + ADDRESS_SIZE + ROOMS_LIST_SIZE;

    public HotelData(String filename) throws IOException {
        this.raf = new RandomAccessFile(filename, "rw");
    }

    public HotelData() {
    }

    public String create(Hotel hotel) throws IOException {
        try {
            if (hotel.getName().length() > NAME_LENGTH ||
                    hotel.getAddress().length() > ADDRESS_LENGTH) {
                return createJsonResponse(false, "Name or address exceeds maximum length", null);
            }

            raf.seek(raf.length());
            writeHotel(hotel);
            return createJsonResponse(true, "Hotel created successfully", hotel);
        } catch (Exception e) {
            return createJsonResponse(false, "Error creating hotel: " + e.getMessage(), null);
        }
    }

    public String retrieveById(int hotelId) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                if (raf.readInt() == hotelId) {
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
            if (hotel.getName().length() > NAME_LENGTH ||
                    hotel.getAddress().length() > ADDRESS_LENGTH) {
                return createJsonResponse(false, "Name or address exceeds maximum length", null);
            }

            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                if (raf.readInt() == hotel.getHotelId()) {
                    raf.seek(pos);
                    writeHotel(hotel);
                    return createJsonResponse(true, "Hotel updated successfully", hotel);
                }
            }
            return createJsonResponse(false, "Hotel not found", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error updating hotel: " + e.getMessage(), null);
        }
    }

    public String delete(int hotelId) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                if (raf.readInt() == hotelId) {
                    moveRemainingRecords(pos);
                    raf.setLength(raf.length() - RECORD_SIZE);
                    return createJsonResponse(true, "Hotel deleted successfully", null);
                }
            }
            return createJsonResponse(false, "Hotel not found", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error deleting hotel: " + e.getMessage(), null);
        }
    }

    private void writeHotel(Hotel hotel) throws IOException {
        raf.writeInt(hotel.getHotelId());
        writeString(hotel.getName(), NAME_LENGTH);
        writeString(hotel.getAddress(), ADDRESS_LENGTH);

        List<Room> rooms = hotel.getRooms() != null ? hotel.getRooms() : new ArrayList<>();
        for (int i = 0; i < MAX_ROOMS; i++) {
            String roomNumber = i < rooms.size() && rooms.get(i) != null ?
                    rooms.get(i).getRoomNumber() : "";
            writeString(roomNumber, 12);
        }
    }

    private Hotel readHotel() throws IOException {
        int hotelId = raf.readInt();
        String name = readString(NAME_LENGTH);
        String address = readString(ADDRESS_LENGTH);

        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < MAX_ROOMS; i++) {
            String roomNumber = readString(12);
            if (!roomNumber.trim().isEmpty()) {
                rooms.add(new Room(roomNumber, null, null, null));
            }
        }

        return new Hotel(hotelId, name, address, rooms);
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
        long nextPos = pos + RECORD_SIZE;
        while (nextPos < raf.length()) {
            raf.seek(nextPos);
            byte[] nextRecord = new byte[RECORD_SIZE];
            raf.readFully(nextRecord);
            raf.seek(nextPos - RECORD_SIZE);
            raf.write(nextRecord);
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
}