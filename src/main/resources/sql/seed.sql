USE LIMS_DB;
GO

-- 1. Insert 5 Roles
SET IDENTITY_INSERT ROLES ON;
INSERT INTO ROLES (RoleID, RoleName, Description) VALUES
(1, 'Admin', 'System Administrator with full access to users, roles, and audit logs'),
(2, 'Manager', 'Warehouse / Inventory Manager with approval permissions and reports access'),
(3, 'Warehouse Staff', 'Warehouse operator handling import, handover, export, and physical inventory checks'),
(4, 'Sales Staff', 'Sales representative creating export requests for customer delivery'),
(5, 'Purchasing Staff', 'Purchasing agent creating import requests for supplier procurement');
SET IDENTITY_INSERT ROLES OFF;
GO

-- 2. Insert Default Users
-- Names assigned according to SE1976-JS(AD) Group 5 Team:
-- 1. Admin: Bùi Tuấn Phong
-- 2. Manager: Nguyễn Văn Mạnh
-- 3. Warehouse Staff: Phạm Huy Thái
-- 4. Sales Staff: Bùi Tuấn Hiệp
-- 5. Purchasing Staff: Nguyễn Tuấn Kiệt
-- Password for all demo accounts: 123456 (BCrypt work factor 10)
SET IDENTITY_INSERT USERS ON;
INSERT INTO USERS (UserID, Username, PasswordHash, FullName, Email, Phone, Status, CreatedAt) VALUES
(1, 'admin', '$2a$10$sZR7mHLsop2ipiE2PZzGouC/VnYLRi9bKzgJCw8pl2TuHgEvdp/Je', N'Bùi Tuấn Phong', 'phongbt@lims.com', '0901234567', 'ACTIVE', GETDATE()),
(2, 'manager', '$2a$10$sZR7mHLsop2ipiE2PZzGouC/VnYLRi9bKzgJCw8pl2TuHgEvdp/Je', N'Nguyễn Văn Mạnh', 'manhnv@lims.com', '0902345678', 'ACTIVE', GETDATE()),
(3, 'warehouse', '$2a$10$sZR7mHLsop2ipiE2PZzGouC/VnYLRi9bKzgJCw8pl2TuHgEvdp/Je', N'Phạm Huy Thái', 'thaiph@lims.com', '0903456789', 'ACTIVE', GETDATE()),
(4, 'sales', '$2a$10$sZR7mHLsop2ipiE2PZzGouC/VnYLRi9bKzgJCw8pl2TuHgEvdp/Je', N'Bùi Tuấn Hiệp', 'hiepbt@lims.com', '0904567890', 'ACTIVE', GETDATE()),
(5, 'purchasing', '$2a$10$sZR7mHLsop2ipiE2PZzGouC/VnYLRi9bKzgJCw8pl2TuHgEvdp/Je', N'Nguyễn Tuấn Kiệt', 'kietnt@lims.com', '0905678901', 'ACTIVE', GETDATE());
SET IDENTITY_INSERT USERS OFF;
GO

-- 3. Assign Roles to Users
INSERT INTO USER_ROLES (UserID, RoleID) VALUES
(1, 1), -- admin -> Admin (Bùi Tuấn Phong)
(2, 2), -- manager -> Manager (Nguyễn Văn Mạnh)
(3, 3), -- warehouse -> Warehouse Staff (Phạm Huy Thái)
(4, 4), -- sales -> Sales Staff (Bùi Tuấn Hiệp)
(5, 5); -- purchasing -> Purchasing Staff (Nguyễn Tuấn Kiệt)
GO

-- 4. Brands (Chỉ lấy Dell và Lenovo)
SET IDENTITY_INSERT BRANDS ON;
INSERT INTO BRANDS (BrandID, BrandName, Status) VALUES
(1, 'Dell', 'ACTIVE'),
(2, 'Lenovo', 'ACTIVE');
SET IDENTITY_INSERT BRANDS OFF;
GO

-- 5. Product Lines
-- Dell: Inspiron, Vostro, Latitude, XPS, G-Series, Alienware, Precision
-- Lenovo: ThinkPad, ThinkBook, IdeaPad, Yoga, Legion, LOQ, Legion Go
SET IDENTITY_INSERT PRODUCT_LINES ON;
INSERT INTO PRODUCT_LINES (ProductLineID, BrandID, LineName) VALUES
-- Dell Lines (1 -> 7)
(1, 1, 'Inspiron'),
(2, 1, 'Vostro'),
(3, 1, 'Latitude'),
(4, 1, 'XPS'),
(5, 1, 'G-Series'),
(6, 1, 'Alienware'),
(7, 1, 'Precision'),
-- Lenovo Lines (8 -> 14)
(8, 2, 'ThinkPad'),
(9, 2, 'ThinkBook'),
(10, 2, 'IdeaPad'),
(11, 2, 'Yoga'),
(12, 2, 'Legion'),
(13, 2, 'LOQ'),
(14, 2, 'Legion Go');
SET IDENTITY_INSERT PRODUCT_LINES OFF;
GO

-- 6. Sample Laptop Products for Dell and Lenovo
SET IDENTITY_INSERT PRODUCTS ON;
INSERT INTO PRODUCTS (ProductID, ProductCode, ProductName, ProductLineID, CPU, RAM, Storage, GPU, Screen, UnitPrice, Status) VALUES
-- Dell Laptops
(1, 'DELL-INSP-3520', N'Dell Inspiron 15 3520 Carbon Black', 1, 'Intel Core i5-1235U', '8GB DDR4', '512GB NVMe SSD', 'Intel Iris Xe', '15.6 FHD 120Hz', 13990000.00, 'ACTIVE'),
(2, 'DELL-VOST-3430', N'Dell Vostro 14 3430 Titan Grey', 2, 'Intel Core i5-1335U', '16GB DDR4', '512GB NVMe SSD', 'Intel Iris Xe', '14.0 FHD IPS', 16500000.00, 'ACTIVE'),
(3, 'DELL-LATI-5440', N'Dell Latitude 5440 Enterprise', 3, 'Intel Core i7-1355U', '16GB DDR5', '512GB NVMe SSD', 'Intel Iris Xe', '14.0 FHD Anti-Glare', 24500000.00, 'ACTIVE'),
(4, 'DELL-XPS-9320', N'Dell XPS 13 Plus 9320 Platinum', 4, 'Intel Core i7-1360P', '16GB LPDDR5', '512GB NVMe SSD', 'Intel Iris Xe', '13.4 OLED 3.5K Touch', 36900000.00, 'ACTIVE'),
(5, 'DELL-G15-5530', N'Dell Gaming G15 5530 Dark Shadow', 5, 'Intel Core i7-13650HX', '16GB DDR5', '1TB NVMe SSD', 'NVIDIA RTX 4060 8GB', '15.6 FHD 165Hz sRGB', 28900000.00, 'ACTIVE'),
(6, 'DELL-ALW-M16-R2', N'Dell Alienware m16 R2 Lunar Silver', 6, 'Intel Core Ultra 7 155H', '32GB DDR5', '1TB NVMe SSD', 'NVIDIA RTX 4070 8GB', '16.0 QHD+ 240Hz', 56900000.00, 'ACTIVE'),
(7, 'DELL-PREC-3581', N'Dell Precision 3581 Workstation', 7, 'Intel Core i7-13800H vPro', '32GB DDR5', '1TB NVMe SSD', 'NVIDIA RTX A1000 6GB', '15.6 FHD IPS 100% sRGB', 42500000.00, 'ACTIVE'),

-- Lenovo Laptops
(8, 'LEN-TP-X1C11', N'Lenovo ThinkPad X1 Carbon Gen 11', 8, 'Intel Core i7-1365U vPro', '16GB LPDDR5', '1TB NVMe SSD', 'Intel Iris Xe', '14.0 2.8K OLED HDR', 42900000.00, 'ACTIVE'),
(9, 'LEN-TB-14-G6', N'Lenovo ThinkBook 14 G6+ ABP', 9, 'AMD Ryzen 7 7730U', '16GB DDR5', '512GB NVMe SSD', 'AMD Radeon 680M', '14.0 2.5K 90Hz', 18900000.00, 'ACTIVE'),
(10, 'LEN-IP-SLIM5', N'Lenovo IdeaPad Slim 5 16IAH8', 10, 'Intel Core i5-12450H', '16GB LPDDR5', '512GB NVMe SSD', 'Intel UHD Graphics', '16.0 WUXGA IPS', 15400000.00, 'ACTIVE'),
(11, 'LEN-YOGA-9-14', N'Lenovo Yoga 9 14IRP8 2-in-1 Oatmeal', 11, 'Intel Core i7-1360P', '16GB LPDDR5', '1TB NVMe SSD', 'Intel Iris Xe', '14.0 4K OLED PureSight Touch', 46900000.00, 'ACTIVE'),
(12, 'LEN-LEG-PRO5', N'Lenovo Legion Pro 5 16IRX9', 12, 'Intel Core i9-14900HX', '32GB DDR5', '1TB NVMe SSD', 'NVIDIA RTX 4070 8GB', '16.0 WQXGA 240Hz HDR', 49990000.00, 'ACTIVE'),
(13, 'LEN-LOQ-15IRH8', N'Lenovo LOQ 15IRH8 Storm Grey', 13, 'Intel Core i5-13420H', '16GB DDR5', '512GB NVMe SSD', 'NVIDIA RTX 4050 6GB', '15.6 FHD 144Hz G-Sync', 21500000.00, 'ACTIVE'),
(14, 'LEN-LEG-GO', N'Lenovo Legion Go Handheld PC', 14, 'AMD Ryzen Z1 Extreme', '16GB LPDDR5X', '512GB NVMe SSD', 'AMD Radeon 780M', '8.8 QHD+ 144Hz Touch', 19990000.00, 'ACTIVE');
SET IDENTITY_INSERT PRODUCTS OFF;
GO

-- 7. Suppliers
SET IDENTITY_INSERT SUPPLIERS ON;
INSERT INTO SUPPLIERS (SupplierID, SupplierName, ContactName, Email, Phone, Address, Status) VALUES
(1, N'Công ty TNHH Phân Phối Synnex FPT', N'Trần Hải Đăng', 'contact@synnexfpt.com.vn', '02473006666', N'Tòa nhà FPT Cầu Giấy, Hà Nội', 'ACTIVE'),
(2, N'Công ty CP Thế Giới Số (Digiworld)', N'Võ Hoàng Quân', 'info@digiworld.com.vn', '02839290059', N'195 Cô Bắc, Quận 1, TP.HCM', 'ACTIVE');
SET IDENTITY_INSERT SUPPLIERS OFF;
GO

-- 8. Customers (CellphoneS, Thế Giới Di Động, Điện Máy Xanh, FPT Shop)
SET IDENTITY_INSERT CUSTOMERS ON;
INSERT INTO CUSTOMERS (CustomerID, CustomerName, Phone, Email, Address) VALUES
(1, N'CellphoneS', '02871088089', 'cskh@cellphones.com.vn', N'350-352 Võ Văn Kiệt, Phường Cô Giang, Quận 1, TP.HCM'),
(2, N'Thế Giới Di Động', '18001060', 'cskh@thegioididong.com', N'128 Trần Quang Khải, Phường Tân Định, Quận 1, TP.HCM'),
(3, N'Điện Máy Xanh', '18001061', 'cskh@dienmayxanh.com', N'Lô T2-1.2, Đường D1, Khu Công Nghệ Cao, TP.Thủ Đức, TP.HCM'),
(4, N'FPT Shop', '18006601', 'fptshop@fpt.com.vn', N'261 - 263 Khánh Hội, Phường 2, Quận 4, TP.HCM');
SET IDENTITY_INSERT CUSTOMERS OFF;
GO

-- 9. Sample Inventory (Serial Numbers mapped to Dell & Lenovo models)
SET IDENTITY_INSERT INVENTORY ON;
INSERT INTO INVENTORY (InventoryID, ProductID, SerialNumber, Location, Status, UpdatedAt) VALUES
-- Dell Inspiron
(1, 1, 'SN-DL-INSP-001', 'A-01-01', 'IN_STOCK', GETDATE()),
(2, 1, 'SN-DL-INSP-002', 'A-01-02', 'IN_STOCK', GETDATE()),
-- Dell XPS
(3, 4, 'SN-DL-XPS-001', 'A-02-01', 'IN_STOCK', GETDATE()),
(4, 4, 'SN-DL-XPS-002', 'A-02-02', 'RESERVED', GETDATE()),
-- Dell Alienware
(5, 6, 'SN-DL-ALW-001', 'B-01-01', 'IN_STOCK', GETDATE()),
-- Lenovo ThinkPad X1
(6, 8, 'SN-LN-TPX1-001', 'C-01-01', 'IN_STOCK', GETDATE()),
(7, 8, 'SN-LN-TPX1-002', 'C-01-02', 'IN_STOCK', GETDATE()),
-- Lenovo Legion Pro 5
(8, 12, 'SN-LN-LEG5-001', 'B-02-01', 'IN_STOCK', GETDATE()),
(9, 12, 'SN-LN-LEG5-002', 'B-02-02', 'EXPORTED', GETDATE()),
-- Lenovo Legion Go
(10, 14, 'SN-LN-LGO-001', 'C-02-01', 'IN_STOCK', GETDATE());
SET IDENTITY_INSERT INVENTORY OFF;
GO
