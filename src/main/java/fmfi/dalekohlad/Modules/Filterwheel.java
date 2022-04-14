package fmfi.dalekohlad.Modules;

import com.google.gson.JsonObject;
import fmfi.dalekohlad.Communication.Communication;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import javafx.util.Pair;
import java.util.Map;

public class Filterwheel implements GUIModule {
    private Pane pane;
    private Label info;
    private ComboBox comboBox;
    private final int FILTER_B_CODE = 67;
    private final int FILTER_V_CODE = 66;
    private final int FILTER_R_CODE = 86;
    private final int FILTER_I_CODE = 82;
    private final int FILTER_C_CODE = 73;

    public void init(Pane p) {
        pane = p;
        info = (Label) GUIModule.GetById(pane, "FWFilter");
        info.setText("...");

        comboBox = ((ComboBox)GUIModule.GetById(pane, "FWFilterComboBox"));
        comboBox.setOnAction(actionEvent -> SetFilter());
        SetItemsColors();
    }

    public void SetItemsColors(){
        comboBox.setCellFactory(new Callback<ListView<String>, ListCell<String>>(){
            @Override public ListCell<String> call(ListView<String> p){
                return new ListCell<>() {
                    private final Rectangle rectangle = new Rectangle(10, 10);

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if(item == null || empty){
                            setGraphic(null);
                        }else{
                            switch(item) {
                                case "Filter B" : rectangle.setFill(Color.BLUE); break;
                                case "Filter V" : rectangle.setFill(Color.GREEN); break;
                                case "Filter R" : rectangle.setFill(Color.RED); break;
                                case "Filter I" : rectangle.setFill(Color.ORANGE); break;
                                case "Filter C" : rectangle.setFill(Color.GRAY); break;
                            }
                            setGraphic(rectangle);
                            setText(item);
                        }
                    }
                };
            }
        });
    }

    public void SetFilter(){
        ComboBox comboBox = (ComboBox) GUIModule.GetById(pane, "FWFilterComboBox");
        assert comboBox != null;
        String data = (String)comboBox.getValue();

        if(data.contains(" ")) SetFilter(data.split(" ")[1]);
        else SetFilter(data);
    }

    public void SetFilter(String s){
        switch(s){
            case "B": Communication.sendData(String.valueOf(FILTER_B_CODE));
            case "V": Communication.sendData(String.valueOf(FILTER_V_CODE));
            case "R": Communication.sendData(String.valueOf(FILTER_R_CODE));
            case "I": Communication.sendData(String.valueOf(FILTER_I_CODE));
            case "C": Communication.sendData(String.valueOf(FILTER_C_CODE));
        }
        System.out.println("Set Filter: " + s);
    }

    public void update(JsonObject jo) {
        if(jo.get("FWFilter") == null) return;
        Color color = Color.WHITE;

        switch(jo.get("FWFilter").getAsString()){
            case "B": color = Color.BLUE; break;
            case "V": color = Color.GREEN; break;
            case "R": color = Color.RED; break;
            case "I": color = Color.ORANGE; break;
            case "C": color = Color.GRAY; break;
        }
        assert comboBox != null;
        Color finalColor = color;
        Platform.runLater(() -> comboBox.setBackground(new Background(new BackgroundFill(finalColor, new CornerRadii(30), Insets.EMPTY))));
        Platform.runLater(() -> comboBox.setValue(jo.get("FWFilter").getAsString()));
        Platform.runLater(() -> info.setText(jo.get("FWFilter").getAsString()));
    }

    @Override
    public void registerShortcuts(Map<Pair<Boolean, KeyCode>, Runnable> shortcuts) {
        // B - filter B
        Pair<Boolean, KeyCode> filter_B = new Pair<>(true, KeyCode.B);
        shortcuts.put(filter_B, () -> SetFilter("B"));
        // V - filter V
        Pair<Boolean, KeyCode> filter_V = new Pair<>(true, KeyCode.V);
        shortcuts.put(filter_V, () -> SetFilter("V"));
        // R - filter R
        Pair<Boolean, KeyCode> filter_R = new Pair<>(true, KeyCode.R);
        shortcuts.put(filter_R, () -> SetFilter("R"));
        // I - filter I
        Pair<Boolean, KeyCode> filter_I = new Pair<>(true, KeyCode.I);
        shortcuts.put(filter_I, () -> SetFilter("I"));
        // C - filter C
        Pair<Boolean, KeyCode> filter_C = new Pair<>(true, KeyCode.C);
        shortcuts.put(filter_C, () -> SetFilter("C"));
    }
}