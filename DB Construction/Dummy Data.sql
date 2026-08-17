-- ==========================================
-- 1. PARENT TABLES (No Foreign Key Dependencies)
-- ==========================================

-- Customers
INSERT INTO Customer (Name, Email, Password, Phone, Address) 
VALUES ('Youssef Mohamed', 'yosef3dwy@example.com', '1234', '+201012345678', 'Cairo, Egypt');
INSERT INTO Customer (Name, Email, Password, Phone, Address) 
VALUES ('Ahmed Ali', 'ahmed.a@example.com', '1234', '+201112345678', 'Giza, Egypt');
INSERT INTO Customer (Name, Email, Password, Phone, Address) 
VALUES ('Omar Tariq', 'omar.t@example.com', '1234', '+201212345678', 'Alexandria, Egypt');
INSERT INTO Customer (Name, Email, Password, Phone, Address) 
VALUES ('Sara Samir', 'sara.s@example.com', '1234', '+201512345678', 'Mansoura, Egypt');
INSERT INTO Customer (Name, Email, Password, Phone, Address) 
VALUES ('Khaled Hassan', 'khaled.h@example.com', '1234', '+201098765432', 'Aswan, Egypt');

-- Suppliers
INSERT INTO Supplier (Name, Email, Password, Phone) 
VALUES ('Tech Components ME', 'sales@techcomponents.me', '1234', '+201200000001');
INSERT INTO Supplier (Name, Email, Password, Phone) 
VALUES ('Electro Parts Hub', 'orders@electroparts.com', '1234', '+201100000002');
INSERT INTO Supplier (Name, Email, Password, Phone) 
VALUES ('Global Silicon Dynamics', 'info@globalsilicon.com', '1234', '+201500000003');

-- Admin
INSERT INTO AdminUser (Name, Email, Password)
VALUES ('System Admin', 'admin@example.com', '1234');

-- Warehouses
INSERT INTO Warehouse (TotalCapacity, Location, FreeSpace) 
VALUES (20000, 'Cairo Central Logistics', 15500);
INSERT INTO Warehouse (TotalCapacity, Location, FreeSpace) 
VALUES (10000, 'Alexandria Port Storage', 6200);

-- Products
INSERT INTO Product (Title, "SIZE", Description, Price) 
VALUES ('STM32F446RET6 Microcontroller', 2, 'ARM Cortex-M4 MCU 180 MHz, LQFP-64', 12.50);
INSERT INTO Product (Title, "SIZE", Description, Price) 
VALUES ('Nextion 5.0 inch HMI Display', 10, 'Intelligent LCD Touch Display', 45.00);
INSERT INTO Product (Title, "SIZE", Description, Price) 
VALUES ('CAN Bus Transceiver SN65HVD230', 1, '3.3V CAN Bridge Component', 2.20);
INSERT INTO Product (Title, "SIZE", Description, Price) 
VALUES ('Battery Management System (BMS)', 8, '4S 40A Li-ion Lithium Battery Charger', 18.00);
INSERT INTO Product (Title, "SIZE", Description, Price) 
VALUES ('8 MHz Crystal Oscillator', 1, '20pF 80 Ohms HC-49/US', 0.50);
INSERT INTO Product (Title, "SIZE", Description, Price) 
VALUES ('Raspberry Pi 4 Model B', 12, '8GB RAM Single Board Computer', 85.00);
INSERT INTO Product (Title, "SIZE", Description, Price) 
VALUES ('ESP32-WROOM-32 Module', 3, 'Wi-Fi & Bluetooth IoT Microcontroller', 5.50);
INSERT INTO Product (Title, "SIZE", Description, Price) 
VALUES ('10k Ohm Resistor Pack', 2, '100 pcs Through-Hole Resistors', 1.00);
INSERT INTO Product (Title, "SIZE", Description, Price) 
VALUES ('100uF Electrolytic Capacitor', 1, '16V Radial Capacitor, 10 pcs', 0.80);
INSERT INTO Product (Title, "SIZE", Description, Price) 
VALUES ('Female to Female Jumper Wires', 4, '40 pcs 20cm ribbon cable', 3.00);


-- ==========================================
-- 2. ASSOCIATIVE & CHILD TABLES
-- ==========================================

-- Supply (Linking Suppliers to Products with their wholesale cost)
INSERT INTO Supply (SupplierID, ProductID, Cost) VALUES (1, 1, 9.50);
INSERT INTO Supply (SupplierID, ProductID, Cost) VALUES (1, 3, 1.20);
INSERT INTO Supply (SupplierID, ProductID, Cost) VALUES (1, 5, 0.15);
INSERT INTO Supply (SupplierID, ProductID, Cost) VALUES (2, 2, 35.00);
INSERT INTO Supply (SupplierID, ProductID, Cost) VALUES (2, 4, 12.00);
INSERT INTO Supply (SupplierID, ProductID, Cost) VALUES (3, 6, 75.00);
INSERT INTO Supply (SupplierID, ProductID, Cost) VALUES (3, 7, 4.00);
INSERT INTO Supply (SupplierID, ProductID, Cost) VALUES (3, 8, 0.50);
INSERT INTO Supply (SupplierID, ProductID, Cost) VALUES (3, 9, 0.40);
INSERT INTO Supply (SupplierID, ProductID, Cost) VALUES (3, 10, 1.50);

-- Inventory (Distributing Products into Warehouses)
-- Warehouse 1 (Cairo)
INSERT INTO Inventory (WarehouseID, ProductID, Quantity) VALUES (1, 1, 250);
INSERT INTO Inventory (WarehouseID, ProductID, Quantity) VALUES (1, 2, 50);
INSERT INTO Inventory (WarehouseID, ProductID, Quantity) VALUES (1, 3, 500);
INSERT INTO Inventory (WarehouseID, ProductID, Quantity) VALUES (1, 4, 100);
INSERT INTO Inventory (WarehouseID, ProductID, Quantity) VALUES (1, 5, 1000);
-- Warehouse 2 (Alexandria)
INSERT INTO Inventory (WarehouseID, ProductID, Quantity) VALUES (2, 6, 80);
INSERT INTO Inventory (WarehouseID, ProductID, Quantity) VALUES (2, 7, 300);
INSERT INTO Inventory (WarehouseID, ProductID, Quantity) VALUES (2, 8, 500);
INSERT INTO Inventory (WarehouseID, ProductID, Quantity) VALUES (2, 9, 400);
INSERT INTO Inventory (WarehouseID, ProductID, Quantity) VALUES (2, 10, 250);

-- Carts (Assigning empty carts to customers 1 and 2)
INSERT INTO Cart (CustomerID) VALUES (1);
INSERT INTO Cart (CustomerID) VALUES (2);

-- CartItems (Filling the carts)
INSERT INTO CartItem (CartID, ProductID, Quantity) VALUES (1, 3, 10);
INSERT INTO CartItem (CartID, ProductID, Quantity) VALUES (1, 5, 20);
INSERT INTO CartItem (CartID, ProductID, Quantity) VALUES (2, 6, 1);
INSERT INTO CartItem (CartID, ProductID, Quantity) VALUES (2, 7, 5);

-- Orders
-- Order 1: Delivered
INSERT INTO Orders (CustomerID, Status, OrderDate, DeliveredDate) 
VALUES (1, 'DELIVERED', DATE '2026-06-10', DATE '2026-06-14');
-- Order 2: Pending
INSERT INTO Orders (CustomerID, Status, OrderDate, DeliveredDate) 
VALUES (3, 'PENDING', DATE '2026-08-05', NULL);
-- Order 3: Delivered
INSERT INTO Orders (CustomerID, Status, OrderDate, DeliveredDate) 
VALUES (5, 'DELIVERED', DATE '2026-07-20', DATE '2026-07-22');
-- Order 4: Pending
INSERT INTO Orders (CustomerID, Status, OrderDate, DeliveredDate) 
VALUES (2, 'PENDING', DATE '2026-08-07', NULL);

-- OrderItems (Attaching purchased products to the specific orders)
-- Items for Order 1
INSERT INTO OrderItem (OrderID, ProductID, Quantity, UnitPrice) VALUES (1, 1, 5, 12.50);
INSERT INTO OrderItem (OrderID, ProductID, Quantity, UnitPrice) VALUES (1, 4, 2, 18.00);
-- Items for Order 2
INSERT INTO OrderItem (OrderID, ProductID, Quantity, UnitPrice) VALUES (2, 6, 2, 85.00);
INSERT INTO OrderItem (OrderID, ProductID, Quantity, UnitPrice) VALUES (2, 2, 1, 45.00);
-- Items for Order 3
INSERT INTO OrderItem (OrderID, ProductID, Quantity, UnitPrice) VALUES (3, 8, 5, 1.00);
INSERT INTO OrderItem (OrderID, ProductID, Quantity, UnitPrice) VALUES (3, 9, 5, 0.80);
INSERT INTO OrderItem (OrderID, ProductID, Quantity, UnitPrice) VALUES (3, 10, 2, 3.00);
-- Items for Order 4
INSERT INTO OrderItem (OrderID, ProductID, Quantity, UnitPrice) VALUES (4, 7, 10, 5.50);
