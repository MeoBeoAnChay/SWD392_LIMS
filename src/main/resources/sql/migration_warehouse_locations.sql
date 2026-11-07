/* Warehouse location directory. Safe to run repeatedly. */
IF OBJECT_ID('WAREHOUSE_LOCATIONS','U') IS NULL
BEGIN
    CREATE TABLE WAREHOUSE_LOCATIONS (
        LocationID INT IDENTITY(1,1) PRIMARY KEY,
        LocationName NVARCHAR(50) NOT NULL UNIQUE,
        CreatedAt DATETIME NOT NULL CONSTRAINT DF_WarehouseLocations_CreatedAt DEFAULT GETDATE()
    );
END;
GO

/* Import existing inventory locations into the directory. */
INSERT INTO WAREHOUSE_LOCATIONS(LocationName)
SELECT DISTINCT i.Location
FROM INVENTORY i
WHERE NULLIF(LTRIM(RTRIM(i.Location)), '') IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM WAREHOUSE_LOCATIONS l WHERE l.LocationName=i.Location);
GO
