package com.MagicalStay.data;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class HotelData {
    private RandomAccessFile raf;
    private final int HOTEL_ID_SIZE = 4;
    private final int NAME_SIZE = 50;
    private final int ADDRESS_SIZE = 100;
    private final int TAMANO_REGISTRO = HOTEL_ID_SIZE + NAME_SIZE + ADDRESS_SIZE;

    public HotelData(String filePath) throws IOException {
        raf = new RandomAccessFile(filePath, "rw");
    }

    public void create(int hotelId, String name, String address) throws IOException {
        raf.seek(raf.length());
        raf.writeInt(hotelId);
        writeString(name, NAME_SIZE);
        writeString(address, ADDRESS_SIZE);
    }

    public List<String> readAll() throws IOException {
        List<String> hotels = new ArrayList<>();
        long totalRegistros = raf.length() / TAMANO_REGISTRO;
        for (int i = 0; i < totalRegistros; i++) {
            raf.seek(i * TAMANO_REGISTRO);
            int hotelId = raf.readInt();
            String name = readString(NAME_SIZE, raf.getFilePointer());
            String address = readString(ADDRESS_SIZE, raf.getFilePointer());
            hotels.add(hotelId + ": " + name + " - " + address);
        }
        return hotels;
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