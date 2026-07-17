package com.inventory.reportingservice.dto.response;

import java.time.LocalDateTime;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private int status;
    public ApiResponse() { this.timestamp = LocalDateTime.now(); }
    public ApiResponse(boolean success, String message, T data, int status) { this(); this.success = success; this.message = message; this.data = data; this.status = status; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
}
