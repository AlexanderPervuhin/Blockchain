package blockchain;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

class BlockChain implements Serializable {
    private static final long serialVersionUID = 10L;
    public static final long MINER_REWARD = 100;
    public static final long STARTING_BALANCE = 100;
    private static final int UPPER_TIME_LIMIT = 10;
    private static final int LOWER_TIME_LIMIT = 1;
    private static final int ZEROES_IN_HASH_LIMIT = 5;

    ArrayList<Block> blocks = new ArrayList<>();
    private int zeroesInHash;
    List<Transaction> transactionsBuffer = Collections.synchronizedList(new ArrayList<>());
    private long transactionIdCounter = 0;
    long lastBlockMaxId = 0;

    synchronized void addTransaction(Transaction transaction) {
        if (canAdd(transaction)) {
            transactionsBuffer.add(transaction);
            transactionIdCounter += 1;
        }
    }

    boolean canAdd(Transaction transaction) {
        String issuer = transaction.getIssuer();
        String recipient = transaction.getRecipient();
        boolean result = false;
        if (transaction.getId() > lastBlockMaxId
                && transaction.getCoins() > 0
                && !issuer.equals(recipient)) {
            long balance = getBalanceOf(issuer) + getBalanceFromBufferOf(issuer);
            if (balance - transaction.getCoins() >= 0) {
                result = true;
            }
        }
        return result;

    }


    long getNextTransactionId() {
        return transactionIdCounter + 1;
    }

    public int getZeroesInHash() {
        return zeroesInHash;
    }

    public int size() {
        return blocks.size();
    }

    BlockChain(int zeroesInHash) {
        this.zeroesInHash = zeroesInHash;
    }


    Block getBlockBy(int id) {
        return blocks.get(id - 1);
    }

    Block getLastBlock() {
        return (size() > 0) ? blocks.get(size() - 1) : null;
    }

    void add(Block block) {
        if (canAdd(block)) {
            blocks.add(block);
            adjustZeroesInHash();
            lastBlockMaxId = block.getMaxTransactionId();
        }
    }


    void adjustZeroesInHash() {
        long elapsedTime = Stopwatch.getElapsedSeconds();
        printGenerationTimeMsg(elapsedTime);
        if (elapsedTime > UPPER_TIME_LIMIT) {
            zeroesInHash -= 1;
            System.out.printf("N was decreased to %d\n\n", zeroesInHash);
        } else if (elapsedTime < LOWER_TIME_LIMIT) {
            zeroesInHash += 1;
            System.out.printf("N was increased to %d\n\n", zeroesInHash);
        } else
            System.out.println("N stays the same\n\n");

        zeroesInHash = Math.min(ZEROES_IN_HASH_LIMIT, zeroesInHash);
    }

    private static void printGenerationTimeMsg(long sec) {
        System.out.printf("Block was generating for %d seconds\n", sec);
    }

    boolean canAdd(Block block) {
        return block.hasValid(zeroesInHash) & canHaveValid(block);
    }


    boolean canHaveValid(Block block) {
        int id = block.id;
        boolean isValid = true;
        if (id > 1) {
            String previousHash = block.previousHash;
            Block previousBlock = getBlockBy(id - 1);
            String hash = previousBlock.generateHash();
            long maxTransactionId = previousBlock.getMaxTransactionId();
            if (!hash.equals(previousHash) && block.transactionIdsAreGreater(maxTransactionId)) {
                isValid = false;
            }
        }
        return isValid;
    }


    boolean isValidAccordingTo(HashMap<String, PublicKey> publicKeysLedger) throws Exception {
        boolean chainIsValid = true;
        for (Block block : blocks) {
            if (!canHaveValid(block) && block.transactionsAreTrueTo(publicKeysLedger)
            ) {
                chainIsValid = false;
                break;
            }
        }
        return chainIsValid;
    }

    void printAllBlocks() {
        for (Block block : blocks
        ) {
            block.printInfo();
            System.out.println();
        }
    }

    synchronized long getBalanceOf(String name) {
        long balance = STARTING_BALANCE;
        for (Block block :
                blocks) {
            balance += block.getBalanceChangeFor(name);
        }
        return balance;
    }

    synchronized long getBalanceFromBufferOf(String name) {
        long balance = 0;
        for (Transaction t :
                transactionsBuffer) {
            balance += t.getBalanceChangeFor(name);
        }
        return balance;
    }
}


class Block implements Serializable {
    private static final long serialVersionUID = 8L;
    String previousHash;
    long timesStamp = new Date().getTime();
    int id;
    int magicNumber;
    int minerId;
    ArrayList<Transaction> transactions;

    Block(String hash, int id, ArrayList<Transaction> transactions) {
        this.previousHash = hash;
        this.id = id;
        this.transactions = transactions;
    }

    boolean transactionIdsAreGreater(long n) {
        return transactions.stream()
                .allMatch(m -> m.getId() > n);
    }

    long getMaxTransactionId() {
        return transactions.stream()
                .mapToLong(Transaction::getId)
                .max()
                .orElse(0);
    }


    boolean transactionsAreTrueTo(HashMap<String, PublicKey> publicKeysLedger) throws Exception {
        boolean transactionsAreValid = true;
        for (Transaction t :
                transactions) {
            String author = t.getIssuer();
            PublicKey key = publicKeysLedger.get(author);
            if (!t.verify(key)) {
                transactionsAreValid = false;
                break;
            }
        }
        return transactionsAreValid;
    }

    synchronized long getBalanceChangeFor(String name) {
        long balanceChange = 0;
        for (Transaction t :
                transactions) {
            balanceChange += t.getBalanceChangeFor(name);
        }
        if (name.equals("miner" + minerId)) balanceChange += BlockChain.MINER_REWARD;
        return balanceChange;
    }


    void printInfo() {

        StringBuilder output = new StringBuilder("Block:" +
                "\nCreated by miner" + minerId +
                "\nminer" + minerId + " gets " + BlockChain.MINER_REWARD + " VC" +
                "\nId: " + id +
                "\nTimestamp: " + timesStamp +
                "\nMagic number: " + magicNumber +
                "\nHash of the previous block:\n" + previousHash +
                "\nHash of the block:\n" + generateHash() +
                "\nBlock data:");

        if (transactions.isEmpty()) {
            output.append("no transactions");
        } else {
            output.append("\n").append(transactionsToString());
        }
        System.out.println(output);

    }

    private String transactionsToString() {
        return transactions.stream()
                .map(Transaction::toString)
                .collect(Collectors.joining("\n"));
    }

    private String transactionsWithIdToString() {
        return transactions.stream()
                .map(transaction -> String.valueOf(transaction.getId()) + transaction)
                .collect(Collectors.joining(" "));
    }


    boolean hasValid(int zeroesInHash) {
        String validHashRegex = "0{" + zeroesInHash + "}\\w*";
        String hash = generateHash();
        return hash.matches(validHashRegex);
    }

    String generateHash() {
        return StringConverter.applySha256(previousHash + id + minerId + timesStamp + magicNumber + transactionsWithIdToString());
    }

    String generateHashUsing(int magicNumber) {
        return StringConverter.applySha256(previousHash + id + minerId + timesStamp + magicNumber + transactionsWithIdToString());
    }
}

