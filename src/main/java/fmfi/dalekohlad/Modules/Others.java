package fmfi.dalekohlad.Modules;

import com.google.gson.JsonObject;
import eap.fits.FitsHDU;
import eap.fits.FitsImageData;
import eap.fits.RandomAccessFitsFile;
import eap.fitsbrowser.FitsImageViewer;
import fmfi.dalekohlad.Communication.Communication;
import fmfi.dalekohlad.Mediator;
import fmfi.dalekohlad.Operations;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.RandomAccessFile;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Others implements GUIModule {
    private static final String shortcuts_screen = "/fxml/shortcuts_scene.fxml";
    private static final Logger lgr = LogManager.getLogger(Others.class);
    private final int FITS_SIZE = 170;
    private final Dimension DIMENSION = new Dimension(FITS_SIZE, FITS_SIZE);
    private final int IMAGE_HDU = 0;
    private static boolean wasUpdated = true;
    private static boolean connectionFailed = false;
    private final int START_SCHEDULING = 59;
    private final int STOP_SCHEDULING = 46;
    private final String pathToScript = "/script.py";

    private Parent main_screen_root;
    private Pane pane;
    private Label info;
    private Label status;
    private Label pathFITS;
    private SwingNode swingNode;
    private Button startStopScheduling;

    @Override
    public void init(Pane pane) {
        this.pane = pane;
        this.info = (Label) GUIModule.GetById(pane, "info");
        this.status = (Label) GUIModule.GetById(pane, "Connected");
        this.pathFITS = (Label) GUIModule.GetById(pane, "path_to_last_frame");
        this.swingNode = (SwingNode) GUIModule.GetById(pane, "node_FITS");
        this.startStopScheduling = (Button) GUIModule.GetById(pane, "start_scheduling");
        this.main_screen_root = pane.getParent().getScene().getRoot();
        displayConnectionStatus();

        startStopScheduling.setOnAction(event -> startStopScheduling());
        ((Button) GUIModule.GetById(pane, "load_scheduling")).setOnAction(event -> this.startLoadingScript());
        ((Button) GUIModule.GetById(pane,"Exit")).setOnAction(event -> System.exit(0));
        ((Button) GUIModule.GetById(pane,"Shortcuts")).setOnAction(event -> this.setDisplayingShortcuts());
    }

    @Override
    public void update(JsonObject jo) {
        markFlagsAsConnected();
        String infoText = "";
        try {
            String time = jo.get("TIMEUTC").getAsString();
            infoText += time + " ";
        } catch (Exception e) {
            lgr.debug("Time UTC wasn't loaded.");
        }
        try {
            String timeUT1 = jo.get("TIMEUT1UTC").getAsString();
            infoText += timeUT1;
        } catch (Exception e) {
            lgr.debug("Time UTC wasn't loaded.");
        }
        try {
            String fits_path = jo.get("LastFITSPath").getAsString();
            this.fitsHandle(fits_path);
        } catch (Exception e) {
            lgr.debug("Path to FITS wasn't loaded");
        }

        String finalInfoText = infoText;
        Platform.runLater(() -> this.info.setText(finalInfoText));
    }

    @Override
    public void registerShortcuts(Map<Pair<Boolean, KeyCode>, Runnable> shortcuts) {
        Pair<Boolean, KeyCode> startScheduling = new Pair<>(true, KeyCode.J);
        shortcuts.put(startScheduling, this::startScheduling);
        Pair<Boolean, KeyCode> stopScheduling = new Pair<>(true, KeyCode.K);
        shortcuts.put(stopScheduling, this::stopScheduling);
    }

    private void fitsHandle(String pathToFITS) {
        //suppose FITS file is never rewritten -> new photo is written to new file
        if(changingImageNotNecessary(pathToFITS)) {
            lgr.debug("FITS hasn't changed.");
            return;
        }
        try {
            FitsImageViewer image = loadImageFromFits(pathToFITS);

            image.setMaximumSize(DIMENSION);

            Platform.runLater(() -> this.swingNode.setContent(image));
        } catch (Exception e) {
            lgr.error("Failed load FITS file");
        }

        Platform.runLater(() -> pathFITS.setText(pathToFITS));
    }

    private boolean changingImageNotNecessary(String pathToFits) {
        return (pathToFits == null || pathToFits.compareTo(pathFITS.getText()) == 0);
    }

    private FitsImageViewer loadImageFromFits(String pathoTiFits) throws Exception {
        RandomAccessFile lastFrame = new RandomAccessFile(pathoTiFits, "r");
        RandomAccessFitsFile fitFile = new RandomAccessFitsFile(lastFrame);
        FitsHDU hdu = fitFile.getHDU(IMAGE_HDU);
        FitsImageData imageData = (FitsImageData) hdu.getData();
        return new FitsImageViewer(imageData);
    }

    private void startStopScheduling() {
        String actualState = startStopScheduling.getText();
        if(actualState.equals("Start scheduling")) {
            startScheduling();
        }
        else {
            stopScheduling();
        }
    }

    private void startScheduling() {
        String actualState = startStopScheduling.getText();
        if(actualState.equals("Stop scheduling")) {
            Operations.add("You can't start scheduling.\nScheduling is already running.");
        }
        else if(Communication.sendData(String.valueOf(START_SCHEDULING))){
            Platform.runLater(() -> startStopScheduling.setText("Stop scheduling"));
        }
    }

    private void stopScheduling() {
        String actualState = startStopScheduling.getText();
        if (actualState.equals("Start scheduling")) {
            Operations.add("You can't stop scheduling.\nScheduling is not running.");
        } else if(Communication.sendData(String.valueOf(STOP_SCHEDULING))){
            Platform.runLater(() -> startStopScheduling.setText("Start scheduling"));
        }
    }


    private void markFlagsAsConnected() {
        Others.wasUpdated = true;
    }

    private void setDisplayingShortcuts() {
        Pane shortcutsPane = null;
        Scene scene;
        try {
            shortcutsPane = FXMLLoader.load(Others.class.getResource(shortcuts_screen));
        }
        catch (Exception e) {
            lgr.fatal("Unable to load FXML resource: " + shortcuts_screen, e);
            System.exit(Mediator.EXIT_FXML_ERROR);
        }
        scene = pane.getParent().getScene();
        setReturningToMain(shortcutsPane);

        scene.setRoot(shortcutsPane);
    }

    private void setReturningToMain(Pane shortcutsPane) {
        Scene scene = pane.getParent().getScene();
        Button backToMain = (Button) GUIModule.GetById(shortcutsPane, "back");
        backToMain.setOnAction(e -> scene.setRoot(main_screen_root));
    }

    private void displayConnectionStatus() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!Others.wasUpdated && !Others.connectionFailed) {
                    BackgroundFill redBackground = new BackgroundFill(Color.RED, null, null);
                    Platform.runLater(() -> status.setBackground(new Background(redBackground)));
                    Platform.runLater(() -> status.setText("disconnected"));
                    connectionFailed = true;
                }
                else if(Others.connectionFailed && Others.wasUpdated) {
                    Others.connectionFailed = false;
                    BackgroundFill greenBackground = new BackgroundFill(Color.GREEN, null, null);
                    Platform.runLater(() -> status.setBackground(new Background(greenBackground)));
                    Platform.runLater(() -> status.setText("connected"));
                }

                Others.wasUpdated = false;
            }
        }, 6000,2500);
    }

    private void startLoadingScript() {
        try {
            URL url = Others.class.getResource(pathToScript);
            Path script_path = Paths.get(url.toURI());
            ProcessBuilder pb = new ProcessBuilder("python",script_path.toString());
            Process p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            Double loaded = Double.parseDouble(in.readLine());
            ProgressBar progressBar = ((ProgressBar) GUIModule.GetById(pane, "progress"));

            if(progressBar.isDisabled()) {
                progressBar.setDisable(false);
            }
            if(loaded >= 1) {
                Operations.add("Scheduler was loaded.");
            }
            progressBar.setProgress(loaded);

        } catch(Exception e) {
            lgr.error("Loading script failed");
        }

    }
}
