package blockchain;

import java.security.PrivateKey;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ChatEmulator {
    private static ChatEmulator instance = new ChatEmulator();
    HashMap<String, PrivateKey> privateKeysLedger;
    int threads = 1;
    Set<TransactionBot> bots;
    ExecutorService executorService;

    public static ChatEmulator getInstance() {
        return instance;
    }

    void haveChatAt(BlockChain blockChain) {
        bots = new HashSet<>();
        executorService = Executors.newFixedThreadPool(threads);
        ArrayList<String> people = new ArrayList<>(privateKeysLedger.keySet());
        privateKeysLedger.forEach((name, key) -> bots.add(new TransactionBot(name, people, key, blockChain)));
        bots.forEach(executorService::submit);
    }


    private ChatEmulator() {
    }

    void stop() {
        bots.forEach(TransactionBot::stop);
        executorService.shutdown();
    }
}