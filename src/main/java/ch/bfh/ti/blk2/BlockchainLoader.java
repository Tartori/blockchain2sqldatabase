package ch.bfh.ti.blk2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoinRpcException;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class BlockchainLoader {
    URL blockchainUrl;
    BitcoinJSONRPCClient client;
    HashMap<Integer, Long> times;


    public BlockchainLoader() throws MalformedURLException {
        times=new HashMap<>();
        blockchainUrl= new URL("http://user:password@127.0.0.1:18332");
        client = new BitcoinJSONRPCClient(blockchainUrl);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            public void run() {
                times.forEach((x,y)->System.out.println("["+x+"]"+y/1000));
            }
        }));
    }

    public void load(){
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            DatabaseConnector connector = new DatabaseConnector();
            int maxBlockInDb = connector.getHighestBlockCount();
            if(maxBlockInDb == 0){
                connector.clearDatabase();
                for(OpCode code: OpCode.values()){
                    connector.saveOpCodeToDatabase(code);
                }

            }

            connector.executeOpCodeBatches();
            for(int i=maxBlockInDb+1;i<=client.getBlockCount();i++) {
                long start= System.currentTimeMillis();
                BitcoindRpcClient.Block blk = client.getBlock(i);
                //System.out.println(gson.toJson(blk));
                connector.saveBlockToDatabase(blk);
                if(i==0)continue; //fix because the bitcoin client doesn't recognize the transaction of the genesis block
                for(BitcoindRpcClient.RawTransaction tx : (Iterable<? extends BitcoindRpcClient.RawTransaction>) blk.tx().parallelStream().map(x->
                        client.decodeRawTransaction(client.getRawTransactionHex(x)))::iterator){
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
                connector.executeBatches();
                times.put(i, System.currentTimeMillis()-start);
            }
        }
        catch (Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
        }
        System.out.println(new Date());
    }


    private String[] parseOutScript(String asm){
        return asm.split(" ");
    }




}
