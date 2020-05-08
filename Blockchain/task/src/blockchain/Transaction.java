package blockchain;

import java.io.Serializable;
import java.security.*;


public class Transaction implements Serializable {
    private String issuer;
    private String recipient;
    private long coins;
    private byte[] signature;
    private long id;

    public long getId() {
        return id;
    }

    public String getIssuer() {
        return issuer;
    }

    @Override
    public String toString() {
        return issuer + " sent " + coins + " VC " + "to " + recipient;
    }

    public String getRecipient() {
        return recipient;
    }

    public byte[] getSignature() {
        return signature;
    }

    public long getCoins() {
        return coins;
    }

    public byte[] getData() {
        return (id + this.toString()).getBytes();
    }


    public Transaction(long id, String issuer, String recipient, long coins, PrivateKey key) throws Exception {
        this.id = id;
        this.issuer = issuer;
        this.recipient = recipient;
        this.coins = coins > 0 ? coins : 0;
        this.signature = sign(id + this.toString(), key);

    }

    long getBalanceChangeFor(String name) {
        long balanceChange = 0;
        if (recipient.equals(name)) balanceChange += coins;
        else if (issuer.equals(name)) balanceChange -= coins;
        return balanceChange;
    }

    public byte[] sign(String data, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature rsa = Signature.getInstance("SHA384withRSA");
        rsa.initSign(key);
        rsa.update(data.getBytes());
        return rsa.sign();
    }

    public boolean verify(PublicKey key) throws Exception {
        Signature sig = Signature.getInstance("SHA384withRSA");
        sig.initVerify(key);
        sig.update(getData());
        return sig.verify(getSignature());
    }

}