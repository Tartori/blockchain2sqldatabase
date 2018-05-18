package ch.bfh.ti.blk2;

import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLType;

public class SqlStatements {

    public static final String BLOCK_INSERT_STATEMENT = "INSERT INTO `block` (`block_hash`, `prev_block_hash`, `height`, `size`, `version`, `merkleroot`, `time`, `difficulty`, `chainwork`, `nonce`) VALUES (?,?,?,?,?,?,?,?,?,?)";
    public static final String TRANSACTION_INSERT_STATEMENT = "INSERT INTO `transaction` (`txid`, `block_hash`, `txhash`, `version`, `size`, `locktime`) VALUES (?,?,?,?,?,?)";
    public static final String VOUT_INSERT_STATEMENT = "INSERT INTO `vout` (`txid`, `voutid`, `value`, `type`) VALUES (?,?,?,?)";
    public static final String OUTPUT_SCRIPT_INSTRUCTION_INSERT_STATEMENT = "INSERT INTO `output_script_instruction` (`txid`, `voutid`, `line`, `opcode`, `value`) VALUES (?,?,?,?,?)";
    public static final String VIN_INSERT_STATEMENT= "INSERT INTO `vin` (`txid`, `vinid`, `txidout`, `voutid`) VALUES (?,?,?,?)";
    public static final String VIN_INSTRUCTION_INSERT_STATEMENT  = "INSERT INTO `input_script_instruction` (`txid`, `vinid`, `line`, `opcode`, `value`)  VALUES (?,?,?,?,?)";
    public static final String VIN_WITNESS_INSERT_STATEMENT = "INSERT INTO `txinwitness` (`txid`, `vinid`, `witnessnr`, `value`) VALUES (?,?,?,?)";
    public static final String OP_CODE_INSERT_STATEMENT = "INSERT INTO `op_code` (`code`, `name`) VALUES (?,?)";

    public static final String MAX_BLOCK_SELECT = "SELECT max(height) FROM `block`;";
}
