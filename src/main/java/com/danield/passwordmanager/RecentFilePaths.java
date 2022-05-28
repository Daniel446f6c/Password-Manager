package com.danield.passwordmanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import com.danield.util.OSValidator;

/**
 * The {@code RecentFilePaths} class manages the recent file paths.
 * <p>
 * Its responsibilities are to read from and write to the recent_filepaths file.
 * @author Daniel D
 */
public class RecentFilePaths {

    private static final String FILE_NAME = "recent_filepaths";
    private static final String DIR_PATH_WINDOWS = String.format("%s\\AppData\\Local\\%s", System.getProperty("user.home"), App.TITLE);
    private static final String FILE_PATH_WINDOWS = String.format("%s\\%s", DIR_PATH_WINDOWS, FILE_NAME);
    // TODO Linux Directory Path
    // TODO Linux File Path
    private static final String DIR_PATH_LINUX = "";
    private static final String FILE_PATH_LINUX = "";
    private static ArrayList<String> recentFilePaths = new ArrayList<String>();

    private RecentFilePaths() {} // we don't want this class to be instantiated.

    /**
     * Check wether or not the given string is already in the list.
     * @param s : the string
     * @return {@code true} if string is in list, {@code false} otherwise
     */
    private static boolean isValuePresent(String s) {

        for (String path : recentFilePaths) {
            if (path.equals(s)) { return true; }
        }
        return false;

    }

    /**
     * Read filepaths from file.
     * @return a list of filepaths
     */
    public static ArrayList<String> readFromFile() {

        recentFilePaths.clear();

        File dir = new File("");
        File file = new File("");

        if (OSValidator.isWindows()) {
            dir = new File(DIR_PATH_WINDOWS);
            file = new File(FILE_PATH_WINDOWS);
        }
        else if (OSValidator.isUnix()) {
            dir = new File(DIR_PATH_LINUX);
            file = new File(FILE_PATH_LINUX);
        }
        
        if (dir.getName().equals("") || file.getName().equals("")) { return recentFilePaths; }
        if (!dir.exists()) { dir.mkdirs(); }

        try {
            if (file.createNewFile()) {
                return recentFilePaths;
            }
            else {
                try (Scanner scanner = new Scanner(file)) {
                    while (scanner.hasNextLine()) {
                        recentFilePaths.add(scanner.nextLine());
                    }
                    return recentFilePaths;
                }
            }
        }
        catch (IOException e) { return recentFilePaths; }
        
    }

    /**
     * Write filepaths to file.
     * @return {@code true} if successful, {@code false} otherwise
     */
    public static boolean writeToFile() {

        File file = new File("");

        if (OSValidator.isWindows())   { file = new File(FILE_PATH_WINDOWS); }
        else if (OSValidator.isUnix()) { file = new File(FILE_PATH_LINUX); }

        try (PrintWriter printWriter = new PrintWriter(file)) {
            for (String filePath : recentFilePaths) {
                printWriter.println(filePath);
            }
            return true;
        }
        catch (FileNotFoundException e) { return false; }
        
    }

    /**
     * Add at index. Removes duplicates.
     * @param filePath : the filepath
     * @param index : the index
     */
    public static void addAt(int index, String filePath) {

        if (isValuePresent(filePath)) { recentFilePaths.remove(filePath); }
        recentFilePaths.add(index, filePath);

    }

    /**
     * Remove at index.
     * @param index : the index
     */
    public static void removeAt(int index) {
        recentFilePaths.remove(index);
    }

    /**
     * Does not read from disk like {@code readFromFile}.
     * @return a list of filepaths
     */
    public static ArrayList<String> get() {
        return recentFilePaths;
    }

}
