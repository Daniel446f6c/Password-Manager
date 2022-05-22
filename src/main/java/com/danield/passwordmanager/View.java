package com.danield.passwordmanager;

/**
 * The {@code View} enum provides the different views.
 * @author Daniel D
 */
public enum View {
    
    LOGIN("LoginView.fxml"),
    MAIN("MainView.fxml"),
    NEW("NewView.fxml");

    private String fileName;

    View(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

}
