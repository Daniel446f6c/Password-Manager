package com.danield.passwordmanager;

import javafx.beans.property.SimpleStringProperty;

/**
 * The {@code UserCredentials} class represents one entry of the {@link DataBase Model-Class}.
 * @author Daniel D
 */
public class UserCredentials {
    
    private SimpleStringProperty application;
    private SimpleStringProperty username;
    private SimpleStringProperty password;

    public UserCredentials(String application, String username, String password) {
        this.application = new SimpleStringProperty(application);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
    }

    public String getApplication() {
        return application.get();
    }

    public String getUsername() {
        return username.get();
    }

    public String getPassword() {
        return password.get();
    }

    public void setApplication(String application) {
        this.application.set(application);
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

}
