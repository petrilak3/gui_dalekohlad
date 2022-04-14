package fmfi.dalekohlad.Modules;

import com.google.gson.JsonObject;
import fmfi.dalekohlad.Communication.Communication;
import fmfi.dalekohlad.InputHandling.InputConfirmation;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Camera implements GUIModule {
    private Pane pane;
    private HashMap<String, Label> info;
    private final int IMAGE_TYPE_CODE = 116;
    private final int CAMERA_MODE_CODE = 109;
    private final int EXPOSURE_TIME_CODE = 101;
    private final int COOLER_SET_POINT_CODE = 115;
    private final int EXPOSURE_DELAY_CODE = 100;
    private final int SEQUENCE_REPEATS_CODE = 114;
    private final int OBSERVER_CODE = 79;
    private final int OBJECT_CODE = 111;
    private final int NOTES_CODE = 110;
    private final int EXPOSE_SEQUENCE_CODE = 69;
    private final int EXPOSE_FROM_NEAREST_SECOND_CODE = 88;
    private final int ABORT_IMAGING_CODE = 65;

    public void init(Pane p) {
        pane = p;
        info = new HashMap<>();

        for(String s:new String[]{
                "CAMType","CAMExposure","CAMMode","CAMRBIFlushCount","CAMRBIFloodTime","CAMTDIMode","CAMBGFlush",
                "CAMBinning","CAMSubframe1","CAMSubframe2","CAMObserver","CAMObject","CAMNotes","CAMSetpoint",
                "CAMCooler1","CAMCooler2","CAMDelay","CAMRemaining1","CAMRemaining2","CAMStatus"}){
            info.put(s, (Label) GUIModule.GetById(pane, s));
            info.get(s).setText("...");
        }

        ((ChoiceBox) Objects.requireNonNull(GUIModule.GetById(pane, "CAMTypeChoiceBox"))).setOnAction(actionEvent -> ChoiceBoxCommand("CAMType", IMAGE_TYPE_CODE));
        ((ChoiceBox) Objects.requireNonNull(GUIModule.GetById(pane, "CAMModeChoiceBox"))).setOnAction(actionEvent -> ChoiceBoxCommand("CAMMode", CAMERA_MODE_CODE));
        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "ExposureTimeButton"))).setOnAction(actionEvent -> ExposureTime());
        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "CoolerSetpointButton"))).setOnAction(actionEvent -> CoolerSetPoint());
        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "ExposureDelayButton"))).setOnAction(actionEvent -> ExposureDelay());
        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "SequenceRepeatsButton"))).setOnAction(actionEvent -> SequenceRepeats());
        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "ObserverButton"))).setOnAction(actionEvent -> Observer());
        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "ObjectButton"))).setOnAction(actionEvent -> Object());
        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "NotesButton"))).setOnAction(actionEvent -> Notes());
        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "ExposeSequenceButton"))).setOnAction(actionEvent -> ExposeSequence());
        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "ExposeFromNearestSecondButton"))).setOnAction(actionEvent -> ExposeFromNearestSecond());
        ((Button) Objects.requireNonNull(GUIModule.GetById(pane, "AbortImagingButton"))).setOnAction(actionEvent -> AbortImaging());

        ((TextField) Objects.requireNonNull(GUIModule.GetById(pane, "ExposureTimeField"))).setOnAction(actionEvent -> ExposureTime());
        ((TextField) Objects.requireNonNull(GUIModule.GetById(pane, "CoolerSetPointField"))).setOnAction(actionEvent -> CoolerSetPoint());
        ((TextField) Objects.requireNonNull(GUIModule.GetById(pane, "ExposureDelayField"))).setOnAction(actionEvent -> ExposureDelay());
        ((TextField) Objects.requireNonNull(GUIModule.GetById(pane, "SequenceRepeatsField"))).setOnAction(actionEvent -> SequenceRepeats());
    }

    public void ChoiceBoxCommand(String element, int CODE){
        ChoiceBox choiceBox = (ChoiceBox)GUIModule.GetById(pane, element + "ChoiceBox");
        String data = (String) choiceBox.getValue();

        int actual = choiceBox.getItems().indexOf(info.get(element).getText());
        int wanted = choiceBox.getItems().indexOf(choiceBox.getValue());
        int num = wanted - actual;
        if(num < 0) num += 3;
        if(actual == -1) num = wanted;
        for(int i = 0; i < num; i++) Communication.sendData(String.valueOf(CODE));

        System.out.println("Set " + element + ": " + data);
    }

    public void ImageType(){
        Communication.sendData(String.valueOf(IMAGE_TYPE_CODE));
        System.out.println("image type has benn changed");
    }

    public void CameraMode(){
        Communication.sendData(String.valueOf(CAMERA_MODE_CODE));
        System.out.println("camera mode has been changed");
    }

    public void ExposureTime(){
        TextField exposure_time = ((TextField)GUIModule.GetById(pane,"ExposureTimeField"));

        try{
            double value = Double.parseDouble(exposure_time.getText());
            boolean confirm = true;

            if(value <= 0) confirm = InputConfirmation.confirm("time should be positive", "warning");
            if(confirm){
                Communication.sendData(EXPOSURE_TIME_CODE + ";" + value);
                System.out.println("Exposure time: " + value);
                exposure_time.setText("");
            }
        }
        catch(Exception e){
            InputConfirmation.warn("Data was entered incorrectly!");
        }
    }

    public void CoolerSetPoint(){
        TextField cooler_setpoint = ((TextField)GUIModule.GetById(pane,"CoolerSetPointField"));
        try{
            double value = Double.parseDouble(cooler_setpoint.getText());
            Communication.sendData(COOLER_SET_POINT_CODE + ";" + value);
            System.out.println("Cooler set point: " + value);
            cooler_setpoint.setText("");
        }
        catch(Exception e){
            InputConfirmation.warn("Data was entered incorrectly!");
        }
    }

    public void ExposureDelay(){
        TextField exposure_delay = ((TextField)GUIModule.GetById(pane,"ExposureDelayField"));
        try{
            double value = Double.parseDouble(exposure_delay.getText());
            boolean confirm = true;

            if(value <= 0) confirm = InputConfirmation.confirm("exposure delay should be positive", "warning");
            if(confirm){
                Communication.sendData(EXPOSURE_DELAY_CODE + ";" + value);
                System.out.println("Exposure delay: " + value);
                exposure_delay.setText("");
            }
        }
        catch(Exception e){
            InputConfirmation.warn("Data was entered incorrectly!");
        }
    }

    public void SequenceRepeats(){
        TextField sequence_repeats = ((TextField)GUIModule.GetById(pane,"SequenceRepeatsField"));
        try{
            int value = Integer.parseInt(sequence_repeats.getText());
            boolean confirm = true;

            if(value <= 0) confirm = InputConfirmation.confirm("sequence repeats should be positive", "warning");
            if(confirm){
                Communication.sendData(SEQUENCE_REPEATS_CODE + ";" + value);
                System.out.println("Sequence repeats: " + value);
                sequence_repeats.setText("");
            }
        }
        catch(Exception e){
            InputConfirmation.warn("Data was entered incorrectly!");
        }
    }

    public void Observer(){
        TextArea observer = ((TextArea)GUIModule.GetById(pane,"ObserverField"));
        Communication.sendData(OBSERVER_CODE + ";" + observer.getText());
        System.out.println("Observer: " + observer.getText());
        observer.setText("");
    }

    public void Object(){
        TextArea object = ((TextArea)GUIModule.GetById(pane,"ObjectField"));
        Communication.sendData(OBJECT_CODE + ";" + object.getText());
        System.out.println("Object: " + object.getText());
        object.setText("");
    }

    public void Notes(){
        TextArea notes = ((TextArea)GUIModule.GetById(pane,"NotesField"));
        Communication.sendData(NOTES_CODE + ";" + notes.getText());
        System.out.println("Notes: " + notes.getText());
        notes.setText("");
    }

    public void ExposeSequence(){
        Communication.sendData(String.valueOf(EXPOSE_SEQUENCE_CODE));
        System.out.println("expose sequence");
    }

    public void ExposeFromNearestSecond(){
        Communication.sendData(String.valueOf(EXPOSE_FROM_NEAREST_SECOND_CODE));
        System.out.println("expose from nearest second");
    }

    public void AbortImaging(){
        Communication.sendData(String.valueOf(ABORT_IMAGING_CODE));
        System.out.println("abort imaging");
    }

    public void update(JsonObject jo){
        for(String s:info.keySet()){
            if(jo.get(s) != null) Platform.runLater(() -> info.get(s).setText(jo.get(s).getAsString()));
        }
    }

    @Override
    public void registerShortcuts(Map<Pair<Boolean, KeyCode>, Runnable> shortcuts) {
        // O - observer
        Pair<Boolean, KeyCode> observer = new Pair<>(true, KeyCode.O);
        shortcuts.put(observer, () -> FocusTextField(true,"ObserverField", pane));
        // o - object
        Pair<Boolean, KeyCode> object = new Pair<>(false, KeyCode.O);
        shortcuts.put(object, () -> FocusTextField(true,"ObjectField", pane));
        // n - notes
        Pair<Boolean, KeyCode> notes = new Pair<>(false, KeyCode.N);
        shortcuts.put(notes, () -> FocusTextField(true,"NotesField", pane));

        // e - exposure time
        Pair<Boolean, KeyCode> exposure_time = new Pair<>(false, KeyCode.E);
        shortcuts.put(exposure_time, () -> FocusTextField(false,"ExposureTimeField", pane));
        // s - cooler setpoint
        Pair<Boolean, KeyCode> cooler_setpoint = new Pair<>(false, KeyCode.S);
        shortcuts.put(cooler_setpoint, () -> FocusTextField(false,"CoolerSetPointField", pane));
        // d - exposure delay
        Pair<Boolean, KeyCode> exposure_delay = new Pair<>(false, KeyCode.D);
        shortcuts.put(exposure_delay, () -> FocusTextField(false,"ExposureDelayField", pane));
        // r - sequence repeats
        Pair<Boolean, KeyCode> sequence_repeats = new Pair<>(false, KeyCode.R);
        shortcuts.put(sequence_repeats, () -> FocusTextField(false,"SequenceRepeatsField", pane));

        // m - camera mode
        Pair<Boolean, KeyCode> camera_mode = new Pair<>(false, KeyCode.M);
        shortcuts.put(camera_mode, this::CameraMode);
        // t - image type
        Pair<Boolean, KeyCode> image_type = new Pair<>(false, KeyCode.T);
        shortcuts.put(image_type, this::ImageType);

        // E - expose sequence
        Pair<Boolean, KeyCode> expose_sequence = new Pair<>(true, KeyCode.E);
        shortcuts.put(expose_sequence, this::ExposeSequence);
        // X - expose from nearest second
        Pair<Boolean, KeyCode> expose_from_nearest_second = new Pair<>(true, KeyCode.X);
        shortcuts.put(expose_from_nearest_second, this::ExposeFromNearestSecond);
        // A - abort imaging
        Pair<Boolean, KeyCode> abort_imaging = new Pair<>(true, KeyCode.A);
        shortcuts.put(abort_imaging, this::AbortImaging);
    }
}