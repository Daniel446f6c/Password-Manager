package com.danield.passwordmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import com.danield.protector.*;

public class Test {
    


    public static void main(String[] args) {

        // TEST DATA
        ArrayList<String> plainStrings = new ArrayList<String>();
        for (int i = 1; i <= 15; i++) {
            plainStrings.add(new String(("GEHEIMER TEXT " + i + ",").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1));
        }

        StringBuilder stringBuilder = new StringBuilder();
        while (!plainStrings.isEmpty()) {
            stringBuilder.append(plainStrings.get(0));
            plainStrings.remove(0);
        }

        String data = stringBuilder.toString();

        // PRINT TEST DATA
        System.out.println(data);

        // TEST FILE
        File file = new File("C:\\Users\\Daniel\\Desktop\\TEST");

        HashMap<String, byte[]> testMap = new HashMap<>();

        // WRITE TO FILE
        try (FileOutputStream fOutputStream = new FileOutputStream(file)) {
            testMap = AES.encrypt(data.getBytes(StandardCharsets.ISO_8859_1), null);
            fOutputStream.write(testMap.get("DATA"));
            //fOutputStream.write(AES.Encrypt(data.getBytes(StandardCharsets.ISO_8859_1), "password", null));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // STORAGE TO READ THE DATA INTO
        byte[] DATA = null;

        // READ FROM FILE
        try (FileInputStream fInputStream = new FileInputStream(file)) {
            DATA = fInputStream.readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // DECRYPT DATA
        try {
            DATA = AES.decrypt(DATA, testMap.get("KEY"), null);
            //DATA = AES.Decrypt(DATA, "password", null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // PRINT DATA TO CONSOLE
        String str = new String(DATA, StandardCharsets.ISO_8859_1);
        System.out.println(str);

        // Split Data
        String[] content = str.split(",");

        for (String s : content) {
            System.out.println(s + " - 0");
        }

    }
}
