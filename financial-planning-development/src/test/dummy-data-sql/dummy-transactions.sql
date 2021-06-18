-- MySQL dump 10.13  Distrib 8.0.16, for Win64 (x86_64)
-- ------------------------------------------------------
-- Server version	8.0.13

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
SET NAMES utf8;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

--
-- Dumping data for table `additional_info_domestic`
--

LOCK TABLES `additional_info_domestic` WRITE;
/*!40000 ALTER TABLE `additional_info_domestic`
    DISABLE KEYS */;
INSERT INTO `additional_info_domestic`
VALUES (100, '', '', ''),
       (104, '', '', ''),
       (108, '', '', ''),
       (112, '12345', '55441', '54321'),
       (116, '12345', '55441', '54321'),
       (120, '12345', '55441', '54321'),
       (124, '12345', '55441', '55441'),
       (128, '12345', '55441', '54321'),
       (132, '54321', '55441', '12345'),
       (136, '55441', '12345', '54321'),
       (140, '55441', '55441', '54321'),
       (144, '1234', '15975', '98765'),
       (148, '6543', '35775', '32145');
/*!40000 ALTER TABLE `additional_info_domestic`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `party_accounts`
--

LOCK TABLES `party_accounts` WRITE;
/*!40000 ALTER TABLE `party_accounts`
    DISABLE KEYS */;
INSERT INTO `party_accounts`
VALUES (101, '302', '302'),
       (105, '302', '302'),
       (109, '302', '302'),
       (113, '302', '302'),
       (117, '302', '302'),
       (121, '302', '302'),
       (125, '302', '302'),
       (129, '302', '302'),
       (133, '302', '302'),
       (137, '302', '302'),
       (141, '302', '302'),
       (145, '302', '302'),
       (149, '302', '302');
/*!40000 ALTER TABLE `party_accounts`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions`
    DISABLE KEYS */;
INSERT INTO `transactions`
VALUES (99, 302, '2020-04-01 04:06:06.000000', 'OUTGOING', 'Energie', 'PAYMENT_HOME', '2020-04-01 04:06:06.000000', 100,
        101, 102),
       (103, 302, '2020-04-03 04:06:06.000000', 'OUTGOING', 'Nákup Tesco', 'PAYMENT_HOME', '2020-04-03 04:06:06.000000',
        104, 105, 106),
       (107, 302, '2020-04-05 04:06:06.000000', 'INCOMING', 'Výplata', 'PAYMENT_HOME', '2020-04-05 04:06:06.000000',
        108, 109, 110),
       (111, 302, '2020-04-08 04:06:06.000000', 'OUTGOING', 'Hypotéka', 'PAYMENT_HOME', '2020-04-08 04:06:06.000000',
        112, 113, 114),
       (115, 302, '2020-04-09 04:06:06.000000', 'OUTGOING', 'Nákup New Yorker', 'PAYMENT_HOME',
        '2020-04-09 04:06:06.000000', 116, 117, 118),
       (119, 302, '2020-04-11 04:06:06.000000', 'OUTGOING', 'Nákup Humanic', 'PAYMENT_HOME',
        '2020-04-11 04:06:06.000000', 120, 121, 122),
       (123, 302, '2020-04-13 04:06:06.000000', 'OUTGOING', 'Benzín', 'CARD', '2020-04-13 04:06:06.000000', 124, 125,
        126),
       (127, 302, '2020-04-16 04:06:06.000000', 'OUTGOING', 'Kino Cinestar', 'PAYMENT_HOME',
        '2020-04-16 04:06:06.000000', 128, 129, 130),
       (131, 302, '2020-04-20 04:06:06.000000', 'OUTGOING', 'Nákup Tesco', 'CARD', '2020-04-20 04:06:06.000000', 132,
        133, 134),
       (135, 302, '2020-04-23 04:06:06.000000', 'OUTGOING', 'Nákup Teta', 'PAYMENT_HOME', '2020-04-23 04:06:06.000000',
        136, 137, 138),
       (139, 302, '2020-04-27 04:06:06.000000', 'OUTGOING', 'Oprava auta', 'CARD', '2020-04-27 04:06:06.000000', 140,
        141, 142),
       (143, 302, '2020-04-28 04:06:06.000000', 'OUTGOING', 'Nákup Tesco', 'PAYMENT_HOME', '2020-04-28 04:06:06.000000',
        144, 145, 146),
       (147, 302, '2020-04-30 04:06:06.000000', 'OUTGOING', 'Nákup Benu', 'CARD', '2020-04-30 04:06:06.000000', 148,
        149, 150);
/*!40000 ALTER TABLE `transactions`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `values`
--

LOCK TABLES `values` WRITE;
/*!40000 ALTER TABLE `values`
    DISABLE KEYS */;
INSERT INTO `values`
VALUES (102, 3500.00, 'CZK'),
       (106, 1000.00, 'CZK'),
       (110, 50000.00, 'CZK'),
       (114, 10000.00, 'CZK'),
       (118, 659.00, 'CZK'),
       (122, 740.00, 'CZK'),
       (126, 2000.00, 'CZK'),
       (130, 480.00, 'CZK'),
       (134, 1000.00, 'CZK'),
       (138, 1000.00, 'CZK'),
       (142, 3000.00, 'CZK'),
       (146, 1500.00, 'CZK'),
       (150, 1000.00, 'CZK');
/*!40000 ALTER TABLE `values`
    ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2020-05-01  0:44:58
