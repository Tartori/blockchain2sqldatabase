package ch.bfh.ti.blk2;

import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import javax.xml.transform.Result;
import java.sql.*;

public class DatabaseConnector {
    Connection con;
    PreparedStatement blockStatement;
    PreparedStatement transactionStatement;
    PreparedStatement voutStatement;
    PreparedStatement voutInstructionStatement;
    PreparedStatement vinStatement;
    PreparedStatement vinInstructionStatement;
    PreparedStatement witnessStatement;
    PreparedStatement opCodeStatement;


    public DatabaseConnector() throws ClassNotFoundException, SQLException {
        Class.forName("org.mariadb.jdbc.Driver");
        con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/testnet3", "testnet3", "testnet3");
        con.setAutoCommit(false);
        blockStatement = con.prepareStatement(SqlStatements.BLOCK_INSERT_STATEMENT);
        transactionStatement = con.prepareStatement(SqlStatements.TRANSACTION_INSERT_STATEMENT);
        voutStatement= con.prepareStatement(SqlStatements.VOUT_INSERT_STATEMENT);
        voutInstructionStatement = con.prepareStatement(SqlStatements.OUTPUT_SCRIPT_INSTRUCTION_INSERT_STATEMENT);
        vinStatement= con.prepareStatement(SqlStatements.VIN_INSERT_STATEMENT);
        vinInstructionStatement= con.prepareStatement(SqlStatements.VIN_INSTRUCTION_INSERT_STATEMENT);
        witnessStatement = con.prepareStatement(SqlStatements.VIN_WITNESS_INSERT_STATEMENT);
        opCodeStatement = con.prepareStatement(SqlStatements.OP_CODE_INSERT_STATEMENT);
    }

    public void clearDatabase() throws SQLException {
        Statement s = con.createStatement();
        s.execute("delete from `txinwitness`;");
        s.execute("delete from `input_script_instruction`;");
        s.execute("delete from `vin`;");
        s.execute("delete from `output_script_instruction`;");
        s.execute("delete from `op_code`;");
        s.execute("delete from `vout`;");
        s.execute("delete from `transaction`;");
        s.execute("delete from `block`;");
        con.commit();
    }

    public int getHighestBlockCount() throws SQLException{
        Statement s = con.createStatement();
        ResultSet rs = s.executeQuery(SqlStatements.MAX_BLOCK_SELECT);
        int max = 0;
        while(rs.next()){
            max=rs.getInt(1);
        }
        return max;
    }

    public void saveBlockToDatabase(BitcoindRpcClient.Block block) throws SQLException {
        blockStatement.setString(1, block.hash());
        blockStatement.setString(2, block.previousHash());
        blockStatement.setInt(3, block.height());
        blockStatement.setInt(4, block.size());
        blockStatement.setInt(5, block.version());
        blockStatement.setString(6, block.merkleRoot());
        blockStatement.setTimestamp(7, convertJavaToSqlDate(block.time()));
        blockStatement.setDouble(8, block.difficulty());
        blockStatement.setString(9, block.chainwork());
        blockStatement.setLong(10, block.nonce());
        blockStatement.addBatch();
    }

    public void saveTransactionToDatabase(BitcoindRpcClient.RawTransaction tx, String blockHash) throws SQLException {
        transactionStatement.setString(1, tx.txId());
        transactionStatement.setString(2, blockHash);
        transactionStatement.setString(3, tx.hash());
        transactionStatement.setInt(4, tx.version());
        transactionStatement.setLong(5, tx.size());
        transactionStatement.setLong(6, tx.lockTime());
        transactionStatement.addBatch();
    }

    public void saveVOutToDatabase(BitcoindRpcClient.RawTransaction.Out tx, String txid) throws SQLException {
        voutStatement.setString(1, txid);
        voutStatement.setInt(2, tx.n());
        voutStatement.setDouble(3, tx.value());
        voutStatement.setString(4, tx.scriptPubKey().type());
        voutStatement.addBatch();
    }

    public void saveVOutLineToDatabase(String txid, int voutid, int line, OpCode code, String value) throws SQLException {
        generateInstructionStatement(voutInstructionStatement, txid, voutid, line, code, value);
        voutInstructionStatement.addBatch();
    }

    public void saveVinLineToDatabase(String txid, int vinid, int line, OpCode code, String value) throws SQLException {
        generateInstructionStatement(vinInstructionStatement, txid, vinid, line, code, value);
        vinInstructionStatement.addBatch();
    }

    public void saveVInToDatabase(BitcoindRpcClient.RawTransaction.In tx, String txid, int vinid) throws SQLException {
        vinStatement.setString(1, txid);
        vinStatement.setInt(2, vinid);
        vinStatement.setString(3, tx.txid());
        vinStatement.setInt(4, tx.txid() == null ? 0 : tx.vout());
        vinStatement.addBatch();
    }

    public void saveInWitnessToDatabase(String txid, int vinid, int witnessNr, String value) throws SQLException {
        witnessStatement.setString(1, txid);
        witnessStatement.setInt(2, vinid);
        witnessStatement.setInt(3, witnessNr);
        witnessStatement.setString(4, value);
        witnessStatement.addBatch();
    }

    public void saveOpCodeToDatabase(OpCode code) throws SQLException {
        opCodeStatement.setInt(1, code.getCodeNumber());
        opCodeStatement.setString(2, code.name());
        opCodeStatement.addBatch();
    }

    public void executeOpCodeBatches() throws SQLException{
        opCodeStatement.executeBatch();
        con.commit();
    }

    public void executeBatches() throws SQLException {
        blockStatement.executeBatch();
        transactionStatement.executeBatch();
        voutStatement.executeBatch();
        voutInstructionStatement.executeBatch();
        vinStatement.executeBatch();
        vinInstructionStatement.executeBatch();
        witnessStatement.executeBatch();
        con.commit();
    }

    private void generateInstructionStatement(PreparedStatement statement, String txid, int vid, int line, OpCode code, String value) throws SQLException {
        statement.setString(1, txid);
        statement.setInt(2, vid);
        statement.setInt(3, line);
        statement.setInt(4, code.getCodeNumber());
        statement.setString(5, value);
    }


    private static java.sql.Timestamp convertJavaToSqlDate(java.util.Date toConvert) {
        return new java.sql.Timestamp(toConvert.getTime());
    }
}

