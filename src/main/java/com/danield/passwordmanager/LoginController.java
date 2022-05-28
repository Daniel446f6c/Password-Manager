package com.danield.passwordmanager;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import com.danield.protector.SHA;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * The {@code LoginController} class provides the logic behind the LoginView.fxml View.
 * @author Daniel D
 */
public class LoginController implements Initializable {
    
    @FXML private TextField txtFldFilePath;
    @FXML private PasswordField pwFldMasterPassword;
    @FXML private Label lblInfoText;
    @FXML private Button btnBrowse;
    @FXML private Button btnLogin;
    @FXML private ComboBox<String> cmbxOpenRecent;

    /**
     * 1. Set Event Handler <p>
     * 2. Add Listener <p>
     * 3. Read the recent filepaths from file into {@link RecentFilePaths} <p>
     * 4. Populate {@code ComboBox} RecentFiles
     * @param location : can be ignored
     * @param resources : can be ignored
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        // unfocus focused control items
        btnLogin.getParent().getParent().getParent().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                btnLogin.getParent().requestFocus();
            }
        });

        cmbxOpenRecent.getItems().addAll(RecentFilePaths.readFromFile());
        cmbxOpenRecent.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> selected, String oldVal, String newVal) {
                
                if (selected.getValue() == null) { return; }
                File file = new File(selected.getValue());
                if (file.exists()) {
                    txtFldFilePath.setText(selected.getValue());
                    txtFldFilePath.setBorder(CustomBorder.NONE.getBorder());
                    infoText("", Color.BLACK);
                    pwFldMasterPassword.requestFocus();
                }
                else {
                    int index = cmbxOpenRecent.getSelectionModel().getSelectedIndex();
                    cmbxOpenRecent.getSelectionModel().selectedItemProperty().removeListener(this);
                    RecentFilePaths.removeAt(index);
                    cmbxOpenRecent.getItems().remove(index);
                    cmbxOpenRecent.getSelectionModel().clearSelection();
                    cmbxOpenRecent.getSelectionModel().selectedItemProperty().addListener(this);
                    txtFldFilePath.setText("");
                    infoText("FILE NOT FOUND.", Color.RED);
                    btnBrowse.requestFocus();
                }

            }
        });
        
    }

    /**
     * Display information, errors, warnings to the user.
     * @param text : the text to display
     * @param color : the color of the text
     */
    private void infoText(String text, Color color) {
        lblInfoText.setTextFill(color);
        lblInfoText.setText(text);
    }

    /**
     * Compares the hash stored in the file to the hash generated from the entered password.
     * @return {@code true} if the hashes match, {@code false} otherwise
     */
    private boolean validatePassword() {

        byte[] storedHash = DataBase.readHashedKeyFromFile();
        if (storedHash.length == 0) {
            infoText("UNABLE TO VALIDATE PASSWORD.", Color.RED);
            return false;
        }

        byte[] hash = SHA.SHA256(pwFldMasterPassword.getText().getBytes());

        if (Arrays.equals(storedHash, hash)) { return true; }
        else { return false; }

    }

    /**
     * Event handler bind to the Password Field's {@code onKeyPressed} event.
     * <p>
     * Lets the user login with the ENTER-Key.
     * @param event : the event
     */
    public void onCheckKeyPressed(KeyEvent event) {

        if (event.getCode().equals(KeyCode.ENTER)) {
            Event.fireEvent(btnLogin, new ActionEvent(pwFldMasterPassword, btnLogin));
        }

    }

    /**
     * Event handler bind to the Login Button's {@code onAction} event.
     * <p>
     * Lets the user login to a file.
     * @param event : the event
     */
    public void onLogin(ActionEvent event) {

        if (txtFldFilePath.getLength() == 0) {
            txtFldFilePath.setBorder(CustomBorder.RED.getBorder());
            infoText("EMPTY FILE PATH.", Color.RED);
            btnBrowse.requestFocus();
        }
        else if (pwFldMasterPassword.getLength() == 0) {
            pwFldMasterPassword.setBorder(CustomBorder.RED.getBorder());
            infoText("EMPTY MASTER PASSWORD.", Color.RED);
            pwFldMasterPassword.requestFocus();
        }
        else {
            DataBase.setFilePath(txtFldFilePath.getText());
            if (validatePassword()) {
                DataBase.setKey(pwFldMasterPassword.getText());
                RecentFilePaths.addAt(0, txtFldFilePath.getText());
                RecentFilePaths.writeToFile();
                ViewSwitcher.switchTo(View.MAIN);
                if (ThemeSwitcher.getCurrentTheme() == Theme.LOGIN_DEFAULT) {
                    ThemeSwitcher.switchTo(Theme.MAIN_DEFAULT);
                }
                else {
                    ThemeSwitcher.switchTo(Theme.MAIN_DARK);
                }
            }
            else {
                infoText("WRONG PASSWORD.", Color.RED);
                pwFldMasterPassword.requestFocus();
            }
        }

    }

    /**
     * Event handler bind to the New Button's {@code onAction} event.
     * <p>
     * Lets the user switch to the "New Page" where he can create a new file.
     * @param event : the event
     */
    public void onNew(ActionEvent event) {
        RecentFilePaths.writeToFile();
        ViewSwitcher.switchTo(View.NEW);
        if (ThemeSwitcher.getCurrentTheme() == Theme.LOGIN_DEFAULT) {
            ThemeSwitcher.switchTo(Theme.NEW_DEFAULT);
        }
        else {
            ThemeSwitcher.switchTo(Theme.NEW_DARK);
        }
    }

    /**
     * Event handler bind to the Quit Button's {@code onAction} event.
     * <p>
     * Lets the user quit the application.
     * @param event : the event
     */
    public void onQuit(ActionEvent event) {
        RecentFilePaths.writeToFile();
        Platform.exit();
    }

    /**
     * Event handler bind to the About Button's {@code onAction} event.
     * <p>
     * TODO method description.
     * @param event : the event
     */
    public void onAbout(ActionEvent event) {

        //TODO method implementation.

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(String.format("%s - About", App.TITLE));
        alert.setResizable(true);
        alert.setContentText(String.format("%s, %s, %s, %s", System.getProperty("os.name"),
                                                             System.getProperty("java.vendor.url"),
                                                             System.getProperty("java.vm.vendor"),
                                                             System.getProperty("java.specification.vendor")));
        alert.initOwner(txtFldFilePath.getScene().getWindow());
        alert.showAndWait();
        
        btnBrowse.getParent().requestFocus();

    }

    /**
     * Event handler bind to the Theme Top Button's {@code onAction} event.
     * <p>
     * Lets the user switch between multiple themes.
     * @param event : the event
     */
    public void onTheme(ActionEvent event) {

        if (ThemeSwitcher.getCurrentTheme() == Theme.LOGIN_DEFAULT) {
            ThemeSwitcher.switchTo(Theme.LOGIN_DARK);
        }
        else {
            ThemeSwitcher.switchTo(Theme.LOGIN_DEFAULT);
        }

        btnBrowse.getParent().requestFocus();

    }

    /**
     * Event handler bind to the Browse Button's {@code onAction} event.
     * <p>
     * Lets the user select a file from the file system using a {@code FileChooser}.
     * @param event : the event
     */
    public void onBrowse(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Protected Files", "*.protected"),
            new ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(txtFldFilePath.getScene().getWindow());
        if (selectedFile != null) {
            txtFldFilePath.setText(selectedFile.getAbsolutePath());
            txtFldFilePath.setBorder(CustomBorder.NONE.getBorder());
            cmbxOpenRecent.getSelectionModel().clearSelection();
            infoText("", Color.BLACK);
            pwFldMasterPassword.requestFocus();
        }

    }

}
