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

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Map component. To use the MapView, construct it and add it to your scene. Then the #initializedProperty should be
 * observed as well as bindings/observations to other properties should be established. Then the #initialize() Method
 * must be called. When the MapView is initialized and ready to be used, the #initializedProperty is set to true.
 *
 * No map is displayed until #setCenter(Coordinate) is called.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public final class MapView extends Region {
// ------------------------------ FIELDS ------------------------------

    /** minimal zoom level, OL defines this as 0 */
    public static final int MIN_ZOOM = 0;
    /** maximal zoom level, OL defines this as 28 */
    public static final int MAX_ZOOM = 28;
    /** initial zoom value for the map */
    public static final int INITIAL_ZOOM = 15;

    private static final Logger logger = LoggerFactory.getLogger(MapView.class);

    /** URL of the html code for the WebView */
    private static final String MAPVIEW_HTML = "/mapview.html";

    /** the WebEngine of the WebView containing the OpenLayers Map */
    private final WebEngine webEngine;

    /** readonly property that informs if this MapView is fully initialized */
    private final ReadOnlyBooleanWrapper initialized = new ReadOnlyBooleanWrapper(false);

    /** property containing the map's center */
    private SimpleObjectProperty<Coordinate> center;

    /** property containing the map's zoom */
    private SimpleDoubleProperty zoom;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * create a MapView with no initial center coordinate.
     */
    public MapView() {
        this(null);
    }

    /**
     * create a MapView with nthe given initial center coordinate.
     *
     * @param centerCoordinate
     *         initial center coordinate
     */
    public MapView(Coordinate centerCoordinate) {
        initProperties(centerCoordinate);
        // instantiate the WebView, resize it with this region by letting it observe the changes and add it as child
        WebView webView = new WebView();
        webEngine = webView.getEngine();
        webView.prefWidthProperty().bind(widthProperty());
        webView.prefHeightProperty().bind(heightProperty());
        getChildren().add(webView);
    }

    /**
     * iitializes the JavaFX properties.
     *
     * @param centerCoordinate
     *         optional coordinate for the center property
     */
    private final void initProperties(Coordinate centerCoordinate) {
        center = new SimpleObjectProperty<>(centerCoordinate);

        // the zoom property needs a change listener as it might be bound to i.e. a slider; then the setter is not
        // called on changes
        zoom = new SimpleDoubleProperty(INITIAL_ZOOM);
        zoom.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setZoom(newValue.doubleValue(), true);
            }
        });
    }

// -------------------------- OTHER METHODS --------------------------

    public SimpleObjectProperty<Coordinate> centerProperty() {
        return center;
    }

    /**
     * initializes the MapView. The internal HTML file is loaded into the contained WebView and the necessary setup is
     * made for communication between this object and the Javascript elements on the web page.
     */
    public void initialize() {
        logger.debug("initializing...");
        URL mapviewUrl = getClass().getResource(MAPVIEW_HTML);
        if (null == mapviewUrl) {
            logger.error("resource not found: {}", MAPVIEW_HTML);
        } else {
            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue,
                                    Worker.State newValue) {
                    logger.debug("WebEngine loader state  {} -> {}", oldValue, newValue);
                    if (Worker.State.SUCCEEDED == newValue) {
                        // set an interface object named 'app' in the web engine
                        ((JSObject) webEngine.executeScript("window")).setMember("app", new JavaApp());

                        initialized.set(true);
                        // check if a cordinate was set in the constructor
                        if (null != getCenter()) {
                            setCenter(getCenter());
                        }
                        setZoom(INITIAL_ZOOM);
                        logger.debug("initialized.");
                    } else if (Worker.State.FAILED == newValue) {
                        logger.error("error loading {}", MAPVIEW_HTML);
                    }
                }
            });
            // start loading the html containing the OL code
            webEngine.load(mapviewUrl.toExternalForm());
        }
    }

    /**
     * sets the center property
     *
     * @param center
     *         new center
     */
    public void setCenter(Coordinate center) {
        setCenter(center, true);
    }

    public void setZoom(double zoom) {
        setZoom(zoom, true);
    }

    /**
     * @return the readonly initialized property.
     */
    public ReadOnlyBooleanProperty initializedProperty() {
        return initialized.getReadOnlyProperty();
    }

    /**
     * sets the center property and eventually propagates the new center to the map
     *
     * @param center
     *         new center
     * @param sendToMap
     *         flag, if map should be changed
     */
    private void setCenter(Coordinate center, boolean sendToMap) {
        this.center.set(center);
        if (sendToMap && getInitialized()) {
            logger.debug("setting center in OpenLayers map: {}", getCenter());
            webEngine.executeScript("setCenter(" + center.getLatitude() + ',' + center.getLongitude() + ')');
        }
    }

    public Coordinate getCenter() {
        return center.get();
    }

    /**
     * sets the zoom level and eventually propagates it to the map
     *
     * @param zoom
     *         new zoom level mus be between #MIN_ZOOM and #MAX_ZOOM
     * @param sendToMap
     *         flag, if map should be changed
     */
    public void setZoom(double zoom, boolean sendToMap) {
        zoom = Math.round(zoom);
        if (zoom < MIN_ZOOM || zoom > MAX_ZOOM) {
            return;
        }
        this.zoom.set(zoom);
        if (sendToMap && getInitialized()) {
            logger.debug("setting zoom in OpenLayers map: {}", getZoom());
            webEngine.executeScript("setZoom(" + getZoom() + ')');
        }
    }

    public boolean getInitialized() {
        return initialized.get();
    }

    public double getZoom() {
        return zoom.get();
    }

    public SimpleDoubleProperty zoomProperty() {
        return zoom;
    }

// -------------------------- INNER CLASSES --------------------------

    /**
     * JavaScript interface object. Methods of an object of this class are called from JS code in the web page.
     */
    public class JavaApp {
// -------------------------- OTHER METHODS --------------------------

        public void centerMovedTo(String lat, String lon) {
            if (null == lat || null == lon) {
                return;
            }
            try {
                logger.debug("map moved by JS to {}/{}", lat, lon);
                setCenter(new Coordinate(Double.valueOf(lat), Double.valueOf(lon)), false);
            } catch (NumberFormatException e) {
                logger.warn("illegal coordinate strings {}/{}", lat, lon);
            }
        }

        public void zoomChanged(String zoom) {
            if (null != zoom) {
                try {
                    logger.debug("zoom changed by JS to {}", zoom);
                    setZoom(Double.valueOf(zoom), false);
                } catch (NumberFormatException e) {
                    logger.warn("illegal zoom string {}", zoom);
                }
            }
        }
    }
}
