package fmfi.dalekohlad.Modules;

import com.google.gson.JsonObject;
import fmfi.dalekohlad.Communication.Communication;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import fmfi.dalekohlad.InputHandling.InputConfirmation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Target implements GUIModule {
    private Pane pane;
    private Label[] info;

    private final int GOTO_CANCEL_CODE = 71;
    private final int SWITCH_POLE_CROSSING_CODE = 112;
    private final List<String> INFO_NAMES = List.of("TAREncoder1","TARdEnc1","TARHAApparent","TARDEApparent","TARRAJ2000","TARDEJ2000","TARAzimuth","TARElevation","TARPoleCrossing","TARStatus");

    public void init(Pane p) {
        pane = p;
        info = new Label[10];

        for(int i = 0; i < 10; i++){
            info[i] = (Label) GUIModule.GetById(pane, "target" + (i+1));
            int finalI = i;
            Platform.runLater(() -> {info[finalI].setText("...");});
        }

        ((Button)GUIModule.GetById(pane,"LoadTarget")).setOnAction(actionEvent -> LoadTarget());
        ((Button)GUIModule.GetById(pane,"GoToCancel")).setOnAction(actionEvent -> GoToCancel());
        ((Button)GUIModule.GetById(pane,"PoleCrossing")).setOnAction(actionEvent -> PoleCrossing());

        ((TextField) Objects.requireNonNull(GUIModule.GetById(pane, "LoadTargetRA"))).setOnAction(actionEvent -> LoadTarget());
        ((TextField) Objects.requireNonNull(GUIModule.GetById(pane, "LoadTargetDE"))).setOnAction(actionEvent -> LoadTarget());
    }

    public static boolean goodFormat(String input) {
        if (input == null) return false;

        if(!input.matches("[0-9][0-9][.][0-9][0-9][0-9]") && !input.matches("[0-9][0-9][:][0-9][0-9][:][0-9][0-9][.][0-9]")){
            return false;
        }
        return true;
    }

    public void LoadTarget(){
        TextField ra = ((TextField)GUIModule.GetById(pane,"LoadTargetRA"));
        TextField de = ((TextField)GUIModule.GetById(pane,"LoadTargetDE"));

        String ra_text = ra.getText();
        String de_text = de.getText();

        if(goodFormat(ra_text) && goodFormat(de_text)) {
            InputConfirmation.warn("Currently not implemented!");
        }
        else {
            InputConfirmation.warn("Data was entered incorrectly!");
        }

        Platform.runLater(() -> {ra.setText("");});
        Platform.runLater(() -> {de.setText("");});
    }

    public void GoToCancel(){
        Communication.sendData(String.valueOf(GOTO_CANCEL_CODE));
    }

    public void PoleCrossing(){
        Button button = (Button) GUIModule.GetById(pane, "PoleCrossing");
        if(button.getText().equals("Pole crossing on")) Platform.runLater(() -> {button.setText("Pole crossing off");});
        else Platform.runLater(() -> {button.setText("Pole crossing on");});

        Communication.sendData(String.valueOf(SWITCH_POLE_CROSSING_CODE));
    }

    public void update(JsonObject jo) {
        updateInformations(jo,INFO_NAMES,info);
    }

    @Override
    public void registerShortcuts(Map<Pair<Boolean, KeyCode>, Runnable> shortcuts) {
        // L - load target
        Pair<Boolean, KeyCode> load_target = new Pair<>(true, KeyCode.L);
        shortcuts.put(load_target, () -> FocusTextField(false,"LoadTargetRA", pane));

        // p - pole crossing
        Pair<Boolean, KeyCode> pole_crossing = new Pair<>(false, KeyCode.P);
        shortcuts.put(pole_crossing, this::PoleCrossing);
        // G - goto/cancel
        Pair<Boolean, KeyCode> goto_cancel = new Pair<>(true, KeyCode.G);
        shortcuts.put(goto_cancel, this::GoToCancel);
    }
}