package com.danield.passwordmanager;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import com.danield.passwordgenerator.PwGen;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * The {@code MainController} class provides the logic behind the MainView.fxml View.
 * @author Daniel D
 */
public class MainController implements Initializable {
    
    @FXML private TableView<UserCredentials> tblViewDataEntrys = new TableView<UserCredentials>();
    @FXML private TableColumn<UserCredentials, String> tblColApplication = new TableColumn<UserCredentials, String>();
    @FXML private TableColumn<UserCredentials, String> tblColUsername = new TableColumn<UserCredentials, String>();
    @FXML private TableColumn<UserCredentials, String> tblColPassword = new TableColumn<UserCredentials, String>();
    @FXML private TextField txtFldApp;
    @FXML private TextField txtFldUsername;
    @FXML private TextField txtFldPassword;
    @FXML private Button btnAdd;
    @FXML private Slider sldrPwdLength;
    @FXML private Label lblPwdLength;

    private final PwGen passwordGenerator = new PwGen();
    private boolean hasRecentlyChanged = false;

    /**
     * 1) Create the cells. <p>
     * 2) Read the data from file into {@link DataBase}. <p>
     * 3) Populate the cells with the data. <p>
     * 4) Password length slider's change listener.
     * @param location : can be ignored
     * @param resources : can be ignored
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        // 1)
        tblColApplication.setCellFactory(TextFieldTableCell.<UserCredentials>forTableColumn());
        tblColApplication.setCellValueFactory(new PropertyValueFactory<UserCredentials, String>("application"));
        
        tblColUsername.setCellFactory(TextFieldTableCell.<UserCredentials>forTableColumn());
        tblColUsername.setCellValueFactory(new PropertyValueFactory<UserCredentials, String>("username"));
        
        tblColPassword.setCellFactory(TextFieldTableCell.<UserCredentials>forTableColumn());
        tblColPassword.setCellValueFactory(new PropertyValueFactory<UserCredentials, String>("password"));
        
        // 2)
        DataBase.readEntrysFromFile();
        
        // 3)
        tblViewDataEntrys.getItems().addAll(DataBase.getEntrys());

        // 4)
        sldrPwdLength.valueProperty().addListener( (observable, oldValue, newValue) -> {
            lblPwdLength.setText(String.valueOf(newValue.intValue()));
            txtFldPassword.setText(passwordGenerator.generate(newValue.intValue()));
        });

    }

    /**
     * Calls {@link DataBase#writeToFile()}.
     */
    private void saveFile() {
        DataBase.writeToFile();
    }

    /**
     * Display an {@code Alert} which asks the user if he wants to save the file.
     * <p>
     * Possible answers: YES , NO , CANCEL
     * @param title : the title
     * @param header : the header
     * @return {@code ButtonType.YES} or {@code ButtonType.NO} or {@code ButtonType.CANCEL}
     */
    private ButtonType saveFileAlert(String title, String header) {
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getButtonTypes().remove(ButtonType.OK);
        alert.getButtonTypes().add(ButtonType.CANCEL);
        alert.getButtonTypes().add(ButtonType.NO);
        alert.getButtonTypes().add(ButtonType.YES);
        alert.setTitle(String.format("Password Manager - %s", title));
        alert.setHeaderText(String.format("You are about to %s", header));
        alert.setContentText("Do you want to save before continuing?");
        alert.initOwner(tblViewDataEntrys.getScene().getWindow());
        Optional<ButtonType> res = alert.showAndWait();
        
        if(res.isPresent()) {
            return res.get();
        }

        return null; // unreachable code but it won't compile without

    }    

    /**
     * Event handler bind to the {@code onEditCommit} event on:
     * <p>
     * 1) {@code tblColApplication} <p>
     * 2) {@code tblColUsername} <p>
     * 3) {@code tblColPassword}
     * <p>
     * On every edit commit it removes all the {@link DataBase#getSeparator()} that got typed
     * and deletes the row if all the cells on the row are now empty.
     * @param event : the event
     */
    public void onEditCell(CellEditEvent<UserCredentials, String> event) {
        
        if (event.getTarget().equals(tblColApplication)) {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setApplication(event.getNewValue().replaceAll(String.valueOf(DataBase.getSeparator()), ""));
            event.getTableView().refresh();
        }
        else if (event.getTarget().equals(tblColUsername)) {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setUsername(event.getNewValue().replaceAll(String.valueOf(DataBase.getSeparator()), ""));
            event.getTableView().refresh();
        }
        else if (event.getTarget().equals(tblColPassword)) {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setPassword(event.getNewValue().replaceAll(String.valueOf(DataBase.getSeparator()), ""));
            event.getTableView().refresh();
        }
        if (tblColApplication.getCellData(event.getTablePosition().getRow()).equals("") &&
            tblColUsername.getCellData(event.getTablePosition().getRow()).equals("") &&
            tblColPassword.getCellData(event.getTablePosition().getRow()).equals("")) {
                int row = event.getTablePosition().getRow();
                event.getTableView().getItems().remove(row); // delete from table row
                DataBase.getEntrys().remove(row); // delete from database
        }
        hasRecentlyChanged = true;

    }

    /**
     * Event handler bind to the {@code onKeyPressed} event on:
     * <p>
     * 1) {@code tblViewDataEntrys} <p>
     * 2) {@code txtFldApp} <p>
     * 3) {@code txtFldUsername} <p>
     * 4) {@code txtFldPassword}
     * <p>
     * Depending on which key has been pressed it performs a certain action.
     * @param event : the event
     */
    public void onCheckKeyPressed(KeyEvent event) { // Could use switch-case instead.

        if (event.getTarget().equals(tblViewDataEntrys)) {
            if (event.getCode().equals(KeyCode.DELETE) && tblViewDataEntrys.getSelectionModel().getSelectedIndex() != -1) {
                int selectedIndex = tblViewDataEntrys.getSelectionModel().getSelectedIndex();
                tblViewDataEntrys.getItems().remove(selectedIndex);
                DataBase.getEntrys().remove(selectedIndex);
                tblViewDataEntrys.getSelectionModel().select(selectedIndex);
                hasRecentlyChanged = true;
            }
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                tblViewDataEntrys.getSelectionModel().clearSelection();
            }
        }
        else if (event.getTarget().equals(txtFldApp)) {
            if (event.getCode().equals(KeyCode.ENTER)) {
                txtFldUsername.requestFocus();
            }
        }
        else if (event.getTarget().equals(txtFldUsername)) {
            if (event.getCode().equals(KeyCode.ENTER)) {
                txtFldPassword.requestFocus();
            }
        }
        else if (event.getTarget().equals(txtFldPassword)) {
            if (event.getCode().equals(KeyCode.ENTER)) {
                btnAdd.requestFocus();
                Event.fireEvent(btnAdd, new ActionEvent(txtFldPassword, btnAdd));
            }
        }

    }

    /**
     * Event handler bind to the {@code onKeyTyped} event on:
     * <p>
     * 1) {@code txtFldApp} <p>
     * 2) {@code txtFldUsername} <p>
     * 3) {@code txtFldPassword}
     * <p>
     * It removes every {@link DataBase#getSeparator()} that gets typed.
     * @param event : the event
     */
    public void onCheckForInvalidKey(KeyEvent event) {

        if (event.getCharacter().equals(String.valueOf(DataBase.getSeparator()))) {
            if (event.getTarget().equals(txtFldApp)) {
                txtFldApp.setText(txtFldApp.getText().replaceAll(String.valueOf(DataBase.getSeparator()), ""));
            }
            else if (event.getTarget().equals(txtFldUsername)) {
                txtFldUsername.setText(txtFldUsername.getText().replaceAll(String.valueOf(DataBase.getSeparator()), ""));
            }
            else if (event.getTarget().equals(txtFldPassword)) {
                txtFldPassword.setText(txtFldPassword.getText().replaceAll(String.valueOf(DataBase.getSeparator()), ""));
            }
        }

    }

    /**
     * Event handler bind to the Generate Button's {@code onAction} event.
     * <p>
     * Lets the user generate a password. Length is selected via slider. 
     * @param event : the event
     */
    public void onGenerate(ActionEvent event) {
        txtFldPassword.setText(passwordGenerator.generate((int)sldrPwdLength.getValue()));
    }

    /**
     * Event handler bind to the Add Button's {@code onAction} event.
     * <p>
     * Lets the user add a new entry to the end of the table or
     * insert it if a row is selected.
     * @param event : the event
     */
    public void onAdd(ActionEvent event) {

        if (txtFldApp.getText().equals("") || txtFldApp.getText().equals(" ")) {
            txtFldApp.setBorder(CustomBorder.RED.getBorder());
            txtFldApp.requestFocus();
            return;
        }
        else {
            txtFldApp.setBorder(CustomBorder.NONE.getBorder());
        }

        if (txtFldUsername.getText().equals("") || txtFldUsername.getText().equals(" ")) {
            txtFldUsername.setBorder(CustomBorder.RED.getBorder());
            txtFldUsername.requestFocus();
            return;
        }
        else {
            txtFldUsername.setBorder(CustomBorder.NONE.getBorder());
        }

        if (txtFldPassword.getText().equals("") || txtFldPassword.getText().equals(" ")) {
            txtFldPassword.setBorder(CustomBorder.RED.getBorder());
            txtFldPassword.requestFocus();
            return;
        }
        else {
            txtFldPassword.setBorder(CustomBorder.NONE.getBorder());
        }

        UserCredentials uc = new UserCredentials(txtFldApp.getText(),
                                                 txtFldUsername.getText(),
                                                 txtFldPassword.getText());
        
        if (tblViewDataEntrys.getSelectionModel().getSelectedIndex() != -1) {
            DataBase.addNewEntryAt(uc, tblViewDataEntrys.getSelectionModel().getSelectedIndex()+1);
            tblViewDataEntrys.getItems().add(tblViewDataEntrys.getSelectionModel().getSelectedIndex()+1, uc);
        }
        else {
            DataBase.addNewEntry(uc);
            tblViewDataEntrys.getItems().add(uc);
        }
        txtFldApp.requestFocus();
        hasRecentlyChanged = true;

    }

    /**
     * Event handler bind to the New Button's {@code onAction} event.
     * <p>
     * Lets the user close the file and return to the "New Page".
     * @param event : the event
     */
    public void onNew(ActionEvent event) {

        if (hasRecentlyChanged) {
            ButtonType res = saveFileAlert("New", "create a new file.");
            if (res.equals(ButtonType.YES)) {
                saveFile();
            }
            else if (res.equals(ButtonType.CANCEL)) {
                return;
            }
        }
        DataBase.clear();
        ViewSwitcher.switchTo(View.NEW);
        if (ThemeSwitcher.getCurrentTheme() == Theme.MAIN_DEFAULT) {
            ThemeSwitcher.switchTo(Theme.NEW_DEFAULT);
        }
        else {
            ThemeSwitcher.switchTo(Theme.NEW_DARK);
        }

    }

    /**
     * Event handler bind to the Login Button's {@code onAction} event.
     * <p>
     * Same behaviour as {@link MainController#onClose()}
     * @param event : the event
     */
    public void onLogin(ActionEvent event) {

        if (hasRecentlyChanged) {
            ButtonType res = saveFileAlert("Login", "login to a file.");
            if (res.equals(ButtonType.YES)) {
                saveFile();
            }
            else if (res.equals(ButtonType.CANCEL)) {
                return;
            }
        }
        DataBase.clear();
        ViewSwitcher.switchTo(View.LOGIN);
        if (ThemeSwitcher.getCurrentTheme() == Theme.MAIN_DEFAULT) {
            ThemeSwitcher.switchTo(Theme.LOGIN_DEFAULT);
        }
        else {
            ThemeSwitcher.switchTo(Theme.LOGIN_DARK);
        }

    }

    /**
     * Event handler bind to the Save Button's {@code onAction} event.
     * <p>
     * Lets the user save the file.
     * @param event : the event
     */
    public void onSave(ActionEvent event) {
        saveFile();
        hasRecentlyChanged = false;
    }

    /**
     * Event handler bind to the Close Button's {@code onAction} event.
     * <p>
     * Lets the user close the file and return to the "Login Page".
     * @param event : the event
     */
    public void onClose(ActionEvent event) {

        if (hasRecentlyChanged) {
            ButtonType res = saveFileAlert("Close", "close this file.");
            if (res.equals(ButtonType.YES)) {
                saveFile();
            }
            else if (res.equals(ButtonType.CANCEL)) {
                return;
            }
        }
        DataBase.clear();
        ViewSwitcher.switchTo(View.LOGIN);
        if (ThemeSwitcher.getCurrentTheme() == Theme.MAIN_DEFAULT) {
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

        if (hasRecentlyChanged) {
            ButtonType res = saveFileAlert("Quit", "quit the application.");
            if (res.equals(ButtonType.YES)) {
                saveFile();
            }
            else if (res.equals(ButtonType.CANCEL)) {
                return;
            }
        }
        Platform.exit();

    }

    /**
     * Event handler bind to the About Button's {@code onAction} event.
     * <p>
     * TODO method description.
     * @param event : the event
     */
    public void onAbout(ActionEvent event) {

        // TODO method implementation.

        btnAdd.getParent().requestFocus();
        
    }

    /**
     * Event handler bind to the Theme Top Button's {@code onAction} event.
     * <p>
     * Lets the user switch between multiple themes.
     * @param event : the event
     */
    public void onTheme(ActionEvent event) {

        if (ThemeSwitcher.getCurrentTheme() == Theme.MAIN_DEFAULT) {
            ThemeSwitcher.switchTo(Theme.MAIN_DARK);
        }
        else {
            ThemeSwitcher.switchTo(Theme.MAIN_DEFAULT);
        }

        btnAdd.getParent().requestFocus();

    }

}
