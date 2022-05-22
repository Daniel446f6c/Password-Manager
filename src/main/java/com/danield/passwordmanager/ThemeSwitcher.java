package com.danield.passwordmanager;

import javafx.scene.Scene;

/**
 * The {@code ThemeSwitcher} class provides the logic to switch between multiple themes.
 * @author Daniel D
 */
public class ThemeSwitcher {

    private static Scene scene;
    private static Theme theme;

    private ThemeSwitcher() {}; // we don't want this class to be instantiated.

    public static void setScene(Scene scene) {
        ThemeSwitcher.scene = scene;
    }

    public static Theme getCurrentTheme() {
        return theme;
    }

    public static void switchTo(Theme theme) {

        ThemeSwitcher.theme = theme;
        scene.getRoot().getStylesheets().clear();
        scene.getRoot().getStylesheets().add(ThemeSwitcher.class.getResource(ThemeSwitcher.theme.getFileName()).toExternalForm());

    }

}
