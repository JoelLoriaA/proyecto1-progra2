package com.MagicalStay.shared.data;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.MagicalStay.shared.domain.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FrontDeskDataTest {

    private File frontDeskFile;
    private FrontDeskData frontDeskData;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() throws IOException {
        frontDeskFile = File.createTempFile("frontdesk", ".dat");
        frontDeskData = new FrontDeskData(frontDeskFile.getAbsolutePath());
    }

    @AfterEach
    public void cleanup() throws IOException {
        if (frontDeskData != null) frontDeskData.close();
    }

    @Test
    public void testCreateFrontDeskClerk() throws IOException {
        FrontDeskClerk clerk = new FrontDeskClerk("María", "González Pérez", "EMP001",
                88887777, 123456789L, "maria.gonzalez", "password123");

        String createResult = frontDeskData.create(clerk);
        System.out.println("Resultado creación recepcionista: " + createResult);
        assertNotNull(createResult);
        assertTrue(createResult.contains("\"success\":true"), " Error al crear recepcionista");

        // Leer recepcionista
        String readResult = frontDeskData.retrieveById("EMP001");
        System.out.println("📄 Resultado lectura recepcionista: " + readResult);
        assertNotNull(readResult);
        assertTrue(readResult.contains("María"), " No contiene nombre");
        assertTrue(readResult.contains("González Pérez"), " No contiene apellidos");
        assertTrue(readResult.contains("EMP001"), " No contiene ID empleado");
        assertTrue(readResult.contains("maria.gonzalez"), " No contiene username");
    }

    @Test
    public void testUpdateFrontDeskClerk() throws IOException {

        FrontDeskClerk originalClerk = new FrontDeskClerk("Carlos", "Rodríguez", "EMP002",
                77776666, 987654321L, "carlos.rodriguez", "oldpass");

        String createResult = frontDeskData.create(originalClerk);
        assertTrue(createResult.contains("\"success\":true"), " Error al crear recepcionista");


        FrontDeskClerk updatedClerk = new FrontDeskClerk("Carlos Alberto", "Rodríguez Méndez", "EMP002",
                99998888, 987654321L, "carlos.alberto", "newpass123");

        String updateResult = frontDeskData.update(updatedClerk);
        System.out.println(" Resultado actualización: " + updateResult);
        assertTrue(updateResult.contains("\"success\":true"), " Error al actualizar recepcionista");


        String readResult = frontDeskData.retrieveById("EMP002");
        assertTrue(readResult.contains("Carlos Alberto"), " No se actualizó el nombre");
        assertTrue(readResult.contains("Rodríguez Méndez"), " No se actualizaron los apellidos");
        assertTrue(readResult.contains("carlos.alberto"), " No se actualizó el username");
        assertTrue(readResult.contains("newpass123"), " No se actualizó la contraseña");
    }

    @Test
    public void testDeleteFrontDeskClerk() throws IOException {

        FrontDeskClerk clerk = new FrontDeskClerk("Ana", "López", "EMP003",
                55554444, 111222333L, "ana.lopez", "anapass");

        String createResult = frontDeskData.create(clerk);
        assertTrue(createResult.contains("\"success\":true"), " Error al crear recepcionista");


        String readBeforeDelete = frontDeskData.retrieveById("EMP003");
        assertTrue(readBeforeDelete.contains("Ana"), " Recepcionista no encontrado antes de eliminar");


        String deleteResult = frontDeskData.delete("EMP003");
        System.out.println("🗑 Resultado eliminación: " + deleteResult);
        assertTrue(deleteResult.contains("\"success\":true"), " Error al eliminar recepcionista");


        String readAfterDelete = frontDeskData.retrieveById("EMP003");
        System.out.println("📖 Después de eliminar: " + readAfterDelete);
        assertTrue(readAfterDelete.contains("\"success\":false"), " Recepcionista todavía existe");
    }

    @Test
    public void testRetrieveAllFrontDeskClerks() throws IOException {

        List<FrontDeskClerk> clerksToCreate = List.of(
                new FrontDeskClerk("Luis", "Hernández", "EMP004", 11111111, 444555666L, "luis.hernandez", "luispass"),
                new FrontDeskClerk("Carmen", "Jiménez", "EMP005", 22222222, 777888999L, "carmen.jimenez", "carmenpass"),
                new FrontDeskClerk("Roberto", "Vargas", "EMP006", 33333333, 123789456L, "roberto.vargas", "robertopass")
        );


        for (FrontDeskClerk clerk : clerksToCreate) {
            String result = frontDeskData.create(clerk);
            assertTrue(result.contains("\"success\":true"),
                    " Error al crear recepcionista: " + clerk.getName());
        }

        // Recuperar todos
        String retrieveAllResult = frontDeskData.retrieveAll();
        System.out.println(" Resultado retrieveAll: " + retrieveAllResult);

        JsonResponse response = objectMapper.readValue(retrieveAllResult, JsonResponse.class);
        assertTrue(response.isSuccess(), " Error al recuperar recepcionistas");

        List<FrontDeskClerk> retrievedClerks = objectMapper.convertValue(
                response.getData(),
                new TypeReference<List<FrontDeskClerk>>() {
                }
        );

        assertEquals(3, retrievedClerks.size(), " Número incorrecto de recepcionistas");
        assertTrue(retrievedClerks.stream().anyMatch(c -> c.getName().equals("Luis")), " Falta Luis");
        assertTrue(retrievedClerks.stream().anyMatch(c -> c.getName().equals("Carmen")), " Falta Carmen");
        assertTrue(retrievedClerks.stream().anyMatch(c -> c.getName().equals("Roberto")), " Falta Roberto");
    }

    @Test
    public void testSearchByName() throws IOException {

        FrontDeskClerk clerk1 = new FrontDeskClerk("José Carlos", "Mendoza", "EMP007",
                44444444, 111111111L, "jose.carlos", "josepass");
        FrontDeskClerk clerk2 = new FrontDeskClerk("María José", "Fernández", "EMP008",
                55555555, 222222222L, "maria.jose", "mariapass");
        FrontDeskClerk clerk3 = new FrontDeskClerk("Antonio", "García", "EMP009",
                66666666, 333333333L, "antonio", "antoniopass");

        frontDeskData.create(clerk1);
        frontDeskData.create(clerk2);
        frontDeskData.create(clerk3);


        String searchResult = frontDeskData.retrieveByName("José");
        System.out.println(" Búsqueda por nombre 'José': " + searchResult);

        JsonResponse response = objectMapper.readValue(searchResult, JsonResponse.class);
        assertTrue(response.isSuccess(), "Error en búsqueda por nombre");

        List<FrontDeskClerk> foundClerks = objectMapper.convertValue(
                response.getData(),
                new TypeReference<List<FrontDeskClerk>>() {
                }
        );

        assertEquals(2, foundClerks.size(), " Debería encontrar 2 recepcionistas con 'José'");
        assertTrue(foundClerks.stream().anyMatch(c -> c.getName().contains("José Carlos")), "❌ Falta José Carlos");
        assertTrue(foundClerks.stream().anyMatch(c -> c.getName().contains("María José")), "❌ Falta María José");
    }

    @Test
    public void testAuthentication() throws IOException {

        FrontDeskClerk clerk = new FrontDeskClerk("Admin", "Usuario", "ADMIN001",
                99999999, 123456789L, "admin", "admin123");

        String createResult = frontDeskData.create(clerk);
        assertTrue(createResult.contains("\"success\":true"), " Error al crear recepcionista");


        String authSuccessResult = frontDeskData.authenticate("admin", "admin123");
        System.out.println(" Autenticación correcta: " + authSuccessResult);
        assertTrue(authSuccessResult.contains("\"success\":true"), " Autenticación correcta falló");
        assertTrue(authSuccessResult.contains("Autenticación exitosa"), " Mensaje incorrecto");


        String authFailResult1 = frontDeskData.authenticate("admin", "wrongpass");
        System.out.println(" Autenticación incorrecta 1: " + authFailResult1);
        assertTrue(authFailResult1.contains("\"success\":false"), "Debería fallar con password incorrecto");


        String authFailResult2 = frontDeskData.authenticate("wronguser", "admin123");
        System.out.println(" Autenticación incorrecta 2: " + authFailResult2);
        assertTrue(authFailResult2.contains("\"success\":false"), " Debería fallar con username incorrecto");

        // Autenticación incorrecta - ambos incorrectos
        String authFailResult3 = frontDeskData.authenticate("wronguser", "wrongpass");
        System.out.println(" Autenticación incorrecta 3: " + authFailResult3);
        assertTrue(authFailResult3.contains("\"success\":false"), " Debería fallar con credenciales incorrectas");
    }

    @Test
    public void testSearchByDni() throws IOException {

        FrontDeskClerk clerk1 = new FrontDeskClerk("Laura", "Ramírez", "EMP010",
                77777777, 111111111L, "laura.ramirez", "laurapass");
        FrontDeskClerk clerk2 = new FrontDeskClerk("Diego", "Morales", "EMP011",
                88888888, 222222222L, "diego.morales", "diegopass");
        FrontDeskClerk clerk3 = new FrontDeskClerk("Sofia", "Castro", "EMP012",
                99999999, 111111111L, "sofia.castro", "sofiapass"); // Mismo DNI que Laura

        frontDeskData.create(clerk1);
        frontDeskData.create(clerk2);
        frontDeskData.create(clerk3);


        String searchResult = frontDeskData.retrieveByDni(111111111L);
        System.out.println("🆔 Búsqueda por DNI 111111111: " + searchResult);

        JsonResponse response = objectMapper.readValue(searchResult, JsonResponse.class);
        assertTrue(response.isSuccess(), " Error en búsqueda por DNI");

        List<FrontDeskClerk> foundClerks = objectMapper.convertValue(
                response.getData(),
                new TypeReference<List<FrontDeskClerk>>() {
                }
        );

        assertEquals(2, foundClerks.size(), " Debería encontrar 2 recepcionistas con mismo DNI");
        assertTrue(foundClerks.stream().anyMatch(c -> c.getName().equals("Laura")), " Falta Laura");
        assertTrue(foundClerks.stream().anyMatch(c -> c.getName().equals("Sofia")), " Falta Sofia");
    }

    @Test
    public void testFieldLengthValidation() throws IOException {

        String longName = "A".repeat(40);
        String longLastNames = "B".repeat(60);
        String longEmployeeId = "C".repeat(15);
        String longUsername = "D".repeat(25);
        String longPassword = "E".repeat(25);

        FrontDeskClerk invalidClerk = new FrontDeskClerk(longName, longLastNames, longEmployeeId,
                88887777, 123456789L, longUsername, longPassword);

        String createResult = frontDeskData.create(invalidClerk);
        System.out.println(" Intento crear con campos largos: " + createResult);
        assertTrue(createResult.contains("\"success\":false"), " Debería fallar con campos largos");
        assertTrue(createResult.contains("exceden la longitud máxima"), " Mensaje de error incorrecto");
    }

    @Test
    public void testDeleteNonExistentClerk() throws IOException {

        String deleteResult = frontDeskData.delete("NONEXISTENT");
        System.out.println(" Intento eliminar inexistente: " + deleteResult);
        assertTrue(deleteResult.contains("\"success\":false"), " Debería fallar al eliminar inexistente");
        assertTrue(deleteResult.contains("no encontrado"), " Mensaje de error incorrecto");
    }

    @Test
    public void testUpdateNonExistentClerk() throws IOException {

        FrontDeskClerk nonExistentClerk = new FrontDeskClerk("Test", "User", "NOEXIST",
                12345678, 987654321L, "testuser", "testpass");

        String updateResult = frontDeskData.update(nonExistentClerk);
        System.out.println(" Intento actualizar inexistente: " + updateResult);
        assertTrue(updateResult.contains("\"success\":false"), " Debería fallar al actualizar inexistente");
        assertTrue(updateResult.contains("no encontrado"), " Mensaje de error incorrecto");
    }

    @Test
    public void testDeleteOneClerkOnly() throws IOException {

        FrontDeskClerk clerk1 = new FrontDeskClerk("Empleado1", "Apellidos1", "EMP101",
                11111111, 111111111L, "emp1", "pass1");
        FrontDeskClerk clerk2 = new FrontDeskClerk("Empleado2", "Apellidos2", "EMP102",
                22222222, 222222222L, "emp2", "pass2");
        FrontDeskClerk clerk3 = new FrontDeskClerk("Empleado3", "Apellidos3", "EMP103",
                33333333, 333333333L, "emp3", "pass3");

        frontDeskData.create(clerk1);
        frontDeskData.create(clerk2);
        frontDeskData.create(clerk3);


        String deleteResult = frontDeskData.delete("EMP102");
        System.out.println("Resultado eliminación: " + deleteResult);
        assertTrue(deleteResult.contains("\"success\":true"), " Falló al eliminar recepcionista EMP102");


        String retrieveAllResult = frontDeskData.retrieveAll();
        JsonResponse response = objectMapper.readValue(retrieveAllResult, JsonResponse.class);
    }
}
