package com.MagicalStay.shared.config;

public class ConfiguracionApp {

    // Configuración del servidor
    public static final String HOST_SERVIDOR = "192.168.6.79"; // Cambiar por la IP del servidor para conexiones remotas
    public static final int PUERTO_SERVIDOR = 5000;

    // Configuración de la aplicación
    public static final String NOMBRE_APLICACION = "MagicalStay Hotel Management System";
    public static final String VERSION = "1.0.0";

    // Timeout de conexión (en milisegundos)
    public static final int TIMEOUT_CONEXION = 5000;

    // Configuración de interfaz
    public static final String TEMA_PRINCIPAL = "#336699";
    public static final String COLOR_EXITO = "#4CAF50";
    public static final String COLOR_ERROR = "#f44336";
    public static final String COLOR_ADVERTENCIA = "#FF9800";

    // Rutas de archivos FXML
    public static final String FXML_HOTEL_MANAGEMENT = "/com/MagicalStay/hotel-management.fxml";
    public static final String FXML_ROOM_MANAGEMENT = "/com/MagicalStay/room-management.fxml";
    public static final String FXML_BOOKING_MANAGEMENT = "/com/MagicalStay/booking-management.fxml";
    public static final String FXML_GUEST_MANAGEMENT = "/com/MagicalStay/guest-management.fxml";
    public static final String FXML_REPORTS = "/com/MagicalStay/reports.fxml";

    // Mensajes del sistema
    public static final String MSG_CONEXION_EXITOSA = "Conexión establecida correctamente";
    public static final String MSG_CONEXION_ERROR = "Error al conectar con el servidor";
    public static final String MSG_DESCONEXION = "Desconectado del servidor";

    // Para conexiones remotas, usar métodos estáticos
    public static void configurarServidorRemoto(String host, int puerto) {
        // En una implementación real, podrías usar un archivo de configuración
        System.setProperty("magicalstay.server.host", host);
        System.setProperty("magicalstay.server.port", String.valueOf(puerto));
    }

    public static String getHostServidor() {
        return System.getProperty("magicalstay.server.host", HOST_SERVIDOR);
    }

    public static int getPuertoServidor() {
        return Integer.parseInt(System.getProperty("magicalstay.server.port", String.valueOf(PUERTO_SERVIDOR)));
    }
}