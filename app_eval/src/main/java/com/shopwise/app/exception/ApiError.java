package com.shopwise.app.exception;

import java.time.Instant;
import java.util.Map;

public class ApiError {
    private String code;
    private String message;
    private Instant timestamp;
    private String path;
    private Map<String, String> details;

    public ApiError(String code, String message, Instant timestamp,
            String path, Map<String, String> details) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
        this.details = details;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
    public String getPath() { return path; }
    public Map<String, String> getDetails() { return details; }
}
