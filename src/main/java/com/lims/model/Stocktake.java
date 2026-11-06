package com.lims.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Stocktake {
    private int checkId, assignedTo;
    private String status, note, locationFilter, cancelReason, managerName, staffName;
    private Timestamp checkDate, approvedDate;
    private List<StocktakeDetail> details = new ArrayList<>();
    public int getCheckId(){return checkId;} public void setCheckId(int v){checkId=v;}
    public int getAssignedTo(){return assignedTo;} public void setAssignedTo(int v){assignedTo=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getNote(){return note;} public void setNote(String v){note=v;}
    public String getLocationFilter(){return locationFilter;} public void setLocationFilter(String v){locationFilter=v;}
    public String getCancelReason(){return cancelReason;} public void setCancelReason(String v){cancelReason=v;}
    public String getManagerName(){return managerName;} public void setManagerName(String v){managerName=v;}
    public String getStaffName(){return staffName;} public void setStaffName(String v){staffName=v;}
    public Timestamp getCheckDate(){return checkDate;} public void setCheckDate(Timestamp v){checkDate=v;}
    public Timestamp getApprovedDate(){return approvedDate;} public void setApprovedDate(Timestamp v){approvedDate=v;}
    public List<StocktakeDetail> getDetails(){return details;} public void setDetails(List<StocktakeDetail> v){details=v;}
}
