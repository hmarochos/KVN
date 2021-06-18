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
-- Dumping data for table `addresses`
--

LOCK TABLES `addresses` WRITE;
/*!40000 ALTER TABLE `addresses`
    DISABLE KEYS */;
INSERT INTO `addresses`
VALUES (2, 'Springfield', 192005, 'Evergreen Terrace 742'),
       (4, 'Springfield', 192005, 'Evergreen Terrace 742'),
       (6, 'Springfield', 192005, 'Evergreen Terrace 742'),
       (8, 'Springfield', 192005, 'Evergreen Terrace 742'),
       (10, 'Springfield', 192005, 'Evergreen Terrace 742'),
       (12, 'Hradec Králové', 50002, 'Někde v HK 123');
/*!40000 ALTER TABLE `addresses`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users`
    DISABLE KEYS */;
INSERT INTO `users`
VALUES (1, NULL, '2020-05-01 13:07:27.335105', 'homer.simpson@gmail.com', 'Homer', '2020-05-01 13:07:27.335105',
        'Simpson', '$2a$10$/8oIWHiyC.FqL/MEELmFDecDDu/K.uEOdr94t9fR.dTmXS/rb9BWW', '939 555 011',
        '2020-05-01 13:07:27.335105', 2),
       (3, NULL, '2020-05-01 13:07:27.543598', 'march.simpson@seznam.cz', 'March', '2020-05-01 13:07:27.543598',
        'Simpson', '$2a$10$gRZhc.wvvrWnESSN3b2n9ukCAVrQFWHRs8PMjGbeSQKSf/ej3QlKq', '939 555 011',
        '2020-05-01 13:07:27.543598', 4),
       (5, NULL, '2020-05-01 13:07:27.753073', 'lisa.simpson@gmail.com', 'Lisa', '2020-05-01 13:07:27.753073',
        'Simpson', '$2a$10$191kpYVzlxUmwoorfrzV5u65jMCOBI8bxkPBuJU10sXom84VH8U4u', NULL, '2020-05-01 13:07:27.753073',
        6),
       (7, NULL, '2020-05-01 13:07:27.961197', 'bart.simpson@seznam.cz', 'Bart', '2020-05-01 13:07:27.961197',
        'Simpson', '$2a$10$zhxOODtCMhV58e0Dm9.XH.yVoq7VaoXDYZAudnuSyoY3.agLySlYe', NULL, '2020-05-01 13:07:27.961197',
        8),
       (9, NULL, '2020-05-01 13:07:28.168683', 'maggie.simpson@gmail.com', 'Maggie', '2020-05-01 13:07:28.168683',
        'Simpson', '$2a$10$BVMZjFLBOIFjAwg9dPaOueGIurD.4xKCbjk8qYBtqwcZYz8xA0G9i', NULL, '2020-05-01 13:07:28.168683',
        10),
       (11, 302, '2020-05-01 13:07:28.377798', 'mois@example.com', 'Josef', '2020-05-01 13:07:28.377798', 'Unicorn',
        '$2a$10$vSeRqsPXMiE0mei04BLuUu8KwUOWRoYPPfB6c.IkO9KxbyJsnP/Fa', NULL, '2020-05-01 13:07:28.377798', 12);
/*!40000 ALTER TABLE `users`
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

-- Dump completed on 2020-05-01 15:09:50
