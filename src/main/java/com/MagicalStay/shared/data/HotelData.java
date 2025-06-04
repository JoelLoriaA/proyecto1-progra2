package com.MagicalStay.shared.data;

import com.MagicalStay.shared.domain.Hotel;
import com.MagicalStay.shared.domain.Room;

import java.io.File;
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

        // Inicializar lista de rooms si es null
        List<Room> rooms = hotel.getRooms() != null ? hotel.getRooms() : new ArrayList<>();
        
        // Escribir las habitaciones
        for (int i = 0; i < MAX_ROOMS; i++) {
            String roomNumber = i < rooms.size() ? rooms.get(i).getRoomNumber() : "";
            writeString(buffer, roomNumber, 10);
        }

        raf.seek(raf.length());
        raf.write(buffer.array());

        return createJsonResponse(true, "Hotel created successfully", hotel);
    } catch (Exception e) {
        return createJsonResponse(false, "Error creating hotel: " + e.getMessage(), null);
    }
}

        // Métodos públicos de retrieveById (antes read/retrieveById)
    public String retrieveById(int hotelId) throws IOException {
        try {
            // Buscar el hotel en el archivo
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();

                int currentHotelId = buffer.getInt();
                if (currentHotelId == hotelId) {
                    // Si encuentra el hotel, lee sus datos
                    String name = readString(buffer, NAME_LENGTH);
                    String address = readString(buffer, ADDRESS_LENGTH);

                    List<Room> rooms = new ArrayList<>();
                    for (int i = 0; i < MAX_ROOMS; i++) {
                        String roomNumber = readString(buffer, 10);
                        if (!roomNumber.isEmpty()) {
                            rooms.add(new Room(roomNumber, null, null, null));
                        }
                    }

                    Hotel hotel = new Hotel(hotelId, name, address, rooms);
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
            return createJsonResponse(true, "Hoteles recuperados exitosamente", hotels);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al recuperar hoteles: " + e.getMessage(), null);
        }
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

    public String retrieveByName(String searchName) throws IOException {  // nombre se mantiene
    try {
        List<Hotel> matchingHotels = retrieveHotelsByName(searchName); // antes findHotelsByName
        if (!matchingHotels.isEmpty()) {
            return createJsonResponse(true, "Hoteles encontrados", matchingHotels);
        }
        return createJsonResponse(false, "No se encontraron hoteles con ese nombre", null);
    } catch (Exception e) {
        return createJsonResponse(false, "Error al buscar hoteles: " + e.getMessage(), null);
    }
}

private List<Hotel> retrieveHotelsByName(String searchName) throws IOException {  // antes findHotelsByName
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

public String retrieveByAddress(String searchAddress) throws IOException {  // nombre se mantiene
    try {
        List<Hotel> matchingHotels = retrieveHotelsByAddress(searchAddress);
        if (!matchingHotels.isEmpty()) {
            return createJsonResponse(true, "Hoteles encontrados", matchingHotels);
        }
        return createJsonResponse(false, "No se encontraron hoteles en esa dirección", null);
    } catch (Exception e) {
        return createJsonResponse(false, "Error al buscar hoteles: " + e.getMessage(), null);
    }
}
    // Update
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

    

    // Update
    private boolean updateHotelInFile(Hotel hotel) throws IOException {
        // Validación de campos básicos
        if (hotel.getName() == null || hotel.getAddress() == null) {
            throw new IllegalArgumentException("Nombre y dirección son requeridos");
        }

        if (hotel.getName().length() > NAME_LENGTH ||
            hotel.getAddress().length() > ADDRESS_LENGTH) {
            throw new IllegalArgumentException("Nombre o dirección exceden el largo máximo");
        }

        // Buscar y actualizar el hotel
        for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
            raf.seek(pos);
            if (raf.readInt() == hotel.getHotelId()) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);

                // Escribir datos básicos
                buffer.putInt(hotel.getHotelId());
                writeString(buffer, hotel.getName(), NAME_LENGTH);
                writeString(buffer, hotel.getAddress(), ADDRESS_LENGTH);

                // Validar y escribir habitaciones
                List<Room> rooms = hotel.getRooms();
                if (rooms == null) {
                    // Si las habitaciones son nulas, mantener las habitaciones existentes
                    raf.seek(pos + 4 + NAME_SIZE + ADDRESS_SIZE); // Posicionarse después de los datos básicos
                    byte[] existingRooms = new byte[ROOMS_LIST_SIZE];
                    raf.readFully(existingRooms);
                    buffer.put(existingRooms);
                } else {
                    // Escribir las nuevas habitaciones
                    for (int i = 0; i < MAX_ROOMS; i++) {
                        String roomNumber = "";
                        if (i < rooms.size() && rooms.get(i) != null) {
                            roomNumber = rooms.get(i).getRoomNumber();
                        }
                        writeString(buffer, roomNumber != null ? roomNumber : "", 10);
                    }
                }

                // Escribir el buffer actualizado
                raf.seek(pos);
                raf.write(buffer.array());
                raf.getFD().sync(); // Asegurar que los cambios se escriban en disco
                
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
    
    

    private List<Hotel> retrieveHotelsByAddress(String searchAddress) throws IOException {
        List<Hotel> matchingHotels = new ArrayList<>();
        searchAddress = searchAddress.toLowerCase();

        for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
            raf.seek(pos);
            ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
            raf.readFully(buffer.array());
            buffer.rewind();

            int hotelId = buffer.getInt();
            String name = readString(buffer, NAME_LENGTH);
            String address = readString(buffer, ADDRESS_LENGTH);

            if (address.toLowerCase().contains(searchAddress)) {
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
    
}