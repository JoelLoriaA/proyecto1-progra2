package com.MagicalStay.shared.data;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.MagicalStay.shared.domain.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GuestDataTest {

    private File guestFile;
    private GuestData guestData;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() throws IOException {
        guestFile = File.createTempFile("guests", ".dat");
        guestData = new GuestData(guestFile.getAbsolutePath());
    }

    @AfterEach
    public void cleanup() throws IOException {
        if (guestData != null) guestData.close();
    }

    @Test
    public void testCreateGuest() throws IOException {

        Guest guest = new Guest("Juan", "Pérez García", 12345678, 88887777,
                "juan.perez@email.com", "Calle 123, Ciudad", "Costa Rica");

        String createResult = guestData.create(guest);
        System.out.println("👤 Resultado creación huésped: " + createResult);
        assertNotNull(createResult);
        assertTrue(createResult.contains("\"success\":true"), " Error al crear huésped");

        String readResult = guestData.retrieveById(12345678);
        System.out.println("📄 Resultado lectura huésped: " + readResult);
        assertNotNull(readResult);
        assertTrue(readResult.contains("Juan"), " No contiene nombre");
        assertTrue(readResult.contains("Pérez García"), " No contiene apellido");
        assertTrue(readResult.contains("juan.perez@email.com"), " No contiene email");
        assertTrue(readResult.contains("Costa Rica"), " No contiene nacionalidad");
    }

    @Test
    public void testCreateDuplicateGuest() throws IOException {

        Guest guest1 = new Guest("María", "González", 87654321, 99998888,
                "maria@email.com", "Avenida 456", "México");

        String createResult1 = guestData.create(guest1);
        assertTrue(createResult1.contains("\"success\":true"), " Error al crear primer huésped");

        Guest guest2 = new Guest("Carlos", "López", 87654321, 77776666,
                "carlos@email.com", "Boulevard 789", "Guatemala");

        String createResult2 = guestData.create(guest2);
        System.out.println(" Intento crear duplicado: " + createResult2);
        assertTrue(createResult2.contains("\"success\":false"), " Debería fallar al crear duplicado");
        assertTrue(createResult2.contains("already exists"), " Mensaje de error incorrecto");
    }

    @Test
    public void testUpdateGuest() throws IOException {

        Guest originalGuest = new Guest("Ana", "Rodríguez", 11223344, 55554444,
                "ana@email.com", "Calle Original", "España");

        String createResult = guestData.create(originalGuest);
        assertTrue(createResult.contains("\"success\":true"), " Error al crear huésped");


        Guest updatedGuest = new Guest("Ana María", "Rodríguez Sánchez", 11223344, 66665555,
                "ana.maria@newemail.com", "Nueva Dirección 123", "España");

        String updateResult = guestData.update(updatedGuest);
        System.out.println("✏ Resultado actualización: " + updateResult);
        assertTrue(updateResult.contains("\"success\":true"), " Error al actualizar huésped");


        String readResult = guestData.retrieveById(11223344);
        assertTrue(readResult.contains("Ana María"), " No se actualizó el nombre");
        assertTrue(readResult.contains("Rodríguez Sánchez"), " No se actualizó el apellido");
        assertTrue(readResult.contains("ana.maria@newemail.com"), " No se actualizó el email");
    }

    @Test
    public void testDeleteGuest() throws IOException {

        Guest guest = new Guest("Pedro", "Martínez", 99887766, 33332222,
                "pedro@email.com", "Plaza Central", "Nicaragua");

        String createResult = guestData.create(guest);
        assertTrue(createResult.contains("\"success\":true"), "Error al crear huésped");


        String readBeforeDelete = guestData.retrieveById(99887766);
        assertTrue(readBeforeDelete.contains("Pedro"), " Huésped no encontrado antes de eliminar");


        String deleteResult = guestData.delete(99887766);
        System.out.println(" Resultado eliminación: " + deleteResult);
        assertTrue(deleteResult.contains("\"success\":true"), " Error al eliminar huésped");


        String readAfterDelete = guestData.retrieveById(99887766);
        System.out.println(" Después de eliminar: " + readAfterDelete);
        assertTrue(readAfterDelete.contains("\"success\":false"), " Huésped todavía existe");
    }

    @Test
    public void testRetrieveAllGuests() throws IOException {

        List<Guest> guestsToCreate = List.of(
                new Guest("Luis", "Hernández", 12121212, 11111111, "luis@email.com", "Dirección 1", "Honduras"),
                new Guest("Carmen", "Jiménez", 34343434, 22222222, "carmen@email.com", "Dirección 2", "El Salvador"),
                new Guest("Roberto", "Vargas", 56565656, 33333333, "roberto@email.com", "Dirección 3", "Panamá")
        );


        for (Guest guest : guestsToCreate) {
            String result = guestData.create(guest);
            assertTrue(result.contains("\"success\":true"), " Error al crear huésped: " + guest.getName());
        }


        String retrieveAllResult = guestData.retrieveAll();
        System.out.println(" Resultado retrieveAll: " + retrieveAllResult);

        JsonResponse response = objectMapper.readValue(retrieveAllResult, JsonResponse.class);
        assertTrue(response.isSuccess(), "Error al recuperar huéspedes");

        List<Guest> retrievedGuests = objectMapper.convertValue(
                response.getData(),
                new TypeReference<List<Guest>>() {}
        );

        assertEquals(3, retrievedGuests.size(), " Número incorrecto de huéspedes");
        assertTrue(retrievedGuests.stream().anyMatch(g -> g.getName().equals("Luis")), " Falta Luis");
        assertTrue(retrievedGuests.stream().anyMatch(g -> g.getName().equals("Carmen")), " Falta Carmen");
        assertTrue(retrievedGuests.stream().anyMatch(g -> g.getName().equals("Roberto")), " Falta Roberto");
    }

    @Test
    public void testSearchByName() throws IOException {

        Guest guest1 = new Guest("José Carlos", "Mendoza", 77777777, 44444444,
                "jose@email.com", "Calle José", "Costa Rica");
        Guest guest2 = new Guest("María José", "Fernández", 88888888, 55555555,
                "maria.jose@email.com", "Avenida María", "Costa Rica");
        Guest guest3 = new Guest("Antonio", "García", 99999999, 66666666,
                "antonio@email.com", "Boulevard Antonio", "Costa Rica");

        guestData.create(guest1);
        guestData.create(guest2);
        guestData.create(guest3);


        String searchResult = guestData.retrieveByName("José");
        System.out.println("Búsqueda por nombre 'José': " + searchResult);

        JsonResponse response = objectMapper.readValue(searchResult, JsonResponse.class);
        assertTrue(response.isSuccess(), " Error en búsqueda por nombre");

        List<Guest> foundGuests = objectMapper.convertValue(
                response.getData(),
                new TypeReference<List<Guest>>() {}
        );

        assertEquals(2, foundGuests.size(), " Debería encontrar 2 huéspedes con 'José'");
        assertTrue(foundGuests.stream().anyMatch(g -> g.getName().contains("José Carlos")), " Falta José Carlos");
        assertTrue(foundGuests.stream().anyMatch(g -> g.getName().contains("María José")), " Falta María José");
    }

    @Test
    public void testSearchByEmail() throws IOException {

        Guest guest1 = new Guest("Laura", "Ramírez", 11111111, 77777777,
                "laura@gmail.com", "Dirección Laura", "Costa Rica");
        Guest guest2 = new Guest("Diego", "Morales", 22222222, 88888888,
                "diego@gmail.com", "Dirección Diego", "Costa Rica");
        Guest guest3 = new Guest("Sofia", "Castro", 33333333, 99999999,
                "sofia@yahoo.com", "Dirección Sofia", "Costa Rica");

        guestData.create(guest1);
        guestData.create(guest2);
        guestData.create(guest3);


        String searchResult = guestData.retrieveByEmail("gmail");
        System.out.println("📧 Búsqueda por email 'gmail': " + searchResult);

        JsonResponse response = objectMapper.readValue(searchResult, JsonResponse.class);
        assertTrue(response.isSuccess(), " Error en búsqueda por email");

        List<Guest> foundGuests = objectMapper.convertValue(
                response.getData(),
                new TypeReference<List<Guest>>() {}
        );

        assertEquals(2, foundGuests.size(), " Debería encontrar 2 huéspedes con 'gmail'");
        assertTrue(foundGuests.stream().anyMatch(g -> g.getEmail().contains("laura@gmail.com")), " Falta Laura");
        assertTrue(foundGuests.stream().anyMatch(g -> g.getEmail().contains("diego@gmail.com")), " Falta Diego");
    }

    @Test
    public void testFieldLengthValidation() throws IOException {

        String longName = "A".repeat(50);
        String longLastName = "B".repeat(50);
        String longEmail = "C".repeat(60) + "@email.com";
        String longAddress = "D".repeat(150);
        String longNationality = "E".repeat(50);

        Guest invalidGuest = new Guest(longName, longLastName, 12345678, 88887777,
                longEmail, longAddress, longNationality);

        String createResult = guestData.create(invalidGuest);
        System.out.println(" Intento crear con campos largos: " + createResult);
        assertTrue(createResult.contains("\"success\":false"), " Debería fallar con campos largos");
        assertTrue(createResult.contains("exceed maximum length"), " Mensaje de error incorrecto");
    }

    @Test
    public void testDeleteNonExistentGuest() throws IOException {

        String deleteResult = guestData.delete(99999999);
        System.out.println(" Intento eliminar inexistente: " + deleteResult);
        assertTrue(deleteResult.contains("\"success\":false"), " Debería fallar al eliminar inexistente");
        assertTrue(deleteResult.contains("not found"), " Mensaje de error incorrecto");
    }
}
