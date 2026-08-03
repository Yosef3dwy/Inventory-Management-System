-- Insert Dummy Customers
INSERT INTO Customer (CustomerID, Name, Email, Password, Phone, Address) VALUES 
(1, 'Alice Johnson', 'alice@example.com', 'hashed_pw_1', '555-0101', '123 Maple St, NY'),
(2, 'Bob Smith', 'bob@example.com', 'hashed_pw_2', '555-0102', '456 Oak Ave, CA'),
(3, 'Charlie Brown', 'charlie@example.com', 'hashed_pw_3', '555-0103', '789 Pine Rd, TX');

-- Insert Dummy Suppliers
INSERT INTO Supplier (SupplierID, Name, Email, Password, Phone) VALUES 
(1, 'Tech Supplies Inc.', 'contact@techsupplies.com', 'sup_pw_1', '800-555-0001'),
(2, 'Global Electronics', 'sales@globalelec.com', 'sup_pw_2', '800-555-0002');

-- Insert Dummy Products
INSERT INTO Product (ProductID, Title, Size, Description, Price) VALUES 
(101, 'Wireless Mouse', 'Standard', 'Ergonomic wireless mouse', 25.99),
(102, 'Mechanical Keyboard', 'Full Size', 'RGB mechanical keyboard with blue switches', 89.99),
(103, '27-inch Monitor', '27 inch', '4K IPS Display Monitor', 350.00);

-- Insert Dummy Warehouses
INSERT INTO Warehouse (WarehouseID, TotalCapacity, Location, FreeSpace) VALUES 
(1, 5000, 'New York Facility', 3500),
(2, 10000, 'Los Angeles Hub', 8200);

-- Insert Dummy Orders
-- Notice that CustomerIDs (1, 2) match the Customer table
INSERT INTO "Order" (OrderID, CustomerID, Status, OrderDate, DeliveredDate) VALUES 
(1001, 1, 'Delivered', '2023-10-01', '2023-10-05'),
(1002, 2, 'Shipped', '2023-10-15', NULL),
(1003, 1, 'Pending', '2023-10-20', NULL);

-- Insert Dummy Carts
INSERT INTO Cart (CartID, CustomerID) VALUES 
(1, 1),
(2, 2),
(3, 3);

-- Insert Dummy Order Items
-- Notice that OrderIDs (1001, 1002) and ProductIDs (101, 102, 103) match
INSERT INTO OrderItem (OrderID, ProductID, Quantity, UnitPrice) VALUES 
(1001, 101, 2, 25.99),
(1001, 102, 1, 89.99),
(1002, 103, 1, 350.00);

-- Insert Dummy Cart Items
-- Matching CartIDs (1, 3) and ProductIDs (101, 103)
INSERT INTO CartItem (CartID, ProductID, Quantity) VALUES 
(1, 103, 1),
(3, 101, 3);

-- Insert Dummy Supplies (Historical supply records)
-- Matching SupplierIDs (1, 2) and ProductIDs (101, 102, 103)
INSERT INTO Supply (SupplierID, ProductID, SupplyDate, Quantity, Cost) VALUES 
(1, 101, '2023-09-01', 500, 10.00),
(1, 102, '2023-09-05', 200, 45.00),
(2, 103, '2023-09-10', 100, 250.00);

-- Insert Dummy Inventory
-- Reflecting the current stock in Warehouses (1, 2) for Products (101, 102, 103)
INSERT INTO Inventory (WarehouseID, ProductID, Quantity) VALUES 
(1, 101, 350),
(1, 102, 150),
(2, 103, 80);