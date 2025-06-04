package com.MagicalStay.shared.data;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import com.MagicalStay.shared.domain.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class HotelDataTest {
    private File hotelFile;
    private HotelData hotelData;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws IOException {
        hotelFile = File.createTempFile("hotels", ".dat");
        hotelData = new HotelData(hotelFile.getAbsolutePath());
        objectMapper = new ObjectMapper();
    }



    @Test
    public void testCreateHotel() throws IOException {
        Hotel hotel = new Hotel(1, "Hotel Test", "Dirección Test");
        
        String createResult = hotelData.create(hotel);
        System.out.println("✅ Resultado de creación: " + createResult);
        
        assertNotNull(createResult, "Resultado de creación no debería ser nulo");
        assertTrue(createResult.contains("\"success\":true"), "Creación fallida: " + createResult);

        String retrieveResult = hotelData.retrieveById(1);
        System.out.println("📖 Resultado de lectura: " + retrieveResult);
        
        assertNotNull(retrieveResult, "Resultado de lectura no debería ser nulo");
        assertTrue(retrieveResult.contains("Hotel Test"), "No contiene el nombre del hotel");
        assertTrue(retrieveResult.contains("Dirección Test"), "No contiene la dirección del hotel");
    }

    @Test
    public void testRetrieveById() throws IOException {
        // Crear hotel de prueba
        Hotel hotel = new Hotel(1, "Hotel Test", "Dirección Test");
        hotelData.create(hotel);

        // Probar retrieveById
        String retrieveResult = hotelData.retrieveById(1);
        System.out.println("📖 Resultado retrieveById: " + retrieveResult);
        
        JsonResponse response = objectMapper.readValue(retrieveResult, JsonResponse.class);
        Hotel retrievedHotel = objectMapper.convertValue(response.getData(), Hotel.class);

        assertTrue(response.isSuccess(), "❌ La operación retrieveById debería ser exitosa");
        assertEquals(1, retrievedHotel.getHotelId(), "❌ ID incorrecto");
        assertEquals("Hotel Test", retrievedHotel.getName(), "❌ Nombre incorrecto");
        assertEquals("Dirección Test", retrievedHotel.getAddress(), "❌ Dirección incorrecta");
    }

    @Test
    public void testRetrieveAll() throws IOException {
        // Crear hoteles de prueba
        Hotel hotel1 = new Hotel(1, "Hotel Playa", "Av. Costa 123");
        Hotel hotel2 = new Hotel(2, "Hotel Centro", "Jr. Lima 456");
        Hotel hotel3 = new Hotel(3, "Hotel Norte", "Av. Norte 789");

        System.out.println("📝 Creando hoteles de prueba...");
        hotelData.create(hotel1);
        hotelData.create(hotel2);
        hotelData.create(hotel3);

        String retrieveAllResult = hotelData.retrieveAll();
        System.out.println("📚 Resultado retrieveAll: " + retrieveAllResult);

        JsonResponse response = objectMapper.readValue(retrieveAllResult, JsonResponse.class);
        List<Hotel> hotels = objectMapper.convertValue(
            response.getData(), 
            new TypeReference<List<Hotel>>() {}
        );

        assertTrue(response.isSuccess(), "❌ La operación retrieveAll debería ser exitosa");
        assertEquals(3, hotels.size(), "❌ Número incorrecto de hoteles");
    }

    @Test
    public void testRetrieveByName() throws IOException {
        // Crear hoteles con nombres similares
        Hotel hotel1 = new Hotel(1, "Gran Hotel Plaza", "Dirección 1");
        Hotel hotel2 = new Hotel(2, "Hotel Plaza Real", "Dirección 2");
        Hotel hotel3 = new Hotel(3, "Hotel Centenario", "Dirección 3");

        System.out.println("📝 Creando hoteles de prueba...");
        hotelData.create(hotel1);
        hotelData.create(hotel2);
        hotelData.create(hotel3);

        String searchResult = hotelData.retrieveByName("Plaza");
        System.out.println("\n🔍 Resultado búsqueda: " + searchResult);

        JsonResponse response = objectMapper.readValue(searchResult, JsonResponse.class);
        List<Hotel> foundHotels = objectMapper.convertValue(
            response.getData(), 
            new TypeReference<List<Hotel>>() {}
        );

        assertTrue(response.isSuccess(), "❌ La búsqueda debería ser exitosa");
        assertEquals(2, foundHotels.size(), "❌ Deberían encontrarse 2 hoteles");
    }

    @Test
    public void testRetrieveByAddress() throws IOException {
        // Crear hoteles con diferentes direcciones
        Hotel hotel1 = new Hotel(1, "Hotel A", "Miraflores, Lima");
        Hotel hotel2 = new Hotel(2, "Hotel B", "San Isidro, Lima");
        
        System.out.println("📝 Creando hoteles de prueba...");
        hotelData.create(hotel1);
        hotelData.create(hotel2);

        String searchResult = hotelData.retrieveByAddress("Lima");
        System.out.println("\n🔍 Resultado búsqueda: " + searchResult);

        JsonResponse response = objectMapper.readValue(searchResult, JsonResponse.class);
        List<Hotel> foundHotels = objectMapper.convertValue(
            response.getData(), 
            new TypeReference<List<Hotel>>() {}
        );

        assertTrue(response.isSuccess(), "❌ La búsqueda debería ser exitosa");
        assertEquals(2, foundHotels.size(), "❌ Deberían encontrarse 2 hoteles");
    }

    @Test
    public void testUpdateHotel() throws IOException {
        // Crear hotel inicial
        Hotel hotel = new Hotel(1, "Hotel Original", "Dirección Original");
        hotelData.create(hotel);

        // Modificar y actualizar
        hotel.setName("Hotel Actualizado");
        hotel.setAddress("Dirección Actualizada");
        
        String updateResult = hotelData.update(hotel);
        System.out.println("🔄 Resultado actualización: " + updateResult);
        
        assertTrue(updateResult.contains("\"success\":true"), "❌ Actualización fallida");

        // Verificar cambios
        String retrieveResult = hotelData.retrieveById(1);
        assertTrue(retrieveResult.contains("Hotel Actualizado"), "❌ Nombre no actualizado");
        assertTrue(retrieveResult.contains("Dirección Actualizada"), "❌ Dirección no actualizada");
    }

    @Test
    public void testDeleteHotel() throws IOException {
        // Crear hotel para eliminar
        Hotel hotel = new Hotel(1, "Hotel a Eliminar", "Dirección Test");
        hotelData.create(hotel);

        // Eliminar hotel
        String deleteResult = hotelData.delete(1);
        System.out.println("🗑 Resultado eliminación: " + deleteResult);
        
        assertTrue(deleteResult.contains("\"success\":true"), "❌ Eliminación fallida");

        // Verificar que no existe
        String retrieveResult = hotelData.retrieveById(1);
        assertTrue(retrieveResult.contains("\"success\":false"), 
                  "❌ El hotel no debería existir después de eliminarlo");
    }
}