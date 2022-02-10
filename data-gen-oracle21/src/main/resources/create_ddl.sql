CREATE TABLE instrument  (
 id NUMBER(12) NOT NULL PRIMARY KEY,
 type VARCHAR2(20),
 name VARCHAR2(100),
 price NUMBER(20,5),
 inceptionDate DATE,
 createdBy VARCHAR2(20),
 createdTime TIMESTAMP,
 updatedBy VARCHAR2(20),
 updatedTime TIMESTAMP,
 version INTEGER
);

CREATE TABLE account  (
  id NUMBER(12) NOT NULL PRIMARY KEY,
  balance NUMBER(20,5),
  clientId VARCHAR2(50),
  type VARCHAR(20),
  active CHAR(1),
  createdBy VARCHAR2(20),
  createdTime TIMESTAMP,
  updatedBy VARCHAR2(20),
  updatedTime TIMESTAMP,
  version INTEGER
);