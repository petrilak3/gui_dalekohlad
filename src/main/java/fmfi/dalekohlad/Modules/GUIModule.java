package fmfi.dalekohlad.Modules;

import com.google.gson.JsonObject;
import fmfi.dalekohlad.Communication.Communication;
import fmfi.dalekohlad.InputHandling.InputConfirmation;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

import java.util.List;
import java.util.Map;

public interface GUIModule {
    // Vsetky moduly by mali mat default konstruktor.
    
    // Kazdy modul ma v inite prideleny prislusny Pane, ktory moze lubovolne upravovat.
    
    // Pane prisluchajuci urcitemu modulu musi byt v GUI priamym potomkom sceny a mat id v tvare "module_%nazov_modulu%".
    
    // update bude volany vzdy ked sa dostane v komunikacii novy konfig.
    
    // Kazdy modul musi robit zmeny v GUI pomocou Platform.runLater() alebo Task (https://stackoverflow.com/questions/13784333/platform-runlater-and-task-in-javafx),
    // napriklad: Platform.runLater(() -> {label.text = "zmeneny";});.
    // Je to nevyhnutne kvoli https://stackoverflow.com/questions/25171039/what-is-the-best-way-to-manage-multithreading-in-javafx-8       .
    
    // Ukazka ako z JsonObject parsovat data: https://github.com/TIS2020-FMFI/dalekohlad/blob/1d5e7f971384653d84f5641c6328198cc86621b9/src/test/java/fmfi/dalekohlad/JsonTest.java#L23
    
    void update(JsonObject jo);
    void init(Pane pane);
    default void registerShortcuts(Map<Pair<Boolean, KeyCode>, Runnable> shortcuts) {}  // Pair<isShiftDown(), KeyCode>

    static Node GetById(Pane pane, String id){
        for(Node i:pane.getChildren()){
            if(i.getId() != null && i.getId().equals(id)) return i;
        }
        return null;
    }

    default void FocusTextField(boolean textArea, String id, Pane pane){
        Node field;
        field = GetById(pane,id);
        field.requestFocus();

        if(textArea) Platform.runLater(() -> ((TextArea) field).setText(""));
        else  Platform.runLater(() -> ((TextField) field).setText(""));
    }

    default void updateInformations(JsonObject jo, List<String> namesOfCells, Label[] infoLabels) {
        for(int i = 0; i < infoLabels.length; i++) {
            if(jo.get(namesOfCells.get(i)) != null) {
                int finalI = i;
                Platform.runLater(() -> {infoLabels[finalI].setText(jo.get(namesOfCells.get(finalI)).getAsString());});
            }
        }
    }
}
