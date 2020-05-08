package blockchain;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Random;

public class TransactionBot implements Runnable {
    String name;
    private ArrayList<String> peopleNames;
    BlockChain blockChain;
    int maxSleepDuration = 150;
    volatile boolean isStopped = false;
    private PrivateKey key;

    public TransactionBot(String name, ArrayList<String> peopleNames, PrivateKey privateKey, BlockChain blockChain) {
        this.name = name;
        this.peopleNames = peopleNames;
        this.blockChain = blockChain;
        this.key = privateKey;
    }

    void SendRandomTransaction() {
        Random rand = new Random();
        try {
            int coins = rand.nextInt(50) + 1;
            String recipient = peopleNames.get(rand.nextInt(peopleNames.size()));
            long id = blockChain.getNextTransactionId();
            Transaction transaction = new Transaction(id, name, recipient, coins, key);
            blockChain.addTransaction(transaction);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void stop() {
        isStopped = true;
    }

    @Override
    public void run() {
        long sleepTimer = new Random().nextInt(maxSleepDuration);

        while (!isStopped) {
            SendRandomTransaction();
            try {
                Thread.sleep(sleepTimer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}





