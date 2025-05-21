package com.MagicalStay.data;

import com.MagicalStay.domain.Room;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class RoomData {
    private RandomAccessFile raf;
    private final int ROOM_NUMBER_SIZE = 4;
    private final int ROOM_TYPE_SIZE = 20; // Max nombre del RoomType
    private final int ROOM_CONDITION_SIZE = 20; // Max nombre del RoomCondition
    private final int HOTEL_ID_SIZE = 4;
    private final int TAMANO_REGISTRO = ROOM_NUMBER_SIZE + ROOM_TYPE_SIZE + ROOM_CONDITION_SIZE + HOTEL_ID_SIZE;

    public RoomData(String filePath) throws IOException {
        raf = new RandomAccessFile(filePath, "rw");
    }

    public void create(Room room) throws IOException {
        raf.seek(raf.length()); // Escribimos al final del archivo
        raf.writeInt(room.getRoomNumber());
        writeString(room.getRoomType().toString(), ROOM_TYPE_SIZE);
        writeString(room.getRoomCondition().toString(), ROOM_CONDITION_SIZE);
        raf.writeInt(room.getHotelId());
    }

    public List<Room> readAll() throws IOException {
        List<Room> rooms = new ArrayList<>();
        long totalRegistros = raf.length() / TAMANO_REGISTRO;
        for (int i = 0; i < totalRegistros; i++) {
            raf.seek(i * TAMANO_REGISTRO);
            int roomNumber = raf.readInt();
            String roomType = readString(ROOM_TYPE_SIZE, raf.getFilePointer());
            String roomCondition = readString(ROOM_CONDITION_SIZE, raf.getFilePointer());
            int hotelId = raf.readInt();
            rooms.add(new Room(roomNumber, roomType, roomCondition, hotelId));
        }
        return rooms;
    }

    public void update(Room room) throws IOException {
        long totalRegistros = raf.length() / TAMANO_REGISTRO;
        for (int i = 0; i < totalRegistros; i++) {
            raf.seek(i * TAMANO_REGISTRO);
            if (raf.readInt() == room.getRoomNumber()) { // Si encontramos el número de habitación
                writeString(room.getRoomType().toString(), ROOM_TYPE_SIZE);
                writeString(room.getRoomCondition().toString(), ROOM_CONDITION_SIZE);
                raf.writeInt(room.getHotelId());
                return;
            }
        }
    }

    public boolean delete(int roomNumber) throws IOException {
        long totalRegistros = raf.length() / TAMANO_REGISTRO;
        for (int i = 0; i < totalRegistros; i++) {
            raf.seek(i * TAMANO_REGISTRO);
            if (raf.readInt() == roomNumber) { // Si encontramos el número de habitación
                shiftRecords(i, totalRegistros);
                raf.setLength(raf.length() - TAMANO_REGISTRO); // Reduzco el tamaño del archivo
                return true;
            }
        }
        return false; // No encontrado
    }

    private void shiftRecords(int deletedIndex, long totalRegistros) throws IOException {
        for (int i = deletedIndex; i < totalRegistros - 1; i++) {
            raf.seek((i + 1) * TAMANO_REGISTRO);
            byte[] nextRecord = new byte[TAMANO_REGISTRO];
            raf.readFully(nextRecord);
            raf.seek(i * TAMANO_REGISTRO);
            raf.write(nextRecord);
        }
    }

    private String readString(int tamano, long posicion) throws IOException {
        raf.seek(posicion);
        byte[] datos = new byte[tamano];
        raf.readFully(datos);
        return new String(datos).trim();
    }

    private void writeString(String data, int tamano) throws IOException {
        byte[] bytes = new byte[tamano];
        byte[] dataBytes = data.getBytes();
        for (int i = 0; i < tamano; i++) {
            if (i < dataBytes.length) {
                bytes[i] = dataBytes[i];
            } else {
                bytes[i] = ' ';
            }
        }
        raf.write(bytes);
    }
}