CREATE DATABASE IF NOT EXISTS crypto_platform;
USE crypto_platform;

-- MySQL dump 10.13  Distrib 9.5.0, for macos14.7 (arm64)
--
-- Host: database-1.cd6022qiadpx.us-east-1.rds.amazonaws.com    Database: crypto_platform
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '';

--
-- Table structure for table `market`
--

DROP TABLE IF EXISTS `market`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `market` (
                          `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                          `exchange_id` smallint unsigned NOT NULL,
                          `base_asset_id` bigint unsigned NOT NULL COMMENT 'What you receive when you BUY',
                          `quote_asset_id` bigint unsigned NOT NULL COMMENT 'What you pay when you BUY',
                          `symbol` varchar(64) NOT NULL COMMENT 'Pair symbol, e.g., BTCUSDT',
                          `exchange_name` varchar(32) NOT NULL,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uq_market_exchange_symbol` (`exchange_id`,`symbol`),
                          KEY `fk_market_base_asset` (`base_asset_id`),
                          KEY `fk_market_quote_asset` (`quote_asset_id`),
                          KEY `market_symbol_IDX` (`symbol`) USING BTREE,
                          CONSTRAINT `fk_market_base_asset` FOREIGN KEY (`base_asset_id`) REFERENCES `asset` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
                          CONSTRAINT `fk_market_exchange` FOREIGN KEY (`exchange_id`) REFERENCES `exchange` (`id`),
                          CONSTRAINT `fk_market_quote_asset` FOREIGN KEY (`quote_asset_id`) REFERENCES `asset` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Markets (trading pairs) per exchange';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `market`
--

LOCK TABLES `market` WRITE;
/*!40000 ALTER TABLE `market` DISABLE KEYS */;
INSERT INTO `market` VALUES (1,1,3,1,'BTC-USDT','BINANCE'),(2,1,4,1,'ETH-USDT','BINANCE'),(3,1,5,1,'BNB-USDT','BINANCE'),(4,1,6,1,'SOL-USDT','BINANCE'),(5,1,7,1,'DOGE-USDT','BINANCE'),(6,1,8,1,'TRX-USDT','BINANCE'),(7,1,9,1,'XRP-USDT','BINANCE'),(8,1,3,2,'BTC-USDC','BINANCE'),(9,1,4,2,'ETH-USDC','BINANCE'),(10,1,5,2,'BNB-USDC','BINANCE'),(11,1,6,2,'SOL-USDC','BINANCE'),(12,1,7,2,'DOGE-USDC','BINANCE'),(13,1,8,2,'TRX-USDC','BINANCE'),(14,1,9,2,'XRP-USDC','BINANCE'),(15,2,3,1,'BTC-USDT','OKX'),(16,2,4,1,'ETH-USDT','OKX'),(17,2,5,1,'BNB-USDT','OKX'),(18,2,6,1,'SOL-USDT','OKX'),(19,2,7,1,'DOGE-USDT','OKX'),(20,2,8,1,'TRX-USDT','OKX'),(21,2,9,1,'XRP-USDT','OKX'),(22,2,3,2,'BTC-USDC','OKX'),(23,2,4,2,'ETH-USDC','OKX'),(24,2,5,2,'BNB-USDC','OKX'),(25,2,6,2,'SOL-USDC','OKX'),(26,2,7,2,'DOGE-USDC','OKX'),(27,2,8,2,'TRX-USDC','OKX'),(28,2,9,2,'XRP-USDC','OKX'),(29,3,3,1,'BTC-USDT','CRYPTO'),(30,3,4,1,'ETH-USDT','CRYPTO'),(31,3,5,1,'BNB-USDT','CRYPTO'),(32,3,6,1,'SOL-USDT','CRYPTO'),(33,3,7,1,'DOGE-USDT','CRYPTO'),(34,3,8,1,'TRX-USDT','CRYPTO'),(35,3,9,1,'XRP-USDT','CRYPTO'),(36,3,3,2,'BTC-USDC','CRYPTO'),(37,3,4,2,'ETH-USDC','CRYPTO'),(38,3,5,2,'BNB-USDC','CRYPTO'),(39,3,6,2,'SOL-USDC','CRYPTO'),(40,3,7,2,'DOGE-USDC','CRYPTO'),(41,3,8,2,'TRX-USDC','CRYPTO'),(42,3,9,2,'XRP-USDC','CRYPTO'),(43,4,3,1,'BTC-USDT','BYBIT'),(44,4,4,1,'ETH-USDT','BYBIT'),(45,4,5,1,'BNB-USDT','BYBIT'),(46,4,6,1,'SOL-USDT','BYBIT'),(47,4,7,1,'DOGE-USDT','BYBIT'),(48,4,8,1,'TRX-USDT','BYBIT'),(49,4,9,1,'XRP-USDT','BYBIT'),(50,4,3,2,'BTC-USDC','BYBIT'),(51,4,4,2,'ETH-USDC','BYBIT'),(52,4,5,2,'BNB-USDC','BYBIT'),(53,4,6,2,'SOL-USDC','BYBIT'),(54,4,7,2,'DOGE-USDC','BYBIT'),(55,4,8,2,'TRX-USDC','BYBIT'),(56,4,9,2,'XRP-USDC','BYBIT');
/*!40000 ALTER TABLE `market` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `market_data`
--

DROP TABLE IF EXISTS `market_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `market_data` (
                               `market_id` bigint unsigned NOT NULL,
                               `kline_interval` bigint unsigned NOT NULL,
                               `open_time` bigint unsigned NOT NULL,
                               `close_time` bigint unsigned NOT NULL,
                               `open_price` decimal(36,18) NOT NULL,
                               `high_price` decimal(36,18) NOT NULL,
                               `low_price` decimal(36,18) NOT NULL,
                               `close_price` decimal(36,18) NOT NULL,
                               `volume` decimal(36,18) NOT NULL COMMENT 'Base asset volume',
                               PRIMARY KEY (`market_id`,`kline_interval`,`open_time`),
                               KEY `idx_market_interval_time` (`market_id`,`kline_interval`,`open_time`),
                               CONSTRAINT `fk_kline_market` FOREIGN KEY (`market_id`) REFERENCES `market` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Historical candlestick (kline) data for each market and interval';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `market_data`
--

LOCK TABLES `market_data` WRITE;
/*!40000 ALTER TABLE `market_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `market_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `history`
--

DROP TABLE IF EXISTS `history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `history` (
                           `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                           `order_id` bigint unsigned NOT NULL,
                           `market_id` bigint unsigned NOT NULL,
                           `fill_price` decimal(36,18) NOT NULL,
                           `fill_qty` decimal(36,18) NOT NULL,
                           `occurred_at` timestamp(3) NOT NULL,
                           PRIMARY KEY (`id`),
                           KEY `idx_order_time` (`order_id`,`occurred_at`),
                           KEY `idx_market_time` (`market_id`,`occurred_at`),
                           CONSTRAINT `fk_fill_market` FOREIGN KEY (`market_id`) REFERENCES `market` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
                           CONSTRAINT `fk_fill_order` FOREIGN KEY (`order_id`) REFERENCES `order_ticket` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Order execution fills that drive the ledger';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `history`
--

LOCK TABLES `history` WRITE;
/*!40000 ALTER TABLE `history` DISABLE KEYS */;
/*!40000 ALTER TABLE `history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `asset`
--

DROP TABLE IF EXISTS `asset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asset` (
                         `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                         `symbol` varchar(32) NOT NULL COMMENT 'Ticker symbol, e.g., BTC, ETH, USDT',
                         `name` varchar(128) NOT NULL COMMENT 'Full name of the cryptocurrency, e.g., Bitcoin',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `symbol` (`symbol`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='List of tradable crypto assets';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `asset`
--

LOCK TABLES `asset` WRITE;
/*!40000 ALTER TABLE `asset` DISABLE KEYS */;
INSERT INTO `asset` VALUES (1,'USDT','Tether'),(2,'USDC','USD Coin'),(3,'BTC','Bitcoin'),(4,'ETH','Ethereum'),(5,'BNB','BNB'),(6,'SOL','Solana'),(7,'DOGE','Dogecoin'),(8,'TRX','TRON'),(9,'XRP','XRP');
/*!40000 ALTER TABLE `asset` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exchange`
--

DROP TABLE IF EXISTS `exchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exchange` (
                            `id` smallint unsigned NOT NULL AUTO_INCREMENT,
                            `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `code` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exchange`
--

LOCK TABLES `exchange` WRITE;
/*!40000 ALTER TABLE `exchange` DISABLE KEYS */;
INSERT INTO `exchange` VALUES (1,'BINANCE'),(4,'BYBIT'),(3,'CRYPTO'),(2,'OKX');
/*!40000 ALTER TABLE `exchange` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-14 22:43:58
