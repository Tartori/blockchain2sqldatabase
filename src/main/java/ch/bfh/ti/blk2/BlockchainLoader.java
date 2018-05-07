package ch.bfh.ti.blk2;

import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.sql.*;

public class BlockchainLoader {



    public void saveBlockToDatabase(BitcoindRpcClient.Block block) throws SQLException, ClassNotFoundException {
        Class.forName("org.mariadb.jdbc.Driver");
        Connection con= DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/testnet3","testnet3","testnet3");
        Statement d = con.createStatement();
        d.execute("delete from testnet3.block;");
        PreparedStatement s = InsertStatements.generateBlockStatement(con, block);
        s.execute();
    }
}
