package com.MagicalStay.data;

import com.MagicalStay.domain.Room;
import com.MagicalStay.domain.RoomType;
import com.MagicalStay.domain.RoomCondition;
import com.MagicalStay.domain.Hotel;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class RoomData extends JsonDataResponse {
    private RandomAccessFile raf;
    private static final int ROOM_NUMBER_SIZE = 20;    // 10 caracteres
    private static final int ROOM_TYPE_ID_SIZE = 8;    // long para RoomType ID
    private static final int ROOM_CONDITION_ID_SIZE = 8; // long para RoomCondition ID
    private static final int HOTEL_ID_SIZE = 8;        // long para Hotel ID
    private static final int RECORD_SIZE = ROOM_NUMBER_SIZE + ROOM_TYPE_ID_SIZE +
            ROOM_CONDITION_ID_SIZE + HOTEL_ID_SIZE;

    // Dependencias para obtener objetos completos
    private RoomTypeData roomTypeData;
    private RoomConditionData roomConditionData;
    private HotelData hotelData;

    public RoomData(String filename, RoomTypeData roomTypeData,
                    RoomConditionData roomConditionData, HotelData hotelData) throws IOException {
        this.raf = new RandomAccessFile(filename, "rw");
        this.roomTypeData = roomTypeData;
        this.roomConditionData = roomConditionData;
        this.hotelData = hotelData;
    }

    public String create(Room room) throws IOException {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);

            writeString(buffer, room.getRoomNumber(), ROOM_NUMBER_SIZE);
            buffer.putLong(room.getRoomType().getId());
            buffer.putLong(room.getRoomCondition().getId());
            buffer.putLong(room.getHotel().getId());

            raf.seek(raf.length());
            raf.write(buffer.array());

            return createJsonResponse(true, "Habitación creada exitosamente", room);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al crear la habitación: " + e.getMessage(), null);
        }
    }

    public String read(String roomNumber) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();

                String currentRoomNumber = readString(buffer, ROOM_NUMBER_SIZE);
                if (currentRoomNumber.trim().equals(roomNumber)) {
                    long roomTypeId = buffer.getLong();
                    long roomConditionId = buffer.getLong();
                    long hotelId = buffer.getLong();

                    // Obtener objetos completos usando las dependencias
                    RoomType roomType = getRoomTypeById(roomTypeId);
                    RoomCondition roomCondition = getRoomConditionById(roomConditionId);
                    Hotel hotel = getHotelById(hotelId);

                    if (roomType != null && roomCondition != null && hotel != null) {
                        Room room = new Room(currentRoomNumber.trim(), roomType, roomCondition, hotel);
                        return createJsonResponse(true, "Habitación encontrada", room);
                    } else {
                        return createJsonResponse(false, "Error: Referencias inválidas en la habitación", null);
                    }
                }
            }
            return createJsonResponse(false, "Habitación no encontrada", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al leer la habitación: " + e.getMessage(), null);
        }
    }

    public String readAll() throws IOException {
        try {
            List<Room> rooms = new ArrayList<>();
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();

                String roomNumber = readString(buffer, ROOM_NUMBER_SIZE);
                long roomTypeId = buffer.getLong();
                long roomConditionId = buffer.getLong();
                long hotelId = buffer.getLong();

                // Obtener objetos completos
                RoomType roomType = getRoomTypeById(roomTypeId);
                RoomCondition roomCondition = getRoomConditionById(roomConditionId);
                Hotel hotel = getHotelById(hotelId);

                if (roomType != null && roomCondition != null && hotel != null) {
                    rooms.add(new Room(roomNumber.trim(), roomType, roomCondition, hotel));
                }
            }
            return createJsonResponse(true, "Habitaciones recuperadas exitosamente", rooms);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al recuperar las habitaciones: " + e.getMessage(), null);
        }
    }

    public String update(Room room) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer readBuffer = ByteBuffer.allocate(ROOM_NUMBER_SIZE);
                raf.readFully(readBuffer.array());
                String currentRoomNumber = readString(readBuffer, ROOM_NUMBER_SIZE);

                if (currentRoomNumber.trim().equals(room.getRoomNumber())) {
                    raf.seek(pos);
                    ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);

                    writeString(buffer, room.getRoomNumber(), ROOM_NUMBER_SIZE);
                    buffer.putLong(room.getRoomType().getId());
                    buffer.putLong(room.getRoomCondition().getId());
                    buffer.putLong(room.getHotel().getId());

                    raf.write(buffer.array());
                    return createJsonResponse(true, "Habitación actualizada exitosamente", room);
                }
            }
            return createJsonResponse(false, "Habitación no encontrada", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al actualizar la habitación: " + e.getMessage(), null);
        }
    }

    public String delete(String roomNumber) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer readBuffer = ByteBuffer.allocate(ROOM_NUMBER_SIZE);
                raf.readFully(readBuffer.array());
                String currentRoomNumber = readString(readBuffer, ROOM_NUMBER_SIZE);

                if (currentRoomNumber.trim().equals(roomNumber)) {
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
                    return createJsonResponse(true, "Habitación eliminada exitosamente", null);
                }
            }
            return createJsonResponse(false, "Habitación no encontrada", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al eliminar la habitación: " + e.getMessage(), null);
        }
    }

    // Métodos auxiliares para obtener objetos por ID
    private RoomType getRoomTypeById(long id) {
        try {
            // Asumiendo que RoomTypeData tiene un método similar
            String response = roomTypeData.read(String.valueOf(id));
            // Necesitarías parsear la respuesta JSON para obtener el objeto RoomType
            // Esto depende de cómo implementes JsonDataResponse
            return null; // Implementar según tu lógica de deserialización
        } catch (Exception e) {
            return null;
        }
    }

    private RoomCondition getRoomConditionById(long id) {
        try {
            String response = roomConditionData.read(String.valueOf(id));
            // Parsear respuesta JSON para obtener RoomCondition
            return null; // Implementar según tu lógica
        } catch (Exception e) {
            return null;
        }
    }

    private Hotel getHotelById(long id) {
        try {
            String response = hotelData.read(String.valueOf(id));
            // Parsear respuesta JSON para obtener Hotel
            return null; // Implementar según tu lógica
        } catch (Exception e) {
            return null;
        }
    }

    private void writeString(ByteBuffer buffer, String str, int size) {
        for (int i = 0; i < size/2; i++) {
            buffer.putChar(i < str.length() ? str.charAt(i) : ' ');
        }
    }

    private String readString(ByteBuffer buffer, int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size/2; i++) {
            sb.append(buffer.getChar());
        }
        return sb.toString();
    }

    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }
}