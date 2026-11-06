package com.lims.model;

import java.math.BigDecimal;

public class ProductOption {
    private int productId;
    private String productCode;
    private String productName;
    private BigDecimal unitPrice;

    public ProductOption(int productId, String productCode, String productName, BigDecimal unitPrice) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.unitPrice = unitPrice;
    }
    public int getProductId() { return productId; }
    public String getProductCode() { return productCode; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
