package ch.bfh.ti.blk2;

import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertStatements {
    public static PreparedStatement generateBlockStatement(Connection con, BitcoindRpcClient.Block block) throws SQLException{
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO `block` (`block_hash`, `prev_block_hash`, `height`, `size`, `version`, `merkleroot`, `time`, `difficulty`, `chainwork`, `nonce`)" +
                        " VALUES (?,?,?,?,?,?,?,?,?,?)");
        statement.setString(1, block.hash());
        statement.setString(2, block.previousHash());
        statement.setInt(3, block.height());
        statement.setInt(4, block.size());
        statement.setInt(5, block.version());
        statement.setString(6, block.merkleRoot());
        statement.setTimestamp(7, convertJavaToSqlDate(block.time()));
        statement.setDouble(8, block.difficulty());
        statement.setString(9, block.chainwork());
        statement.setLong(10, block.nonce());
        return statement;
    }
    public static PreparedStatement generateTransactionStatement(Connection con, BitcoindRpcClient.RawTransaction tx,  String blockHash) throws SQLException{
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO `transaction` (`txid`, `block_hash`, `txhash`, `version`, `size`, `locktime`)" +
                        " VALUES (?,?,?,?,?,?)");
        statement.setString(1, tx.txId());
        statement.setString(2, blockHash);
        statement.setString(3, tx.hash());
        statement.setInt(4, tx.version());
        statement.setLong(5, tx.size());
        statement.setLong(6, tx.lockTime());
        return statement;
    }

    public static PreparedStatement generateVoutStatement(Connection con, BitcoindRpcClient.RawTransaction.Out tx, String txid) throws SQLException{
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO `vout` (`txid`, `voutid`, `value`, `type`)" +
                        " VALUES (?,?,?,?)");
        statement.setString(1, txid);
        statement.setInt(2, tx.n());
        statement.setDouble(3, tx.value());
        statement.setString(4, tx.scriptPubKey().type());
        return statement;
    }

    public static PreparedStatement generateVoutInstructionStatement(Connection con, String txid, int voutid, int line, OpCode code, String value) throws SQLException{
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO `output_script_instruction` (`txid`, `voutid`, `line`, `opcode`, `value`)" +
                        " VALUES (?,?,?,?,?)");
        return generateInstructionStatement(statement, txid, voutid, line, code, value);
    }


    public static PreparedStatement generateVinInstructionStatement(Connection con, String txid, int vinid, int line, OpCode code, String value) throws SQLException{
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO `input_script_instruction` (`txid`, `vinid`, `line`, `opcode`, `value`)" +
                        " VALUES (?,?,?,?,?)");
        return generateInstructionStatement(statement, txid, vinid, line, code, value);
    }

    private static PreparedStatement generateInstructionStatement(PreparedStatement statement, String txid, int vid, int line, OpCode code, String value)throws SQLException{
        statement.setString(1, txid);
        statement.setInt(2, vid);
        statement.setInt(3, line);
        statement.setInt(4, code.getCodeNumber());
        statement.setString(5, value);
        return statement;
    }

    public static PreparedStatement generateVinStatement(Connection con, BitcoindRpcClient.TxInput tx, String txid, int vinid) throws SQLException{
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO `vin` (`txid`, `vinid`, `txidout`, `voutid`)" +
                        " VALUES (?,?,?,?)");
        statement.setString(1, txid );
        statement.setInt(2, vinid);
        statement.setString(3, tx.txid());
        statement.setInt(4, tx.vout());
        return statement;
    }

    public static PreparedStatement generateWitnessStatement(Connection con, String txid, int vinid, int witnessNr, String value) throws SQLException{
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO `txinwitness` (`txid`, `vinid`, `witnessnr`, `value`)" +
                        " VALUES (?,?,?,?)");
        statement.setString(1, txid );
        statement.setInt(2, vinid);
        statement.setInt(3, witnessNr);
        statement.setString(4, value);
        return statement;
    }

    public static PreparedStatement generateOPcodeStatement(Connection con, OpCode code) throws SQLException{
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO `op_code` (`code`, `name`)" +
                        " VALUES (?,?)");
        statement.setInt(1, code.getCodeNumber());
        statement.setString(2, code.name());
        return statement;
    }




    private static java.sql.Timestamp convertJavaToSqlDate(java.util.Date toConvert) {
        return new java.sql.Timestamp(toConvert.getTime());
    }
}
