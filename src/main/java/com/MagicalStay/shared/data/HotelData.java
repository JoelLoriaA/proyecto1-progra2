package com.MagicalStay.shared.data;

import com.MagicalStay.shared.domain.Hotel;
import com.MagicalStay.shared.domain.Room;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class HotelData extends JsonDataResponse {
    private RandomAccessFile raf;
    private static final int HOTEL_ID_SIZE = 3 * 2; // String de 3 caracteres (6 bytes)
    private static final int NAME_LENGTH = 50;
    private static final int ADDRESS_LENGTH = 100;
    private static final int NAME_SIZE = NAME_LENGTH * 2;
    private static final int ADDRESS_SIZE = ADDRESS_LENGTH * 2;
    private static final int MAX_ROOMS = 50;
    private static final int ROOMS_LIST_SIZE = MAX_ROOMS * (12 * 2); // 12 chars por número de habitación
    private static final int RECORD_SIZE = HOTEL_ID_SIZE + NAME_SIZE + ADDRESS_SIZE + ROOMS_LIST_SIZE;


    public HotelData(String filename) throws IOException {
        this.raf = new RandomAccessFile(filename, "rw");
    }

    public HotelData() {
    }

    // Create
    public String create(Hotel hotel) throws IOException {
        try {
            if (hotel.getName().length() > NAME_LENGTH ||
                hotel.getAddress().length() > ADDRESS_LENGTH) {
                return createJsonResponse(false, "Name or address exceeds maximum length", null);
            }

            ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
            buffer.putInt(hotel.getHotelId());
            writeString(buffer, hotel.getName(), NAME_LENGTH);
            writeString(buffer, hotel.getAddress(), ADDRESS_LENGTH);


            if (!getAllHotels().isEmpty()) {

                List<Room> rooms = hotel.getRooms();
                for (int i = 0; i < MAX_ROOMS; i++) {
                String roomNumber = i < rooms.size() ? rooms.get(i).getRoomNumber() : "";
                writeString(buffer, roomNumber, 10);
                }   
                
        }  

            raf.seek(raf.length());
            raf.write(buffer.array());

            return createJsonResponse(true, "Hotel created successfully", hotel);
        } catch (Exception e) {
            return createJsonResponse(false, "Error creating hotel: " + e.getMessage(), null);
        }
    }

    public String read(int hotelId) throws IOException {
        try {
            Hotel hotel = findHotel(hotelId);
            if (hotel != null) {
                return createJsonResponse(true, "Hotel found", hotel);
            }
            return createJsonResponse(false, "Hotel not found", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error reading hotel: " + e.getMessage(), null);
        }
    }

    public String readAll() throws IOException {
        try {
            List<Hotel> hotels = getAllHotels();
            return createJsonResponse(true, "Hotels retrieved successfully", hotels);
        } catch (Exception e) {
            return createJsonResponse(false, "Error retrieving hotels: " + e.getMessage(), null);
        }
    }

    public String update(Hotel hotel) throws IOException {
        try {
            if (hotel.getName().length() > NAME_LENGTH ||
                hotel.getAddress().length() > ADDRESS_LENGTH) {
                return createJsonResponse(false, "Name or address exceeds maximum length", null);
            }

            if (updateHotelInFile(hotel)) {
                return createJsonResponse(true, "Hotel updated successfully", hotel);
            }
            return createJsonResponse(false, "Hotel not found", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error updating hotel: " + e.getMessage(), null);
        }
    }

    public String delete(int hotelId) throws IOException {
        try {
            if (deleteHotelFromFile(hotelId)) {
                return createJsonResponse(true, "Hotel deleted successfully", null);
            }
            return createJsonResponse(false, "Hotel not found", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error deleting hotel: " + e.getMessage(), null);
        }
    }

    public String findByName(String searchName) throws IOException {
        try {
            List<Hotel> matchingHotels = findHotelsByName(searchName);
            if (!matchingHotels.isEmpty()) {
                return createJsonResponse(true, "Hotels found", matchingHotels);
            }
            return createJsonResponse(false, "No hotels found with the given name", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error searching hotels: " + e.getMessage(), null);
        }
    }

    // Read
    private Hotel findHotel(int hotelId) throws IOException {
        for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
            raf.seek(pos);
            ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
            raf.readFully(buffer.array());
            buffer.rewind();

            int currentHotelId = buffer.getInt();
            if (currentHotelId == hotelId) {
                String name = readString(buffer, NAME_LENGTH);
                String address = readString(buffer, ADDRESS_LENGTH);

                List<Room> rooms = new ArrayList<>();
                for (int i = 0; i < MAX_ROOMS; i++) {
                    String roomNumber = readString(buffer, 10);
                    if (!roomNumber.isEmpty()) {
                        // Aquí solo guardamos el número de habitación, los demás detalles
                        // deberán cargarse desde RoomData
                        rooms.add(new Room(roomNumber, null, null, null));
                    }
                }

                return new Hotel(hotelId, name, address, rooms);
            }
        }
        return null;
    }

    // Read All
    public List<Hotel> getAllHotels() throws IOException {
        List<Hotel> hotels = new ArrayList<>();
        for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
            raf.seek(pos);
            ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
            raf.readFully(buffer.array());
            buffer.rewind();

            int hotelId = buffer.getInt();
            String name = readString(buffer, NAME_LENGTH);
            String address = readString(buffer, ADDRESS_LENGTH);

            List<Room> rooms = new ArrayList<>();
            for (int i = 0; i < MAX_ROOMS; i++) {
                String roomNumber = readString(buffer, 10);
                if (!roomNumber.isEmpty()) {
                    rooms.add(new Room(roomNumber, null, null, null));
                }
            }

            hotels.add(new Hotel(hotelId, name, address, rooms));
        }
        return hotels;
    }

    // Update
    private boolean updateHotelInFile(Hotel hotel) throws IOException {
        if (hotel.getName().length() > NAME_LENGTH ||
            hotel.getAddress().length() > ADDRESS_LENGTH) {
            throw new IllegalArgumentException("Name or address exceeds maximum length");
        }

        for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
            raf.seek(pos);
            if (raf.readInt() == hotel.getHotelId()) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);

                buffer.putInt(hotel.getHotelId());
                writeString(buffer, hotel.getName(), NAME_LENGTH);
                writeString(buffer, hotel.getAddress(), ADDRESS_LENGTH);

                List<Room> rooms = hotel.getRooms();
                for (int i = 0; i < MAX_ROOMS; i++) {
                    String roomNumber = i < rooms.size() ? rooms.get(i).getRoomNumber() : "";
                    writeString(buffer, roomNumber, 10);
                }

                raf.write(buffer.array());
                return true;
            }
        }
        return false;
    }

    // Delete
    private boolean deleteHotelFromFile(int hotelId) throws IOException {
        for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
            raf.seek(pos);
            if (raf.readInt() == hotelId) {
                // Mover todos los registros siguientes una posición hacia arriba
                long nextPos = pos + RECORD_SIZE;
                while (nextPos < raf.length()) {
                    raf.seek(nextPos);
                    byte[] nextRecord = new byte[RECORD_SIZE];
                    raf.readFully(nextRecord);
                    raf.seek(nextPos - RECORD_SIZE);
                    raf.write(nextRecord);
                    nextPos += RECORD_SIZE;
                }
                raf.setLength(raf.length() - RECORD_SIZE);
                return true;
            }
        }
        return false;
    }

    // Métodos auxiliares para manejo de strings
    private void writeString(ByteBuffer buffer, String str, int length) {
        for (int i = 0; i < length; i++) {
            buffer.putChar(i < str.length() ? str.charAt(i) : '\0');
        }
    }

    private String readString(ByteBuffer buffer, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = buffer.getChar();
            if (c != '\0') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // Método para buscar por nombre (búsqueda parcial)
    private List<Hotel> findHotelsByName(String searchName) throws IOException {
        List<Hotel> matchingHotels = new ArrayList<>();
        searchName = searchName.toLowerCase();

        for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
            raf.seek(pos);
            ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
            raf.readFully(buffer.array());
            buffer.rewind();

            int hotelId = buffer.getInt();
            String name = readString(buffer, NAME_LENGTH);

            if (name.toLowerCase().contains(searchName)) {
                // Si encuentra coincidencia, lee el registro completo
                String address = readString(buffer, ADDRESS_LENGTH);
                List<Room> rooms = new ArrayList<>();
                for (int i = 0; i < MAX_ROOMS; i++) {
                    String roomNumber = readString(buffer, 10);
                    if (!roomNumber.isEmpty()) {
                        rooms.add(new Room(roomNumber, null, null, null));
                    }
                }
                matchingHotels.add(new Hotel(hotelId, name, address, rooms));
            }
        }
        return matchingHotels;
    }

    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }

    public Hotel findById(int id) throws IOException {
        for (int pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
            raf.seek(pos);
            ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
            raf.readFully(buffer.array());
            buffer.rewind();
    
            int hotelId = buffer.getInt();
            String name = readString(buffer, NAME_LENGTH);
            String address = readString(buffer, ADDRESS_LENGTH);
    
            List<Room> rooms = new ArrayList<>();
            for (int i = 0; i < MAX_ROOMS; i++) {
                String roomNumber = readString(buffer, 10);
                if (!roomNumber.isEmpty()) {
                    rooms.add(new Room(roomNumber, null, null, null));
                }
            }
    
            if (hotelId == id) {
                return new Hotel(hotelId, name, address, rooms);
            }
        }
        return null;
    }
    
}