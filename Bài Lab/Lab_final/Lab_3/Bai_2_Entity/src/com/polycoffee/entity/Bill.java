package com.polycoffee.entity;

import java.time.LocalDateTime;

public class Bill {
    private Integer id;
    private Integer userId;
    private String code;
    private LocalDateTime createdAt;
    private int total;
    private String status;

    public Bill() {}

    public Bill(Integer id, Integer userId, String code, LocalDateTime createdAt, int total, String status) {
        this.id = id;
        this.userId = userId;
        this.code = code;
        this.createdAt = createdAt;
        this.total = total;
        this.status = status;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
