package fmfi.dalekohlad;

import fmfi.dalekohlad.Modules.GUIModule;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

public class Operations {
    private static Pane pane = null;

    public static void init(Pane p) {
        pane = p;
    }

    public static Pane getPane() {
        return pane;
    }

    public static void add(String operation) {
        TextArea ta = ((TextArea) GUIModule.GetById(pane,"text_area"));
        String oldText = ta.getText();
        if (oldText == null) {
            oldText = "";
        }
        String finalOldText = oldText;
        Platform.runLater(() -> {ta.setText(finalOldText + operation + "\n");});
    }
}
