/*
 Copyright 2015 Peter-Josef Meisch (pj.meisch@sothawo.com)

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

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
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
 * The MapView does it's logging using java logging with level FINER.<br><br>
 *
 * All the setters return the MapView itself to enable fluent programming.
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
    private final AtomicReference<Coordinate> lastCoordinateFromMap = new AtomicReference<>();

    /** used to store the last zoom value that was reported by the map to prevent setting it again in the map. */
    private final AtomicReference<Double> lastZoomFromMap = new AtomicReference<>();

    /** property containing the actual map style, defaults to {@link com.sothawo.mapjfx.MapType#OSM} */
    private SimpleObjectProperty<MapType> mapType;

    /** markers in the map together with the listeners f */
    private final Map<Marker, MarkerChangeListeners> markers = new HashMap<>();

    /** cache for loading images in base64 strings */
    private final ConcurrentHashMap<URL, String> imgCache = new ConcurrentHashMap<>();

    /** Connector object that is created in the web page and initialized when the page is fully loaded */
    private JSObject javascriptConnector;

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
    private void initProperties() {
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
                //noinspection NumberEquality
                if (newValue != lastZoomFromMap.get()) {
                    logger.finer(() -> "zoom changed from " + oldValue + " to " + newValue);
                    setZoomInMap();
                }
            }
        });

        animationDuration = new SimpleIntegerProperty(0);

        mapType = new SimpleObjectProperty<>(MapType.OSM);
        mapType.addListener(new ChangeListener<MapType>() {
            @Override
            public void changed(ObservableValue<? extends MapType> observable, MapType oldValue, MapType newValue) {
                logger.finer(() -> "map type changed from " + oldValue + " to " + newValue);
                setMapTypeInMap();
            }
        });
    }

    /**
     * sets the value of the center property in the OL map.
     */
    private void setCenterInMap() {
        Coordinate actCenter = getCenter();
        if (getInitialized() && null != actCenter) {
            logger.finer(
                    () -> "setting center in OpenLayers map: " + actCenter + ", animation: " + animationDuration.get());
            // using Double objects instead of primitives works here
            javascriptConnector
                    .call("setCenter", actCenter.getLatitude(), actCenter.getLongitude(), animationDuration.get());
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
            logger.finer(
                    () -> "setting zoom in OpenLayers map: " + zoomInt + ", animation: " + animationDuration.get());
            javascriptConnector.call("setZoom", zoomInt, animationDuration.get());
        }
    }

    /**
     * @return the current zoom value.
     */
    public double getZoom() {
        return zoom.get();
    }

    /**
     * sets the value of the mapType property in the OL map.
     */
    private void setMapTypeInMap() {
        if (getInitialized()) {
            String script = "setMapType('" + getMapType().toString() + "')";
            logger.finer(() -> "setting map type in OpenLayers map: " + script);
            webEngine.executeScript(script);
        }
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * adds a marker to the map. If it was already added, nothing is changed and false is returned.
     *
     * @param marker
     *         the marker
     * @return true if added
     * @throws java.lang.IllegalArgumentException
     *         if marker is null
     */
    public boolean addMarker(Marker marker) {
        if (null == marker) {
            throw new IllegalArgumentException();
        }
        if (null != markers.get(marker)) {
            return false;
        }
        // create a change listener for the coordinate and store it along with the marker
        ChangeListener<Coordinate> coordinateChangeListener = (observable, oldValue, newValue) -> {
            if (null == oldValue) {
                // if no position was available in the first call to addMarker, we need to add it to the map now
                addMarkerInMap(marker);
            } else {
                moveMarkerInMap(marker);
            }
        };
        // the same for the visibility
        ChangeListener<Boolean> visibileChangeListener =
                (observable, oldValue, newValue) -> setMarkerVisibleInMap(marker);
        markers.put(marker, new MarkerChangeListeners(coordinateChangeListener, visibileChangeListener));

        // observe the markers position and visibility
        marker.positionProperty().addListener(coordinateChangeListener);
        marker.visibleProperty().addListener(visibileChangeListener);

        // add the marker in the map and show it if needed
        if (marker.getVisible() && null != marker.getPosition()) {
            addMarkerInMap(marker);
        }
        logger.finer(() -> "added marker " + marker);

        return true;
    }

    /**
     * adjusts the markers position in the map.
     *
     * @param marker
     *         the marker to move
     */
    private void moveMarkerInMap(Marker marker) {
        if (getInitialized()) {
            String script = String.format(Locale.US, "moveMarker('%s',%f,%f)", marker.getId(),
                    marker.getPosition().getLatitude(), marker.getPosition().getLongitude());
            logger.finer(() -> "move marker in OpenLayers map " + script);
            webEngine.executeScript(script);
        }
    }

    /**
     * sets the visibilty of a marker in the map.
     *
     * @param marker
     *         the marker to show or hide
     */
    private void setMarkerVisibleInMap(Marker marker) {
        if (getInitialized()) {
            if (marker.getVisible()) {
                addMarkerInMap(marker);
            } else {
                removeMarkerInMap(marker);
            }
        }
    }

    /**
     * removes the given marker from the OL map
     *
     * @param marker
     *         the marker to remove
     */
    private void removeMarkerInMap(Marker marker) {
        if (getInitialized()) {
            String script = String.format(Locale.US, "removeMarker('%s')", marker.getId());
            logger.finer(() -> "remove marker in OpenLayers map " + script);
            webEngine.executeScript(script);
        }
    }

    /**
     * shows the new marker in the map
     *
     * @param marker
     *         marker to show
     */
    private void addMarkerInMap(Marker marker) {
        if (getInitialized() && null != marker.getPosition()) {
            String url = marker.getImageURL().toExternalForm();
            /*
            OBSOLETE since loading the page from memory string
            // check if the image must be loaded here and sent encoded via JS
            if (!mapViewLoadedFromLocalFile && URL_PROTOCOL_FILE.equals(marker.getImageURL().getProtocol())) {
                // can't give the orignal URL to the WebEngine because of CORS restriction, get a base64 encoding of
                // the img
                logger.finer(() -> "need to create data uri for " + marker.getImageURL().toExternalForm());
                url = createDataURI(marker.getImageURL());
            }
            */
            if (null != url) {
                String script = String.format(Locale.US, "addMarkerWithURL('%s','%s',%f,%f,%d,%d)", marker.getId(),
                        url, marker.getPosition().getLatitude(), marker.getPosition().getLongitude(),
                        marker.getOffsetX(),
                        marker.getOffsetY());
                logger.finer(() -> "add marker in OpenLayers map " + script);
                webEngine.executeScript(script);
            }
        }
    }

    /**
     * loads an image and converts it's data to a base64 encoded data url.
     *
     * NOT NEEDED AT THE MOMENT
     *
     * @param imageURL
     *         where to load the image from, may not be null
     * @return the encoded image as data url
     */
    @SuppressWarnings("UnusedDeclaration")
    private String createDataURI(final URL imageURL) {
        return imgCache.computeIfAbsent(imageURL, url -> {
            String dataUrl = null;
            try (InputStream isGuess = url.openStream();
                 InputStream isConvert = url.openStream();
                 ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                String contentType = URLConnection.guessContentTypeFromStream(isGuess);
                if (null != contentType) {
                    byte[] chunk = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = isConvert.read(chunk)) > 0) {
                        os.write(chunk, 0, bytesRead);
                    }
                    os.flush();
                    dataUrl = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(os
                            .toByteArray());
                } else {
                    logger.warning(() -> "could not get content type from " + imageURL.toExternalForm());
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "error loading image", e);
            }
            if (null == dataUrl) {
                logger.warning(() -> "could not create data url from " + imageURL.toExternalForm());
            }
            return dataUrl;
        });
    }

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
        // we could load the html via the URL, but then we run into problems loading local images or track files when
        // the mapView is embededded in a jar and loaded via jar: URI. If we load the page with loadContent, these
        // restrictions do not apply.
        loadMapViewHtml().ifPresent((html) -> {
            // instantiate the WebView, resize it with this region by letting it observe the changes and add it as child
            WebView webView = new WebView();
            logger.finer("WebView created");
            webEngine = webView.getEngine();
            webView.prefWidthProperty().bind(widthProperty());
            webView.prefHeightProperty().bind(heightProperty());
            // no context menu
            webView.setContextMenuEnabled(false);
            getChildren().add(webView);
            // log versions after webEngine is available
            logVersions();

            // watch for load changes
            webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                        logger.finer(() -> "WebEngine loader state " + oldValue + " -> " + newValue);
                        if (Worker.State.SUCCEEDED == newValue) {
                            // set an interface object named 'javaConnector' in the web engine
                            JSObject window = (JSObject) webEngine.executeScript("window");
                            window.setMember("javaConnector", new JavaConnector());

                            // get the Javascript connector object
                            javascriptConnector = (JSObject) webEngine.executeScript("getJsConnector()");
                            javascriptConnector.call("hello", "master");

                            initialized.set(true);
                            setCenterInMap();
                            setZoomInMap();
                            logger.finer("initialized.");
                        } else if (Worker.State.FAILED == newValue) {
                            logger.severe(() -> "error loading " + MAPVIEW_HTML);
                        }
                    }
            );
            // do the load
            logger.finer("load html into WebEngine");
            webEngine.loadContent(html);
        });
    }

    /**
     * log Java, JavaFX , OS and WebKit version.
     */
    private void logVersions() {
        logger.finer(() -> "Java Version:   " + System.getProperty("java.runtime.version"));
        logger.finer(() -> "JavaFX Version: " + System.getProperty("javafx.runtime.version"));
        logger.finer(() -> "OS:             " + System.getProperty("os.name") + ", " + System.getProperty("os.version")
                + ", " + System.getProperty("os.arch"));
        logger.finer(() -> "User Agent:     " + webEngine.getUserAgent());
    }

    /**
     * @return the readonly initialized property.
     */
    public ReadOnlyBooleanProperty initializedProperty() {
        return initialized.getReadOnlyProperty();
    }

    /**
     * loads the mapview.html file from the classpath into a string. The file is utf-8 encoded. The URL of the
     * mapview.html file is injected as &lt;base&gt; element after the &lt;head&gt; opening tag, so that css and js
     * files can be found by the WebView.
     *
     * @return the loaded string in an Optional
     */
    private Optional<String> loadMapViewHtml() {
        String mapViewHtml = null;
        URL mapviewURL = getClass().getResource(MAPVIEW_HTML);
        if (null == mapviewURL) {
            logger.severe(() -> "resource not found: " + MAPVIEW_HTML);
        } else {
            logger.finer(() -> "loading from " + mapviewURL.toExternalForm());
            try (
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(mapviewURL.openStream(), StandardCharsets.UTF_8))
            ) {
                StringBuilder sb = new StringBuilder();
                String line;
                while (null != (line = bufferedReader.readLine())) {
                    line = line.trim();
                    if ("<head>".equalsIgnoreCase(line)) {
                        sb.append(line);
                        line = "<base href=\"" + mapviewURL.toExternalForm() + "\">";
                    }
                    sb.append(line);
                }
                mapViewHtml = sb.toString();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "loading " + mapviewURL.toExternalForm(), e);
            }
        }
        return Optional.ofNullable(mapViewHtml);
    }

    /**
     * @return the mapType property.
     */
    public SimpleObjectProperty<MapType> mapTypeProperty() {
        return mapType;
    }

    /**
     * removes the given marker from th map and deregisters the change listeners. If the marker was not in the map,
     * nothing happens
     *
     * @param marker
     *         marker to remove
     * @throws java.lang.IllegalArgumentException
     *         if marker is null
     */
    public void removeMarker(Marker marker) {
        if (null == marker) {
            throw new IllegalArgumentException();
        }
        if (markers.containsKey(marker)) {
            marker.positionProperty().removeListener(markers.get(marker).getCoordinateChangeListener());
            marker.visibleProperty().removeListener(markers.get(marker).getVisibileChangeListener());
            markers.remove(marker);
            removeMarkerInMap(marker);
            marker.setVisible(false);
            logger.finer(() -> "removed marker " + marker);
        }
    }

    /**
     * sets the animation duration in ms. If a value greater than 1 is set, then panning or zooming the map by setting
     * the center or zoom property will be animated in the given time. Setting this to zero does not switch off the zoom
     * animation shown when clicking the controlas in the map.
     *
     * @param animationDuration
     *         animation duration in ms
     * @return this object
     */
    public MapView setAnimationDuration(int animationDuration) {
        this.animationDuration.set(animationDuration);
        return this;
    }

    /**
     * sets the center of the map. The coordinate must be in EPSG:4326 coordinates (WGS)
     *
     * @param center
     *         new center
     * @return this object
     */
    public MapView setCenter(Coordinate center) {
        this.center.set(center);
        return this;
    }

    /**
     * sets the center and zoom of the map so that the given extent is visible.
     *
     * @param extent
     *         extent to show, if null, nothing is changed
     * @return this object
     */
    public MapView setExtent(Extent extent) {
        if (getInitialized() && null != extent) {
            logger.finer(
                    () -> "setting extent in OpenLayers map: " + extent + ", animation: " + animationDuration.get());
            javascriptConnector.call("setExtent", extent.getMin().getLatitude(), extent.getMin().getLongitude(),
                    extent.getMax().getLatitude(), extent.getMax().getLongitude(), animationDuration.get());
        }
        return this;
    }

    /**
     * sets the current MapType.
     *
     * @param mapType
     *         the new MapType
     * @return this object
     */
    public MapView setMapType(MapType mapType) {
        this.mapType.set(mapType);
        return this;
    }

    /**
     * sets the zoom level. the zoom value is rounded to the next whole number using {@link Math#round(double)} and then
     * checked to be in the range between {@link #MIN_ZOOM} and {@link #MAX_ZOOM }. If the value is not in this range,
     * the call is ignored.
     *
     * @param zoom
     *         new zoom level
     * @return this object
     */
    public MapView setZoom(double zoom) {
        double rounded = Math.round(zoom);
        if (rounded < MIN_ZOOM || rounded > MAX_ZOOM) {
            return this;
        }
        this.zoom.set(rounded);
        return this;
    }

    public SimpleDoubleProperty zoomProperty() {
        return zoom;
    }

// -------------------------- INNER CLASSES --------------------------

    /**
     * Connector object. Methods of an object of this class are called from JS code in the web page.
     */
    public class JavaConnector {
        // -------------------------- OTHER METHODS --------------------------
        private final Logger logger = Logger.getLogger(JavaConnector.class.getCanonicalName());

        /**
         * called when the user has moved the map. the coordinates are EPSG:4326 (WGS) values. The arguments are double
         * primitives and no Double objects.
         *
         * @param lat
         *         new latitude value
         * @param lon
         *         new longitude value
         */
        public void centerMovedTo(double lat, double lon) {
            Coordinate newCenter = new Coordinate(lat, lon);
            logger.finer(() -> "JS reports center value " + newCenter);
            lastCoordinateFromMap.set(newCenter);
            setCenter(newCenter);
        }

        /**
         * called from the JS in the web page to output a message to the application's log.
         *
         * @param msg
         *         the message to log
         */
        public void debug(String msg) {
            logger.finer(() -> "JS: " + msg);
        }

        /**
         * called when an a href in the map is clicked and shows the URL in the default browser.
         *
         * @param href
         *         the url to show
         */
        public void showLink(String href) {
            logger.finer(() -> "JS aks to browse to " + href);
            if (!Desktop.isDesktopSupported()) {
                logger.warning(() -> "no desktop support for displaying " + href);
            } else {
                try {
                    Desktop.getDesktop().browse(new URI(href));
                } catch (IOException | URISyntaxException e) {
                    logger.log(Level.WARNING, "can't display " + href, e);
                }
            }
        }

        /**
         * called when the user has single-clicked in the map. the coordinates are EPSG:4326 (WGS) values.
         *
         * @param lat
         *         new latitude value
         * @param lon
         *         new longitude value
         */
        public void singleClickAt(double lat, double lon) {
            Coordinate coordinate = new Coordinate(lat, lon);
            logger.finer(() -> "JS reports single click at " + coordinate);
            // fire a coordinate event to whom it may be of importance
            fireEvent(new CoordinateEvent(CoordinateEvent.MAP_CLICKED, coordinate));
        }

        /**
         * called when the user changed the zoom with the controls in the map.
         *
         * @param zoom
         *         new zoom value
         */
        public void zoomChanged(double newZoom) {
            logger.finer(() -> "JS reports zoom value " + newZoom);
            lastZoomFromMap.set(newZoom);
            setZoom(newZoom);
        }
    }
}
