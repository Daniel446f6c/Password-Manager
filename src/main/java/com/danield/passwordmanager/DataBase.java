package com.danield.passwordmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Vector;

import com.danield.protector.AES;
import com.danield.protector.SHA;

/**
 * The {@code DataBase} class represents the model of the {@link PasswordManagerApp Application}.
 * <p>
 * Its responsibilities are to read, write, manipulate, encrypt, decrypt and hash data.
 * @author Daniel D
 */
public class DataBase {

    private static final Vector<UserCredentials> ENTRYS = new Vector<UserCredentials>();
    private static final Charset CHARSET = StandardCharsets.ISO_8859_1;    
    private static final char SEPARATOR = '°';
    private static final int KEY_BYTE_LENGTH = 32;
    private static String filePath = "";
    private static String key = "";

    private DataBase() {}; // we don't want this class to be instantiated.

    /**
     * TODO method description.
     * @return
     */
    private static byte[] prepareEntrysForEncryption() {
 
        StringBuilder stringBuilder = new StringBuilder(); 
        for (UserCredentials entry : DataBase.ENTRYS) {
            stringBuilder.append(entry.getApplication());
            stringBuilder.append(DataBase.SEPARATOR);
            stringBuilder.append(entry.getUsername());
            stringBuilder.append(DataBase.SEPARATOR);
            stringBuilder.append(entry.getPassword());
            stringBuilder.append(DataBase.SEPARATOR);
        }
        return stringBuilder.toString().getBytes(DataBase.CHARSET);

    }

    /**
     * TODO method description.
     * @return
     */
    private static byte[] encryptEntrys() {
        
        try {
            return AES.encrypt(prepareEntrysForEncryption(), DataBase.key, null);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }

    }

    /**
     * TODO method description.
     * @param entrys
     * @return
     */
    private static byte[] decryptEntrys(byte[] entrys) {

        try {
            return AES.decrypt(entrys, DataBase.key, null);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }

    }

    /**
     * Clear the {@code DataBase}.
     */
    public static void clear() {

        DataBase.key = "";
        DataBase.filePath = "";
        DataBase.ENTRYS.clear();
        System.gc(); // run the garbage collector. (hopefully make unused memory like the password string unaccessible, even for an attacker)

    }

    /**
     * Set the key.
     * @param key : the key
     */
    public static void setKey(String key) {
        DataBase.key = key;
    }

    /**
     * Set the file path.
     * @param filePath : the file path.
     */
    public static void setFilePath(String filePath) {
        DataBase.filePath = filePath;
    }

    /**
     * Get the entrys.
     * @return the entrys
     */
    public static Vector<UserCredentials> getEntrys() {
        return DataBase.ENTRYS;
    }

    /**
     * Get the entry separator.
     * @return the separator
     */
    public static char getSeparator() {
        return DataBase.SEPARATOR;
    }    
        
    /**
     * Appends a {@link UserCredentials entry} to the end of {@code DataBase}.
     * @param uc : the entry
     */
    public static void addNewEntry(UserCredentials uc) {
        DataBase.ENTRYS.add(uc);
    }

    /**
     * Inserts a {@link UserCredentials entry} at the specified {@code index} in the {@code DataBase}.
     * @param uc : the entry
     * @param index : the index
     * @throws ArrayIndexOutOfBoundsException if the index is out of range (index < 0 || index > DataBase.getEntrys().size())
     */
    public static void addNewEntryAt(UserCredentials uc, int index) {
        DataBase.ENTRYS.add(index, uc);
    }

    /**
     * TODO method description.
     */
    public static void writeToFile() {

        try (FileOutputStream fOutputStream = new FileOutputStream(new File(filePath))) {
            fOutputStream.write(SHA.SHA256(key.getBytes(CHARSET)));
            fOutputStream.write(encryptEntrys());
        }
        catch (FileNotFoundException e) {
            if (filePath.equals("")) {
                return;
            }
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * TODO method description.
     * @return the hash
     */
    public static byte[] readHashedKeyFromFile() {

        try (FileInputStream fileInputStream = new FileInputStream(new File(filePath))) {
            byte[] key = new byte[KEY_BYTE_LENGTH];
            fileInputStream.read(key);
            return key;
        }
        catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
        
    }

    /**
     * TODO method description.
     */
    public static void readEntrysFromFile() {

        byte[] data = {};
        try (FileInputStream fileInputStream = new FileInputStream(new File(filePath))) {
            data = fileInputStream.readAllBytes();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (data.length == KEY_BYTE_LENGTH || data.length == 0) {
            return;
        }

        String[] entrys = new String(decryptEntrys(Arrays.copyOfRange(data, DataBase.KEY_BYTE_LENGTH, data.length)), DataBase.CHARSET).split(String.valueOf(DataBase.SEPARATOR));

        String tmpApp = "";
        String tmpUser = "";
        String tmpPw = "";
        int count = 0;
        for (String entry : entrys) {
            if (count == 0) {
                tmpApp = entry;
                count++;
            }
            else if (count == 1) {
                tmpUser = entry;
                count++;
            }
            else if (count == 2) {
                tmpPw = entry;
                DataBase.ENTRYS.add(new UserCredentials(tmpApp, tmpUser, tmpPw));
                count = 0;
            }
        }

    }

}
