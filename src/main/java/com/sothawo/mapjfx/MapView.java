/*
 Copyright 2014 Peter-Josef Meisch (pj.meisch@sothawo.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.sothawo.mapjfx;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Map component. To use the MapView, construct it and add it to your scene. Then the  {@link #initialized} property
 * should be observed as well as bindings/observations to other properties should be established. <br><br>
 *
 * After that, the {@link #initialize()} Method must be called. When the MapView is initialized and ready to be used,
 * the {@link #initialized} property is set to true.<br><br>
 *
 * No map is displayed until {@link #setCenter(Coordinate)} is called.<br><br>
 *
 * The MapView does it's logging using java logging with level FINER.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public final class MapView extends Region {
// ------------------------------ FIELDS ------------------------------

    /** minimal zoom level, OL defines this as 0. */
    public static final int MIN_ZOOM = 0;
    /** maximal zoom level, OL defines this as 28. */
    public static final int MAX_ZOOM = 28;
    /** initial zoom value for the map. */
    public static final int INITIAL_ZOOM = 14;

    /** Logger for the class */
    private static final Logger logger = Logger.getLogger(MapView.class.getCanonicalName());

    /** URL of the html code for the WebView. */
    private static final String MAPVIEW_HTML = "/mapview.html";

    /** the WebEngine of the WebView containing the OpenLayers Map. */
    private WebEngine webEngine;

    /** readonly property that informs if this MapView is fully initialized. */
    private final ReadOnlyBooleanWrapper initialized = new ReadOnlyBooleanWrapper(false);

    /** property containing the map's center. */
    private SimpleObjectProperty<Coordinate> center;

    /**
     * property containing the map's zoom; This is a Double so that the property might be bound to a slider, internally
     * a rounded value is used.
     */
    private SimpleDoubleProperty zoom;

    /** property containing the map's animation duration in ms. */
    private SimpleIntegerProperty animationDuration;

    /** used to store the last coordinate that was reported by the map to prevent setting it again in the map. */
    private AtomicReference<Coordinate> lastCoordinateFromMap = new AtomicReference<>();

    /** used to store the last zoom value that was reported by the map to prevent setting it again in the map. */
    private AtomicReference<Double> lastZoomFromMap = new AtomicReference<>();

    /** property containing the actual map style, defaults to {@link com.sothawo.mapjfx.MapType#OSM} */
    private SimpleObjectProperty<MapType> mapType;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * create a MapView with no initial center coordinate.
     */
    public MapView() {
        initProperties();
        // we don't initialize the WebView here, as this would prevent the MapView from being created in SceneBuilder.
        // This is all done in the initialize method.

        // set a silver background to make the MapView distinguishable in SceneBuilder, this will later be hidden by
        // the WebView
        setBackground(new Background(new BackgroundFill(Paint.valueOf("#ccc"), null, null)));
    }

    /**
     * initializes the JavaFX properties.
     */
    private final void initProperties() {
        center = new SimpleObjectProperty<>();
        center.addListener(new ChangeListener<Coordinate>() {
            @Override
            public void changed(ObservableValue<? extends Coordinate> observable, Coordinate oldValue,
                                Coordinate newValue) {
                // check if this is the same value that was just reported from the map using object equality
                if (newValue != lastCoordinateFromMap.get()) {
                    logger.finer(() -> "center changed from " + oldValue + " to " + newValue);
                    setCenterInMap();
                }
            }
        });

        zoom = new SimpleDoubleProperty(INITIAL_ZOOM);
        zoom.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                // check if this is the same value that was just reported from the map using object equality
                if (newValue != lastZoomFromMap.get()) {
                    logger.finer(() -> "zoom changed from " + oldValue + " to " + newValue);
                    setZoomInMap();
                }
            }
        });

        animationDuration = new SimpleIntegerProperty(0);

        mapType = new SimpleObjectProperty<>(MapType.OSM);
    }

    /**
     * sets the value of the center property in the OL map.
     */
    private void setCenterInMap() {
        Coordinate actCenter = getCenter();
        if (getInitialized() && null != actCenter) {
            logger.finer(() -> "setting center in OpenLayers map: " + actCenter);
            webEngine.executeScript("setCenter(" + actCenter.getLatitude() + ',' + actCenter.getLongitude() + ',' +
                    animationDuration.get() + ')');
        }
    }

    /**
     * @return the current center of the map.
     */
    public Coordinate getCenter() {
        return center.get();
    }

    /**
     * @return true if the MapView is initialized.
     */
    public boolean getInitialized() {
        return initialized.get();
    }

    /**
     * sets the value of the actual zoom property in the OL map.
     */
    private void setZoomInMap() {
        if (getInitialized()) {
            int zoomInt = (int) getZoom();
            logger.finer(() -> "setting zoom in OpenLayers map: " + zoomInt);
            webEngine.executeScript("setZoom(" + zoomInt + ',' + animationDuration.get() + ')');
        }
    }

    /**
     * @return the current zoom value.
     */
    public double getZoom() {
        return zoom.get();
    }

// -------------------------- OTHER METHODS --------------------------

    public SimpleIntegerProperty animationDurationProperty() {
        return animationDuration;
    }

    public SimpleObjectProperty<Coordinate> centerProperty() {
        return center;
    }

    /**
     * @return the current animation duration.
     */
    public int getAnimationDuration() {
        return animationDuration.get();
    }

    /**
     * @return the curtrent MapType.
     */
    public MapType getMapType() {
        return mapType.get();
    }

    /**
     * initializes the MapView. The internal HTML file is loaded into the contained WebView and the necessary setup is
     * made for communication between this object and the Javascript elements on the web page.
     */
    public void initialize() {
        logger.finer("initializing...");
        // instantiate the WebView, resize it with this region by letting it observe the changes and add it as child
        WebView webView = new WebView();
        webEngine = webView.getEngine();
        webView.prefWidthProperty().bind(widthProperty());
        webView.prefHeightProperty().bind(heightProperty());
        getChildren().add(webView);

        URL mapviewUrl = getClass().getResource(MAPVIEW_HTML);
        if (null == mapviewUrl) {
            logger.severe(() -> "resource not found: " + MAPVIEW_HTML);
        } else {
            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue,
                                    Worker.State newValue) {
                    logger.finer(() -> "WebEngine loader state " + oldValue + " -> " + newValue);
                    if (Worker.State.SUCCEEDED == newValue) {
                        // set an interface object named 'app' in the web engine
                        ((JSObject) webEngine.executeScript("window")).setMember("app", new JSConnector());

                        initialized.set(true);
                        setCenterInMap();
                        setZoomInMap();
                        logger.finer("initialized.");
                    } else if (Worker.State.FAILED == newValue) {
                        logger.severe(() -> "error loading " + MAPVIEW_HTML);
                    }
                }
            });
            // start loading the html containing the OL code
            webEngine.load(mapviewUrl.toExternalForm());
        }
    }

    /**
     * @return the readonly initialized property.
     */
    public ReadOnlyBooleanProperty initializedProperty() {
        return initialized.getReadOnlyProperty();
    }

    /**
     * @return the mapType property.
     */
    public SimpleObjectProperty<MapType> mapTypeProperty() {
        return mapType;
    }

    /**
     * sets the animation duration in ms. If a value greater than 1 is set, then panning or zooming the map by setting
     * the center or zoom property will be animated in the given time. Setting this to zero does not switch off the zoom
     * animation shown when clicking the controlas in the map.
     *
     * @param animationDuration
     *         animation duration in ms
     */
    public void setAnimationDuration(int animationDuration) {
        this.animationDuration.set(animationDuration);
    }

    /**
     * sets the center of the map. The coordinate must be in EPSG:4326 coordinates (WGS)
     *
     * @param center
     *         new center
     */
    public void setCenter(Coordinate center) {
        this.center.set(center);
    }

    /**
     * sets the current MapType.
     *
     * @param mapType
     *         the new MapType
     */
    public void setMapType(MapType mapType) {
        this.mapType.set(mapType);
    }

    /**
     * sets the zoom level. the zoom value is rounded to the next whole number using {@link Math#round(double)} and then
     * checked to be in the range between {@link #MIN_ZOOM} and {@link #MAX_ZOOM }. If the value is not in this range,
     * the call is ignored.
     *
     * @param zoom
     *         new zoom level
     */
    public void setZoom(double zoom) {
        double rounded = Math.round(zoom);
        if (rounded < MIN_ZOOM || rounded > MAX_ZOOM) {
            return;
        }
        this.zoom.set(rounded);
    }

    public SimpleDoubleProperty zoomProperty() {
        return zoom;
    }

// -------------------------- INNER CLASSES --------------------------

    /**
     * JavaScript interface object. Methods of an object of this class are called from JS code in the web page.
     */
    public class JSConnector {
// -------------------------- OTHER METHODS --------------------------

        /**
         * called when the user has moved the map. the coordinates are EPSG:4326 (WGS) values.
         *
         * @param lat
         *         new latitude value
         * @param lon
         *         new longitude value
         */
        public void centerMovedTo(String lat, String lon) {
            if (null == lat || null == lon) {
                return;
            }
            try {
                logger.finer(() -> "JS reports new center value " + lat + "/" + lon);
                Coordinate newCenter = new Coordinate(Double.valueOf(lat), Double.valueOf(lon));
                lastCoordinateFromMap.set(newCenter);
                setCenter(newCenter);
            } catch (NumberFormatException e) {
                logger.warning(() -> "illegal coordinate strings " + lat + "/" + lon);
            }
        }

        /**
         * called from the JS in the web page to output a message to the applicatin's log.
         *
         * @param msg
         *         the message to log
         */
        public void debug(String msg) {
            logger.finer(() -> "JS: " + msg);
        }

        /**
         * called when the user changed the zoom with the controls in the map.
         *
         * @param zoom
         *         new zoom value
         */
        public void zoomChanged(String zoom) {
            if (null != zoom) {
                try {
                    logger.finer(() -> "JS reports zoom value " + zoom);
                    Double newZoom = Double.valueOf(zoom);
                    lastZoomFromMap.set(newZoom);
                    setZoom(newZoom);
                } catch (NumberFormatException e) {
                    logger.warning(() -> "illegal zoom string " + zoom);
                }
            }
        }
    }
}
