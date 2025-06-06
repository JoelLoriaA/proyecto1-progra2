
package com.MagicalStay.client.sockets;

import java.io.*;
import java.util.List;

public class FileClient {
    private final SocketCliente socketCliente;

    public FileClient(SocketCliente socketCliente) {
        this.socketCliente = socketCliente;
    }

   // En FileClient.java
    public void subirArchivo(String nombre, byte[] datos) {
        socketCliente.enviarMensaje("subirarchivo|" + nombre);
        socketCliente.enviarObjeto(datos); // Envía el archivo como byte[]
    }

    public void descargarArchivo(String nombre) {
        socketCliente.enviarMensaje("descargararchivo|" + nombre);
        // Recibir el archivo como byte[]
    }

    public void listarArchivos() {
        socketCliente.enviarMensaje("listararchivos");
        // Recibir la lista de archivos
    }
}
