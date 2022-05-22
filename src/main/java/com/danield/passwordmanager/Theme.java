package com.danield.passwordmanager;

/**
 * The {@code Style} enum provides the different styles.
 * @author Daniel D
 */
public enum Theme {
    
    LOGIN_DEFAULT("LoginView.css"),
    LOGIN_DARK("LoginViewDark.css"),
    NEW_DEFAULT("NewView.css"),
    NEW_DARK("NewViewDark.css"),
    MAIN_DEFAULT("MainView.css"),
    MAIN_DARK("MainViewDark.css");

    private String fileName;

    Theme(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

}
