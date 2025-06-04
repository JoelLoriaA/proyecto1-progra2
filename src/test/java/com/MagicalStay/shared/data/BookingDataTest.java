package com.MagicalStay.shared.data;

        import static org.junit.jupiter.api.Assertions.*;
        import org.junit.jupiter.api.*;
        import com.MagicalStay.shared.domain.*;
        import com.fasterxml.jackson.core.type.TypeReference;
        import com.fasterxml.jackson.databind.ObjectMapper;
        import java.io.File;
        import java.io.IOException;
        import java.time.LocalDate;
        import java.util.ArrayList;
        import java.util.List;
        import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
        import com.fasterxml.jackson.databind.SerializationFeature;

        public class BookingDataTest {
            private File bookingFile;
            private BookingData bookingData;
            private ObjectMapper objectMapper;

            @BeforeEach
            public void setup() throws IOException {
                bookingFile = File.createTempFile("bookings", ".dat");
                bookingData = new BookingData(bookingFile.getAbsolutePath());
                objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            }

            @AfterEach
            public void cleanup() throws IOException {
                if (bookingData != null) {
                    bookingData.close();
                }
                if (bookingFile != null && bookingFile.exists()) {
                    bookingFile.delete();
                }
            }

            private Booking createTestBooking(int id) {
                List<Room> rooms = new ArrayList<>();
                rooms.add(new Room("101", null, null, null));
                return new Booking(id, LocalDate.now(), LocalDate.now().plusDays(2), rooms);
            }

            @Test
            public void testCreateBooking() throws IOException {
                Booking booking = createTestBooking(1);
                String createResult = bookingData.create(booking);
                System.out.println("✅ Resultado de creación: " + createResult);

                JsonResponse<Booking> createResponse = objectMapper.readValue(createResult,
                    new TypeReference<JsonResponse<Booking>>() {});

                assertTrue(createResponse.isSuccess(), "❌ La creación debería ser exitosa");
                assertNotNull(createResponse.getData(), "❌ Los datos no deberían ser nulos");

                String retrieveResult = bookingData.retrieveById(1);
                JsonResponse<Booking> retrieveResponse = objectMapper.readValue(retrieveResult,
                    new TypeReference<JsonResponse<Booking>>() {});

                assertTrue(retrieveResponse.isSuccess(), "❌ La lectura debería ser exitosa");
                assertNotNull(retrieveResponse.getData(), "❌ Los datos recuperados no deberían ser nulos");
                assertEquals(1, retrieveResponse.getData().getBookingId(), "❌ ID incorrecto");
            }

            @Test
            public void testRetrieveById() throws IOException {
                Booking booking = createTestBooking(1);
                bookingData.create(booking);

                String retrieveResult = bookingData.retrieveById(1);
                System.out.println("📖 Resultado retrieveById: " + retrieveResult);

                JsonResponse<Booking> response = objectMapper.readValue(retrieveResult,
                    new TypeReference<JsonResponse<Booking>>() {});

                assertTrue(response.isSuccess(), "❌ La operación retrieveById debería ser exitosa");
                assertNotNull(response.getData(), "❌ Los datos no deberían ser nulos");
                assertEquals(1, response.getData().getBookingId(), "❌ ID incorrecto");
                assertEquals(booking.getStartDate(), response.getData().getStartDate(), "❌ Fecha inicio incorrecta");
                assertEquals(booking.getLeavingDate(), response.getData().getLeavingDate(), "❌ Fecha salida incorrecta");
            }

            @Test
            public void testRetrieveAll() throws IOException {
                // Crear múltiples reservas
                bookingData.create(createTestBooking(1));
                bookingData.create(createTestBooking(2));
                bookingData.create(createTestBooking(3));

                String retrieveAllResult = bookingData.retrieveAll();
                System.out.println("📚 Resultado retrieveAll: " + retrieveAllResult);

                JsonResponse<List<Booking>> response = objectMapper.readValue(retrieveAllResult,
                    new TypeReference<JsonResponse<List<Booking>>>() {});

                assertTrue(response.isSuccess(), "❌ La operación retrieveAll debería ser exitosa");
                assertNotNull(response.getData(), "❌ Los datos no deberían ser nulos");
                assertEquals(3, response.getData().size(), "❌ Número incorrecto de reservas");

                // Verificar que las fechas se mantienen correctamente
                for (Booking b : response.getData()) {
                    assertNotNull(b.getStartDate(), "❌ Fecha inicio no debería ser nula");
                    assertNotNull(b.getLeavingDate(), "❌ Fecha salida no debería ser nula");
                }
            }

            @Test
            public void testUpdateBooking() throws IOException {
                Booking booking = createTestBooking(1);
                bookingData.create(booking);

                // Modificar la reserva
                List<Room> newRooms = new ArrayList<>();
                newRooms.add(new Room("102", null, null, null));
                booking.setReservedRooms(newRooms);
                booking.setLeavingDate(booking.getLeavingDate().plusDays(1));

                String updateResult = bookingData.update(booking);
                System.out.println("🔄 Resultado actualización: " + updateResult);

                JsonResponse<Booking> updateResponse = objectMapper.readValue(updateResult,
                    new TypeReference<JsonResponse<Booking>>() {});

                assertTrue(updateResponse.isSuccess(), "❌ La actualización debería ser exitosa");
                assertNotNull(updateResponse.getData(), "❌ Los datos actualizados no deberían ser nulos");

                // Verificar cambios
                String retrieveResult = bookingData.retrieveById(1);
                JsonResponse<Booking> retrieveResponse = objectMapper.readValue(retrieveResult,
                    new TypeReference<JsonResponse<Booking>>() {});

                assertEquals("102", retrieveResponse.getData().getReservedRooms().get(0).getRoomNumber(),
                    "❌ Habitación no actualizada");
                assertEquals(booking.getLeavingDate(), retrieveResponse.getData().getLeavingDate(),
                    "❌ Fecha de salida no actualizada");
            }

            @Test
            public void testDeleteBooking() throws IOException {
                Booking booking = createTestBooking(1);
                bookingData.create(booking);

                String deleteResult = bookingData.delete(1);
                System.out.println("🗑 Resultado eliminación: " + deleteResult);

                JsonResponse<Object> deleteResponse = objectMapper.readValue(deleteResult,
                    new TypeReference<JsonResponse<Object>>() {});

                assertTrue(deleteResponse.isSuccess(), "❌ La eliminación debería ser exitosa");

                String retrieveResult = bookingData.retrieveById(1);
                JsonResponse<Booking> retrieveResponse = objectMapper.readValue(retrieveResult,
                    new TypeReference<JsonResponse<Booking>>() {});

                assertFalse(retrieveResponse.isSuccess(), "❌ La reserva no debería existir después de eliminarla");
                assertNull(retrieveResponse.getData(), "❌ Los datos deberían ser nulos después de eliminar");
            }
        }