package fmfi.dalekohlad;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import fmfi.dalekohlad.Communication.Communication;
import fmfi.dalekohlad.InputHandling.ShortcutHandler;
import fmfi.dalekohlad.LockInstance.LockInstance;
import fmfi.dalekohlad.Modules.GUIModule;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Mediator {
    private static final Logger lgr = LogManager.getLogger(Main.class);
    private static final String main_screen = "/fxml/main_screen.fxml";
    private static final String module_prefix = "module_";
    private static final String config = "/config.json";
    public static final int EXIT_LOCK_INSTANCE_ERROR = 1;
    public static final int EXIT_NETWORK_CONFIG_ERROR = 2;
    public static final int EXIT_FXML_ERROR = 3;
    public static final int EXIT_MISSING_OPERATIONS = 4;
    private static ArrayList<GUIModule> modules = new ArrayList<>();
    private static HashMap<Pair<Boolean, KeyCode>, Runnable> shortcuts = new HashMap<>();

    public static void meditate(Stage stage) {
        LockInstance.lock_instance();
        loadScene(stage);
        bindModules(stage);
        InetSocketAddress host = loadHostFromConfig();
        Communication.init(host, modules);
    }

    private static void loadScene(Stage stage) {
        Scene scene = null;
        try {
            scene = FXMLLoader.load(Mediator.class.getResource(main_screen));
        }
        catch (Exception e) {
            lgr.fatal("Unable to load FXML resource: " + main_screen, e);
            System.exit(EXIT_FXML_ERROR);
        }
        stage.setScene(scene);
        stage.show();
        lgr.debug("Loaded FXML " + main_screen);
    }

    private static JsonObject getNetworkConfigJsonObject() {
        JsonReader reader = null;
        try {
            URL url = Mediator.class.getResource(config);
            Path config_path = Paths.get(url.toURI());
            reader = new JsonReader(new FileReader(config_path.toString()));
        }
        catch (Exception e) {
            lgr.fatal("Failed to load network config", e);
            System.exit(EXIT_NETWORK_CONFIG_ERROR);
        }
        return JsonParser.parseReader(reader).getAsJsonObject();
    }

    private static InetSocketAddress loadHostFromConfig() {
        JsonObject jo = getNetworkConfigJsonObject();
        String address = jo.getAsJsonPrimitive("address").getAsString();
        short port = jo.getAsJsonPrimitive("port").getAsShort();
        lgr.debug("Loaded network config");
        return new InetSocketAddress(address, port);
    }

    public static void registerShortcuts(Stage stage, Map<Pair<Boolean, KeyCode>, Runnable> shortcuts) {
        stage.getScene().setOnKeyPressed(new ShortcutHandler<KeyEvent>(shortcuts));
    }

    private static void loadModule(GUIModule module, Pane pane) {
        module.init(pane);
        module.registerShortcuts(shortcuts);
        modules.add(module);
    }

    private static void loadModuleByID(String id, Pane pane) {
        String module_name = id.substring(module_prefix.length());
        String package_name = Mediator.class.getPackageName();
        String module_path = package_name + ".Modules." + module_name;
        try {
            GUIModule module = (GUIModule) Class.forName(module_path).getConstructor().newInstance();
            loadModule(module, pane);
            lgr.debug("Loaded module: " + module_name);
        }
        catch (Exception e) {
            lgr.error("Failed to load module " + module_name, e);
        }
    }

    private static void extractModuleFromNode(Node node) {
        if (!(node instanceof Pane)) {
            return;
        }
        Pane pane = (Pane) node;
        pane.setOnMouseClicked(actionEvent -> pane.requestFocus());
        String id = pane.getId();
        if (id.equals("Operations")) {
            Operations.init(pane);
        }
        else if (id.startsWith(module_prefix)) {
            loadModuleByID(id, pane);
        }
    }

    private static void bindModules(Stage stage) {
        // initializes and collects correct modules
        Scene scene = stage.getScene();
        Parent root = scene.getRoot();
        for (Node node: root.getChildrenUnmodifiable()) {
            extractModuleFromNode(node);
        }
        if (Operations.getPane() == null) {
            lgr.fatal("Unable to find Operations pane");
            System.exit(EXIT_MISSING_OPERATIONS);
        }
        registerShortcuts(stage, shortcuts);
    }
}
