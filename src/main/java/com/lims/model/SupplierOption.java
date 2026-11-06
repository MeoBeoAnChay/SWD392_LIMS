package com.lims.model;

public class SupplierOption {
    private int supplierId;
    private String supplierName;

    public SupplierOption(int supplierId, String supplierName) { this.supplierId = supplierId; this.supplierName = supplierName; }
    public int getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
}
