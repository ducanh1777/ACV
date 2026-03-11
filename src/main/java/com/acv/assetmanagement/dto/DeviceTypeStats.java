package com.acv.assetmanagement.dto;

public class DeviceTypeStats {
    private String type;
    private long total;
    private long inStock;
    private long deployed;
    private long backup;

    public DeviceTypeStats(String type) {
        this.type = type != null ? type : "Chưa phân loại";
    }

    public void addTotal(int qty) {
        this.total += qty;
    }

    public void addInStock(int qty) {
        this.inStock += qty;
    }

    public void addDeployed(int qty) {
        this.deployed += qty;
    }

    public void addBackup(int qty) {
        this.backup += qty;
    }

    // Getters
    public String getType() {
        return type;
    }

    public long getTotal() {
        return total;
    }

    public long getInStock() {
        return inStock;
    }

    public long getDeployed() {
        return deployed;
    }

    public long getBackup() {
        return backup;
    }
}
