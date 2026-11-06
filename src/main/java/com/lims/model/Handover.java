package com.lims.model;

import java.sql.Timestamp;

public class Handover {
    private int handoverId;
    private String handoverBy;
    private Timestamp handoverDate;
    private int handoverQuantity;
    private String note;

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
}
