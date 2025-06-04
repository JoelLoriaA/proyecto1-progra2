package com.MagicalStay.shared.data;

import com.MagicalStay.shared.domain.Guest;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class GuestData extends JsonDataResponse {
    private RandomAccessFile raf;
    private static final int DNI_SIZE = 4;
    private static final int NAME_LENGTH = 30;
    private static final int LAST_NAME_LENGTH = 30;
    private static final int EMAIL_LENGTH = 50;
    private static final int ADDRESS_LENGTH = 100;
    private static final int NATIONALITY_LENGTH = 30;
    private static final int PHONE_SIZE = 4;

    private static final int NAME_SIZE = NAME_LENGTH * 2;
    private static final int LAST_NAME_SIZE = LAST_NAME_LENGTH * 2;
    private static final int EMAIL_SIZE = EMAIL_LENGTH * 2;
    private static final int ADDRESS_SIZE = ADDRESS_LENGTH * 2;
    private static final int NATIONALITY_SIZE = NATIONALITY_LENGTH * 2;

    private static final int RECORD_SIZE = DNI_SIZE + NAME_SIZE + LAST_NAME_SIZE + EMAIL_SIZE +
            ADDRESS_SIZE + NATIONALITY_SIZE + PHONE_SIZE;

    public GuestData(String filename) throws IOException {
        this.raf = new RandomAccessFile(filename, "rw");
    }

    public GuestData() {
    }


    public String create(Guest guest) throws IOException {
        try {
            if (guest.getName().length() > NAME_LENGTH ||
                    guest.getLastName().length() > LAST_NAME_LENGTH ||
                    guest.getEmail().length() > EMAIL_LENGTH ||
                    guest.getAddress().length() > ADDRESS_LENGTH ||
                    guest.getNationality().length() > NATIONALITY_LENGTH) {
                return createJsonResponse(false, "One or more fields exceed maximum length", null);
            }

            // Verificar si el DNI ya existe
            if (findById(guest.getId()) != null) {
                return createJsonResponse(false, "Guest with this ID already exists", null);
            }

            raf.seek(raf.length());
            writeGuest(guest);
            return createJsonResponse(true, "Guest created successfully", guest);
        } catch (Exception e) {
            return createJsonResponse(false, "Error creating guest: " + e.getMessage(), null);
        }
    }

    public String retrieveById(int dni) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                if (raf.readInt() == dni) {
                    raf.seek(pos);
                    Guest guest = readGuest();
                    return createJsonResponse(true, "Guest encontrado", guest);
                }
            }
            return createJsonResponse(false, "Guest no encontrado", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al recuperar guest: " + e.getMessage(), null);
        }
    }

    public String retrieveAll() throws IOException {
        try {
            List<Guest> guests = new ArrayList<>();
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                guests.add(readGuest());
            }
            return createJsonResponse(true, "Guests recuperados exitosamente", guests);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al recuperar guests: " + e.getMessage(), null);
        }
    }

    public String update(Guest guest) throws IOException {
        try {
            if (guest.getName().length() > NAME_LENGTH ||
                    guest.getLastName().length() > LAST_NAME_LENGTH ||
                    guest.getEmail().length() > EMAIL_LENGTH ||
                    guest.getAddress().length() > ADDRESS_LENGTH ||
                    guest.getNationality().length() > NATIONALITY_LENGTH) {
                return createJsonResponse(false, "One or more fields exceed maximum length", null);
            }

            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                if (raf.readInt() == guest.getId()) {
                    raf.seek(pos);
                    writeGuest(guest);
                    return createJsonResponse(true, "Guest updated successfully", guest);
                }
            }
            return createJsonResponse(false, "Guest not found", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error updating guest: " + e.getMessage(), null);
        }
    }

    public String delete(int dni) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                if (raf.readInt() == dni) {
                    moveRemainingRecords(pos);
                    raf.setLength(raf.length() - RECORD_SIZE);
                    return createJsonResponse(true, "Guest deleted successfully", null);
                }
            }
            return createJsonResponse(false, "Guest not found", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error deleting guest: " + e.getMessage(), null);
        }
    }

    public String retrieveByName(String searchName) throws IOException {
        try {
            List<Guest> matchingGuests = new ArrayList<>();
            searchName = searchName.toLowerCase();

            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                Guest guest = readGuest();
                if (guest.getName().toLowerCase().contains(searchName) ||
                        guest.getLastName().toLowerCase().contains(searchName)) {
                    matchingGuests.add(guest);
                }
            }

            if (!matchingGuests.isEmpty()) {
                return createJsonResponse(true, "Guests encontrados", matchingGuests);
            }
            return createJsonResponse(false, "No se encontraron guests con ese nombre", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al buscar guests: " + e.getMessage(), null);
        }
    }

    public String retrieveByEmail(String searchEmail) throws IOException {
        try {
            List<Guest> matchingGuests = new ArrayList<>();
            searchEmail = searchEmail.toLowerCase();

            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                Guest guest = readGuest();
                if (guest.getEmail().toLowerCase().contains(searchEmail)) {
                    matchingGuests.add(guest);
                }
            }

            if (!matchingGuests.isEmpty()) {
                return createJsonResponse(true, "Guests encontrados", matchingGuests);
            }
            return createJsonResponse(false, "No se encontraron guests con ese email", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al buscar guests: " + e.getMessage(), null);
        }
    }

    private void writeGuest(Guest guest) throws IOException {
        raf.writeInt(guest.getId());
        writeString(guest.getName(), NAME_LENGTH);
        writeString(guest.getLastName(), LAST_NAME_LENGTH);
        writeString(guest.getEmail(), EMAIL_LENGTH);
        writeString(guest.getAddress(), ADDRESS_LENGTH);
        writeString(guest.getNationality(), NATIONALITY_LENGTH);
        raf.writeInt(guest.getPhoneNumber());
    }

    private Guest readGuest() throws IOException {
        int dni = raf.readInt();
        String name = readString(NAME_LENGTH);
        String lastName = readString(LAST_NAME_LENGTH);
        String email = readString(EMAIL_LENGTH);
        String address = readString(ADDRESS_LENGTH);
        String nationality = readString(NATIONALITY_LENGTH);
        int phoneNumber = raf.readInt();

        return new Guest(name, lastName, dni, phoneNumber, email, address, nationality);
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

    public Guest findById(int id) throws IOException {
        for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
            raf.seek(pos);
            int guestId = raf.readInt();
            if (guestId == id) {
                raf.seek(pos);
                return readGuest();
            }
        }
        return  null;
    }


}