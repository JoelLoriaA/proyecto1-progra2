package com.MagicalStay.client.ui.controllers;

import com.MagicalStay.client.sockets.SocketCliente;
import com.MagicalStay.client.data.DataFactory;
import com.MagicalStay.server.FileTransferService;
import com.MagicalStay.shared.config.ConfiguracionApp;
import com.MagicalStay.shared.data.HotelData;
import com.MagicalStay.shared.data.JsonResponse;
import com.MagicalStay.shared.data.RoomData;
import com.MagicalStay.shared.domain.*;
import com.MagicalStay.shared.util.FXUtility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.Closeable;
import javafx.application.Platform;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;


public class RoomManagementController implements Closeable {
    @FXML private ComboBox<Hotel> hotelComboBox;
    @FXML private TextField searchTextField;
    @FXML public TextField numberTextField;
    @FXML public TextField priceTextField;
    @FXML public ComboBox<RoomType> typeComboBox;
    @FXML public ComboBox<RoomCondition> statusComboBox;
    @FXML public TextArea descriptionTextArea;
    @FXML public TextArea featuresTextArea;
    @FXML public Spinner<Integer> capacitySpinner;
    @FXML private Button addButton, editButton, deleteButton;
    @FXML private Button closeButton;
    @FXML public Button saveButton;
    @FXML public Button cancelButton;
    @FXML private Button searchButton;
    @FXML public Label statusLabel; 
    @FXML public ListView<Room> roomListView;
    @FXML private TableColumn<Room, String> roomNumberColumn, roomTypeColumn, roomStatusColumn;
    @FXML private TableColumn<Room, Integer> roomCapacityColumn;
    @FXML private TableColumn<Room, Double> roomPriceColumn;
    @FXML private Button selectImageButton;
    @FXML private ImageView roomImageView;
    private File selectedImageFile;
    public String selectedImagePath;
    public RoomData roomData;
    private HotelData hotelData; 
    public static ObjectMapper objectMapper;
    public static ObservableList<Room> roomList = FXCollections.observableArrayList();
    private ObservableList<Hotel> hotelList = FXCollections.observableArrayList(); 
    public Room selectedRoom;
    public Hotel selectedHotel;
    public boolean editMode = false;
    private final SocketCliente socketCliente;
    @FXML
    private TableView imagesTableView;
    @FXML
    private TableColumn imagePathColumn;
    @FXML
    private TableColumn imageNameColumn;
  

    public RoomManagementController() {
        socketCliente = new SocketCliente(new SocketCliente.ClienteCallback() {
            @Override public void onMensajeRecibido(String mensaje) {
                Platform.runLater(() -> procesarRespuestaServidor(mensaje));
            }
            @Override public void onError(String error) {
                Platform.runLater(() -> FXUtility.alertError("Error de comunicación", error).show());
            }
            @Override public void onConexionEstablecida() {
                Platform.runLater(() -> loadRoomsFromServer());
            }
            @Override public void onDesconexion() {
                Platform.runLater(() -> FXUtility.alertError("Desconexión", "Se perdió la conexión con el servidor").show());
            }
        });
    }

    @FXML
    private void initialize() {
        try {
            roomData = DataFactory.getRoomData();
            hotelData = DataFactory.getHotelData(); 
            objectMapper = new ObjectMapper();

            System.out.println("Ruta del archivo: " + new File("rooms.dat").getAbsolutePath());

            setupControls();
            setFieldsEnabled(false);
            roomListView.setItems(roomList);

            hotelComboBox.setItems(hotelList); 
            hotelComboBox.setConverter(new StringConverter<Hotel>() {
                @Override public String toString(Hotel hotel) {
                    return hotel != null ? hotel.getName() : "";
                }

                @Override public Hotel fromString(String string) {
                    return null;
                }
            });
            hotelComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                selectedHotel = newVal;
                loadRoomsFromFile();
                roomImageView.setImage(null);
            });

            loadHotels(); 

            roomListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                selectedRoom = newVal;
                handleRoomSelection();
            });

            roomListView.setCellFactory(listView -> new ListCell<Room>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                if (empty || room == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String priceString = priceTextField.getText();
                    Label number = new Label("🛏️ " + room.getRoomNumber());
                    number.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                    Label type = new Label("Tipo: " + room.getRoomType());
                    Label capacity = new Label("Capacidad: " + room.getCapacity());
                    Label status = new Label("Estado: " + room.getRoomCondition());
                    Label price = new Label(String.format("Precio: 💲%.2f", room.getPrice()));                    price.setStyle("-fx-font-weight: bold;");

                    VBox leftBox = new VBox(number, type, capacity);
                    leftBox.setSpacing(2);

                    VBox rightBox = new VBox(status, price);
                    rightBox.setSpacing(2);
                    rightBox.setAlignment(Pos.CENTER_RIGHT);

                    HBox hBox = new HBox(leftBox, new Region(), rightBox);
                    HBox.setHgrow(hBox.getChildren().get(1), Priority.ALWAYS); 
                    hBox.setSpacing(10);
                    hBox.setPadding(new Insets(5));
                    hBox.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 5px;");

                    setGraphic(hBox);
                }
            }
        });

            searchTextField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());

        } catch (Exception e) {
            FXUtility.alertError("Error de Inicialización", "No se pudieron cargar los datos: " + e.getMessage()).show();
        }
    }
    

    private void loadHotels() {
        try {
            String json = hotelData.retrieveAll();
            JsonResponse response = objectMapper.readValue(json, JsonResponse.class);
            if (response.isSuccess()) {
                List<Hotel> hotels = objectMapper.convertValue(response.getData(), new TypeReference<List<Hotel>>() {});
                hotelList.setAll(hotels);
    
                if (!hotelList.isEmpty()) {
                    hotelComboBox.getSelectionModel().selectFirst(); 
                }
            }
        } catch (Exception e) {
            FXUtility.alertError("Error", "No se pudieron cargar los hoteles: " + e.getMessage()).show();
        }
    }
    

    private void setupControls() {
        typeComboBox.setItems(FXCollections.observableArrayList(RoomType.values()));
        statusComboBox.setItems(FXCollections.observableArrayList(RoomCondition.values()));
        capacitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
    }

    public void setSelectedHotel(Hotel hotel) {
        this.selectedHotel = hotel;
        loadRoomsFromFile();
    }

    public void loadRoomsFromFile() {
    if (selectedHotel == null) {
        FXUtility.alertError("Error", "No hay hotel seleccionado.").show();
        return;
    }

    try {
        String jsonResponse = roomData.readAll();
        DataResponse response = parseDataResponse(jsonResponse);

        if (response.isSuccess()) {
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> roomMaps = (List<Map<String, Object>>) response.getData();
            List<Room> allRooms = new ArrayList<>();

            for (Map<String, Object> map : roomMaps) {
                Room room = objectMapper.convertValue(map, Room.class);
                allRooms.add(room);
            }

            System.out.println("Habitaciones encontradas: " + allRooms.size());

            List<Room> filteredRooms = allRooms.stream()
                .filter(r -> r.getHotel() != null && r.getHotel().getHotelId() == selectedHotel.getHotelId())
                .collect(Collectors.toList());

            System.out.println("Habitaciones filtradas para hotel ID " + selectedHotel.getHotelId() + ": " + filteredRooms.size());

            roomList.setAll(filteredRooms);
        } else {
            roomList.clear();
            statusLabel.setText("No se encontraron habitaciones: " + response.getMessage());
        }
    } catch (Exception e) {
        roomList.clear();
        statusLabel.setText("Error al cargar habitaciones: " + e.getMessage());
        e.printStackTrace();
    }
}

    @FXML
    private void handleAddRoom() {
        clearFields();
        setFieldsEnabled(true);
        editMode = false;
        statusLabel.setText("Agregando nueva habitación...");
    }

    @FXML
    private void handleEditRoom() {
        if (selectedRoom != null) {
            setFieldsEnabled(true);
            editMode = true;
            statusLabel.setText("Editando habitación...");
        }
    }

    @FXML
    private void handleDeleteRoom() {
        if (selectedRoom == null) {
            FXUtility.alertError("Error", "No hay habitación seleccionada.").show();
            return;
        }
    
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText(null);
        alert.setContentText("¿Está seguro que desea eliminar la habitación \"" +
                             selectedRoom.getRoomNumber() + "\"?");
    
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String jsonResponse = roomData.delete(selectedRoom.getRoomNumber());
                DataResponse response = parseDataResponse(jsonResponse);
    
                if (response.isSuccess()) {
                    loadRoomsFromFile();
                    roomListView.setItems(roomList);
                    clearFields();

                    roomImageView.setImage(null);

    
                    selectedRoom = null;
                    statusLabel.setText("Habitación eliminada con éxito.");
    
                    editButton.setDisable(true);
                    deleteButton.setDisable(true);
                    

                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "No se pudo eliminar la habitación: " + response.getMessage());
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Error al eliminar la habitación: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }    
    
    @FXML
    public void handleSave() {

        if (selectedHotel == null || hotelComboBox.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Debe seleccionar un hotel antes de guardar.");
                return;
        }
        if (!validateFields()) return;

        if (!validateFields()) return;

        if (selectedHotel == null) {
            FXUtility.alertError("Error", "No hay hotel seleccionado.").show();
            return;
        }
        
        try {
            Room room = new Room(
                numberTextField.getText(),
                selectedHotel,
                typeComboBox.getValue(),
                statusComboBox.getValue(),
                Double.parseDouble(priceTextField.getText()),
                capacitySpinner.getValue(),
                featuresTextArea.getText(),
                descriptionTextArea.getText(),
                selectedImagePath
            );

            String jsonResponse;
                    if (editMode) {
                        jsonResponse = roomData.update(room);
                    } else {
                        jsonResponse = roomData.create(room);
                    }

            DataResponse response = parseDataResponse(jsonResponse);

            if (response.isSuccess()) {
                loadRoomsFromFile();
                roomListView.setItems(roomList);
                sincronizarArchivos();

                setFieldsEnabled(false);
                clearFields();
                saveButton.setDisable(true);
                cancelButton.setDisable(true);

                for (Room r : roomList) {
                    if (r.getRoomNumber().equals(room.getRoomNumber())
                        && r.getHotel().getHotelId() == room.getHotel().getHotelId()) {
                        roomListView.getSelectionModel().select(r);
                        break;
                    }
                }

                statusLabel.setText("Habitación guardada con éxito.");

                System.out.println("[handleSave] Habitación guardada: " + room.getRoomNumber() +
                                " | Hotel ID: " + room.getHotel().getHotelId() +
                                " | Tipo: " + room.getRoomType() +
                                " | Estado: " + room.getRoomCondition() + 
                                " | Path " + room.getImagePath());

                System.out.println("[handleSave] Lista actual de habitaciones:");
                for (Room r : roomList) {
                    System.out.println("- " + r.getRoomNumber() + " | hotelId: " +
                        (r.getHotel() != null ? r.getHotel().getHotelId() : "null"));
                }

            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudo guardar la habitación: " + response.getMessage());
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                "Error al guardar la habitación: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void handleCancel() {
        clearFields();
        setFieldsEnabled(false);
        selectedRoom = null;
        editMode = false;
        statusLabel.setText("Operación cancelada.");
    }

    @FXML
    private void handleRoomSelection() {
        if (selectedRoom != null) {
            numberTextField.setText(selectedRoom.getRoomNumber());
            typeComboBox.setValue(selectedRoom.getRoomType());
            statusComboBox.setValue(selectedRoom.getRoomCondition());
            descriptionTextArea.setText(selectedRoom.getDescription());
            featuresTextArea.setText(selectedRoom.getFeatures());
            priceTextField.setText(String.valueOf(selectedRoom.getPrice()));
            capacitySpinner.getValueFactory().setValue(selectedRoom.getCapacity());
            editButton.setDisable(false);
            deleteButton.setDisable(false);

            if (selectedRoom.getImagePath() != null && !selectedRoom.getImagePath().isEmpty()) {
                File imageFile = new File(selectedRoom.getImagePath());
                if (imageFile.exists()) {
                    roomImageView.setImage(new Image(imageFile.toURI().toString()));
                } else {
                    roomImageView.setImage(null); 
                }
            } else {
                roomImageView.setImage(null);
            }

            selectedImagePath = selectedRoom.getImagePath();           

            editButton.setDisable(false);
            deleteButton.setDisable(false);
        }
    }
    
    private void clearFields() {
        numberTextField.clear();
        typeComboBox.setValue(null);
        statusComboBox.setValue(null);
        descriptionTextArea.clear();
        featuresTextArea.clear();
        priceTextField.clear();
        capacitySpinner.getValueFactory().setValue(1);
        selectedImagePath = null;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void setFieldsEnabled(boolean enabled) {
        numberTextField.setDisable(!enabled);
        typeComboBox.setDisable(!enabled);
        statusComboBox.setDisable(!enabled);
        descriptionTextArea.setDisable(!enabled);
        featuresTextArea.setDisable(!enabled);
        priceTextField.setDisable(!enabled);
        capacitySpinner.setDisable(!enabled);
        saveButton.setDisable(!enabled);
        cancelButton.setDisable(!enabled);
        addButton.setDisable(enabled);
        selectImageButton.setDisable(!enabled);
    }

    private boolean validateFields() {
        StringBuilder sb = new StringBuilder();

        // Validar hotel seleccionado primero
        if (selectedHotel == null || hotelComboBox.getValue() == null) {
            sb.append("Debe seleccionar un hotel.\n");
        }
        if (numberTextField.getText().isEmpty()) {
            sb.append("Número de habitación obligatorio.\n");
        }
        if (typeComboBox.getValue() == null) {
            sb.append("Tipo de habitación obligatorio.\n");
        }
        if (statusComboBox.getValue() == null) {
            sb.append("Estado de habitación obligatorio.\n");
        }
        if (priceTextField.getText().isEmpty() || !priceTextField.getText().matches("\\d+(\\.\\d+)?")) {
            sb.append("Precio debe ser un número válido.\n");
        }

        if (sb.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Campos Inválidos", sb.toString());
            return false;
        }
        return true;
    }

    @FXML
    private void handleSearch() {
        String query = searchTextField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            roomListView.setItems(roomList);
        } else {
            ObservableList<Room> filtered = roomList.filtered(room ->
                room.getRoomNumber().toLowerCase().contains(query) ||
                room.getRoomType().name().toLowerCase().contains(query) ||
                room.getRoomCondition().name().toLowerCase().contains(query) ||
                String.valueOf(room.getCapacity()).contains(query) ||
                String.valueOf(room.getPrice()).contains(query)
            );
            roomListView.setItems(filtered);
        }
    }


    private void procesarRespuestaServidor(String respuesta) {
        try {
            JsonResponse response = objectMapper.readValue(respuesta, JsonResponse.class);
            if (response.isSuccess()) {
                List<Room> rooms = (List<Room>) response.getData();
                actualizarTablaHabitaciones(rooms);
            } else {
                FXUtility.alertError("Error", response.getMessage()).show();
            }
        } catch (Exception e) {
            FXUtility.alertError("Error", "Respuesta inválida del servidor: " + e.getMessage()).show();
        }
    }

    private void actualizarTablaHabitaciones(List<Room> rooms) {
        roomList.setAll(rooms);
        roomListView.setItems(roomList);
        roomListView.refresh();
        statusLabel.setText(rooms.isEmpty() ? "Sin habitaciones." : "");
    }

    private void loadRoomsFromServer() {
        if (!socketCliente.estaConectado()) {
            FXUtility.alertError("Error", "Sin conexión al servidor").show();
            return;
        }
        socketCliente.enviarMensaje("OBTENER_HABITACIONES|" + selectedHotel.getHotelId());
    }

    @FXML
    public void handleClose(ActionEvent e) throws IOException {
        DataFactory.closeAll();

        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void close() {
        try {
            if (roomData != null) roomData.close();
            if (socketCliente != null) socketCliente.desconectar();
        } catch (Exception e) {
            FXUtility.alertError("Error", "Error al cerrar: " + e.getMessage()).show();
        }
    }

    private DataResponse parseDataResponse(String jsonResponse) throws Exception {
        return objectMapper.readValue(jsonResponse, DataResponse.class);
    }

    private static class DataResponse {
        private boolean success;
        private String message;
        private Object data;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Habitación");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );

        File initialDir = new File(ConfiguracionApp.RUTA_IMAGENES_SERVIDOR);
        if (!initialDir.exists()) {
            initialDir.mkdirs();
        }
        fileChooser.setInitialDirectory(initialDir);

        Stage stage = (Stage) selectImageButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                String newFileName = "habitacion_" + System.currentTimeMillis() + extension;
                File destFile = new File(ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR, newFileName);

                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                roomImageView.setImage(new Image(destFile.toURI().toString()));

                selectedImagePath = ConfiguracionApp.RUTA_COPIA_IMAGENES_SERVIDOR + newFileName;

                System.out.println("[Imagen seleccionada] Ruta guardada: " + selectedImagePath);

            } catch (IOException e) {
                FXUtility.alertError("Error", "No se pudo copiar la imagen: " + e.getMessage()).show();
                e.printStackTrace();
            }
        }
    }

    private void sincronizarArchivos() {
        try {
            // Sincronizar archivo DAT
            File datFile = new File(ConfiguracionApp.RUTA_ARCHIVOS_SERVIDOR + "rooms.dat");
            if (datFile.exists()) {
                byte[] datComprimido = FileTransferService.compressFile(datFile);
                socketCliente.enviarMensaje("sincronizar_dat|rooms.dat");
                socketCliente.enviarObjeto(datComprimido);
            }

            // Sincronizar imagen si existe
            if (selectedImagePath != null) {
                File imageFile = new File(selectedImagePath);
                if (imageFile.exists()) {
                    byte[] imageData = Files.readAllBytes(imageFile.toPath());
                    String imageName = imageFile.getName();
                    socketCliente.enviarMensaje("sincronizar_imagen|" + imageName);
                    socketCliente.enviarObjeto(imageData);
                }
            }
        } catch (Exception e) {
            FXUtility.alertError("Error", "Error al sincronizar archivos: " + e.getMessage()).show();
        }
    }

}

