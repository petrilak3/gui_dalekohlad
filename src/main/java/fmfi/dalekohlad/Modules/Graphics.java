package fmfi.dalekohlad.Modules;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;


public class Graphics implements GUIModule {
    private Pane pane;
    private Line dirHorizont;
    private Circle dirHorizontCenter;
    private Rectangle dirHorizontStroke;

    private Line dirNSEWBinoculars;
    private Line dirNSEWDome;
    private Rectangle dirNSEWStroke;

    private final double MINIMUM_OF_ELEVATION = -20;
    private final double ROTATION = 45;
    private final double LENGTH_OF_INDICATOR = 42;

    private final double LENGTH_OF_INDICATOR_DOME = 23;
    private final double LENGTH_OF_INDICATOR_BINUCULARUS = 13;

    @Override
    public void init(Pane p) {
        pane = p;
        dirHorizont = (Line) GUIModule.GetById(pane, "HorizontLine");
        dirHorizontCenter = (Circle) GUIModule.GetById(pane, "HorizontCenterCircle");
        dirHorizontStroke = (Rectangle) GUIModule.GetById(pane, "Horizont");
        dirNSEWStroke = (Rectangle) GUIModule.GetById(pane, "NSEW");
        dirNSEWBinoculars = (Line) GUIModule.GetById(pane, "NSEWBinocularsLine");
        dirNSEWDome = (Line) GUIModule.GetById(pane, "NSEWDomeLine");
    }

    public void changeIndicatorColor(double degrees) {
        if(degrees <= MINIMUM_OF_ELEVATION) {
            dirHorizontCenter.setFill(Color.RED);
            dirHorizontStroke.setStroke(Color.RED);
            dirHorizont.setStroke(Color.RED);
        }
        else {
            dirHorizontCenter.setFill(Color.GREEN);
            dirHorizontStroke.setStroke(Color.GREEN);
            dirHorizont.setStroke(Color.GREEN);
        }
    }

    public void changeIndicatorOfBinocularsDomeColor(boolean synchronize) {
        if(synchronize) {
            dirNSEWStroke.setStroke(Color.GREEN);
        }
        else {
            dirNSEWStroke.setStroke(Color.RED);
        }
    }

    public void setIndicatorDirection(double degrees, double lengthOfIndicator, Line indicator, double maxRotation) {
        double length = lengthOfIndicator;
        double alpha = degrees * 2 * Math.PI/maxRotation;

        indicator.setEndX(indicator.getStartX() + length*Math.sin(alpha));
        indicator.setEndY(indicator.getStartY() - length*Math.cos(alpha));
    }

    @Override
    public void update(JsonObject jo) {
        if(jo.get("DEElevation") != null) {
            Platform.runLater(() -> {
                double degrees = Double.parseDouble(jo.get("DEElevation").getAsString()) - ROTATION;
                changeIndicatorColor(degrees);
                setIndicatorDirection(degrees,LENGTH_OF_INDICATOR,dirHorizont,180);
            });
        }
        if(jo.get("PAAzimuth") != null) {
            Platform.runLater(() -> {
                double degrees = Double.parseDouble(jo.get("PAAzimuth").getAsString());
                setIndicatorDirection(degrees,LENGTH_OF_INDICATOR_BINUCULARUS,dirNSEWBinoculars,360);
            });
        }
        if(jo.get("DOMEAzimuth") != null) {
            Platform.runLater(() -> {
                double degrees = Double.parseDouble(jo.get("DOMEAzimuth").getAsString());
                setIndicatorDirection(degrees,LENGTH_OF_INDICATOR_DOME,dirNSEWDome,360);
            });
        }
        if(jo.get("DOMESynch") != null) {
            if(jo.get("DOMESynch").getAsString().equals("No")) changeIndicatorOfBinocularsDomeColor(false);
            else changeIndicatorOfBinocularsDomeColor(true);
        }
    }
}
