-- Extends handover records so Purchasing can assign Warehouse Staff and print the
-- physical receiving form. Safe to run more than once.
IF COL_LENGTH('HANDOVERS', 'ReceivedBy') IS NULL
    ALTER TABLE HANDOVERS ADD ReceivedBy INT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_Handovers_Receiver')
    ALTER TABLE HANDOVERS ADD CONSTRAINT FK_Handovers_Receiver FOREIGN KEY (ReceivedBy) REFERENCES USERS(UserID);
GO

IF COL_LENGTH('HANDOVERS', 'HandoverLocation') IS NULL
    ALTER TABLE HANDOVERS ADD HandoverLocation NVARCHAR(255) NULL;
GO

IF COL_LENGTH('HANDOVERS', 'PackageCount') IS NULL
    ALTER TABLE HANDOVERS ADD PackageCount INT NULL;
GO

IF COL_LENGTH('HANDOVERS', 'GoodsIntact') IS NULL
    ALTER TABLE HANDOVERS ADD GoodsIntact BIT NOT NULL CONSTRAINT DF_Handovers_GoodsIntact DEFAULT 0;
GO

IF COL_LENGTH('HANDOVERS', 'QuantityDiscrepancy') IS NULL
    ALTER TABLE HANDOVERS ADD QuantityDiscrepancy BIT NOT NULL CONSTRAINT DF_Handovers_QuantityDiscrepancy DEFAULT 0;
GO

IF COL_LENGTH('HANDOVERS', 'Damaged') IS NULL
    ALTER TABLE HANDOVERS ADD Damaged BIT NOT NULL CONSTRAINT DF_Handovers_Damaged DEFAULT 0;
GO

IF COL_LENGTH('HANDOVERS', 'SerialDiscrepancy') IS NULL
    ALTER TABLE HANDOVERS ADD SerialDiscrepancy BIT NOT NULL CONSTRAINT DF_Handovers_SerialDiscrepancy DEFAULT 0;
GO

IF COL_LENGTH('HANDOVERS', 'Note') IS NOT NULL
    ALTER TABLE HANDOVERS ALTER COLUMN Note NVARCHAR(1000) NULL;
GO

IF OBJECT_ID('HANDOVER_DETAILS', 'U') IS NULL
BEGIN
    CREATE TABLE HANDOVER_DETAILS (
        HandoverDetailID INT IDENTITY(1,1) PRIMARY KEY,
        HandoverID INT NOT NULL,
        ProductID INT NOT NULL,
        Quantity INT NOT NULL CHECK (Quantity > 0),
        Note NVARCHAR(255) NULL,
        CONSTRAINT FK_HandoverDetails_Handovers FOREIGN KEY (HandoverID) REFERENCES HANDOVERS(HandoverID),
        CONSTRAINT FK_HandoverDetails_Products FOREIGN KEY (ProductID) REFERENCES PRODUCTS(ProductID)
    );
END;
GO
