package com.MagicalStay.shared.data;

import com.MagicalStay.shared.domain.Booking;
import com.MagicalStay.shared.domain.Room;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingData extends JsonDataResponse {
    private RandomAccessFile raf;
    private static final int BOOKING_ID_SIZE = 4;
    private static final int DATE_SIZE = 8;
    private static final int GUEST_ID_SIZE = 20 * 2;
    private static final int CLERK_ID_SIZE = 20 * 2;
    private static final int HOTEL_ID_SIZE = 4;
    private static final int MAX_ROOMS = 10;
    private static final int ROOM_NUMBER_SIZE = 12 * 2;
    private static final int ROOMS_LIST_SIZE = MAX_ROOMS * ROOM_NUMBER_SIZE;
    private static final int RECORD_SIZE = BOOKING_ID_SIZE + (DATE_SIZE * 2) +
            GUEST_ID_SIZE + CLERK_ID_SIZE + HOTEL_ID_SIZE + ROOMS_LIST_SIZE;

    public BookingData(String filename) throws IOException {
        this.raf = new RandomAccessFile(filename, "rw");
    }

    public String create(Booking booking) throws IOException {
        try {
            raf.seek(raf.length());
            writeBooking(booking);
            return createJsonResponse(true, "Reserva creada exitosamente", booking);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al crear la reserva: " + e.getMessage(), null);
        }
    }

    public String retrieveById(int bookingId) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                if (raf.readInt() == bookingId) {
                    raf.seek(pos);
                    Booking booking = readBooking();
                    return createJsonResponse(true, "Reserva encontrada", booking);
                }
            }
            return createJsonResponse(false, "Reserva no encontrada", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al leer la reserva: " + e.getMessage(), null);
        }
    }

    public String retrieveAll() throws IOException {
        try {
            List<Booking> bookings = new ArrayList<>();
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                bookings.add(readBooking());
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
                    writeBooking(booking);
                    return createJsonResponse(true, "Reserva actualizada exitosamente", booking);
                }
            }
            return createJsonResponse(false, "Reserva no encontrada", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al actualizar la reserva: " + e.getMessage(), null);
        }
    }

    public String delete(int bookingId) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                if (raf.readInt() == bookingId) {
                    moveRemainingRecords(pos);
                    raf.setLength(raf.length() - RECORD_SIZE);
                    return createJsonResponse(true, "Reserva eliminada exitosamente", null);
                }
            }
            return createJsonResponse(false, "Reserva no encontrada", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al eliminar la reserva: " + e.getMessage(), null);
        }
    }

    private void writeBooking(Booking booking) throws IOException {
        raf.writeInt(booking.getBookingId());
        raf.writeLong(booking.getStartDate().toEpochDay());
        raf.writeLong(booking.getLeavingDate().toEpochDay());

        writeString(booking.getGuest() != null ? String.valueOf(booking.getGuest().getId()) : "", 20);
        writeString(booking.getFrontDeskClerk() != null ? booking.getFrontDeskClerk().getEmployeeId() : "", 20);
        raf.writeInt(booking.getHotel() != null ? booking.getHotel().getHotelId() : 0);

        List<Room> rooms = booking.getReservedRooms();
        for (int i = 0; i < MAX_ROOMS; i++) {
            String roomNumber = i < rooms.size() && rooms.get(i) != null ? rooms.get(i).getRoomNumber() : "";
            writeString(roomNumber, 12);
        }
    }

    private Booking readBooking() throws IOException {
        int bookingId = raf.readInt();
        LocalDate startDate = LocalDate.ofEpochDay(raf.readLong());
        LocalDate leavingDate = LocalDate.ofEpochDay(raf.readLong());

        String guestId = readString(20);
        String clerkId = readString(20);
        int hotelId = raf.readInt();

        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < MAX_ROOMS; i++) {
            String roomNumber = readString(12);
            if (!roomNumber.trim().isEmpty()) {
                rooms.add(new Room(roomNumber, null, null, null));
            }
        }

        return new Booking(bookingId, startDate, leavingDate, rooms);
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

    public String retrieveByGuest(String searchGuest) throws IOException {
        try {
            List<Booking> matchingBookings = new ArrayList<>();
            searchGuest = searchGuest.toLowerCase();

            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                Booking booking = readBooking();
                if (booking.getGuest() != null &&
                    booking.getGuest().getName().toLowerCase().contains(searchGuest)) {
                    matchingBookings.add(booking);
                }
            }

            if (!matchingBookings.isEmpty()) {
                return createJsonResponse(true, "Reservas encontradas", matchingBookings);
            }
            return createJsonResponse(false, "No se encontraron reservas para ese huésped", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al buscar reservas: " + e.getMessage(), null);
        }
    }

    public String retrieveByHotel(String searchHotel) throws IOException {
        try {
            List<Booking> matchingBookings = new ArrayList<>();
            searchHotel = searchHotel.toLowerCase();

            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                Booking booking = readBooking();
                if (booking.getHotel() != null &&
                    booking.getHotel().getName().toLowerCase().contains(searchHotel)) {
                    matchingBookings.add(booking);
                }
            }

            if (!matchingBookings.isEmpty()) {
                return createJsonResponse(true, "Reservas encontradas", matchingBookings);
            }
            return createJsonResponse(false, "No se encontraron reservas para ese hotel", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al buscar reservas: " + e.getMessage(), null);
        }
    }
}