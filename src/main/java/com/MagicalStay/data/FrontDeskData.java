package com.MagicalStay.data;

import com.MagicalStay.domain.FrontDeskClerk;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

public class FrontDeskData extends JsonDataResponse {
    private RandomAccessFile raf;
    private static final int NAME_SIZE = 60;          // 30 caracteres
    private static final int LAST_NAMES_SIZE = 100;   // 50 caracteres
    private static final int EMPLOYEE_ID_SIZE = 20;   // 10 caracteres
    private static final int PHONE_SIZE = 4;          // int
    private static final int USER_SIZE = 40;          // 20 caracteres
    private static final int PASSWORD_SIZE = 40;      // 20 caracteres
    private static final int RECORD_SIZE = NAME_SIZE + LAST_NAMES_SIZE + EMPLOYEE_ID_SIZE + 
                                         PHONE_SIZE + USER_SIZE + PASSWORD_SIZE;

    public FrontDeskData(String filename) throws IOException {
        this.raf = new RandomAccessFile(filename, "rw");
    }

    public String create(FrontDeskClerk clerk) throws IOException {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
            
            writeString(buffer, clerk.getName(), NAME_SIZE);
            writeString(buffer, clerk.getLastNames(), LAST_NAMES_SIZE);
            writeString(buffer, clerk.getEmployeeId(), EMPLOYEE_ID_SIZE);
            buffer.putInt(clerk.getPhoneNumber());
            writeString(buffer, clerk.getUser(), USER_SIZE);
            writeString(buffer, clerk.getPassword(), PASSWORD_SIZE);

            raf.seek(raf.length());
            raf.write(buffer.array());
            
            return createJsonResponse(true, "Empleado creado exitosamente", clerk);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al crear el empleado: " + e.getMessage(), null);
        }
    }

    public String read(String employeeId) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();

                String name = readString(buffer, NAME_SIZE);
                String lastNames = readString(buffer, LAST_NAMES_SIZE);
                String currentEmployeeId = readString(buffer, EMPLOYEE_ID_SIZE);

                if (currentEmployeeId.trim().equals(employeeId)) {
                    int phoneNumber = buffer.getInt();
                    String user = readString(buffer, USER_SIZE);
                    String password = readString(buffer, PASSWORD_SIZE);

                    FrontDeskClerk clerk = new FrontDeskClerk(name.trim(), lastNames.trim(), 
                        currentEmployeeId.trim(), phoneNumber, user.trim(), password.trim());
                    return createJsonResponse(true, "Empleado encontrado", clerk);
                }
            }
            return createJsonResponse(false, "Empleado no encontrado", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al leer el empleado: " + e.getMessage(), null);
        }
    }

    public String readAll() throws IOException {
        try {
            List<FrontDeskClerk> clerks = new ArrayList<>();
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();

                String name = readString(buffer, NAME_SIZE);
                String lastNames = readString(buffer, LAST_NAMES_SIZE);
                String employeeId = readString(buffer, EMPLOYEE_ID_SIZE);
                int phoneNumber = buffer.getInt();
                String user = readString(buffer, USER_SIZE);
                String password = readString(buffer, PASSWORD_SIZE);

                clerks.add(new FrontDeskClerk(name.trim(), lastNames.trim(), 
                    employeeId.trim(), phoneNumber, user.trim(), password.trim()));
            }
            return createJsonResponse(true, "Empleados recuperados exitosamente", clerks);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al recuperar los empleados: " + e.getMessage(), null);
        }
    }

    public String update(FrontDeskClerk clerk) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos + NAME_SIZE + LAST_NAMES_SIZE);
                ByteBuffer idBuffer = ByteBuffer.allocate(EMPLOYEE_ID_SIZE);
                raf.readFully(idBuffer.array());
                String currentId = readString(idBuffer, EMPLOYEE_ID_SIZE);

                if (currentId.trim().equals(clerk.getEmployeeId())) {
                    raf.seek(pos);
                    ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                    
                    writeString(buffer, clerk.getName(), NAME_SIZE);
                    writeString(buffer, clerk.getLastNames(), LAST_NAMES_SIZE);
                    writeString(buffer, clerk.getEmployeeId(), EMPLOYEE_ID_SIZE);
                    buffer.putInt(clerk.getPhoneNumber());
                    writeString(buffer, clerk.getUser(), USER_SIZE);
                    writeString(buffer, clerk.getPassword(), PASSWORD_SIZE);

                    raf.write(buffer.array());
                    return createJsonResponse(true, "Empleado actualizado exitosamente", clerk);
                }
            }
            return createJsonResponse(false, "Empleado no encontrado", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al actualizar el empleado: " + e.getMessage(), null);
        }
    }

    public String delete(String employeeId) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos + NAME_SIZE + LAST_NAMES_SIZE);
                ByteBuffer idBuffer = ByteBuffer.allocate(EMPLOYEE_ID_SIZE);
                raf.readFully(idBuffer.array());
                String currentId = readString(idBuffer, EMPLOYEE_ID_SIZE);

                if (currentId.trim().equals(employeeId)) {
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
                    return createJsonResponse(true, "Empleado eliminado exitosamente", null);
                }
            }
            return createJsonResponse(false, "Empleado no encontrado", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error al eliminar el empleado: " + e.getMessage(), null);
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

    public String authenticate(String user, String password) throws IOException {
        try {
            for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                raf.seek(pos);
                ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE);
                raf.readFully(buffer.array());
                buffer.rewind();

                // Saltamos hasta la posición del usuario
                buffer.position(NAME_SIZE + LAST_NAMES_SIZE + EMPLOYEE_ID_SIZE + PHONE_SIZE);
                String currentUser = readString(buffer, USER_SIZE).trim();
                String currentPassword = readString(buffer, PASSWORD_SIZE).trim();

                if (currentUser.equals(user) && currentPassword.equals(password)) {
                    buffer.rewind();
                    String name = readString(buffer, NAME_SIZE);
                    String lastNames = readString(buffer, LAST_NAMES_SIZE);
                    String employeeId = readString(buffer, EMPLOYEE_ID_SIZE);
                    int phoneNumber = buffer.getInt();

                    FrontDeskClerk clerk = new FrontDeskClerk(name.trim(), lastNames.trim(), 
                        employeeId.trim(), phoneNumber, currentUser, currentPassword);
                    return createJsonResponse(true, "Autenticación exitosa", clerk);
                }
            }
            return createJsonResponse(false, "Credenciales inválidas", null);
        } catch (Exception e) {
            return createJsonResponse(false, "Error en la autenticación: " + e.getMessage(), null);
        }
    }
}