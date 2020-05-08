package blockchain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AsymmetricKeysGenerator {
    private KeyPairGenerator keyGen;
    private HashMap<String, PublicKey> publicKeysLedger;
    private HashMap<String, PrivateKey> privateKeysLedger;

    public AsymmetricKeysGenerator(int keyLength) throws NoSuchAlgorithmException {
        this.keyGen = KeyPairGenerator.getInstance("RSA");
        this.keyGen.initialize(keyLength);
    }

    public void assemblePrivateAndPublicLedgers(List<String> names) {
        int numberOfPairs = names.size();
        ArrayList<KeyPair> keyPairs = createPairs(numberOfPairs);
        publicKeysLedger = new HashMap<>();
        privateKeysLedger = new HashMap<>();

        for (int i = 0; i < numberOfPairs; i++
        ) {
            KeyPair pair = keyPairs.get(i);
            String name = names.get(i);
            publicKeysLedger.put(name, pair.getPublic());
            privateKeysLedger.put(name, pair.getPrivate());
        }
    }


    public ArrayList<KeyPair> createPairs(int numberOfPairs) {
        ArrayList<KeyPair> keyPairs = new ArrayList<>();
        for (int i = 0; i < numberOfPairs; i++) {
            keyPairs.add(createPair());
        }
        return keyPairs;
    }

    public KeyPair createPair() {
        return this.keyGen.generateKeyPair();
    }

    public HashMap<String, PublicKey> getPublicKeysLedger() {
        return publicKeysLedger;
    }

    public HashMap<String, PrivateKey> getPrivateKeysLedger() {
        return privateKeysLedger;
    }

    public void writeToFile(String path, byte[] key) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(key);
        fos.flush();
        fos.close();
    }

    public PrivateKey readPrivate(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public PublicKey readPublic(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }


}

