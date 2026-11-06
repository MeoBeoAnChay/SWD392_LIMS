package com.lims.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ImportReceipt {
    private int receiptId;
    private int handoverId;
    private int requestId;
    private String supplierName;
    private String warehouseStaffName;
    private String managerName;
    private Timestamp receiptDate;
    private Timestamp approvedDate;
    private String status;
    private String note;
    private String cancelReason;
    private boolean paperSigned;
    private List<ImportReceiptItem> items = new ArrayList<>();

    public int getReceiptId() { return receiptId; }
    public void setReceiptId(int receiptId) { this.receiptId = receiptId; }
    public int getHandoverId() { return handoverId; }
    public void setHandoverId(int handoverId) { this.handoverId = handoverId; }
    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getWarehouseStaffName() { return warehouseStaffName; }
    public void setWarehouseStaffName(String warehouseStaffName) { this.warehouseStaffName = warehouseStaffName; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public Timestamp getReceiptDate() { return receiptDate; }
    public void setReceiptDate(Timestamp receiptDate) { this.receiptDate = receiptDate; }
    public Timestamp getApprovedDate() { return approvedDate; }
    public void setApprovedDate(Timestamp approvedDate) { this.approvedDate = approvedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public boolean isPaperSigned() { return paperSigned; }
    public void setPaperSigned(boolean paperSigned) { this.paperSigned = paperSigned; }
    public List<ImportReceiptItem> getItems() { return items; }
    public void setItems(List<ImportReceiptItem> items) { this.items = items == null ? new ArrayList<>() : items; }
}
