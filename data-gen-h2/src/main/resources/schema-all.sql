DROP TABLE instrument IF EXISTS;

CREATE TABLE instrument  (
 id BIGINT IDENTITY NOT NULL PRIMARY KEY,
 type VARCHAR(20),
 name VARCHAR(100),
 price DECIMAL,
 inceptionDate DATE,
 createdBy varchar(20),
 createdTime datetime,
 updatedBy varchar(20),
 updatedTime datetime,
 version bigint
);

DROP TABLE account IF EXISTS;

CREATE TABLE account  (
 id BIGINT IDENTITY NOT NULL PRIMARY KEY,
 balance DECIMAL,
 clientId varchar(50),
 type VARCHAR(20),
 active boolean,
 createdBy varchar(20),
 createdTime datetime,
 updatedBy varchar(20),
 updatedTime datetime,
 version bigint
);