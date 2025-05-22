package com.MagicalStay.data;

import com.MagicalStay.domain.Booking;
import com.MagicalStay.domain.Room;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingData extends JsonDataResponse {
    private RandomAccessFile raf;
    private static final int BOOKING_ID_SIZE = 4;
    private static final int DATE_SIZE = 8;
    private static final int MAX_ROOMS = 10;
    private static final int ROOMS_LIST_SIZE = MAX_ROOMS * (12 * 2);
    private static final int RECORD_SIZE = BOOKING_ID_SIZE + (DATE_SIZE * 2) + ROOMS_LIST_SIZE;

    public BookingData(String filename) throws IOException {
        this.raf = new RandomAccessFile(filename, "rw");
    }

    public String create(Booking booking) throws IOException {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
            buffer.putInt(booking.getBookingId());
            buffer.putLong(booking.getStartDate().toEpochDay());
            buffer.putLong(booking.getLeavingDate().toEpochDay());

            List<Room> rooms = booking.getReservedRooms();
            for (int i = 0; i < MAX_ROOMS; i++) {
                String roomNumber = i < rooms.size() ? rooms.get(i).getRoomNumber() : "";
                for (int j = 0; j < 10; j++) {
                    buffer.putChar(j < roomNumber.length() ? roomNumber.charAt(j) : '\0');
                }
            }

            raf.seek(raf.length());
            raf.write(buffer.array());
            
            return createJsonResponse(true, "Reserva creada exitosamente", booking);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al crear la reserva: " + e.getMessage(), null);
        }
    }

    public String read(int bookingId) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();

                int currentId = buffer.getInt();
                if (currentId == bookingId) {
                    LocalDate startDate = LocalDate.ofEpochDay(buffer.getLong());
                    LocalDate leavingDate = LocalDate.ofEpochDay(buffer.getLong());

                    List<Room> rooms = new ArrayList<>();
                    for (int i = 0; i < MAX_ROOMS; i++) {
                        StringBuilder roomNumber = new StringBuilder();
                        for (int j = 0; j < 10; j++) {
                            char c = buffer.getChar();
                            if (c != '\0') roomNumber.append(c);
                        }
                        if (!roomNumber.toString().isEmpty()) {
                            rooms.add(new Room(roomNumber.toString(), null, null, null));
                        }
                    }

                    Booking booking = new Booking(bookingId, startDate, leavingDate, rooms);
                    return createJsonResponse(true, "Reserva encontrada", booking);
                }
            }
            return createJsonResponse(false, "Reserva no encontrada", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al leer la reserva: " + e.getMessage(), null);
        }
    }

    public String readAll() throws IOException {
        try {
            List<Booking> bookings = new ArrayList<>();
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();

                int bookingId = buffer.getInt();
                LocalDate startDate = LocalDate.ofEpochDay(buffer.getLong());
                LocalDate leavingDate = LocalDate.ofEpochDay(buffer.getLong());

                List<Room> rooms = new ArrayList<>();
                for (int i = 0; i < MAX_ROOMS; i++) {
                    StringBuilder roomNumber = new StringBuilder();
                    for (int j = 0; j < 10; j++) {
                        char c = buffer.getChar();
                        if (c != '\0') roomNumber.append(c);
                    }
                    if (!roomNumber.toString().isEmpty()) {
                        rooms.add(new Room(roomNumber.toString(), null, null, null));
                    }
                }

                bookings.add(new Booking(bookingId, startDate, leavingDate, rooms));
            }
            return createJsonResponse(true, "Reservas recuperadas exitosamente", bookings);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al recuperar las reservas: " + e.getMessage(), null);
        }
    }

    public String update(Booking booking) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                if (raf.readInt() == booking.getBookingId()) {
                    raf.seek(pos);
                    ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);

                    buffer.putInt(booking.getBookingId());
                    buffer.putLong(booking.getStartDate().toEpochDay());
                    buffer.putLong(booking.getLeavingDate().toEpochDay());

                    List<Room> rooms = booking.getReservedRooms();
                    for (int i = 0; i < MAX_ROOMS; i++) {
                        String roomNumber = i < rooms.size() ? rooms.get(i).getRoomNumber() : "";
                        for (int j = 0; j < 10; j++) {
                            buffer.putChar(j < roomNumber.length() ? roomNumber.charAt(j) : '\0');
                        }
                    }

                    raf.write(buffer.array());
                    return createJsonResponse(true, "Reserva actualizada exitosamente", booking);
                }
            }
            return createJsonResponse(false, "No se encontró la reserva para actualizar", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al actualizar la reserva: " + e.getMessage(), null);
        }
    }

    public String delete(int bookingId) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                if (raf.readInt() == bookingId) {
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
                    return createJsonResponse(true, "Reserva eliminada exitosamente", null);
                }
            }
            return createJsonResponse(false, "No se encontró la reserva para eliminar", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al eliminar la reserva: " + e.getMessage(), null);
        }
    }

    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }
}