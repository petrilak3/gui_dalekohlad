package fmfi.dalekohlad;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        Mediator.meditate(stage);
    }

    public static void main() {
        launch();
    }

}
