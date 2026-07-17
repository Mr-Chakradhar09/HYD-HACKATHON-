package com.inventory.inventory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotent_api_requests")
public class IdempotentApiRequest {

    @Id
    private String idempotencyKey;

    private int responseStatus;

    @Lob
    private String responseBody;

    private LocalDateTime createdAt;

    public IdempotentApiRequest() {}

    public IdempotentApiRequest(String idempotencyKey, int responseStatus, String responseBody) {
        this.idempotencyKey = idempotencyKey;
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
        this.createdAt = LocalDateTime.now();
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public int getResponseStatus() { return responseStatus; }
    public void setResponseStatus(int responseStatus) { this.responseStatus = responseStatus; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
