package com.danield.util;

/**
 * The {@code OSValidator} class validates which OS this Application is running on.
 * @author Daniel D
 */
public class OSValidator {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    private OSValidator() {} // we don't want this class to be instantiated.

    public static boolean isWindows() {
        return (OS_NAME.indexOf("win") >= 0);
    }

    public static boolean isUnix() {
        return (OS_NAME.indexOf("nux") >= 0 || OS_NAME.indexOf("nix") >= 0);
    }

    public static boolean isMac() {
        return (OS_NAME.indexOf("mac") >= 0);
    }

}
