package ch.bfh.ti.blk2;

import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.sql.*;

public class DatabaseConnector {
    Connection con;

    public DatabaseConnector() throws ClassNotFoundException, SQLException {
        Class.forName("org.mariadb.jdbc.Driver");
        con= DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/testnet3","testnet3","testnet3");

    }


    public void saveBlockToDatabase(BitcoindRpcClient.Block block) throws SQLException {
        PreparedStatement s = InsertStatements.generateBlockStatement(con, block);
        s.execute();
    }

    public void saveTransactionToDatabase(BitcoindRpcClient.RawTransaction tx, String blockHash) throws SQLException {
        PreparedStatement s = InsertStatements.generateTransactionStatement(con, tx, blockHash);
        s.execute();
    }

    public void saveVOutToDatabase(BitcoindRpcClient.RawTransaction.Out tx, String txid) throws SQLException {
        PreparedStatement s = InsertStatements.generateVoutStatement(con, tx, txid);
        s.execute();
    }public void saveVOutLineToDatabase(String txid, int voutid, int line, OpCode code, String value) throws SQLException {
        PreparedStatement s = InsertStatements.generateVoutInstructionStatement(con, txid, voutid, line, code, value);
        s.execute();
    }public void saveVinLineToDatabase(String txid, int vinid, int line, OpCode code, String value) throws SQLException {
        PreparedStatement s = InsertStatements.generateVinInstructionStatement(con, txid, vinid, line, code, value);
        s.execute();
    }public void saveVInToDatabase(BitcoindRpcClient.TxInput tx, String txid, int vinid) throws SQLException {
        PreparedStatement s = InsertStatements.generateVinStatement(con, tx, txid, vinid);
        s.execute();
    }public void saveInWitnessToDatabase( String txid, int vinid, int witnessNr, String value) throws SQLException {
        PreparedStatement s = InsertStatements.generateWitnessStatement(con,  txid, vinid, witnessNr, value);
        s.execute();
    }public void saveOpCodeToDatabase(OpCode code) throws SQLException {
        PreparedStatement s = InsertStatements.generateOPcodeStatement(con, code);
        s.execute();
    }


}

