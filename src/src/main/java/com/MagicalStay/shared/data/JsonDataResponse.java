package com.MagicalStay.shared.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class JsonDataResponse {

    private final ObjectMapper objectMapper;

    public JsonDataResponse() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Crea una respuesta JSON estandarizada
     * @param success indica si la operación fue exitosa
     * @param message mensaje descriptivo
     * @param data datos de respuesta (puede ser null)
     * @return JSON string con la respuesta
     */
    protected String createJsonResponse(boolean success, String message, Object data) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", success);
            response.put("message", message);

            if (data != null) {
                response.set("data", objectMapper.valueToTree(data));
            } else {
                response.putNull("data");
            }

            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            // En caso de error al crear el JSON, retornar una respuesta de error
            return "{\"success\":false,\"message\":\"Error al crear respuesta JSON: " +
                    e.getMessage().replace("\"", "'") + "\",\"data\":null}";
        }
    }

    /**
     * Crea una respuesta de error simple
     * @param message mensaje de error
     * @return JSON string con error
     */
    protected String createErrorResponse(String message) {
        return createJsonResponse(false, message, null);
    }

    /**
     * Crea una respuesta de éxito simple
     * @param message mensaje de éxito
     * @return JSON string con éxito
     */
    protected String createSuccessResponse(String message) {
        return createJsonResponse(true, message, null);
    }

    /**
     * Crea una respuesta de éxito con datos
     * @param message mensaje de éxito
     * @param data datos a incluir
     * @return JSON string con éxito y datos
     */
    protected String createSuccessResponse(String message, Object data) {
        return createJsonResponse(true, message, data);
    }
}