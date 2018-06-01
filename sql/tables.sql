/* CREATE USER 'testnet3'@'localhost' IDENTIFIED BY 'testnet3';
*/

CREATE DATABASE `testnet3` /*!40100 COLLATE 'latin1_swedish_ci' */;

USE `testnet3`;

GRANT SELECT, EXECUTE, SHOW VIEW, ALTER, ALTER ROUTINE, CREATE, CREATE ROUTINE, CREATE TEMPORARY TABLES, CREATE VIEW, DELETE, DROP, EVENT, INDEX, INSERT, REFERENCES, TRIGGER, UPDATE, LOCK TABLES  ON `testnet3`.* TO 'testnet3'@'localhost' WITH GRANT OPTION;


CREATE TABLE `block` (
	`block_hash` VARCHAR(64) NOT NULL,
	`prev_block_hash` VARCHAR(64) NULL DEFAULT NULL,
	`height` INT(11) NOT NULL,
	`size` INT(11) NOT NULL,
	`version` INT(11) NOT NULL,
	`merkleroot` VARCHAR(64) NOT NULL,
	`time` DATETIME NOT NULL,
	`difficulty` DOUBLE NOT NULL,
	`chainwork` VARCHAR(64) NOT NULL,
	`nonce` BIGINT(20) NOT NULL,
	PRIMARY KEY (`block_hash`),
	INDEX `height` (`height`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB
;


CREATE TABLE `transaction` (
	`txid` VARCHAR(64) NOT NULL,
	`block_hash` VARCHAR(64) NOT NULL,
	`txhash` VARCHAR(64) NULL DEFAULT NULL,
	`version` INT(11) NULL DEFAULT NULL,
	`size` BIGINT(20) NULL DEFAULT NULL,
	`locktime` BIGINT(20) NULL DEFAULT NULL,
	PRIMARY KEY (`txid`),
	INDEX `txhash` (`txhash`),
	CONSTRAINT `block_transaction` FOREIGN KEY (`block_hash`) REFERENCES `block` (`block_hash`) ON UPDATE CASCADE ON DELETE CASCADE
)
ENGINE=InnoDB
;

CREATE TABLE `vout` (
	`txid` VARCHAR(64) NOT NULL,
	`voutid` INT(11) NOT NULL,
	`value` DECIMAL(65,30) NULL DEFAULT '0',
	`type` VARCHAR(50) NULL DEFAULT NULL,
	INDEX `transaction_vout` (`txid`),
	PRIMARY KEY (`txid`, `voutid`),
	CONSTRAINT `transaction_vout` FOREIGN KEY (`txid`) REFERENCES `transaction` (`txid`) ON UPDATE CASCADE ON DELETE CASCADE
	)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB
;


CREATE TABLE `op_code` (
	`code` INT(11) NOT NULL,
	`name` VARCHAR(50) NOT NULL,
	PRIMARY KEY (`code`, `name`),
	INDEX `name` (`name`),
	INDEX `code` (`code`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB
;


CREATE TABLE `output_script_instruction` (
	`txid` VARCHAR(64) NOT NULL,
	`voutid` INT(11) NOT NULL,
	`line` INT(11) NOT NULL,
	`opcode` INT(11) NULL DEFAULT NULL,
	`value` MEDIUMTEXT NULL DEFAULT NULL,
	PRIMARY KEY (`txid`, `voutid`, `line`),
	INDEX `opcode` (`opcode`),
	CONSTRAINT `opcode` FOREIGN KEY (`opcode`) REFERENCES `op_code` (`code`) ON UPDATE CASCADE,
	CONSTRAINT `vout_instruction` FOREIGN KEY (`txid`, `voutid`) REFERENCES `vout` (`txid`, `voutid`) ON UPDATE CASCADE ON DELETE CASCADE
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB
;




CREATE TABLE `vin` (
	`txid` VARCHAR(64) NOT NULL,
	`vinid` INT NOT NULL,
	`txidout` VARCHAR(64) NULL,
	`voutid` INT NULL,
	PRIMARY KEY (`txid`, `vinid`),
	INDEX `txidout_voutid` (`txidout`, `voutid`),
	CONSTRAINT `vout_vin` FOREIGN KEY (`txidout`, `voutid`) REFERENCES `vout` (`txid`, `voutid`) ON UPDATE RESTRICT,
	CONSTRAINT `transaction_vin` FOREIGN KEY (`txid`) REFERENCES `transaction` (`txid`) ON UPDATE CASCADE ON DELETE CASCADE
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB
;



CREATE TABLE `input_script_instruction` (
	`txid` VARCHAR(64) NOT NULL,
	`vinid` INT NOT NULL,
	`line` INT NOT NULL,
	`opcode` INT NULL DEFAULT NULL,
	`value` MEDIUMTEXT NULL DEFAULT NULL,
	PRIMARY KEY (`txid`, `vinid`, `line`),
	CONSTRAINT `vin_script` FOREIGN KEY (`txid`, `vinid`) REFERENCES `vin` (`txid`, `vinid`) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT `vin_opcode` FOREIGN KEY (`opcode`) REFERENCES `op_code` (`code`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB
;


CREATE TABLE `txinwitness` (
	`txid` VARCHAR(64) NOT NULL,
	`vinid` INT NOT NULL,
	`witnessnr` INT NOT NULL,
	`value` MEDIUMTEXT NULL,
	PRIMARY KEY (`txid`, `vinid`, `witnessnr`),
	CONSTRAINT `vin_witness` FOREIGN KEY (`txid`, `vinid`) REFERENCES `vin` (`txid`, `vinid`) ON UPDATE CASCADE ON DELETE CASCADE
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB
;
