-- Lab 3 - Bài 1: Tạo CSDL polycoffee trên SQL Server
IF DB_ID(N'polycoffee') IS NULL
    CREATE DATABASE polycoffee;
GO
USE polycoffee;
GO

-- Danh mục đồ uống
CREATE TABLE categories (
    id          INT IDENTITY(1,1) NOT NULL,
    name        NVARCHAR(250)     NOT NULL,
    description NVARCHAR(MAX)     NULL,
    active      BIT               NOT NULL CONSTRAINT DF_categories_active DEFAULT 1,
    created_at  DATETIME2         NOT NULL CONSTRAINT DF_categories_created DEFAULT SYSDATETIME(),
    CONSTRAINT PK_categories PRIMARY KEY (id)
);
GO

-- Người dùng (role = 1: quản lý, role = 0: nhân viên)
CREATE TABLE users (
    id        INT IDENTITY(1,1) NOT NULL,
    email     VARCHAR(100)      NOT NULL,
    password  VARCHAR(255)      NOT NULL,
    full_name NVARCHAR(100)     NOT NULL,
    phone     VARCHAR(10)       NULL,
    role      BIT               NOT NULL,
    active    BIT               NOT NULL CONSTRAINT DF_users_active DEFAULT 1,
    CONSTRAINT PK_users PRIMARY KEY (id),
    CONSTRAINT UQ_users_email UNIQUE (email),
    CONSTRAINT UQ_users_phone UNIQUE (phone)
);
GO

-- Đồ uống
CREATE TABLE drinks (
    id          INT IDENTITY(1,1) NOT NULL,
    category_id INT               NOT NULL,
    name        NVARCHAR(250)     NOT NULL,
    price       INT               NOT NULL CONSTRAINT CK_drinks_price CHECK (price >= 0),
    image       NVARCHAR(250)     NOT NULL,
    description NVARCHAR(MAX)     NULL,
    active      BIT               NOT NULL CONSTRAINT DF_drinks_active DEFAULT 1,
    CONSTRAINT PK_drinks PRIMARY KEY (id),
    CONSTRAINT FK_drinks_categories FOREIGN KEY (category_id) REFERENCES categories(id)
);
GO

-- Hóa đơn
CREATE TABLE bills (
    id         INT IDENTITY(1,1) NOT NULL,
    user_id    INT               NOT NULL,
    code       VARCHAR(10)       NOT NULL,
    created_at DATETIME2         NOT NULL CONSTRAINT DF_bills_created DEFAULT SYSDATETIME(),
    total      INT               NOT NULL CONSTRAINT DF_bills_total DEFAULT 0,
    status     VARCHAR(10)       NOT NULL,
    CONSTRAINT PK_bills PRIMARY KEY (id),
    CONSTRAINT UQ_bills_code UNIQUE (code),
    CONSTRAINT FK_bills_users FOREIGN KEY (user_id) REFERENCES users(id)
);
GO

-- Chi tiết hóa đơn (bảng trung gian bills – drinks)
CREATE TABLE bill_details (
    id        INT IDENTITY(1,1) NOT NULL,
    bill_id   INT               NOT NULL,
    drink_id  INT               NOT NULL,
    quantity  INT               NOT NULL CONSTRAINT CK_bill_details_qty CHECK (quantity > 0),
    price     INT               NOT NULL CONSTRAINT CK_bill_details_price CHECK (price >= 0),
    CONSTRAINT PK_bill_details PRIMARY KEY (id),
    CONSTRAINT FK_bill_details_bills  FOREIGN KEY (bill_id)  REFERENCES bills(id),
    CONSTRAINT FK_bill_details_drinks FOREIGN KEY (drink_id) REFERENCES drinks(id)
);
GO
