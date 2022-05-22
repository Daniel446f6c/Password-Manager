package com.danield.passwordmanager;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;

/**
 * The {@code ViewSwitcher} class provides the logic to switch between multiple views.
 * @author Daniel D
 */
public class ViewSwitcher {
    
    private static Scene scene;

    private ViewSwitcher() {}; // we don't want this class to be instantiated.

    public static void setScene(Scene scence) {
        ViewSwitcher.scene = scence;
    }

    public static void switchTo(View view) {

        try {
            scene.getRoot().setDisable(true);
            Parent root = FXMLLoader.load(ViewSwitcher.class.getResource(view.getFileName()));
            scene.setRoot(root);
            root.requestFocus();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            System.out.println("NullPointerException: Cant load \"" + view.getFileName() + "\"");
            e.printStackTrace();
        }
        
    }

}
