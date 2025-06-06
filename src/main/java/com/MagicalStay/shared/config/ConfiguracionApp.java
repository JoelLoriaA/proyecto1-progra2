package com.MagicalStay.shared.config;

public class ConfiguracionApp {

    // Configuración del servidor
    public static final String HOST_SERVIDOR = "192.168.100.8"; // Cambiar por la IP del servidor para conexiones remotas
    public static final int PUERTO_SERVIDOR = 5000;

    //Manejo de archivos en el servidor
    public static final String RUTA_ARCHIVOS_SERVIDOR = "D:\\JAVA_DEV\\progra2-2025\\ULTIMA FASE\\server\\files\\"; // Ruta donde se almacenan los archivos en el servidor
    public static final String RUTA_IMAGENES_SERVIDOR = "D:\\JAVA_DEV\\progra2-2025\\ULTIMA FASE\\server\\images\\"; // Ruta donde se almacenan los archivos en el cliente
    public static final String RUTA_COPIA_IMAGENES_SERVIDOR = "D:\\JAVA_DEV\\progra2-2025\\ULTIMA FASE\\server\\images\\copy\\"; // Ruta donde se almacenan las copias de los archivos en el servidor

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
    public static final String FXML_FRONTDESK_MANAGEMENT = "/com/MagicalStay/frontdesk-management.fxml";

    // Mensajes del sistema
    public static final String MSG_CONEXION_EXITOSA = "Conexión establecida correctamente";
    public static final String MSG_CONEXION_ERROR = "Error al conectar con el servidor";
    public static final String MSG_DESCONEXION = "Desconectado del servidor";

    // Agregar en ConfiguracionApp.java
    private static boolean servidorConectado = true;



}