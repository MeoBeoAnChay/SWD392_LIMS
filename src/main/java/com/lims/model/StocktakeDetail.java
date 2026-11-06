package com.lims.model;

public class StocktakeDetail {
    private int detailId, inventoryId, productId;
    private String productCode, productName, serialNumber, systemStatus, actualStatus, systemLocation, actualLocation, discrepancyReason;
    private boolean counted;
    public int getDetailId(){return detailId;} public void setDetailId(int v){detailId=v;}
    public int getInventoryId(){return inventoryId;} public void setInventoryId(int v){inventoryId=v;}
    public int getProductId(){return productId;} public void setProductId(int v){productId=v;}
    public String getProductCode(){return productCode;} public void setProductCode(String v){productCode=v;}
    public String getProductName(){return productName;} public void setProductName(String v){productName=v;}
    public String getSerialNumber(){return serialNumber;} public void setSerialNumber(String v){serialNumber=v;}
    public String getSystemStatus(){return systemStatus;} public void setSystemStatus(String v){systemStatus=v;}
    public String getActualStatus(){return actualStatus;} public void setActualStatus(String v){actualStatus=v;}
    public String getSystemLocation(){return systemLocation;} public void setSystemLocation(String v){systemLocation=v;}
    public String getActualLocation(){return actualLocation;} public void setActualLocation(String v){actualLocation=v;}
    public String getDiscrepancyReason(){return discrepancyReason;} public void setDiscrepancyReason(String v){discrepancyReason=v;}
    public boolean isCounted(){return counted;} public void setCounted(boolean v){counted=v;}
}
