package com.danield.passwordmanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.danield.protector.SHA;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * The {@code NewController} class provides the logic behind the NewView.fxml View.
 * @author Daniel D
 */
public class NewController {
    
    @FXML private TextField txtFldFilePath;
    @FXML private PasswordField pwFldMasterPassword;
    @FXML private Label lblInfoText;
    @FXML private Button btnBrowse;
    @FXML private Button btnCreate;
    @FXML private Button btnTopLogin;

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
     * Create a file.
     * @return {@code true} if the file was successfully created, {@code false} otherwise
     */
    private boolean createFile() {

        File file = new File(txtFldFilePath.getText());
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            writeHash(file, SHA.SHA256(pwFldMasterPassword.getText().getBytes()));
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Write the hash to a file.
     * @param file : the file.
     * @param hash : the hash.
     */
    private void writeHash(File file, byte[] hash) {

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(hash);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    /**
     * Event handler bind to the Password Field's {@code onKeyPressed} event.
     * <p>
     * Lets the user login with the ENTER-Key.
     * @param event : the event
     */
    public void onCheckKeyPressed(KeyEvent event) {

        if (event.getCode().equals(KeyCode.ENTER)) {
            Event.fireEvent(btnCreate, new ActionEvent(pwFldMasterPassword, btnCreate));
        }

    }

    /**
     * Event handler bind to the Create Button's {@code onAction} event.
     * <p>
     * Lets the user create a new file.
     * @param event : the event
     */
    public void onCreate(ActionEvent event) {

        if (txtFldFilePath.getLength() == 0) {
            txtFldFilePath.setBorder(CustomBorder.RED.getBorder());
            infoText("EMPTY FILE PATH.", Color.RED);
            btnBrowse.requestFocus();
        }
        else if (pwFldMasterPassword.getLength() < 4) {
            pwFldMasterPassword.setBorder(CustomBorder.RED.getBorder());
            infoText("PASSWORD TOO SHORT. MINIMUM LENGTH IS 4 CHARACTERS.", Color.RED);
            pwFldMasterPassword.requestFocus();
        }
        else {
            if (createFile()) {
                pwFldMasterPassword.setBorder(CustomBorder.NONE.getBorder());
                infoText("SUCCESSFULLY CREATED THE NEW FILE.", Color.LIGHTGREEN);
                btnTopLogin.requestFocus();
            }
            else {
                infoText("CANT CREATE FILE.", Color.RED);
            }
        }

    }

    /**
     * Event handler bind to the Login Button's {@code onAction} event.
     * <p>
     * Lets the user switch to the "Login Page" where he can login to a file.
     * @param event : the event
     */
    public void onLogin(ActionEvent event) {

        ViewSwitcher.switchTo(View.LOGIN);
        if (ThemeSwitcher.getCurrentTheme() == Theme.NEW_DEFAULT) {
            ThemeSwitcher.switchTo(Theme.LOGIN_DEFAULT);
        }
        else {
            ThemeSwitcher.switchTo(Theme.LOGIN_DARK);
        }
        
    }

    /**
     * Event handler bind to the Quit Button's {@code onAction} event.
     * <p>
     * Lets the user quit the application.
     * @param event : the event
     */
    public void onQuit(ActionEvent event) {
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

        btnBrowse.getParent().requestFocus();
    }

    /**
     * Event handler bind to the Theme Top Button's {@code onAction} event.
     * <p>
     * Lets the user switch between multiple themes.
     * @param event : the event
     */
    public void onTheme(ActionEvent event) {

        if (ThemeSwitcher.getCurrentTheme() == Theme.NEW_DEFAULT) {
            ThemeSwitcher.switchTo(Theme.NEW_DARK);
        }
        else {
            ThemeSwitcher.switchTo(Theme.NEW_DEFAULT);
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
        fileChooser.setTitle("New File");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Protected Files", "*.protected")
        );
        
        File file = fileChooser.showSaveDialog(txtFldFilePath.getScene().getWindow());
        if (file != null) {
            txtFldFilePath.setText(file.getAbsolutePath());
            txtFldFilePath.setBorder(CustomBorder.NONE.getBorder());
            infoText("", Color.BLACK);
            pwFldMasterPassword.requestFocus();
        }

    }

}
