package com.MagicalStay.shared.data;

import com.MagicalStay.shared.domain.Room;
import com.MagicalStay.shared.domain.RoomType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.MagicalStay.shared.domain.RoomCondition;
import com.MagicalStay.shared.domain.RoomDTO;
import com.MagicalStay.shared.domain.Hotel;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoomData extends JsonDataResponse {
    private RandomAccessFile raf;
    private static final int ROOM_NUMBER_SIZE = 20;    // 10 caracteres
    private static final int ROOM_TYPE_SIZE = 4;       // int para enum ordinal
    private static final int ROOM_CONDITION_SIZE = 4;  // int para enum ordinal
    private static final int HOTEL_ID_SIZE = 4;      // 3 chars × 2 bytes = 6 bytes
    private static final int PRICE_SIZE = 8;             // double
    private static final int CAPACITY_SIZE = 4;          // int
    private static final int FEATURES_SIZE = 100;        // caracteres (ajustable)
    private static final int DESCRIPTION_SIZE = 200;         // long para Hotel ID
    private static final int IMAGE_SIZE = 200; 
    private static final int RECORD_SIZE = ROOM_NUMBER_SIZE + ROOM_TYPE_SIZE +
        ROOM_CONDITION_SIZE + HOTEL_ID_SIZE + PRICE_SIZE +
        CAPACITY_SIZE + FEATURES_SIZE + DESCRIPTION_SIZE + IMAGE_SIZE;


    // Dependencia para obtener objetos Hotel
    private HotelData hotelData;

    public RoomData() {
        throw new UnsupportedOperationException("Usa el constructor RoomData(String, HotelData)");
    }
    

    public RoomData(String filename, HotelData hotelData) throws IOException {
        this.raf = new RandomAccessFile(filename, "rw");
        this.hotelData = hotelData;
        
    }
    

public String create(Room room) {
        try {
            if (room.getRoomNumber() == null || room.getHotel() == null) {
                return createJsonResponse(false, "Faltan datos obligatorios para crear la habitación", null);
            }
    
            ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
    
            writeString(buffer, room.getRoomNumber(), ROOM_NUMBER_SIZE);
            buffer.putInt(room.getRoomType().ordinal());
            buffer.putInt(room.getRoomCondition().ordinal());
            buffer.putInt(room.getHotel().getHotelId());
            buffer.putDouble(room.getPrice());
            buffer.putInt(room.getCapacity());
            writeString(buffer, room.getFeatures(), FEATURES_SIZE);
            writeString(buffer, room.getDescription(), DESCRIPTION_SIZE);
            writeString(buffer, room.getImagePath(), IMAGE_SIZE);
    
            raf.seek(raf.length());
            raf.write(buffer.array());
    
            if (hotelData != null) {
                Hotel hotelCompleto = hotelData.findById(room.getHotel().getHotelId());
                if (hotelCompleto != null) {
                    room.setHotel(hotelCompleto);
                }
            }
    
            return createJsonResponse(true, "Habitación creada exitosamente", room);
    
        } catch (Exception e) {
            e.printStackTrace();
            return createJsonResponse(false, "Error al crear la habitación: " + e.getMessage(), null);
        }
    }
    
     

    public String read(String roomNumber) throws IOException {
        roomNumber = roomNumber.trim();
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();
    
                String currentRoomNumber = readString(buffer, ROOM_NUMBER_SIZE).trim();
                if (currentRoomNumber.equals(roomNumber)) {
                    int roomTypeOrdinal = buffer.getInt();
                    int roomConditionOrdinal = buffer.getInt();
                    int hotelId = buffer.getInt();   // ✅
                    double price = buffer.getDouble();
                    int capacity = buffer.getInt();
                    String features = readString(buffer, FEATURES_SIZE).trim();
                    String description = readString(buffer, DESCRIPTION_SIZE).trim();
                    String image = readString(buffer, IMAGE_SIZE).trim();
    
                    RoomType roomType = RoomType.values()[roomTypeOrdinal];
                    RoomCondition roomCondition = RoomCondition.values()[roomConditionOrdinal];
                    Hotel hotel = getHotelById((int) hotelId);
    
                    if (hotel != null) {
                        Room room = new Room(currentRoomNumber, hotel, roomType, roomCondition, price, capacity, features, description, image);
                        return createJsonResponse(true, "Habitación encontrada", new RoomDTO(room));
                    } else {
                        return createJsonResponse(false, "Error: Hotel no encontrado para la habitación", null);
                    }
                }
            }
    
            return createJsonResponse(false, "Habitación no encontrada", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al leer la habitación: " + e.getMessage(), null);
        }
    }

    public String readAll() {
        try {
            List<Room> rooms = loadRooms(); 
    
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("data", rooms);
            return mapper.writeValueAsString(response);
    
        } catch (Exception e) {
            return createJsonResponse(false, "Error al leer todas las habitaciones: " + e.getMessage(), null);
        }
    }

    public String retrieveAll() throws IOException {
        try {
            List<Room> rooms = new ArrayList<>();
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();
    
                String roomNumber = readString(buffer, ROOM_NUMBER_SIZE).trim();

                if (roomNumber.isEmpty()) {
                    continue; // Ignorar registros vacíos
                }
    
                int typeOrdinal = buffer.getInt();
                int conditionOrdinal = buffer.getInt();
                int hotelId = buffer.getInt();
                double price = buffer.getDouble();
                int capacity = buffer.getInt();
                String features = readString(buffer, 100);
                String description = readString(buffer, 200);
                String image = readString(buffer, 200);
    
                Hotel hotel = hotelData.findById(hotelId);
                if (hotel == null) {
                    System.err.println("⚠️ Hotel no encontrado para ID: " + hotelId);
                    continue; // Saltar esta habitación si el hotel no existe
                }
    
                Room room = new Room(
                    roomNumber,
                    RoomType.values()[typeOrdinal],
                    RoomCondition.values()[conditionOrdinal],
                    hotel
                );
                room.setPrice(price);
                room.setCapacity(capacity);
                room.setFeatures(features);
                room.setDescription(description);
                room.setImagePath(image);
    
                rooms.add(room);
            }
    
            return createJsonResponse(true, "Habitaciones recuperadas exitosamente", rooms);
        } catch (Exception e) {
            e.printStackTrace(); // Ayuda en depuración
            return createJsonResponse(false, "Error al recuperar habitaciones: " + e.getMessage(), null);
        }
    }
    
    
    
    public String update(Room room) throws IOException {
        if (room.getRoomNumber() == null) {
            return createJsonResponse(false, "Número de habitación inválido", null);
        }
    
        String targetRoomNumber = room.getRoomNumber().trim();
    
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();
    
                String currentRoomNumber = readString(buffer, ROOM_NUMBER_SIZE).trim();
    
                if (currentRoomNumber.equals(targetRoomNumber)) {
                    raf.seek(pos);
                    buffer = ByteBuffer.allocate(RECORD_SIZE);
                    writeString(buffer, room.getRoomNumber(), ROOM_NUMBER_SIZE);
                    buffer.putInt(room.getRoomType().ordinal());
                    buffer.putInt(room.getRoomCondition().ordinal());
                    buffer.putInt(room.getHotel().getHotelId());
                    buffer.putDouble(room.getPrice());
                    buffer.putInt(room.getCapacity());
                    writeString(buffer, room.getFeatures(), FEATURES_SIZE);
                    writeString(buffer, room.getDescription(), DESCRIPTION_SIZE);
                    writeString(buffer, room.getImagePath(), IMAGE_SIZE);
                    raf.write(buffer.array());
                    raf.getFD().sync();
    
                    return createJsonResponse(true, "Habitación actualizada exitosamente", new RoomDTO(room));
                }
            }
    
            return createJsonResponse(false, "Habitación no encontrada", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al actualizar la habitación: " + e.getMessage(), null);
        }
    }
    

    public String delete(String roomNumber) {
        try {
            String target = roomNumber.trim();
            List<Room> allRooms = loadRooms(); // Asegúrate de que este método lea correctamente
            List<Room> updatedRooms = new ArrayList<>();
    
            boolean encontrada = false;
    
            for (Room room : allRooms) {
                if (room.getRoomNumber().trim().equalsIgnoreCase(target)) {
                    encontrada = true;
                    System.out.println("[DELETE] Eliminando habitación: " + target);
                    // No se agrega a la lista actualizada
                } else {
                    updatedRooms.add(room);
                }
            }
    
            if (!encontrada) {
                return createJsonResponse(false, "No se encontró la habitación con número: " + roomNumber, null);
            }
    
            // Reescribir el archivo
            raf.setLength(0);
            raf.seek(0);
    
            for (Room room : updatedRooms) {
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                writeString(buffer, room.getRoomNumber() != null ? room.getRoomNumber() : "", ROOM_NUMBER_SIZE);                buffer.putInt(room.getRoomType().ordinal());
                buffer.putInt(room.getRoomCondition().ordinal());
                buffer.putInt(room.getHotel().getHotelId()); 
                buffer.putDouble(room.getPrice());
                buffer.putInt(room.getCapacity());
                writeString(buffer, room.getFeatures() != null ? room.getFeatures() : "", FEATURES_SIZE);       
                writeString(buffer, room.getDescription() != null ? room.getDescription() : "", DESCRIPTION_SIZE);
                writeString(buffer, room.getImagePath() != null ? room.getImagePath() : "", IMAGE_SIZE);
                raf.write(buffer.array());
            }
    
            return createJsonResponse(true, "Habitación eliminada exitosamente", null);
    
        } catch (Exception e) {
            System.err.println("[ERROR] Excepción al eliminar habitación:");
            e.printStackTrace();  // Esto es importante
            return createJsonResponse(false, "Error al eliminar la habitación: " + e.getMessage(), null);
        }
        
    }
    

    public String readByRoomType(RoomType roomType) throws IOException {
        try {
            List<Room> rooms = new ArrayList<>();
            raf.seek(0);
    
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();
    
                String roomNumber = readString(buffer, ROOM_NUMBER_SIZE).trim();
                int roomTypeOrdinal = buffer.getInt();
                int roomConditionOrdinal = buffer.getInt();
                int hotelId = buffer.getInt();
                double price = buffer.getDouble();
                int capacity = buffer.getInt();
                String features = readString(buffer, FEATURES_SIZE).trim();
                String description = readString(buffer, DESCRIPTION_SIZE).trim();
                String image = readString(buffer, IMAGE_SIZE).trim();
    
                if (roomTypeOrdinal == roomType.ordinal()) {
                    RoomCondition condition = RoomCondition.values()[roomConditionOrdinal];
                    Hotel hotel = getHotelById((int) hotelId);
    
                    if (hotel != null) {
                        rooms.add(new Room(roomNumber, hotel, roomType, condition, price, capacity, features, description, image));
                    }
                }
            }
            return createJsonResponse(true, "Habitaciones por tipo recuperadas exitosamente", rooms);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al recuperar habitaciones por tipo: " + e.getMessage(), null);
        }
    }

    public String readByRoomCondition(RoomCondition roomCondition) throws IOException {
        try {
            List<Room> rooms = new ArrayList<>();
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();
    
                String roomNumber = readString(buffer, ROOM_NUMBER_SIZE).trim();
                int roomTypeOrdinal = buffer.getInt();
                int roomConditionOrdinal = buffer.getInt();
                int hotelId = buffer.getInt();
                double price = buffer.getDouble();
                int capacity = buffer.getInt();
                String features = readString(buffer, FEATURES_SIZE).trim();
                String description = readString(buffer, DESCRIPTION_SIZE).trim();
                String image = readString(buffer, IMAGE_SIZE).trim();
    
                if (roomConditionOrdinal == roomCondition.ordinal()) {
                    try {
                        RoomType roomType = RoomType.values()[roomTypeOrdinal];
                        Hotel hotel = getHotelById((int) hotelId);
    
                        if (hotel != null) {
                            rooms.add(new Room(roomNumber, hotel, roomType, roomCondition, price, capacity, features, description, image));
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        continue;
                    }
                }
            }
            return createJsonResponse(true, "Habitaciones por condición recuperadas exitosamente", rooms);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al recuperar habitaciones por condición: " + e.getMessage(), null);
        }
    }
    
    private Hotel getHotelById(int id) {
        try {
            String json = hotelData.retrieveById((int) id);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            boolean success = root.path("success").asBoolean();
            if (!success) return null;

            JsonNode data = root.path("data");
            if (data.isMissingNode()) return null;

            int hotelId = data.path("hotelId").asInt();
            String name = data.path("name").asText();
            String address = data.path("address").asText();

            return new Hotel(hotelId, name, address);

        } catch (Exception e) {
            return null;
        }
    }


    private void writeString(ByteBuffer buffer, String value, int size) {
        byte[] bytes = (value != null ? value : "").getBytes(StandardCharsets.UTF_8);
        if (bytes.length > size) {
            buffer.put(bytes, 0, size);
        } else {
            buffer.put(bytes);
            buffer.put(new byte[size - bytes.length]); 
        }
    }
    
    
    private String readString(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
    
        // Filtra caracteres nulos (\0) para mayor seguridad
        int actualLength = 0;
        while (actualLength < length && bytes[actualLength] != 0) {
            actualLength++;
        }
    
        return new String(bytes, 0, actualLength, StandardCharsets.UTF_8).trim();
    }
    
    
    
    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }

    public List<Room> loadRooms() throws IOException {
        List<Room> rooms = new ArrayList<>();
        raf.seek(0);
    
        while (raf.getFilePointer() < raf.length()) {
            byte[] data = new byte[RECORD_SIZE];
            raf.readFully(data);
            ByteBuffer buffer = ByteBuffer.wrap(data);
    
            String roomNumber = readString(buffer, ROOM_NUMBER_SIZE);
            int typeOrdinal = buffer.getInt();
            int statusOrdinal = buffer.getInt();
            int hotelId = buffer.getInt();
            double price = buffer.getDouble();
            int capacity = buffer.getInt();
            String features = readString(buffer, FEATURES_SIZE);
            String description = readString(buffer, DESCRIPTION_SIZE);
            String image = readString(buffer, IMAGE_SIZE);
    
            
            String json = hotelData.retrieveById(hotelId);
            JsonResponse response = new ObjectMapper().readValue(json, JsonResponse.class);
            Hotel hotel = null;
            
            if (response.isSuccess()) {
                hotel = new ObjectMapper().convertValue(response.getData(), Hotel.class);
            } else {
                System.err.println("⚠️ No se encontró el hotel con ID " + hotelId + ": " + response.getMessage());
                continue; // o manejar como null según el caso
            }
    
            Room room = new Room(
                roomNumber,
                hotel,  
                RoomType.values()[typeOrdinal],
                RoomCondition.values()[statusOrdinal],
                price,
                capacity,
                features,
                description,
                image
            );
    
            rooms.add(room);
        }
    
        return rooms;
    }
    
    public void saveRoom(List<Room> rooms) throws IOException {
        raf.setLength(0); // Borrar contenido previo
        for (Room room : rooms) {
            ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
            writeString(buffer, room.getRoomNumber(), ROOM_NUMBER_SIZE);
            buffer.putInt(room.getRoomType().ordinal());
            buffer.putInt(room.getRoomCondition().ordinal());
            buffer.putInt(room.getHotel().getHotelId());
            buffer.putDouble(room.getPrice());
            buffer.putInt(room.getCapacity());
            writeString(buffer, room.getFeatures(), FEATURES_SIZE);
            writeString(buffer, room.getDescription(), DESCRIPTION_SIZE);
            writeString(buffer, room.getImagePath(), IMAGE_SIZE);
            raf.write(buffer.array());
        }
    }

    public void clearDataFile() throws IOException {
        raf.setLength(0);
        raf.seek(0);
    }

    public List<Room> getRoomsByHotelId(int hotelId) throws IOException {
        return loadRooms().stream()
            .filter(r -> r.getHotel() != null && r.getHotel().getHotelId() == hotelId)
            .collect(Collectors.toList());
    }
    
}