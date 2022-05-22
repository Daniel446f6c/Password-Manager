package com.danield.passwordmanager;

import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 * The {@code CustomBorder} enum provides different border styles.
 * @author Daniel D
 */
public enum CustomBorder {
    
    NONE(new Border(new BorderStroke(Color.BLACK,
                                     BorderStrokeStyle.NONE,
                                     new CornerRadii(20.0),
                                     BorderWidths.DEFAULT))),
    RED(new Border(new BorderStroke(Color.RED,
                                     BorderStrokeStyle.SOLID,
                                     new CornerRadii(20.0),
                                     BorderWidths.DEFAULT)));
    
    private Border border;

    CustomBorder(Border border) {
        this.border = border;
    }

    Border getBorder() {
        return border;
    }

}
