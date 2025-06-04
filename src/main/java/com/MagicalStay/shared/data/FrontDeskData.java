package com.MagicalStay.shared.data;

        import com.MagicalStay.shared.domain.FrontDeskClerk;
        import java.io.IOException;
        import java.io.RandomAccessFile;
        import java.util.ArrayList;
        import java.util.List;

        public class FrontDeskData extends JsonDataResponse {
            private RandomAccessFile raf;
            private static final int NAME_LENGTH = 30;
            private static final int LAST_NAMES_LENGTH = 50;
            private static final int EMPLOYEE_ID_LENGTH = 10;
            private static final int DNI_SIZE = 4;
            private static final int USERNAME_LENGTH = 20;
            private static final int PASSWORD_LENGTH = 20;
            private static final int PHONE_SIZE = 4;

            private static final int NAME_SIZE = NAME_LENGTH * 2;
            private static final int LAST_NAMES_SIZE = LAST_NAMES_LENGTH * 2;
            private static final int EMPLOYEE_ID_SIZE = EMPLOYEE_ID_LENGTH * 2;
            private static final int USERNAME_SIZE = USERNAME_LENGTH * 2;
            private static final int PASSWORD_SIZE = PASSWORD_LENGTH * 2;

            private static final int RECORD_SIZE = NAME_SIZE + LAST_NAMES_SIZE + EMPLOYEE_ID_SIZE +
                                                 DNI_SIZE + USERNAME_SIZE + PASSWORD_SIZE + PHONE_SIZE;

            public FrontDeskData(String filename) throws IOException {
                this.raf = new RandomAccessFile(filename, "rw");
            }

            public String create(FrontDeskClerk clerk) throws IOException {
                try {
                    if (clerk.getName().length() > NAME_LENGTH ||
                        clerk.getLastNames().length() > LAST_NAMES_LENGTH ||
                        clerk.getEmployeeId().length() > EMPLOYEE_ID_LENGTH ||
                        clerk.getUsername().length() > USERNAME_LENGTH ||
                        clerk.getPassword().length() > PASSWORD_LENGTH) {
                        return createJsonResponse(false, "Uno o más campos exceden la longitud máxima", null);
                    }

                    raf.seek(raf.length());
                    writeClerk(clerk);
                    return createJsonResponse(true, "Recepcionista creado exitosamente", clerk);
                } catch (Exception e) {
                    return createJsonResponse(false, "Error al crear recepcionista: " + e.getMessage(), null);
                }
            }

            public String retrieveById(String employeeId) throws IOException {
                try {
                    for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                        raf.seek(pos);
                        FrontDeskClerk clerk = readClerk();
                        if (clerk.getEmployeeId().equals(employeeId)) {
                            return createJsonResponse(true, "Recepcionista encontrado", clerk);
                        }
                    }
                    return createJsonResponse(false, "Recepcionista no encontrado", null);
                } catch (Exception e) {
                    return createJsonResponse(false, "Error al recuperar recepcionista: " + e.getMessage(), null);
                }
            }

            public String retrieveAll() throws IOException {
                try {
                    List<FrontDeskClerk> clerks = new ArrayList<>();
                    for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                        raf.seek(pos);
                        clerks.add(readClerk());
                    }
                    return createJsonResponse(true, "Recepcionistas recuperados exitosamente", clerks);
                } catch (Exception e) {
                    return createJsonResponse(false, "Error al recuperar recepcionistas: " + e.getMessage(), null);
                }
            }

            public String update(FrontDeskClerk clerk) throws IOException {
                try {
                    if (clerk.getName().length() > NAME_LENGTH ||
                        clerk.getLastNames().length() > LAST_NAMES_LENGTH ||
                        clerk.getEmployeeId().length() > EMPLOYEE_ID_LENGTH ||
                        clerk.getUsername().length() > USERNAME_LENGTH ||
                        clerk.getPassword().length() > PASSWORD_LENGTH) {
                        return createJsonResponse(false, "Uno o más campos exceden la longitud máxima", null);
                    }

                    for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                        raf.seek(pos);
                        FrontDeskClerk currentClerk = readClerk();
                        if (currentClerk.getEmployeeId().equals(clerk.getEmployeeId())) {
                            raf.seek(pos);
                            writeClerk(clerk);
                            return createJsonResponse(true, "Recepcionista actualizado exitosamente", clerk);
                        }
                    }
                    return createJsonResponse(false, "Recepcionista no encontrado", null);
                } catch (Exception e) {
                    return createJsonResponse(false, "Error al actualizar recepcionista: " + e.getMessage(), null);
                }
            }

            public String delete(String employeeId) throws IOException {
                try {
                    for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                        raf.seek(pos);
                        FrontDeskClerk clerk = readClerk();
                        if (clerk.getEmployeeId().equals(employeeId)) {
                            moveRemainingRecords(pos);
                            raf.setLength(raf.length() - RECORD_SIZE);
                            return createJsonResponse(true, "Recepcionista eliminado exitosamente", null);
                        }
                    }
                    return createJsonResponse(false, "Recepcionista no encontrado", null);
                } catch (Exception e) {
                    return createJsonResponse(false, "Error al eliminar recepcionista: " + e.getMessage(), null);
                }
            }

            public String retrieveByName(String searchName) throws IOException {
                try {
                    List<FrontDeskClerk> matchingClerks = new ArrayList<>();
                    searchName = searchName.toLowerCase();

                    for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                        raf.seek(pos);
                        FrontDeskClerk clerk = readClerk();
                        if (clerk.getName().toLowerCase().contains(searchName) ||
                            clerk.getLastNames().toLowerCase().contains(searchName)) {
                            matchingClerks.add(clerk);
                        }
                    }

                    if (!matchingClerks.isEmpty()) {
                        return createJsonResponse(true, "Recepcionistas encontrados", matchingClerks);
                    }
                    return createJsonResponse(false, "No se encontraron recepcionistas con ese nombre", null);
                } catch (Exception e) {
                    return createJsonResponse(false, "Error al buscar recepcionistas: " + e.getMessage(), null);
                }
            }

            private void writeClerk(FrontDeskClerk clerk) throws IOException {
                writeString(clerk.getName(), NAME_LENGTH);
                writeString(clerk.getLastNames(), LAST_NAMES_LENGTH);
                writeString(clerk.getEmployeeId(), EMPLOYEE_ID_LENGTH);
                raf.writeInt((int)clerk.getDni());
                writeString(clerk.getUsername(), USERNAME_LENGTH);
                writeString(clerk.getPassword(), PASSWORD_LENGTH);
                raf.writeInt(clerk.getPhoneNumber());
            }

            private FrontDeskClerk readClerk() throws IOException {
                String name = readString(NAME_LENGTH);
                String lastNames = readString(LAST_NAMES_LENGTH);
                String employeeId = readString(EMPLOYEE_ID_LENGTH);
                long dni = raf.readInt();
                String username = readString(USERNAME_LENGTH);
                String password = readString(PASSWORD_LENGTH);
                int phoneNumber = raf.readInt();

                return new FrontDeskClerk(
                    name.trim(),
                    lastNames.trim(),
                    employeeId.trim(),
                    phoneNumber,
                    dni,
                    username.trim(),
                    password.trim()
                );
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
                byte[] buffer = new byte[RECORD_SIZE];
                long nextPos = pos + RECORD_SIZE;

                while (nextPos < raf.length()) {
                    raf.seek(nextPos);
                    raf.readFully(buffer);
                    raf.seek(nextPos - RECORD_SIZE);
                    raf.write(buffer);
                    nextPos += RECORD_SIZE;
                }
            }

            public void close() throws IOException {
                if (raf != null) {
                    raf.close();
                }
            }

            public String authenticate(String username, String password) throws IOException {
                try {
                    for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                        raf.seek(pos);
                        FrontDeskClerk clerk = readClerk();
                        if (clerk.getUsername().equals(username) &&
                            clerk.getPassword().equals(password)) {
                            return createJsonResponse(true, "Autenticación exitosa", clerk);
                        }
                    }
                    return createJsonResponse(false, "Credenciales inválidas", null);
                } catch (Exception e) {
                    return createJsonResponse(false, "Error en la autenticación: " + e.getMessage(), null);
                }
            }

            public String retrieveByDni(long dni) throws IOException {
                try {
                    List<FrontDeskClerk> matchingClerks = new ArrayList<>();

                    for (long pos = 0; pos < raf.length(); pos += RECORD_SIZE) {
                        raf.seek(pos);
                        FrontDeskClerk clerk = readClerk();
                        if (clerk.getDni() == dni) {
                            matchingClerks.add(clerk);
                        }
                    }

                    if (!matchingClerks.isEmpty()) {
                        return createJsonResponse(true, "Recepcionistas encontrados", matchingClerks);
                    }
                    return createJsonResponse(false, "No se encontraron recepcionistas con ese DNI", null);
                } catch (Exception e) {
                    return createJsonResponse(false, "Error al buscar recepcionistas: " + e.getMessage(), null);
                }
            }
        }