package ch.bfh.ti.blk2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoinRpcException;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
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
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            DatabaseConnector connector = new DatabaseConnector();
            for(OpCode code: OpCode.values()){
                connector.saveOpCodeToDatabase(code);
            }
            for(int i=client.getBlockCount()-20;i<=client.getBlockCount();i++) {

                BitcoindRpcClient.Block blk = client.getBlock(i);
                //System.out.println(gson.toJson(blk));
                connector.saveBlockToDatabase(blk);
                for(BitcoindRpcClient.RawTransaction tx : blk.tx().stream().map(x->client.decodeRawTransaction(client.getRawTransactionHex(x))).collect(Collectors.toList())){
                    //System.out.println(gson.toJson(tx));
                    connector.saveTransactionToDatabase(tx, blk.hash());
                    for(BitcoindRpcClient.RawTransaction.Out vout: tx.vOut()){
                        connector.saveVOutToDatabase(vout, tx.txId());
                        int line = 0;
                        for(String l: parseOutScript(vout.scriptPubKey().asm())){
                            if(l.startsWith("OP"))
                                connector.saveVOutLineToDatabase(tx.txId(), vout.n(), line, OpCode.valueOf(l), "");
                            else {
                                connector.saveVOutLineToDatabase(tx.txId(), vout.n(), line, OpCode.OP_NONE, l);
                            }
                            line++;
                        }
                    }
                    int vinCount = 0;
                    for(BitcoindRpcClient.RawTransaction.In vin:tx.vIn()){
                        connector.saveVInToDatabase(vin , tx.txId(), vinCount);
                        int line = 0;
                        if(vin.scriptSig()!=null) {
                            for (String l : parseOutScript(vin.scriptSig().get(vin.txid() != null ? "asm" : "coinbase").toString())) {
                                if (l.startsWith("OP"))
                                    connector.saveVinLineToDatabase(tx.txId(), vinCount, line, OpCode.valueOf(l), "");
                                else {
                                    connector.saveVinLineToDatabase(tx.txId(), vinCount, line, OpCode.OP_NONE, l);
                                }
                                line++;
                            }
                        }
                        vinCount++;
                    }
                }
            }
        }
        catch (Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }


    private String[] parseOutScript(String asm){
        return asm.split(" ");
    }




}
