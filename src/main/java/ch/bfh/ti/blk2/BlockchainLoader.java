package ch.bfh.ti.blk2;

import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

public class BlockchainLoader {
    URL blockchainUrl;
    BitcoinJSONRPCClient client;

    public BlockchainLoader() throws MalformedURLException {
        blockchainUrl= new URL("http://user:password@127.0.0.1:18332");
        client = new BitcoinJSONRPCClient(blockchainUrl);
    }


    public void load(){

        try {
            DatabaseConnector connector = new DatabaseConnector();
            for(OpCode code: OpCode.values()){
                connector.saveOpCodeToDatabase(code);
            }
            for(int i=client.getBlockCount()-20;i<=client.getBlockCount();i++) {
                BitcoindRpcClient.Block blk = client.getBlock(i);
                connector.saveBlockToDatabase(blk);
                for(BitcoindRpcClient.RawTransaction tx : blk.tx().stream().map(x->client.decodeRawTransaction(client.getRawTransactionHex(x))).collect(Collectors.toList())){
                    connector.saveTransactionToDatabase(tx, blk.hash());
                    for(BitcoindRpcClient.RawTransaction.Out vout: tx.vOut()){
                        connector.saveVOutToDatabase(vout, tx.txId());
                    }
                }
            }
        }
        catch (Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }




}
