
package com.MagicalStay.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import java.util.Map;

public abstract class JsonDataResponse {
    protected static final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    protected String createJsonResponse(boolean success, String message, Object data) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", message);
            if (data != null) {
                response.put("data", data);
            }
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return String.format("{\"success\":false,\"message\":\"Error creating JSON response: %s\"}", e.getMessage());
        }
    }
}