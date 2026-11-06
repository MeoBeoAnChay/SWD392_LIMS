package com.lims.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ImportRequest {
    private int requestId;
    private int supplierId;
    private String supplierName;
    private Timestamp requestDate;
    private String status;
    private String cancelReason;
    private List<ImportRequestItem> items = new ArrayList<>();
    private List<Handover> handovers = new ArrayList<>();
    private int orderedQuantity;
    private int handedOverQuantity;

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public Timestamp getRequestDate() { return requestDate; }
    public void setRequestDate(Timestamp requestDate) { this.requestDate = requestDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public List<ImportRequestItem> getItems() { return items; }
    public void setItems(List<ImportRequestItem> items) { this.items = items == null ? new ArrayList<>() : items; }
    public List<Handover> getHandovers() { return handovers; }
    public void setHandovers(List<Handover> handovers) { this.handovers = handovers == null ? new ArrayList<>() : handovers; }
    public int getOrderedQuantity() { return orderedQuantity; }
    public void setOrderedQuantity(int orderedQuantity) { this.orderedQuantity = orderedQuantity; }
    public int getHandedOverQuantity() { return handedOverQuantity; }
    public void setHandedOverQuantity(int handedOverQuantity) { this.handedOverQuantity = handedOverQuantity; }
    public int getRemainingQuantity() { return Math.max(orderedQuantity - handedOverQuantity, 0); }
}
