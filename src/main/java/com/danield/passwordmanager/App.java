package com.danield.passwordmanager;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

/**
 * The {@code App} class represents the entry point of the Application.
 * @author Daniel D
 */
public class App extends Application {

    public static final String TITLE = "Password Manager";
    public static final double MIN_HEIGHT = 450.0;
    public static final double MIN_WIDTH = 475.0;
    public static final String VERSION = "1.0.0";

    @Override
    public void start(Stage stage) throws Exception {
        
        Scene scene = new Scene(new Pane());

        ViewSwitcher.setScene(scene);
        ViewSwitcher.switchTo(View.LOGIN);

        ThemeSwitcher.setScene(scene);
        ThemeSwitcher.switchTo(Theme.LOGIN_DEFAULT);

        stage.getIcons().add(new Image(App.class.getResourceAsStream("PasswordManagerIcon.png")));
        stage.setTitle(String.format("%s %s", TITLE, VERSION));
        stage.setMinHeight(MIN_HEIGHT);
        stage.setMinWidth(MIN_WIDTH);
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
        System.exit(0);
    }

}
