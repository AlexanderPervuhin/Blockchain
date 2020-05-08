package blockchain;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws Exception {
        int zeroesInHash = 0;
        AsymmetricKeysGenerator keysGenerator = new AsymmetricKeysGenerator(1024);
        ArrayList<String> botNames = new ArrayList();
        for (int i = 1; i <= 12; i++) botNames.add("miner" + i);
        keysGenerator.assemblePrivateAndPublicLedgers(botNames);
        BlockChainFactory chainFactory = BlockChainFactory.getInstance();
        HashMap<String, PublicKey> publicLedger = keysGenerator.getPublicKeysLedger();
        BlockChain blockChain = chainFactory.generateChainSizeOf(0, zeroesInHash);
        ChatEmulator chatEmulator = ChatEmulator.getInstance();
        chatEmulator.privateKeysLedger = keysGenerator.getPrivateKeysLedger();
        chatEmulator.haveChatAt(blockChain);
        chainFactory.extend(blockChain, 15);
        chatEmulator.stop();
       // SerializationUtils.serialize(blockChain, "blockChain.data");
    }
}




