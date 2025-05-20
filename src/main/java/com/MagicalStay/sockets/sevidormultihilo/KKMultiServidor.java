package com.MagicalStay.sockets.sevidormultihilo;

import java.io.IOException;
import java.net.ServerSocket;

public class KKMultiServidor {

    /**
     * @param args
     */
    public static void main(String[] args) {
        ServerSocket serverSocket = null; // Este socket espera por
        // una conexión entrante
        boolean escuchando = true;

        try {
            serverSocket = new ServerSocket(9999);
            System.out.println("Servidor activo");
            while(escuchando){
               KKMultiServidorHilo hilo = 
                       new KKMultiServidorHilo(serverSocket.accept());
               hilo.start();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }



    }

}
