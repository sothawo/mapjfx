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

    /**
     * property containing the map's zoom. This is a Double so that the property might be bound to a slider, internally
     * a rounded value is used
     */
    private SimpleDoubleProperty zoom;

    /** property containing the map's animation duration in ms */
    private SimpleDoubleProperty animationDuration;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * create a MapView with no initial center coordinate.
     */
    public MapView() {
        this(null);
    }

    /**
     * create a MapView with the given initial center coordinate.
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
     * initializes the JavaFX properties.
     *
     * @param centerCoordinate
     *         optional initial coordinate for the center property
     */
    private final void initProperties(Coordinate centerCoordinate) {
        center = new SimpleObjectProperty<>(centerCoordinate);
        center.addListener(new ChangeListener<Coordinate>() {
            @Override
            public void changed(ObservableValue<? extends Coordinate> observable, Coordinate oldValue,
                                Coordinate newValue) {
                logger.debug("center changed from {} to {}", oldValue, newValue);
                setCenterInMap();
            }
        });

        zoom = new SimpleDoubleProperty(INITIAL_ZOOM);
        zoom.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                logger.debug("zoom changed from {} to {}", oldValue, newValue);
                setZoomInMap();
            }
        });

        animationDuration = new SimpleDoubleProperty(0);
    }

    /**
     * sets the value of the actual zoom property in the OL map.
     */
    private void setZoomInMap() {
        if (getInitialized()) {
            int zoomInt = (int) getZoom();
            logger.debug("setting zoom in OpenLayers map: {}", zoomInt);
            webEngine.executeScript("setZoom(" + zoomInt + ',' + animationDuration.get() + ')');
        }
    }

    /**
     * @return true if the MapView is initialized.
     */
    public boolean getInitialized() {
        return initialized.get();
    }

    /**
     * @return the current zoom value.
     */
    public double getZoom() {
        return zoom.get();
    }

// -------------------------- OTHER METHODS --------------------------

    public SimpleDoubleProperty animationDurationProperty() {
        return animationDuration;
    }

    public SimpleObjectProperty<Coordinate> centerProperty() {
        return center;
    }

    /**
     * @return the current animation duration.
     */
    public double getAnimationDuration() {
        return animationDuration.get();
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
                        ((JSObject) webEngine.executeScript("window")).setMember("app", new JSConnector());

                        initialized.set(true);
                        setCenterInMap();
                        setZoomInMap();
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
     * sets the value of the center property in the OL map.
     */
    private void setCenterInMap() {
        Coordinate actCenter = getCenter();
        if (getInitialized() && null != actCenter) {
            logger.debug("setting center in OpenLayers map: {}", actCenter);
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
     * sets the zoom level. the zoom value is rounded to the next whole number using Math#round() and then checked to be
     * in the range be #MIN_ZOOM and #MAX_ZOOM. If the value is not inn this range, the call is ignored.
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

    /**
     * @return the readonly initialized property.
     */
    public ReadOnlyBooleanProperty initializedProperty() {
        return initialized.getReadOnlyProperty();
    }

    /**
     * sets the animation duration in ms. If a value greater than 1 is set, then panning or zooming the map by setting
     * the center or zoom property will be animated in the given time. Setting this to zero does not switch off the
     * zoom animation shown when clicking the controlas in the map.
     *
     * @param animationDuration
     */
    public void setAnimationDuration(double animationDuration) {
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

    public SimpleDoubleProperty zoomProperty() {
        return zoom;
    }

// -------------------------- INNER CLASSES --------------------------

    /**
     * JavaScript interface object. Methods of an object of this class are called from JS code in the web page.
     */
    public class JSConnector {

// -------------------------- OTHER METHODS --------------------------

        public void centerMovedTo(String lat, String lon) {
            if (null == lat || null == lon) {
                return;
            }
            try {
                logger.debug("JS reports new center value {}/{}", lat, lon);
                Coordinate newCenter = new Coordinate(Double.valueOf(lat), Double.valueOf(lon));
                // OL sets the center to slightly different values than what was passed in #setCenterInMap(), this is
                // caused by pixel/rounding calculation. Here we only change the center property when the difference
                // is greater that in the 8th digit after the decimal point
                if(!newCenter.isNear(getCenter(), 8)) {
                    setCenter(newCenter);
                }
            } catch (NumberFormatException e) {
                logger.warn("illegal coordinate strings {}/{}", lat, lon);
            }
        }

        public void zoomChanged(String zoom) {
            if (null != zoom) {
                try {
                    logger.debug("JS reports zoom value {}", zoom);
                    setZoom(Double.valueOf(zoom));
                } catch (NumberFormatException e) {
                    logger.warn("illegal zoom string {}", zoom);
                }
            }
        }

        public void debug(String msg) {
            logger.debug("JS: {}", msg);
        }
    }
}
