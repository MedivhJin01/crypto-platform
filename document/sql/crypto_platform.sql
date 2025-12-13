-- crypto_platform.asset definition
SET NAMES utf8mb4;

DROP TABLE IF EXISTS `asset`;
CREATE TABLE `asset` (
                         `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                         `symbol` varchar(32) NOT NULL COMMENT 'Ticker symbol, e.g., BTC, ETH, USDT',
                         `name` varchar(128) NOT NULL COMMENT 'Full name of the cryptocurrency, e.g., Bitcoin',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `symbol` (`symbol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='List of tradable crypto assets';


-- crypto_platform.`user` definition
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                        `email` varchar(255) NOT NULL,
                        `username` varchar(32) NOT NULL,
                        `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Application users';


-- crypto_platform.account definition
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
                           `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                           `user_id` bigint unsigned NOT NULL,
                           `base_ccy_asset_id` bigint unsigned DEFAULT NULL,
                           `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`),
                           KEY `fk_account_display_asset` (`base_ccy_asset_id`),
                           KEY `idx_account_user` (`user_id`),
                           CONSTRAINT `fk_account_display_asset` FOREIGN KEY (`base_ccy_asset_id`) REFERENCES `asset` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
                           CONSTRAINT `fk_account_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Trading accounts per user';


-- crypto_platform.asset_last_price definition
DROP TABLE IF EXISTS `asset_last_price`;
CREATE TABLE `asset_last_price` (
                                    `base_asset_id` bigint unsigned NOT NULL,
                                    `quote_asset_id` bigint unsigned NOT NULL,
                                    `price` decimal(36,18) NOT NULL,
                                    `ts` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                    PRIMARY KEY (`base_asset_id`,`quote_asset_id`),
                                    KEY `fk_lastprice_quote_asset` (`quote_asset_id`),
                                    CONSTRAINT `fk_lastprice_base_asset` FOREIGN KEY (`base_asset_id`) REFERENCES `asset` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                    CONSTRAINT `fk_lastprice_quote_asset` FOREIGN KEY (`quote_asset_id`) REFERENCES `asset` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Latest last/mid price for each (base, quote) asset pair';


-- crypto_platform.balance definition
DROP TABLE IF EXISTS `balance`;
CREATE TABLE `balance` (
                           `account_id` bigint unsigned NOT NULL,
                           `asset_id` bigint unsigned NOT NULL,
                           `quantity` decimal(36,18) NOT NULL DEFAULT '0.000000000000000000',
                           `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`account_id`,`asset_id`),
                           KEY `fk_bal_asset` (`asset_id`),
                           CONSTRAINT `fk_bal_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                           CONSTRAINT `fk_bal_asset` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Current unit balances per (account, asset)';


-- crypto_platform.ledger definition
DROP TABLE IF EXISTS `ledger`;
CREATE TABLE `ledger` (
                          `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                          `account_id` bigint unsigned NOT NULL,
                          `asset_id` bigint unsigned NOT NULL,
                          `delta_qty` decimal(36,18) NOT NULL,
                          `ref_type` enum('TRADE','DEPOSIT','WITHDRAWAL') NOT NULL,
                          `ref_id` bigint DEFAULT NULL,
                          `occurred_at` timestamp(3) NOT NULL,
                          `created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                          PRIMARY KEY (`id`),
                          KEY `fk_ledger_asset` (`asset_id`),
                          KEY `idx_acct_time` (`account_id`,`occurred_at`),
                          KEY `idx_ref` (`ref_type`,`ref_id`),
                          CONSTRAINT `fk_ledger_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
                          CONSTRAINT `fk_ledger_asset` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Immutable quantity changes for accounts (double-entry)';


-- crypto_platform.market definition
DROP TABLE IF EXISTS `market`;
CREATE TABLE `market` (
                          `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                          `base_asset_id` bigint unsigned NOT NULL COMMENT 'What you receive when you BUY',
                          `quote_asset_id` bigint unsigned NOT NULL COMMENT 'What you pay when you BUY',
                          `symbol` varchar(64) NOT NULL COMMENT 'Pair symbol, e.g., BTCUSDT',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uq_exchange_symbol` (`exchange_id`,`symbol`),
                          KEY `fk_market_base_asset` (`base_asset_id`),
                          KEY `fk_market_quote_asset` (`quote_asset_id`),
                          CONSTRAINT `fk_market_base_asset` FOREIGN KEY (`base_asset_id`) REFERENCES `asset` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
                          CONSTRAINT `fk_market_quote_asset` FOREIGN KEY (`quote_asset_id`) REFERENCES `asset` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Markets (trading pairs) per exchange';


-- crypto_platform.market_kline definition
DROP TABLE IF EXISTS `market_kline`;
CREATE TABLE `market_kline` (
                                `market_id` bigint unsigned NOT NULL,
                                `kline_interval` enum('1m','3m','5m','15m','30m','1h','2h','4h','12h','1d','1w','1MON') NOT NULL COMMENT 'Candle duration',
                                `kline_open_time` datetime(3) NOT NULL COMMENT 'UTC open time',
                                `kline_close_time` datetime(3) NOT NULL COMMENT 'UTC close time',
                                `open_price` decimal(36,18) NOT NULL,
                                `high_price` decimal(36,18) NOT NULL,
                                `low_price` decimal(36,18) NOT NULL,
                                `close_price` decimal(36,18) NOT NULL,
                                `volume` decimal(36,18) NOT NULL COMMENT 'Base asset volume',
                                `trades_count` int unsigned NOT NULL,
                                `quote_asset_volume` decimal(36,18) DEFAULT NULL,
                                `taker_buy_base_volume` decimal(36,18) DEFAULT NULL,
                                `taker_buy_quote_volume` decimal(36,18) DEFAULT NULL,
                                PRIMARY KEY (`market_id`,`kline_interval`,`kline_open_time`),
                                KEY `idx_market_time` (`market_id`,`kline_open_time`),
                                CONSTRAINT `fk_kline_market` FOREIGN KEY (`market_id`) REFERENCES `market` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Historical csRaw (kline) data for each market and interval';


-- crypto_platform.order_ticket definition
DROP TABLE IF EXISTS `order_ticket`;
CREATE TABLE `order_ticket` (
                                `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                `account_id` bigint unsigned NOT NULL,
                                `market_id` bigint unsigned NOT NULL,
                                `side` enum('BUY','SELL') NOT NULL,
                                `type` enum('LIMIT','MARKET') NOT NULL,
                                `price` decimal(36,18) DEFAULT NULL,
                                `qty` decimal(36,18) NOT NULL,
                                `status` enum('NEW','PARTIALLY_FILLED','FILLED','CANCELLED','REJECTED') NOT NULL,
                                `created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                PRIMARY KEY (`id`),
                                KEY `idx_account_time` (`account_id`,`created_at`),
                                KEY `idx_market_time` (`market_id`,`created_at`),
                                KEY `idx_status` (`status`),
                                CONSTRAINT `fk_order_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
                                CONSTRAINT `fk_order_market` FOREIGN KEY (`market_id`) REFERENCES `market` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
                                CONSTRAINT `chk_limit_price` CHECK (((`type` <> _utf8mb4'LIMIT') or (`price` is not null)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Order tickets placed by accounts';


-- crypto_platform.position_avg_cost definition
DROP TABLE IF EXISTS `position_avg_cost`;
CREATE TABLE `position_avg_cost` (
                                     `account_id` bigint unsigned NOT NULL,
                                     `asset_id` bigint unsigned NOT NULL,
                                     `qty` decimal(36,18) NOT NULL,
                                     `avg_cost_quote_asset_id` bigint unsigned NOT NULL,
                                     `avg_cost_per_unit` decimal(36,18) NOT NULL,
                                     `realized_pnl` decimal(36,18) NOT NULL DEFAULT '0.000000000000000000',
                                     `updated_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                                     PRIMARY KEY (`account_id`,`asset_id`),
                                     KEY `fk_poscost_asset` (`asset_id`),
                                     KEY `fk_poscost_quote_asset` (`avg_cost_quote_asset_id`),
                                     CONSTRAINT `fk_poscost_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                     CONSTRAINT `fk_poscost_asset` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                     CONSTRAINT `fk_poscost_quote_asset` FOREIGN KEY (`avg_cost_quote_asset_id`) REFERENCES `asset` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Per-account average cost basis and realized PnL for each asset';


-- crypto_platform.history definition
DROP TABLE IF EXISTS `history`;
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