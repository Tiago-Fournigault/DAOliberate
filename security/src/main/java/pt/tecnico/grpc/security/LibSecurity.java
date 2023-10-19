package pt.tecnico.grpc.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.InvalidKeyException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.DecoderException;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKey;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.BadPaddingException;

import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.FileInputStream;

import java.util.Random;
import java.util.Scanner;

/**
 * The LibSecurity class works as a library that contains cryptographic methods used by other classes.
 */
public class LibSecurity {
    
    public static String getSecurePassword(String password, byte[] salt) {

        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    public static byte[] getSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    public static String getRandomSecret() {
        int length = 10;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
 
        StringBuffer randomString = new StringBuffer(length);
        Random random = new Random();
    
        for(int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            randomString.append(randomChar);
        }
    
        return randomString.toString();
    }

    public static void writeObjectEncrypted(Serializable object, String filename, String password) 
        throws IllegalBlockSizeException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException
    {
        int iterations = 1000;
        char[] chars = password.toCharArray();
        byte[] salt = getSalt();
        int keyLength = 32 * 8;

        byte[] key = hashPassword(chars, salt, iterations, keyLength);
        if(key == null) {
            throw new InvalidKeyException();
        }

        FileWriter myWriter = new FileWriter(filename + "-info");
        myWriter.write(iterations + ":" + Hex.encodeHexString(salt));
        myWriter.close();

        // Length is 32 byte
        SecretKeySpec sks = new SecretKeySpec(key, "AES");

        // Create cipher
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, sks);

        SealedObject sealedObject = new SealedObject(object, cipher);

        // Wrap the output stream
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filename));
        outputStream.writeObject(sealedObject);
        outputStream.flush();
    }

    private static byte[] hashPassword(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKey key = skf.generateSecret(spec);
            byte[] hash_password = key.getEncoded();
            return hash_password;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object readObjectEncrypted(String filename, String password) 
        throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, FileNotFoundException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException
    {
        char[] chars = password.toCharArray();

        File file = new File(filename + "-info");
        Scanner reader = new Scanner(file);
        String[] content = reader.nextLine().split(":");
        reader.close();

        int iterations = Integer.parseInt(content[0]);
        byte[] salt = null;
        try{
            salt = Hex.decodeHex(content[1].toCharArray());
        } catch (DecoderException e) {
           throw new InvalidKeyException();
        }
        int keyLength = 32 * 8;

        byte[] key = hashPassword(chars, salt, iterations, keyLength);
        if(key == null) {
            throw new InvalidKeyException();
        }
        
        SecretKeySpec sks = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, sks);

        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename));
        SealedObject sealedObject = (SealedObject) inputStream.readObject();
        return sealedObject.getObject(cipher);
    }
}
