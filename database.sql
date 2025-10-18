-- 创建数据库
CREATE DATABASE IF NOT EXISTS course_ehealth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE course_ehealth;

-- 1. 患者信息表 Client
CREATE TABLE IF NOT EXISTS course_ehealth_Client (
                                                     client_id         INT AUTO_INCREMENT PRIMARY KEY,
                                                     first_name        VARCHAR(50) NOT NULL,
                                                     last_name         VARCHAR(50) NOT NULL,
                                                     date_of_birth     DATE        NOT NULL,
                                                     gender            VARCHAR(10) NOT NULL,
                                                     health_card_num   VARCHAR(20) UNIQUE,
                                                     phone             VARCHAR(20),
                                                     email             VARCHAR(100),
                                                     address           VARCHAR(255),
                                                     postal_code       VARCHAR(20),
                                                     emergency_contact VARCHAR(100),
                                                     notes             TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 药房处方表 Prescription，主键为UUID
CREATE TABLE IF NOT EXISTS course_ehealth_Prescription (
                                                           prescription_id      CHAR(36) PRIMARY KEY,
                                                           client_id            INT NOT NULL,
                                                           prescriber_id        CHAR(36) NOT NULL,
                                                           medication_name      VARCHAR(100) NOT NULL,
                                                           medication_strength  VARCHAR(50),
                                                           medication_form      VARCHAR(50),
                                                           dosage_instructions  TEXT,
                                                           quantity             INT,
                                                           refills_allowed      INT,
                                                           date_prescribed      DATETIME NOT NULL,
                                                           expiry_date          DATE,
                                                           pharmacy_name        VARCHAR(100),
                                                           pharmacy_address     VARCHAR(255),
                                                           status               VARCHAR(20) DEFAULT 'Active',
                                                           notes                TEXT,
                                                           FOREIGN KEY (client_id) REFERENCES course_ehealth_Client(client_id)
                                                               ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 检查/化验申请单表 Requisition，主键为UUID
CREATE TABLE IF NOT EXISTS course_ehealth_Requisition (
                                                          requisition_id   CHAR(36) PRIMARY KEY,
                                                          client_id        INT NOT NULL,
                                                          requester_id     CHAR(36) NOT NULL,
                                                          department       VARCHAR(50) NOT NULL,
                                                          test_type        VARCHAR(100) NOT NULL,
                                                          test_code        VARCHAR(50),
                                                          clinical_info    TEXT,
                                                          date_requested   DATETIME NOT NULL,
                                                          priority         VARCHAR(20) DEFAULT 'Routine',
                                                          status           VARCHAR(20) DEFAULT 'Pending',
                                                          lab_name         VARCHAR(100),
                                                          lab_address      VARCHAR(255),
                                                          result_date      DATE,
                                                          notes            TEXT,
                                                          FOREIGN KEY (client_id) REFERENCES course_ehealth_Client(client_id)
                                                              ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 参考标准：
-- Client表依照 eHealth Ontario、HL7 FHIR Patient、OHIP登记要求；
-- Prescription表依照 HL7 FHIR MedicationRequest、Ontario Drug Benefit(ODB) claims；
-- Requisition表依照 HL7 FHIR ServiceRequest、Ontario Laboratories Information System (OLIS)。

INSERT INTO course_ehealth_Client
(first_name, last_name, date_of_birth, gender, health_card_num, phone, email, address, postal_code, emergency_contact, notes) VALUES
                                                                                                                                  ('Olivia', 'Lefebvre', '1985-04-18', 'Female', 'AHC1234567', '613-555-1010', 'olivia.lefebvre@email.com', '123 Laurier Ave W, Ottawa, ON', 'K1P 1J1', 'Pierre Lefebvre', ''),
                                                                                                                                  ('Liam', 'Smith', '1992-09-22', 'Male', 'AHC2345678', '613-555-2020', 'liam.smith@email.com', '45 Rideau St, Ottawa, ON', 'K1N 5W8', 'Emma Smith', ''),
                                                                                                                                  ('Emma', 'Chen', '1977-12-30', 'Female', 'AHC3456789', '613-555-3030', 'emma.chen@email.com', '200 Elgin St, Ottawa, ON', 'K2P 1L5', 'Wei Chen', ''),
                                                                                                                                  ('Noah', 'Patel', '2001-05-14', 'Male', 'AHC4567890', '613-555-4040', 'noah.patel@email.com', '150 Wellington St, Ottawa, ON', 'K1A 0A9', 'Pari Patel', ''),
                                                                                                                                  ('Sophia', 'Dubois', '1990-03-10', 'Female', 'AHC5678901', '613-555-5050', 'sophia.dubois@email.com', '99 Bank St, Ottawa, ON', 'K1P 5N4', 'Luc Dubois', ''),
                                                                                                                                  ('Jackson', 'Nguyen', '1988-07-11', 'Male', 'AHC6789012', '613-555-6060', 'jackson.nguyen@email.com', '101 Queen St, Ottawa, ON', 'K1P 5C7', 'Mai Nguyen', ''),
                                                                                                                                  ('Charlotte', 'Bennett', '1965-02-05', 'Female', 'AHC7890123', '613-555-7070', 'charlotte.bennett@email.com', '320 Sussex Dr, Ottawa, ON', 'K1N 0A7', 'David Bennett', ''),
                                                                                                                                  ('Benjamin', 'Singh', '1982-11-16', 'Male', 'AHC8901234', '613-555-8080', 'benjamin.singh@email.com', '50 O’Connor St, Ottawa, ON', 'K1P 6L2', 'Raj Singh', ''),
                                                                                                                                  ('Ava', 'Johnson', '1995-08-29', 'Female', 'AHC9012345', '613-555-9090', 'ava.johnson@email.com', '350 Albert St, Ottawa, ON', 'K1R 1A4', 'Michael Johnson', ''),
                                                                                                                                  ('Lucas', 'Taylor', '1987-06-13', 'Male', 'AHC0123456', '613-555-1111', 'lucas.taylor@email.com', '360 Laurier Ave E, Ottawa, ON', 'K1N 6R4', 'Sarah Taylor', ''),
                                                                                                                                  ('Emily', 'Roy', '1979-02-21', 'Female', 'AHC1234578', '613-555-1212', 'emily.roy@email.com', '180 Lyon St N, Ottawa, ON', 'K1R 5V8', 'Jean Roy', ''),
                                                                                                                                  ('William', 'Côté', '1993-03-19', 'Male', 'AHC2345689', '613-555-1313', 'william.cote@email.com', '245 Kent St, Ottawa, ON', 'K2P 2M2', 'Marie Côté', ''),
                                                                                                                                  ('Mia', 'Tremblay', '2000-09-08', 'Female', 'AHC3456790', '613-555-1414', 'mia.tremblay@email.com', '251 Bank St, Ottawa, ON', 'K2P 1X3', 'Louis Tremblay', ''),
                                                                                                                                  ('James', 'Wong', '1983-10-27', 'Male', 'AHC4567801', '613-555-1515', 'james.wong@email.com', '1 Nicholas St, Ottawa, ON', 'K1N 7B7', 'Linda Wong', ''),
                                                                                                                                  ('Ella', 'Rivard', '1972-04-03', 'Female', 'AHC5678912', '613-555-1616', 'ella.rivard@email.com', '410 Laurier Ave W, Ottawa, ON', 'K1R 1B7', 'Marc Rivard', ''),
                                                                                                                                  ('Logan', 'Brown', '2002-11-24', 'Male', 'AHC6789023', '613-555-1717', 'logan.brown@email.com', '251 Queen St, Ottawa, ON', 'K1P 6N1', 'Julia Brown', ''),
                                                                                                                                  ('Amelia', 'Li', '1999-07-12', 'Female', 'AHC7890134', '613-555-1818', 'amelia.li@email.com', '240 Sparks St, Ottawa, ON', 'K1P 6C9', 'Wei Li', ''),
                                                                                                                                  ('Henry', 'Williams', '1975-12-18', 'Male', 'AHC8901245', '613-555-1919', 'henry.williams@email.com', '160 Elgin St, Ottawa, ON', 'K2P 2P7', 'Diana Williams', ''),
                                                                                                                                  ('Chloe', 'Lapointe', '1986-08-26', 'Female', 'AHC9012356', '613-555-2021', 'chloe.lapointe@email.com', '330 Sussex Dr, Ottawa, ON', 'K1N 0C7', 'Luc Lapointe', ''),
                                                                                                                                  ('Jack', 'Kim', '2001-10-06', 'Male', 'AHC0123467', '613-555-2122', 'jack.kim@email.com', '60 O’Connor St, Ottawa, ON', 'K1P 5M4', 'Soo Kim', ''),
                                                                                                                                  ('Grace', 'Clark', '1984-05-29', 'Female', 'AHC1234589', '613-555-2223', 'grace.clark@email.com', '300 Slater St, Ottawa, ON', 'K1P 6A6', 'Tom Clark', ''),
                                                                                                                                  ('Ethan', 'Desjardins', '1996-02-14', 'Male', 'AHC2345690', '613-555-2324', 'ethan.desjardins@email.com', '130 Albert St, Ottawa, ON', 'K1P 5G4', 'Julie Desjardins', ''),
                                                                                                                                  ('Zoe', 'Martin', '1978-07-19', 'Female', 'AHC3456801', '613-555-2425', 'zoe.martin@email.com', '240 Bank St, Ottawa, ON', 'K2P 1X4', 'Paul Martin', ''),
                                                                                                                                  ('Mason', 'Lee', '1981-03-15', 'Male', 'AHC4567912', '613-555-2526', 'mason.lee@email.com', '90 Sparks St, Ottawa, ON', 'K1P 5B4', 'Helen Lee', ''),
                                                                                                                                  ('Harper', 'Gagnon', '1994-10-30', 'Female', 'AHC5678023', '613-555-2627', 'harper.gagnon@email.com', '100 Metcalfe St, Ottawa, ON', 'K1P 5M1', 'Pierre Gagnon', ''),
                                                                                                                                  ('Alexander', 'Wilson', '1973-06-12', 'Male', 'AHC6789134', '613-555-2728', 'alexander.wilson@email.com', '385 Sussex Dr, Ottawa, ON', 'K1N 1J9', 'Anna Wilson', ''),
                                                                                                                                  ('Lily', 'Fortin', '1989-01-24', 'Female', 'AHC7890245', '613-555-2829', 'lily.fortin@email.com', '360 Albert St, Ottawa, ON', 'K1R 7X7', 'Jean Fortin', ''),
                                                                                                                                  ('Jacob', 'Hall', '1998-08-08', 'Male', 'AHC8901356', '613-555-2930', 'jacob.hall@email.com', '50 Rideau St, Ottawa, ON', 'K1N 9J7', 'Donna Hall', ''),
                                                                                                                                  ('Scarlett', 'Lam', '2003-04-02', 'Female', 'AHC9012467', '613-555-3031', 'scarlett.lam@email.com', '100 Queen St, Ottawa, ON', 'K1P 1N2', 'Hung Lam', ''),
                                                                                                                                  ('Michael', 'Pelletier', '1971-11-25', 'Male', 'AHC0123578', '613-555-3132', 'michael.pelletier@email.com', '344 Slater St, Ottawa, ON', 'K1R 7Y3', 'Nicole Pelletier', ''),
                                                                                                                                  ('Layla', 'Morin', '1983-06-15', 'Female', 'AHC1234690', '613-555-3233', 'layla.morin@email.com', '220 Laurier Ave W, Ottawa, ON', 'K1P 5Z9', 'David Morin', ''),
                                                                                                                                  ('Daniel', 'Zhang', '1997-03-27', 'Male', 'AHC2345701', '613-555-3334', 'daniel.zhang@email.com', '80 Elgin St, Ottawa, ON', 'K1P 1C2', 'Jing Zhang', ''),
                                                                                                                                  ('Sophie', 'Walker', '1986-12-31', 'Female', 'AHC3456812', '613-555-3435', 'sophie.walker@email.com', '201 Queen St, Ottawa, ON', 'K1P 5C9', 'Greg Walker', ''),
                                                                                                                                  ('Matthew', 'Wright', '1974-07-22', 'Male', 'AHC4567923', '613-555-3536', 'matthew.wright@email.com', '234 Laurier Ave E, Ottawa, ON', 'K1N 6P1', 'Carol Wright', ''),
                                                                                                                                  ('Victoria', 'Simard', '1991-11-05', 'Female', 'AHC5678134', '613-555-3637', 'victoria.simard@email.com', '120 Metcalfe St, Ottawa, ON', 'K1P 5M9', 'Louis Simard', ''),
                                                                                                                                  ('Samuel', 'Bernier', '1990-04-08', 'Male', 'AHC6789245', '613-555-3738', 'samuel.bernier@email.com', '240 Sparks St, Ottawa, ON', 'K1P 6C9', 'Marie Bernier', ''),
                                                                                                                                  ('Avery', 'Murray', '1982-10-18', 'Female', 'AHC7890356', '613-555-3839', 'avery.murray@email.com', '150 Metcalfe St, Ottawa, ON', 'K2P 1P1', 'John Murray', ''),
                                                                                                                                  ('David', 'Girard', '1980-05-18', 'Male', 'AHC8901467', '613-555-3940', 'david.girard@email.com', '275 Slater St, Ottawa, ON', 'K1P 5H9', 'Lucie Girard', ''),
                                                                                                                                  ('Ella', 'MacDonald', '1993-02-13', 'Female', 'AHC9012578', '613-555-4041', 'ella.macdonald@email.com', '100 Gloucester St, Ottawa, ON', 'K2P 0A4', 'Fiona MacDonald', ''),
                                                                                                                                  ('Luke', 'Ouellet', '1976-09-14', 'Male', 'AHC0123689', '613-555-4142', 'luke.ouellet@email.com', '300 Laurier Ave W, Ottawa, ON', 'K1P 6M7', 'Denise Ouellet', ''),
                                                                                                                                  ('Hannah', 'Reid', '1984-05-16', 'Female', 'AHC1234801', '613-555-4243', 'hannah.reid@email.com', '250 Albert St, Ottawa, ON', 'K1P 6M1', 'Gary Reid', ''),
                                                                                                                                  ('Nathan', 'Woods', '1999-08-11', 'Male', 'AHC2345912', '613-555-4344', 'nathan.woods@email.com', '180 Kent St, Ottawa, ON', 'K1P 0B6', 'Linda Woods', ''),
                                                                                                                                  ('Sofia', 'Martel', '1978-01-06', 'Female', 'AHC3456823', '613-555-4445', 'sofia.martel@email.com', '240 Queen St, Ottawa, ON', 'K1P 5E4', 'Claude Martel', ''),
                                                                                                                                  ('Elijah', 'Dion', '2001-12-02', 'Male', 'AHC4567934', '613-555-4546', 'elijah.dion@email.com', '121 Besserer St, Ottawa, ON', 'K1N 6A9', 'Sophie Dion', ''),
                                                                                                                                  ('Aria', 'Bouchard', '1995-03-30', 'Female', 'AHC5678245', '613-555-4647', 'aria.bouchard@email.com', '110 O’Connor St, Ottawa, ON', 'K1P 5M7', 'Marc Bouchard', ''),
                                                                                                                                  ('Gabriel', 'Roy', '1972-11-17', 'Male', 'AHC6789356', '613-555-4748', 'gabriel.roy@email.com', '130 Slater St, Ottawa, ON', 'K1P 6E2', 'Julie Roy', ''),
                                                                                                                                  ('Madison', 'Ford', '1986-08-12', 'Female', 'AHC7890467', '613-555-4849', 'madison.ford@email.com', '150 Kent St, Ottawa, ON', 'K1P 0E4', 'James Ford', ''),
                                                                                                                                  ('Carter', 'Lavoie', '1979-07-21', 'Male', 'AHC8901578', '613-555-4950', 'carter.lavoie@email.com', '300 Bank St, Ottawa, ON', 'K2P 1X8', 'Marie Lavoie', ''),
                                                                                                                                  ('Penelope', 'Charron', '1988-03-09', 'Female', 'AHC9012689', '613-555-5051', 'penelope.charron@email.com', '100 Wellington St, Ottawa, ON', 'K1A 0A9', 'Luc Charron', ''),
                                                                                                                                  ('Wyatt', 'Gauthier', '2000-05-05', 'Male', 'AHC0123790', '613-555-5152', 'wyatt.gauthier@email.com', '101 Metcalfe St, Ottawa, ON', 'K1P 5K9', 'Julie Gauthier', ''),
                                                                                                                                  ('Camila', 'Houde', '1992-09-19', 'Female', 'AHC1234912', '613-555-5253', 'camila.houde@email.com', '350 Queen St, Ottawa, ON', 'K1R 5A5', 'Jean Houde', '');