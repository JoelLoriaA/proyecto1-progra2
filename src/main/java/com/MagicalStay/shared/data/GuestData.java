package com.MagicalStay.shared.data;

import com.MagicalStay.shared.domain.Guest;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GuestData extends JsonDataResponse {
    private RandomAccessFile raf;
    private static final int NAME_SIZE = 30;
    private static final int LAST_NAME_SIZE = 60;
    private static final int DNI_SIZE = 4;
    private static final int PHONE_SIZE = 4;
    private static final int EMAIL_SIZE = 100;
    private static final int ADDRESS_SIZE = 200;
    private static final int NATIONALITY_SIZE = 60;
    private static final int RECORD_SIZE = NAME_SIZE + LAST_NAME_SIZE + DNI_SIZE + PHONE_SIZE +
            EMAIL_SIZE + ADDRESS_SIZE + NATIONALITY_SIZE;

    public GuestData(String filename) throws IOException {
        this.raf = new RandomAccessFile(filename, "rw");
    }

    public GuestData() {
    }

    public String create(Guest guest) throws IOException {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);

            writeString(buffer, guest.getName(), NAME_SIZE);
            writeString(buffer, guest.getLastName(), LAST_NAME_SIZE);
            buffer.putInt(guest.getId());
            buffer.putInt(guest.getPhoneNumber());
            writeString(buffer, guest.getEmail(), EMAIL_SIZE);
            writeString(buffer, guest.getAddress(), ADDRESS_SIZE);
            writeString(buffer, guest.getNationality(), NATIONALITY_SIZE);

            raf.seek(raf.length());
            raf.write(buffer.array());

            return createJsonResponse(true, "Huésped registrado exitosamente", guest);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al registrar el huésped: " + e.getMessage(), null);
        }
    }

    public String read(int dni) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();

                String name = readString(buffer, NAME_SIZE);
                String lastName = readString(buffer, LAST_NAME_SIZE);
                int readDni = buffer.getInt();

                if (readDni == dni) {
                    int phoneNumber = buffer.getInt();
                    String email = readString(buffer, EMAIL_SIZE);
                    String address = readString(buffer, ADDRESS_SIZE);
                    String nationality = readString(buffer, NATIONALITY_SIZE);

                    Guest guest = new Guest(name.trim(), lastName.trim(), readDni, phoneNumber,
                            email.trim(), address.trim(), nationality.trim());
                    return createJsonResponse(true, "Huésped encontrado", guest);
                }
            }
            return createJsonResponse(false, "Huésped no encontrado", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al buscar el huésped: " + e.getMessage(), null);
        }
    }

    public String readAll() throws IOException {
        try {
            List<Guest> guests = new ArrayList<>();
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();

                String name = readString(buffer, NAME_SIZE);
                String lastName = readString(buffer, LAST_NAME_SIZE);
                int dni = buffer.getInt();
                int phoneNumber = buffer.getInt();
                String email = readString(buffer, EMAIL_SIZE);
                String address = readString(buffer, ADDRESS_SIZE);
                String nationality = readString(buffer, NATIONALITY_SIZE);

                guests.add(new Guest(name.trim(), lastName.trim(), dni, phoneNumber,
                        email.trim(), address.trim(), nationality.trim()));
            }
            return createJsonResponse(true, "Huéspedes recuperados exitosamente", guests);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al recuperar los huéspedes: " + e.getMessage(), null);
        }
    }

    public String update(Guest guest) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos + NAME_SIZE + LAST_NAME_SIZE);
                int readDni = raf.readInt();

                if (readDni == guest.getId()) {
                    raf.seek(pos);
                    ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);

                    writeString(buffer, guest.getName(), NAME_SIZE);
                    writeString(buffer, guest.getLastName(), LAST_NAME_SIZE);
                    buffer.putInt(guest.getId());
                    buffer.putInt(guest.getPhoneNumber());
                    writeString(buffer, guest.getEmail(), EMAIL_SIZE);
                    writeString(buffer, guest.getAddress(), ADDRESS_SIZE);
                    writeString(buffer, guest.getNationality(), NATIONALITY_SIZE);

                    raf.write(buffer.array());
                    return createJsonResponse(true, "Huésped actualizado exitosamente", guest);
                }
            }
            return createJsonResponse(false, "Huésped no encontrado", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al actualizar el huésped: " + e.getMessage(), null);
        }
    }

    public String delete(int dni) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos + NAME_SIZE + LAST_NAME_SIZE);
                if (raf.readInt() == dni) {
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
                    return createJsonResponse(true, "Huésped eliminado exitosamente", null);
                }
            }
            return createJsonResponse(false, "Huésped no encontrado", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al eliminar el huésped: " + e.getMessage(), null);
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