package com.MagicalStay.data;

import com.MagicalStay.domain.Room;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingData {
    private RandomAccessFile raf;
    private final int DATE_SIZE = 8;
    private final int RESERVED_ROOMS_SIZE = 78;
    private final int TAMANO_REGISTRO = DATE_SIZE + DATE_SIZE + RESERVED_ROOMS_SIZE;

    public BookingData(String filePath) throws IOException {
        raf = new RandomAccessFile(filePath, "rw");
    }

    public void create(LocalDate startDate, LocalDate endDate, List<Room> reservedRooms) throws IOException {
        raf.seek(raf.length()); // Escribimos al final del archivo
        raf.writeBytes(startDate.toString());
        raf.writeBytes(endDate.toString());
        StringBuilder roomsString = new StringBuilder();
        for (Room room : reservedRooms) {
            roomsString.append(room.getRoomNumber()).append(",");
        }
        writeString(roomsString.toString(), RESERVED_ROOMS_SIZE);
    }

    public List<String> readAll() throws IOException {
        List<String> bookings = new ArrayList<>();
        long totalRegistros = raf.length() / TAMANO_REGISTRO;
        for (int i = 0; i < totalRegistros; i++) {
            raf.seek(i * TAMANO_REGISTRO);
            String startDate = readString(DATE_SIZE, raf.getFilePointer());
            String endDate = readString(DATE_SIZE, raf.getFilePointer());
            String reservedRooms = readString(RESERVED_ROOMS_SIZE, raf.getFilePointer());
            bookings.add(startDate + " - " + endDate + ": " + reservedRooms);
        }
        return bookings;
    }

    private String readString(int size, long position) throws IOException {
        raf.seek(position);
        byte[] datos = new byte[size];
        raf.readFully(datos);
        return new String(datos).trim();
    }

    private void writeString(String data, int size) throws IOException {
        byte[] bytes = new byte[size];
        byte[] dataBytes = data.getBytes();
        for (int i = 0; i < size; i++) {
            if (i < dataBytes.length) {
                bytes[i] = dataBytes[i];
            } else {
                bytes[i] = ' ';
            }
        }
        raf.write(bytes);
    }
}