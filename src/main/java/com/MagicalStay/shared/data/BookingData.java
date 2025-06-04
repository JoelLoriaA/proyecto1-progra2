package com.MagicalStay.shared.data;

import com.MagicalStay.shared.data.JsonDataResponse;
import com.MagicalStay.shared.domain.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingData extends JsonDataResponse {
            // Constantes para tamaños de campos básicos
            private static final int BOOKING_ID_LENGTH = 3;      // 3 dígitos + espacio
            private static final int BOOKING_ID_SIZE = (BOOKING_ID_LENGTH + 1) * 2;
            private static final int DATE_SIZE = 8;             // LocalDate (epoch days)

            // Constantes para Guest
            private static final int GUEST_LENGTH = 30;         // Nombre + ID
            private static final int GUEST_SIZE = GUEST_LENGTH * 2;

            // Constantes para FrontDeskClerk
            private static final int CLERK_LENGTH = 30;         // Nombre + ID
            private static final int CLERK_SIZE = CLERK_LENGTH * 2;

            // Constantes para Hotel
            private static final int HOTEL_LENGTH = 30;         // Nombre + ID
            private static final int HOTEL_SIZE = HOTEL_LENGTH * 2;

            // Constantes para Room
            private static final int ROOM_NUMBER_LENGTH = 8;
            private static final int ROOM_SIZE = ROOM_NUMBER_LENGTH * 2;
            private static final int MAX_ROOMS = 10;
            private static final int ROOMS_LIST_SIZE = MAX_ROOMS * ROOM_SIZE;

            // Tamaño total del registro
            private static final int RECORD_SIZE =
                BOOKING_ID_SIZE +      // ID de reserva (8)
                (DATE_SIZE * 2) +      // Fechas inicio y fin (16)
                GUEST_SIZE +           // Info del huésped (60)
                CLERK_SIZE +           // Info del recepcionista (60)
                HOTEL_SIZE +           // Info del hotel (60)
                ROOMS_LIST_SIZE;       // Lista de habitaciones (160)
                                      // Total: 364 bytes

            private final RandomAccessFile raf;

            public BookingData(String filename) throws IOException {
                this.raf = new RandomAccessFile(filename, "rw");
            }

            public String create(Booking booking) throws IOException {
                try {
                    String bookingIdStr = String.format("%03d", booking.getBookingId());
                    if (bookingIdStr.length() > BOOKING_ID_LENGTH) {
                        return createJsonResponse(false, "ID de reserva no puede exceder " + BOOKING_ID_LENGTH + " dígitos", null);
                    }

                    raf.seek(raf.length());
                    writeBooking(booking);
                    return createJsonResponse(true, "Reserva creada exitosamente", booking);
                } catch (Exception e) {
                    return createJsonResponse(false, "Error al crear reserva: " + e.getMessage(), null);
                }
            }

            private void writeBooking(Booking booking) throws IOException {
                // ID de reserva (formato ###_)
                String bookingIdStr = String.format("%03d", booking.getBookingId());
                writeString(bookingIdStr, BOOKING_ID_LENGTH);
                writeString(" ", 1);

                // Fechas como epoch days
                raf.writeLong(booking.getStartDate().toEpochDay());
                raf.writeLong(booking.getLeavingDate().toEpochDay());

                // Guest info (nombre;id)
                String guestInfo = "";
                if (booking.getGuest() != null) {
                    guestInfo = booking.getGuest().getName() + ";" + booking.getGuest().getId();
                }
                writeString(padRight(guestInfo, GUEST_LENGTH), GUEST_LENGTH);

                // FrontDeskClerk info (nombre;id)
                String clerkInfo = "";
                if (booking.getFrontDeskClerk() != null) {
                    clerkInfo = booking.getFrontDeskClerk().getName() + ";" +
                               booking.getFrontDeskClerk().getEmployeeId();
                }
                writeString(padRight(clerkInfo, CLERK_LENGTH), CLERK_LENGTH);

                // Hotel info (nombre;id)
                String hotelInfo = "";
                if (booking.getHotel() != null) {
                    hotelInfo = booking.getHotel().getName() + ";" +
                               booking.getHotel().getHotelId();
                }
                writeString(padRight(hotelInfo, HOTEL_LENGTH), HOTEL_LENGTH);

                // Lista de habitaciones
                List<Room> rooms = booking.getReservedRooms() != null ?
                    booking.getReservedRooms() : new ArrayList<>();
                for (int i = 0; i < MAX_ROOMS; i++) {
                    String roomNumber = "";
                    if (i < rooms.size() && rooms.get(i) != null) {
                        roomNumber = rooms.get(i).getRoomNumber();
                    }
                    writeString(padRight(roomNumber, ROOM_NUMBER_LENGTH), ROOM_NUMBER_LENGTH);
                }
            }

            private Booking readBooking() throws IOException {
                // Leer ID
                String bookingIdStr = readString(BOOKING_ID_LENGTH);
                readString(1); // Saltar espacio
                int bookingId = Integer.parseInt(bookingIdStr.trim());

                // Leer fechas
                LocalDate startDate = LocalDate.ofEpochDay(raf.readLong());
                LocalDate leavingDate = LocalDate.ofEpochDay(raf.readLong());

                // Leer guest
                String guestInfo = readString(GUEST_LENGTH).trim();
                Guest guest = null;
                if (!guestInfo.isEmpty()) {
                    String[] guestParts = guestInfo.split(";");
                    if (guestParts.length == 2) {
                        guest = new Guest(guestParts[1], guestParts[0]);
                    }
                }

                // Leer clerk
                String clerkInfo = readString(CLERK_LENGTH).trim();
                FrontDeskClerk clerk = null;
                if (!clerkInfo.isEmpty()) {
                    String[] clerkParts = clerkInfo.split(";");
                    if (clerkParts.length == 2) {
                        clerk = new FrontDeskClerk(
                                clerkParts[0],     // nombre
                                "",                // apellidos (vacío ya que no lo tenemos)
                                clerkParts[1],     // ID empleado
                                0,                 // dni (0 como valor por defecto)
                                "",               // username (vacío)
                                ""                // password (vacío)
                        );
                    }
                }

                // Leer hotel
                String hotelInfo = readString(HOTEL_LENGTH).trim();
                Hotel hotel = null;
                if (!hotelInfo.isEmpty()) {
                    String[] hotelParts = hotelInfo.split(";");
                    if (hotelParts.length == 2) {
                        hotel = new Hotel(Integer.parseInt(hotelParts[1]), hotelParts[0], "");
                    }
                }

                // Leer rooms
                List<Room> rooms = new ArrayList<>();
                for (int i = 0; i < MAX_ROOMS; i++) {
                    String roomNumber = readString(ROOM_NUMBER_LENGTH).trim();
                    if (!roomNumber.isEmpty()) {
                        rooms.add(new Room(roomNumber, null, null, null));
                    }
                }

                Booking booking = new Booking(bookingId, startDate, leavingDate, rooms);
                booking.setGuest(guest);
                booking.setFrontDeskClerk(clerk);
                booking.setHotel(hotel);
                return booking;
            }

            // Los métodos retrieveById, retrieveAll, update, delete permanecen igual
            // Agregamos los nuevos métodos de búsqueda:

            public String retrieveByGuest(String guestName) throws IOException {
                try {
                    List<Booking> matchingBookings = new ArrayList<>();
                    guestName = guestName.toLowerCase();

                    for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                        raf.seek(pos);
                        Booking booking = readBooking();
                        if (booking.getGuest() != null &&
                            booking.getGuest().getName().toLowerCase().contains(guestName)) {
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

            public String retrieveByHotel(String hotelName) throws IOException {
                try {
                    List<Booking> matchingBookings = new ArrayList<>();
                    hotelName = hotelName.toLowerCase();

                    for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                        raf.seek(pos);
                        Booking booking = readBooking();
                        if (booking.getHotel() != null &&
                            booking.getHotel().getName().toLowerCase().contains(hotelName)) {
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

            private String padRight(String str, int length) {
                if (str == null) {
                    str = "";
                }
                str = str.trim();
                return String.format("%-" + length + "s",
                        str.length() > length ? str.substring(0, length) : str);
            }

        public String retrieveById(int bookingId) throws IOException {
            try {
                for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                    raf.seek(pos);
                    String idStr = readString(BOOKING_ID_LENGTH);
                    int currentId = Integer.parseInt(idStr.trim());
                    if (currentId == bookingId) {
                        raf.seek(pos);
                        Booking booking = readBooking();
                        return createJsonResponse(true, "Reserva encontrada", booking);
                    }
                }
                return createJsonResponse(false, "Reserva no encontrada", null);
            } catch (Exception e) {
                return createJsonResponse(false, "Error al recuperar reserva: " + e.getMessage(), null);
            }
        }

        public String retrieveAll() throws IOException {
            try {
                List<Booking> bookings = new ArrayList<>();
                for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                    raf.seek(pos);
                    Booking booking = readBooking();
                    bookings.add(booking);
                }
                return createJsonResponse(true, "Todas las reservas recuperadas", bookings);
            } catch (Exception e) {
                return createJsonResponse(false, "Error al recuperar reservas: " + e.getMessage(), null);
            }
        }

        public String update(Booking booking) throws IOException {
            try {
                for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                    raf.seek(pos);
                    String idStr = readString(BOOKING_ID_LENGTH);
                    int currentId = Integer.parseInt(idStr.trim());

                    if (currentId == booking.getBookingId()) {
                        raf.seek(pos);
                        writeBooking(booking);
                        return createJsonResponse(true, "Reserva actualizada exitosamente", booking);
                    }
                }
                return createJsonResponse(false, "Reserva no encontrada", null);
            } catch (Exception e) {
                return createJsonResponse(false, "Error al actualizar reserva: " + e.getMessage(), null);
            }
        }

        public String delete(int bookingId) throws IOException {
            try {
                List<Booking> bookings = new ArrayList<>();
                boolean found = false;

                for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                    raf.seek(pos);
                    String idStr = readString(BOOKING_ID_LENGTH);
                    int currentId = Integer.parseInt(idStr.trim());

                    if (currentId == bookingId) {
                        found = true; // Marca que se encontró la reserva
                    } else {
                        raf.seek(pos); // Reposiciona para leer el booking completo
                        bookings.add(readBooking());
                    }
                }

                if (found) {
                    raf.setLength(0); // Limpia el archivo
                    for (Booking b : bookings) {
                        writeBooking(b); // Reescribe las reservas restantes
                    }
                    return createJsonResponse(true, "Reserva eliminada exitosamente", null);
                } else {
                    return createJsonResponse(false, "Reserva no encontrada", null);
                }
            } catch (Exception e) {
                return createJsonResponse(false, "Error al eliminar reserva: " + e.getMessage(), null);
            }
        }

        public void close() throws IOException {
            if (raf != null) {
                raf.close();
            }
        }

        public int getNextBookingId() throws IOException {
            int maxId = 0;
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                String idStr = readString(BOOKING_ID_LENGTH);
                try {
                    int currentId = Integer.parseInt(idStr.trim());
                    if (currentId > maxId) {
                        maxId = currentId;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
            return maxId + 1;
        }


}