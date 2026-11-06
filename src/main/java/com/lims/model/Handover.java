package com.lims.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Handover {
    private int handoverId;
    private String handoverBy;
    private Timestamp handoverDate;
    private int handoverQuantity;
    private String note;
    private int requestId;
    private String supplierName;
    private int receivedQuantity;
    private String receiptStatus;
    private String location;
    private Integer packageCount;
    private int receiverId;
    private String receiverName;
    private boolean goodsIntact;
    private boolean quantityDiscrepancy;
    private boolean damaged;
    private boolean serialDiscrepancy;
    private Integer importReceiptId;
    private List<ImportRequestItem> items = new ArrayList<>();
    private String purchasingStaffName;
    private String rejectReason;

    public int getHandoverId() { return handoverId; }
    public void setHandoverId(int handoverId) { this.handoverId = handoverId; }
    public String getHandoverBy() { return handoverBy; }
    public void setHandoverBy(String handoverBy) { this.handoverBy = handoverBy; }
    public Timestamp getHandoverDate() { return handoverDate; }
    public void setHandoverDate(Timestamp handoverDate) { this.handoverDate = handoverDate; }
    public int getHandoverQuantity() { return handoverQuantity; }
    public void setHandoverQuantity(int handoverQuantity) { this.handoverQuantity = handoverQuantity; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public int getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(int receivedQuantity) { this.receivedQuantity = receivedQuantity; }
    public String getReceiptStatus() { return receiptStatus; }
    public void setReceiptStatus(String receiptStatus) { this.receiptStatus = receiptStatus; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getPackageCount() { return packageCount; }
    public void setPackageCount(Integer packageCount) { this.packageCount = packageCount; }
    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public boolean isGoodsIntact() { return goodsIntact; }
    public void setGoodsIntact(boolean goodsIntact) { this.goodsIntact = goodsIntact; }
    public boolean isQuantityDiscrepancy() { return quantityDiscrepancy; }
    public void setQuantityDiscrepancy(boolean quantityDiscrepancy) { this.quantityDiscrepancy = quantityDiscrepancy; }
    public boolean isDamaged() { return damaged; }
    public void setDamaged(boolean damaged) { this.damaged = damaged; }
    public boolean isSerialDiscrepancy() { return serialDiscrepancy; }
    public void setSerialDiscrepancy(boolean serialDiscrepancy) { this.serialDiscrepancy = serialDiscrepancy; }
    public Integer getImportReceiptId() { return importReceiptId; }
    public void setImportReceiptId(Integer importReceiptId) { this.importReceiptId = importReceiptId; }
    public List<ImportRequestItem> getItems() { return items; }
    public void setItems(List<ImportRequestItem> items) { this.items = items == null ? new ArrayList<>() : items; }
    public String getPurchasingStaffName() { return purchasingStaffName; }
    public void setPurchasingStaffName(String purchasingStaffName) { this.purchasingStaffName = purchasingStaffName; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}
