import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.IOException;

public class CallBack extends Application {

    //    private String fingerPrint;
    private WebView webView = new WebView();
    private WebEngine webEngine;
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public final static String APPEND_FINGER_ACTION = "appendFinger";
    public final static String IDENTIFY_FINGER_ACTION = "identifyFinger";
    public final static String ENROLL_FINGER_ACTION = "enrollFinger";


    public CallBack() {
        webEngine = webView.getEngine();
    }

    private Scene scene;

    public CallBack getInstance() {
        return this;
    }

    @Override
    public void start(Stage stage) {
        // create scene
        stage.setTitle("Web View");
        scene = new Scene(new Browser(), 1200, 700, Color.web("#666970"));
        // scene = new Scene(new Browser(), Screen.getPrimary().getBounds().getMaxX(), Screen.getPrimary().getBounds().getMaxY()-70, Color.web("#666970"));
        stage.setScene(scene);
        // show stage
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


    public void identifyFinger(final String fingerPrint) {
        Platform.runLater(new Runnable() {
            public void run() {
                //javaFX operations should go here
                JSObject jsobj = (JSObject) webEngine.executeScript("window");
                Object value = jsobj.call("identifyPatient");
                IdentifyFinger identifyFinger = new IdentifyFinger();
                String patientUuid = "";
                try {
                    patientUuid = identifyFinger.identifyPatient(fingerPrint, value);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                jsobj = (JSObject) webEngine.executeScript("window");
                jsobj.call("updateIdentificationStatus", patientUuid);

                SimpleFingersApplication.DisposeScanner();
            }
        });
    }

    public void enrollFinger(final String fingerPrint) {
        Platform.runLater(new Runnable() {
            public void run() {
                //javaFX operations should go here
                JSObject jsobj = (JSObject) webEngine.executeScript("window");
                Object value = jsobj.call("registerPatient", fingerPrint);
                SimpleFingersApplication.DisposeScanner();
            }
        });

    }

    public void appendFinger(final String fingerPrint) {
        Platform.runLater(new Runnable() {
            public void run() {
                //javaFX operations should go here
                JSObject jsobj = (JSObject) webEngine.executeScript("window");
                Object value = jsobj.call("appendFingerPrint", fingerPrint);
                SimpleFingersApplication.DisposeScanner();
            }
        });

    }

    class Browser extends Region {

        private HBox toolBar;

        final ImageView selectedImage = new ImageView();

        public Browser() {
            //apply the styles
            getStyleClass().add("webView");
            // create the toolbar
            toolBar = new HBox();
            toolBar.setAlignment(Pos.CENTER);
            toolBar.getStyleClass().add("webView-toolbar");
            toolBar.getChildren().add(createSpacer());

            final JavaApp javaApp = new JavaApp();
            webEngine.setJavaScriptEnabled(true);
            // process page loading
            webEngine.getLoadWorker().stateProperty().addListener(
                    new ChangeListener<State>() {

                        public void changed(ObservableValue<? extends State> ov,
                                            State oldState, State newState) {
                            //if (newState == State.SUCCEEDED) {
                            final JSObject win =
                                    (JSObject) webEngine.executeScript("window");
                            win.setMember("app", javaApp);
                            // }
                        }
                    }
            );

            // load the home page
            webEngine.load("http://localhost:8080/openmrs");

            //add components
            getChildren().add(toolBar);
            getChildren().add(webView);
        }

        // JavaScript interface object
        public class JavaApp {


            public void identifyFinger() {
                launchFingerprintScanner(CallBack.IDENTIFY_FINGER_ACTION);
            }

            public void enrollFinger() {
                launchFingerprintScanner(CallBack.ENROLL_FINGER_ACTION);

            }

            public void appendFinger() {
                launchFingerprintScanner(CallBack.APPEND_FINGER_ACTION);

            }

            public void launchFingerprintScanner(String task){
                SimpleFingersApplication.startScanner(getInstance(), task);
            }
        }

        private Node createSpacer() {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            return spacer;
        }

        @Override
        protected void layoutChildren() {
            double w = getWidth();
            double h = getHeight();
            double tbHeight = toolBar.prefHeight(w);
            layoutInArea(webView, 0, 0, w, h - tbHeight, 0, HPos.CENTER, VPos.CENTER);
            layoutInArea(toolBar, 0, h - tbHeight, w, tbHeight, 0, HPos.CENTER, VPos.CENTER);
        }
    }
}

