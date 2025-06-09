//// src/main/java/com/MagicalStay/client/sockets/DirectoryWatcher.java
//package com.MagicalStay.client.sockets;
//
//import java.io.IOException;
//import java.nio.file.*;
//
//public class DirectoryWatcher implements Runnable {
//    private final Path pathToWatch;
//    private final FileClient fileClient;
//    private final boolean esImagen;
//
//    public DirectoryWatcher(String dir, FileClient fileClient, boolean esImagen) {
//        this.pathToWatch = Paths.get(dir);
//        this.fileClient = fileClient;
//        this.esImagen = esImagen;
//    }
//
//    @Override
//    public void run() {
//        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
//            pathToWatch.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
//                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
//
//            while (true) {
//                WatchKey key = watchService.take();
//                for (WatchEvent<?> event : key.pollEvents()) {
//                    Path changed = pathToWatch.resolve((Path) event.context());
//                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE ||
//                            event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
//                        byte[] datos = Files.readAllBytes(changed);
//                        fileClient.subirArchivo(changed.getFileName().toString(), datos, esImagen);
//                    }
//                    // Puedes manejar eliminaciones aquí si lo deseas
//                }
//                key.reset();
//            }
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}