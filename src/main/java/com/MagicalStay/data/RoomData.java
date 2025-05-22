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
    private static final int ROOM_TYPE_SIZE = 4;       // int para enum ordinal
    private static final int ROOM_CONDITION_SIZE = 4;  // int para enum ordinal
    private static final int HOTEL_ID_SIZE = 8;        // long para Hotel ID
    private static final int RECORD_SIZE = ROOM_NUMBER_SIZE + ROOM_TYPE_SIZE +
            ROOM_CONDITION_SIZE + HOTEL_ID_SIZE;

    // Dependencia para obtener objetos Hotel
    private HotelData hotelData;

    public RoomData(String filename, HotelData hotelData) throws IOException {
        this.raf = new RandomAccessFile(filename, "rw");
        this.hotelData = hotelData;
    }

    public String create(Room room) throws IOException {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);

            writeString(buffer, room.getRoomNumber(), ROOM_NUMBER_SIZE);
            buffer.putInt(room.getRoomType().ordinal());
            buffer.putInt(room.getRoomCondition().ordinal());
            buffer.putLong(room.getHotel().getHotelId());

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
                    int roomTypeOrdinal = buffer.getInt();
                    int roomConditionOrdinal = buffer.getInt();
                    long hotelId = buffer.getLong();

                    // Convertir ordinales a enums
                    RoomType roomType = RoomType.values()[roomTypeOrdinal];
                    RoomCondition roomCondition = RoomCondition.values()[roomConditionOrdinal];
                    Hotel hotel = getHotelById(hotelId);

                    if (hotel != null) {
                        Room room = new Room(currentRoomNumber.trim(), roomType, roomCondition, hotel);
                        return createJsonResponse(true, "Habitación encontrada", room);
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

    public String readAll() throws IOException {
        try {
            List<Room> rooms = new ArrayList<>();
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();

                String roomNumber = readString(buffer, ROOM_NUMBER_SIZE);
                int roomTypeOrdinal = buffer.getInt();
                int roomConditionOrdinal = buffer.getInt();
                long hotelId = buffer.getLong();

                try {
                    // Convertir ordinales a enums con validación
                    RoomType roomType = RoomType.values()[roomTypeOrdinal];
                    RoomCondition roomCondition = RoomCondition.values()[roomConditionOrdinal];
                    Hotel hotel = getHotelById(hotelId);

                    if (hotel != null) {
                        rooms.add(new Room(roomNumber.trim(), roomType, roomCondition, hotel));
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // Ignorar registros con ordinales inválidos
                    continue;
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
                    buffer.putInt(room.getRoomType().ordinal());
                    buffer.putInt(room.getRoomCondition().ordinal());
                    buffer.putLong(room.getHotel().getHotelId());

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


    public String readByRoomType(RoomType roomType) throws IOException {
        try {
            List<Room> rooms = new ArrayList<>();
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();

                String roomNumber = readString(buffer, ROOM_NUMBER_SIZE);
                int roomTypeOrdinal = buffer.getInt();
                int roomConditionOrdinal = buffer.getInt();
                long hotelId = buffer.getLong();

                if (roomTypeOrdinal == roomType.ordinal()) {
                    try {
                        RoomCondition roomCondition = RoomCondition.values()[roomConditionOrdinal];
                        Hotel hotel = getHotelById(hotelId);

                        if (hotel != null) {
                            rooms.add(new Room(roomNumber.trim(), roomType, roomCondition, hotel));
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        continue;
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

                String roomNumber = readString(buffer, ROOM_NUMBER_SIZE);
                int roomTypeOrdinal = buffer.getInt();
                int roomConditionOrdinal = buffer.getInt();
                long hotelId = buffer.getLong();

                if (roomConditionOrdinal == roomCondition.ordinal()) {
                    try {
                        RoomType roomType = RoomType.values()[roomTypeOrdinal];
                        Hotel hotel = getHotelById(hotelId);

                        if (hotel != null) {
                            rooms.add(new Room(roomNumber.trim(), roomType, roomCondition, hotel));
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

    // Método auxiliar para obtener Hotel por ID
    private Hotel getHotelById(long id) {
        try {
            String response = hotelData.read((int) id);
            // Necesitarás implementar la deserialización según tu JsonDataResponse
            // Por ahora retorna null, pero debes implementar el parsing del JSON
            return null; // Implementar según tu lógica de deserialización
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