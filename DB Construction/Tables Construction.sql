-- 1. Create Independent Entity Tables

CREATE TABLE Customer (
    CustomerID INT PRIMARY KEY,
    Name VARCHAR(255) NOT NULL,
    Email VARCHAR(255) UNIQUE NOT NULL,
    Password VARCHAR(255) NOT NULL,
    Phone VARCHAR(50),
    Address VARCHAR(255)
);

CREATE TABLE Supplier (
    SupplierID INT PRIMARY KEY,
    Name VARCHAR(255) NOT NULL,
    Email VARCHAR(255) UNIQUE NOT NULL,
    Password VARCHAR(255) NOT NULL,
    Phone VARCHAR(50)
);

CREATE TABLE Product (
    ProductID INT PRIMARY KEY,
    Title VARCHAR(255) NOT NULL,
    Size INT,
    Description TEXT,
    Price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE Warehouse (
    WarehouseID INT PRIMARY KEY,
    TotalCapacity INT NOT NULL,
    Location VARCHAR(255) NOT NULL,
    FreeSpace INT NOT NULL
);

-- 2. Create Dependent Entity Tables (Entities with Foreign Keys)

CREATE TABLE "Order" (
    OrderID INT PRIMARY KEY,
    CustomerID INT NOT NULL,
    Status VARCHAR(50) NOT NULL,
    OrderDate DATE NOT NULL,
    DeliveredDate DATE,
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)
);

CREATE TABLE Cart (
    CartID INT PRIMARY KEY,
    CustomerID INT NOT NULL,
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)
);

-- 3. Create JOIN / Associative Tables

CREATE TABLE OrderItem (
    OrderID INT NOT NULL,
    ProductID INT NOT NULL,
    Quantity INT NOT NULL,
    UnitPrice DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (OrderID, ProductID),
    FOREIGN KEY (OrderID) REFERENCES "Order"(OrderID),
    FOREIGN KEY (ProductID) REFERENCES Product(ProductID)
);

CREATE TABLE CartItem (
    CartID INT NOT NULL,
    ProductID INT NOT NULL,
    Quantity INT NOT NULL,
    PRIMARY KEY (CartID, ProductID),
    FOREIGN KEY (CartID) REFERENCES Cart(CartID),
    FOREIGN KEY (ProductID) REFERENCES Product(ProductID)
);

CREATE TABLE Supply (
    SupplierID INT NOT NULL,
    ProductID INT NOT NULL,
    Cost DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (SupplierID, ProductID, SupplyDate),
    FOREIGN KEY (SupplierID) REFERENCES Supplier(SupplierID),
    FOREIGN KEY (ProductID) REFERENCES Product(ProductID)
);

CREATE TABLE Inventory (
    WarehouseID INT NOT NULL,
    ProductID INT NOT NULL,
    Quantity INT NOT NULL,
    PRIMARY KEY (WarehouseID, ProductID),
    FOREIGN KEY (WarehouseID) REFERENCES Warehouse(WarehouseID),
    FOREIGN KEY (ProductID) REFERENCES Product(ProductID)
);