package fmfi.dalekohlad.Modules;

import com.google.gson.JsonObject;
import fmfi.dalekohlad.Communication.Communication;
import fmfi.dalekohlad.InputHandling.InputConfirmation;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Dome implements GUIModule {
    private Pane pane;
    private HashMap<String, Label> info;
    private final int DOME_WEST_CODE = 297;
    private final int DOME_EAST_CODE = 306;
    private final int DOME_STOP_CODE = 295;
    private final int FREQUENCY_CODE = 102;
    private final int CALIBRATE_AZIMUTH_CODE = 97;
    private final int SYNCHRONIZE_CODE = 121;


    public void init(Pane p) {
        pane = p;
        info = new HashMap<>();

        for(String s: new String[]{"DOMEEncoder", "DOMEAzimuth", "DOMETargetAzimuth", "DOMESynch", "DOMEStatus"}){
            info.put(s, (Label) GUIModule.GetById(pane, s));
            info.get(s).setText("...");
        }

        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "FrequencyButton"))).setOnAction(actionEvent -> Frequency());
        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "CalibrateAzimuthButton"))).setOnAction(actionEvent -> CalibrateAzimuth());
        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "DomeWestButton"))).setOnAction(actionEvent -> DomeWest());
        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "DomeStopButton"))).setOnAction(actionEvent -> DomeStop());
        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "DomeEastButton"))).setOnAction(actionEvent -> DomeEast());
        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "SynchronizeButton"))).setOnAction(actionEvent -> Synchronize());

        ((TextField) Objects.requireNonNull(GUIModule.GetById(pane, "FrequencyField"))).setOnAction(actionEvent -> Frequency());
        ((TextField) Objects.requireNonNull(GUIModule.GetById(pane, "CalibrateAzimuthField"))).setOnAction(actionEvent -> CalibrateAzimuth());
    }

    public void Frequency() {
        TextField frequency = ((TextField)GUIModule.GetById(pane,"FrequencyField"));

        try{
            double value = Double.parseDouble(frequency.getText());
            boolean confirm = true;

            if(value < 0 || value > 60) confirm = InputConfirmation.confirm("frequency should be in interval <0,60>", "warning");
            if(confirm){
                Communication.sendData(FREQUENCY_CODE + ";" + value);
                System.out.println("Frequency: " + value);
                frequency.setText("");
            }
        }
        catch(Exception e){
            InputConfirmation.warn("Data was entered incorrectly!");
        }
    }

    public void CalibrateAzimuth() {
        TextField calibrate_azimuth = ((TextField)GUIModule.GetById(pane,"CalibrateAzimuthField"));

        try{
            double value = Double.parseDouble(calibrate_azimuth.getText());
            boolean confirm = true;

            if(value < 0 || value > 360) confirm = InputConfirmation.confirm("azimuth should be in interval <0,360>", "warning");
            if(confirm){
                Communication.sendData(CALIBRATE_AZIMUTH_CODE + ";" + value);
                System.out.println("Calibrate azimuth: " + value);
                calibrate_azimuth.setText("");
            }
        }
        catch(Exception e){
            InputConfirmation.warn("Data was entered incorrectly!");
        }
    }

    public void DomeWest(){
        Communication.sendData(String.valueOf(DOME_WEST_CODE));
        System.out.println("Dome West");
    }

    public void DomeStop(){
        Communication.sendData(String.valueOf(DOME_STOP_CODE));
        System.out.println("Dome Stop");
    }

    public void DomeEast(){
        Communication.sendData(String.valueOf(DOME_EAST_CODE));
        System.out.println("Dome East");
    }

    public void Synchronize(){
        Communication.sendData(String.valueOf(SYNCHRONIZE_CODE));
        System.out.println("Synchronize");
    }

    public void update(JsonObject jo){
        for(String s:info.keySet()){
            if(jo.get(s) != null) Platform.runLater(() -> info.get(s).setText(jo.get(s).getAsString()));
        }
    }

    @Override
    public void registerShortcuts(Map<Pair<Boolean, KeyCode>, Runnable> shortcuts) {
        // f - frequency  (0 - 60)
        Pair<Boolean, KeyCode> frequency = new Pair<>(false, KeyCode.F);
        shortcuts.put(frequency, () -> FocusTextField(false,"FrequencyField", pane));
        // a - azimuth (0 - 360)
        Pair<Boolean, KeyCode> calibrate_azimuth = new Pair<>(false, KeyCode.A);
        shortcuts.put(calibrate_azimuth, () -> FocusTextField(false,"CalibrateAzimuthField", pane));
        // y - synchronize
        Pair<Boolean, KeyCode> synchronize = new Pair<>(false, KeyCode.Y);
        shortcuts.put(synchronize, this::Synchronize);

        // Insert - dome east
        Pair<Boolean, KeyCode> dome_east = new Pair<>(false, KeyCode.INSERT);
        shortcuts.put(dome_east, this::DomeEast);
        // Page Up - dome west
        Pair<Boolean, KeyCode> dome_west = new Pair<>(false, KeyCode.PAGE_UP);
        shortcuts.put(dome_west, this::DomeWest);
        // Home - dome stop
        Pair<Boolean, KeyCode> dome_stop = new Pair<>(false, KeyCode.HOME);
        shortcuts.put(dome_stop, this::DomeStop);
    }
}
