package com.lims.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class InventoryItem {
    private int inventoryId;
    private int productId;
    private String productCode;
    private String productName;
    private String brandName;
    private String lineName;
    private String cpu;
    private String ram;
    private String storage;
    private String gpu;
    private String screen;
    private BigDecimal unitPrice;
    private String serialNumber;
    private String location;
    private String status; // IN_STOCK, RESERVED, EXPORTED, DAMAGED, RETURNED
    private Timestamp updatedAt;

    public int getInventoryId() { return inventoryId; }
    public void setInventoryId(int inventoryId) { this.inventoryId = inventoryId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getLineName() { return lineName; }
    public void setLineName(String lineName) { this.lineName = lineName; }
    public String getCpu() { return cpu; }
    public void setCpu(String cpu) { this.cpu = cpu; }
    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }
    public String getStorage() { return storage; }
    public void setStorage(String storage) { this.storage = storage; }
    public String getGpu() { return gpu; }
    public void setGpu(String gpu) { this.gpu = gpu; }
    public String getScreen() { return screen; }
    public void setScreen(String screen) { this.screen = screen; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
