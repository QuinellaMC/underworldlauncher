package ch.quinella.launcher.launcher.misc;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class PasswordCrypto {

    private SecretKey key;

    public PasswordCrypto(){

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            this.key = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    public PasswordCrypto(SecretKey key){

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            this.key = key;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    private static byte[] encrypt(final String message, SecretKey cle)
        throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, cle);
        byte[] donnees = message.getBytes();

        return cipher.doFinal(donnees);
    }

    private static String decrypt(final byte[] donnees, SecretKey cle)
        throws NoSuchAlgorithmException, NoSuchPaddingException,
    InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, cle);

        return new String(cipher.doFinal(donnees));
    }

    public byte[] generateToken(String message) {
        try {
            return(encrypt(message, key));
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String decryptToken(byte[] token){
        try {
            return(decrypt(token, key));
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public SecretKey getKey(){
        return key;
    }
}