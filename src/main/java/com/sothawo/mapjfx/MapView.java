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

import com.sothawo.mapjfx.offline.OfflineCache;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

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
@SuppressWarnings("UnusedDeclaration")
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
    private static final String MAP_VIEW_NOT_YET_INITIALIZED = "MapView not yet initialized";

    /** number of retries if Javascript object is not ready. */
    private static final int NUM_RETRIES_FOR_JS = 10;

    /** marker for custom_mapview.css. */
    private static final String CUSTOM_MAPVIEW_CSS = "custom_mapview.css";
    /** readonly property that informs if this MapView is fully initialized. */
    private final ReadOnlyBooleanWrapper initialized = new ReadOnlyBooleanWrapper(false);
    /** used to store the last coordinate that was reported by the map to prevent setting it again in the map. */
    private final AtomicReference<Coordinate> lastCoordinateFromMap = new AtomicReference<>();
    /** used to store the last zoom value that was reported by the map to prevent setting it again in the map. */
    private final AtomicReference<Double> lastZoomFromMap = new AtomicReference<>();
    /**
     * a map from the names of MapCoordinateELements in the map to WeakReferences of the Objects. When
     * mapCoordinateElements are gc'ed the keys in this map point to null and are used to clean up the internal
     * structures.
     */
    private final Map<String, WeakReference<MapCoordinateElement>> mapCoordinateElements = new HashMap<>();
    /**
     * The listeners that are attached to the MapCoordinateElement objects.
     */
    private final Map<String, MapCoordinateElementListener> mapCoordinateElementListeners = new HashMap<>();
    /**
     * a map from the names of CoordinateLines in the map to WeakReferences of the CoordinateLines. When CoordianteLines
     * are gc'ed the keys in this map point to null and are used to clean up the internal structures.
     */
    private final Map<String, WeakReference<CoordinateLine>> coordinateLines = new HashMap<>();
    /**
     * the listeners that are attached to the CoordinateLine objects.
     */
    private final Map<String, CoordinateLineListener> coordinateLineListeners = new HashMap<>();
    /**
     * reference queue for the weak referenced objects. We don't need the objects themselves, so a list of Objects is
     * enough to handle Markers and CoordinateLines.
     */
    private final ReferenceQueue<Object> weakReferenceQueue = new ReferenceQueue<>();
    /** cache for loading images in base64 strings */
    private final ConcurrentHashMap<URL, String> imgCache = new ConcurrentHashMap<>();
    /** the OfflineCache. */
    private final OfflineCache offlineCache = new OfflineCache();
    /** the WebEngine of the WebView containing the OpenLayers Map. */
    private WebEngine webEngine;
    /** property containing the map's center. */
    private SimpleObjectProperty<Coordinate> center;
    /**
     * property containing the map's zoom; This is a Double so that the property might be bound to a slider, internally
     * a rounded value is used.
     */
    private SimpleDoubleProperty zoom;
    /** property containing the map's animation duration in ms. */
    private SimpleIntegerProperty animationDuration;
    /** property containing the actual map style, defaults to {@link com.sothawo.mapjfx.MapType#OSM} */
    private SimpleObjectProperty<MapType> mapType;
    /** Connector object that is created in the web page and initialized when the page is fully loaded */
    private JSObject javascriptConnector;
    /** Pattern to find resources to include in the local html file. */
    private Pattern htmlIncludePattern = Pattern.compile("^#(.+)#$");
    /** Bing Maps API Key. */
    private Optional<String> bingMapsApiKey = Optional.empty();
    /** URL for custom mapview css. */
    private Optional<URL> customMapviewCssURL = Optional.empty();

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

        startWeakRefCleaner();
    }

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * initializes the JavaFX properties.
     */
    private void initProperties() {
        center = new SimpleObjectProperty<>();
        center.addListener((observable, oldValue, newValue) -> {
            // check if this is the same value that was just reported from the map using object equality
            if (newValue != lastCoordinateFromMap.get()) {
                logger.finer(() -> "center changed from " + oldValue + " to " + newValue);
                setCenterInMap();
            }
        });

        zoom = new SimpleDoubleProperty(INITIAL_ZOOM);
        zoom.addListener((observable, oldValue, newValue) -> {
            // check if this is the same value that was just reported from the map using object equality
            //noinspection NumberEquality
            if (newValue != lastZoomFromMap.get()) {
                logger.finer(() -> "zoom changed from " + oldValue + " to " + newValue);
                setZoomInMap();
            }
        });

        animationDuration = new SimpleIntegerProperty(0);

        mapType = new SimpleObjectProperty<>(MapType.OSM);
        mapType.addListener((observable, oldValue, newValue) -> {
            logger.finer(() -> "map type changed from " + oldValue + " to " + newValue);
            if (!checkApiKey(newValue)) {
                logger.warning("no api key defined for map type " + newValue);
                mapType.set(oldValue);
            }
            setMapTypeInMap();
        });
    }

    /**
     * defines and starts the thread watching the weak reference queue(s)
     */
    private void startWeakRefCleaner() {
        Thread thread = new Thread(() -> {
            boolean running = true;
            while (running) {
                try {
                    // just wait, no need to get the - gc'ed object
                    weakReferenceQueue.remove();

                    // clean up the coordinateLines entries
                    final Set<String> coordinateLinesToRemove = new HashSet<>();
                    synchronized (coordinateLines) {
                        coordinateLines.forEach((k, v) -> {
                            if (null == v.get()) {
                                coordinateLinesToRemove.add(k);
                                logger.finer(() -> "need to cleanup gc'ed coordinate line " + k);
                            }
                        });
                    }
                    // run on the JavaFX thread, as removeCoordinateLineWithId() calls methods from the WebView
                    Platform.runLater(() -> coordinateLinesToRemove.forEach(this::removeCoordinateLineWithId));

                    // clean up the MapCoordinateElement entries
                    final Set<String> mapCoordinateElementsToRemove = new HashSet<>();
                    synchronized (mapCoordinateElements) {
                        mapCoordinateElements.forEach((k, v) -> {
                            if (null == v.get()) {
                                mapCoordinateElementsToRemove.add(k);
                                logger.finer(() -> "need to cleanup gc'ed element " + k);
                            }
                        });
                    }
                    // run on the JavaFX thread, as removeCoordinateLineWithId() calls methods from the WebView
                    Platform.runLater(
                            () -> mapCoordinateElementsToRemove.forEach(this::removeMapCoordinateElementWithId));
                } catch (InterruptedException e) {
                    logger.warning("thread interrupted");
                    running = false;
                }
            }
        });
        thread.setName("MapView-WeakRef-Cleaner");
        thread.setDaemon(true);
        thread.start();
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

// -------------------------- OTHER METHODS --------------------------

    /**
     * checks if the given map type needs an api key, and if so, if it is set.
     *
     * @param mapTypeToCheck
     *         the map type
     * @return true if either the map type does not need an api key or an api key was set.
     */
    private boolean checkApiKey(MapType mapTypeToCheck) {
        switch (requireNonNull(mapTypeToCheck)) {
            case BINGMAPS_ROAD:
            case BINGMAPS_AERIAL:
                return bingMapsApiKey.isPresent();
            default:
                return true;
        }
    }

    /**
     * sets the value of the mapType property in the OL map.
     */
    private void setMapTypeInMap() {
        if (getInitialized()) {
            String mapTypeName = getMapType().toString();
            logger.finer(() -> "setting map type in OpenLayers map: " + mapTypeName);
            bingMapsApiKey.ifPresent(apiKey -> javascriptConnector.call("setBingMapsApiKey", apiKey));
            javascriptConnector.call("setMapType", mapTypeName);
        }
    }

    /**
     * @return the current center of the map.
     */
    public Coordinate getCenter() {
        return center.get();
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

    /**
     * @return the current MapType.
     */
    public MapType getMapType() {
        return mapType.get();
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

    public OfflineCache getOfflineCache() {
        return offlineCache;
    }

    /**
     * add a CoordinateLine to the map. If it was already added, nothing happens. The MapView only stores a weak
     * reference to the object, so the caller must keep a reference in order to prevent the line to be removed from the
     * map. This method must only be called after the map is initialized, otherwise a warning is logged and the
     * coordinateLine is not added to the map.
     *
     * @param coordinateLine
     *         the CoordinateLine to add
     * @return this object
     * @throws java.lang.NullPointerException
     *         if argument is null
     */
    public MapView addCoordinateLine(CoordinateLine coordinateLine) {
        if (!getInitialized()) {
            logger.warning(MAP_VIEW_NOT_YET_INITIALIZED);
        } else {
            // sync on the coordinatesLines map as the cleaner thread accesses this as well
            synchronized (coordinateLines) {
                String id = requireNonNull(coordinateLine).getId();
                if (!coordinateLines.containsKey(id)) {
                    logger.fine(() -> "adding coordinate line " + coordinateLine);
                    JSObject jsCoordinateLine = (JSObject) javascriptConnector.call("getCoordinateLine", id);
                    coordinateLine.getCoordinateStream().forEach(
                            (coord) -> jsCoordinateLine
                                    .call("addCoordinate", coord.getLatitude(), coord.getLongitude()));
                    javafx.scene.paint.Color color = coordinateLine.getColor();
                    jsCoordinateLine.call("setColor", color.getRed() * 255, color.getGreen() * 255, color.getBlue() *
                            255, color.getOpacity());
                    jsCoordinateLine.call("setWidth", coordinateLine.getWidth());
                    jsCoordinateLine.call("seal");

                    ChangeListener<Boolean> changeListener =
                            (observable, newValue, oldValue) -> setCoordinateLineVisibleInMap(id);
                    coordinateLine.visibleProperty().addListener(changeListener);
                    // store the listener as we must unregister on removeCooridnateLine
                    coordinateLineListeners.put(id, new CoordinateLineListener(changeListener));
                    // store a weak reference to be able to remove the line from the map if the caller forgets to do so
                    coordinateLines.put(id, new WeakReference<>(coordinateLine, weakReferenceQueue));
                    setCoordinateLineVisibleInMap(id);
                }
            }
        }
        return this;
    }

    /**
     * shows or hides the coordinateline in the map according to it's visible property.
     *
     * @param coordinateLineId
     *         the id of the CoordinateLine object
     */
    private void setCoordinateLineVisibleInMap(String coordinateLineId) {
        if (null != coordinateLineId) {
            WeakReference<CoordinateLine> coordinateLineWeakReference = coordinateLines.get(coordinateLineId);
            if (null != coordinateLineWeakReference) {
                CoordinateLine coordinateLine = coordinateLineWeakReference.get();
                if (null != coordinateLine) {
                    if (coordinateLine.getVisible()) {
                        javascriptConnector.call("showCoordinateLine", coordinateLineId);
                    } else {
                        javascriptConnector.call("hideCoordinateLine", coordinateLineId);
                    }
                }
            }
        }
    }

    /**
     * adds a label to the map. If it was already added, nothing is changed. If the MapView is not yet initialized, a
     * warning is logged and nothing changes. If the label has no coordinate set, it is not added and a logging entry is
     * written.
     *
     * The MapView only keeps a weak reference to the label, so the caller must keep a reference to prevent the Label
     * object from being garbage collected.
     *
     * @param mapLabel
     *         the label
     * @return this object
     * @throws java.lang.NullPointerException
     *         if marker is null
     */
    public MapView addLabel(MapLabel mapLabel) {
        if (!getInitialized()) {
            logger.warning(MAP_VIEW_NOT_YET_INITIALIZED);
        } else {
            if (null == requireNonNull(mapLabel).getPosition()) {
                logger.finer(() -> "label with no position was not added: " + mapLabel);
                return this;
            }
            String id = mapLabel.getId();
            // synchronize on the mapCoordinateElements map as the cleaning thread accesses this as well
            synchronized (mapCoordinateElements) {
                // if the label is attached to a Marker, only add it when the marker is already added to the MapView
                if (mapLabel.getMarker().isPresent() && !mapCoordinateElements.containsKey(mapLabel.getMarker().get()
                        .getId())) {
                    return this;
                }
                if (!mapCoordinateElements.containsKey(id)) {
                    addMapCoordinateElement(mapLabel);
                    javascriptConnector.call("addLabel", id, mapLabel.getText(), mapLabel.getCssClass(),
                            mapLabel.getPosition().getLatitude(), mapLabel.getPosition().getLongitude(),
                            mapLabel.getOffsetX(), mapLabel.getOffsetY());

                    logger.finer(() -> "add label in OpenLayers map " + mapLabel.toString());
                    setMarkerVisibleInMap(id);
                }
            }
        }
        return this;
    }

    /**
     * sets up the internal information about a MpaCoordinate Element.
     *
     * @param mapCoordinateElement
     *         the MpaCooordinate Element
     */
    private void addMapCoordinateElement(MapCoordinateElement mapCoordinateElement) {
        String id = mapCoordinateElement.getId();
        // create change listeners for the coordinate and the visibility and store them with the
        // marker's id.
        ChangeListener<Coordinate> coordinateChangeListener =
                (observable, oldValue, newValue) -> moveMapCoordinateElementInMap(id);
        ChangeListener<Boolean> visibileChangeListener =
                (observable, oldValue, newValue) -> setMarkerVisibleInMap(id);
        mapCoordinateElementListeners.put(id, new MapCoordinateElementListener(coordinateChangeListener,
                visibileChangeListener));

        // observe the mapCoordinateElements position and visibility with the listsners
        mapCoordinateElement.positionProperty().addListener(coordinateChangeListener);
        mapCoordinateElement.visibleProperty().addListener(visibileChangeListener);

        // keep a weak ref of the mapCoordinateELement
        mapCoordinateElements.put(id, new WeakReference<>(mapCoordinateElement, weakReferenceQueue));
    }

    /**
     * sets the visibility of a MapCoordinateElement in the map.
     *
     * @param id
     *         the marker to show or hide
     */
    private void setMarkerVisibleInMap(String id) {
        if (null != id) {
            WeakReference<MapCoordinateElement> weakReference = mapCoordinateElements.get(id);
            if (null != weakReference) {
                MapCoordinateElement mapCoordinateElement = weakReference.get();
                if (null != mapCoordinateElement) {
                    if (mapCoordinateElement.getVisible()) {
                        javascriptConnector.call("showMapObject", mapCoordinateElement.getId());
                    } else {
                        javascriptConnector.call("hideMapObject", mapCoordinateElement.getId());
                    }
                }
            }
        }
    }

    /**
     * adjusts the mapCoordinateElement's position in the map.
     *
     * @param id
     *         the id of the element to move
     */
    private void moveMapCoordinateElementInMap(String id) {
        if (getInitialized() && null != id) {
            WeakReference<MapCoordinateElement> weakReference = mapCoordinateElements.get(id);
            if (null != weakReference) {
                MapCoordinateElement mapCoordinateElement = weakReference.get();
                if (null != mapCoordinateElement) {
                    logger.finer(() -> "move element in OpenLayers map to " + mapCoordinateElement.getPosition());
                    javascriptConnector.call("moveMapObject", mapCoordinateElement.getId(),
                            mapCoordinateElement.getPosition().getLatitude(),
                            mapCoordinateElement.getPosition().getLongitude());
                }
            }
        }
    }

    /**
     * adds a marker to the map. If it was already added, nothing is changed. If the MapView is not yet initialized, a
     * warning is logged and nothing changes. If the marker has no coordinate set, it is not added and a logging entry
     * is written.
     *
     * The MapView only keeps a weak reference to the marker, so the caller must keep a reference to prevent the Marker
     * object from being garbage collected.
     *
     * @param marker
     *         the marker
     * @return this object
     * @throws java.lang.NullPointerException
     *         if marker is null
     */
    public MapView addMarker(Marker marker) {
        if (!getInitialized()) {
            logger.warning(MAP_VIEW_NOT_YET_INITIALIZED);
        } else {
            if (null == requireNonNull(marker).getPosition()) {
                logger.finer(() -> "marker with no position was not added: " + marker);
                return this;
            }
            String id = marker.getId();
            // synchronize on the mapCoordinateElements map as the cleaning thread accesses this as well
            synchronized (mapCoordinateElements) {
                if (!mapCoordinateElements.containsKey(id)) {
                    addMapCoordinateElement(marker);
                    javascriptConnector.call("addMarker", id, marker.getImageURL().toExternalForm(),
                            marker.getPosition().getLatitude(), marker.getPosition().getLongitude(),
                            marker.getOffsetX(), marker.getOffsetY());

                    logger.finer(() -> "add marker in OpenLayers map " + marker.toString());
                    setMarkerVisibleInMap(id);
                }
            }
            marker.getMapLabel().ifPresent(this::addLabel);
        }
        return this;
    }

    public SimpleIntegerProperty animationDurationProperty() {
        return animationDuration;
    }

    public SimpleObjectProperty<Coordinate> centerProperty() {
        return center;
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

    /**
     * @return the current animation duration.
     */
    public int getAnimationDuration() {
        return animationDuration.get();
    }

    /**
     * sets the animation duration in ms. If a value greater than 1 is set, then panning or zooming the map by setting
     * the center or zoom property will be animated in the given time. Setting this to zero does not switch off the zoom
     * animation shown when clicking the controls in the map.
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

            // pass JS alerts to the logger
            webView.getEngine().setOnAlert(event -> logger.warning(() -> "JS alert: " + event.getData()));

            // watch for load changes
            webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                        logger.finer(() -> "WebEngine loader state " + oldValue + " -> " + newValue);
                        if (Worker.State.SUCCEEDED == newValue) {
                            // set an interface object named 'javaConnector' in the web engine
                            JSObject window = (JSObject) webEngine.executeScript("window");
                            window.setMember("javaConnector", new JavaConnector());

                            // get the Javascript connector object. Even if the html file is loaded, JS may not yet
                            // be ready, so prepare for an exception and retry
                            int numRetries = 0;
                            do {
                                Object o = null;
                                try {
                                    o = webEngine.executeScript("getJsConnector()");
                                    javascriptConnector = (JSObject) o;
                                } catch (JSException e) {
                                    logger.warning("JS not ready, retrying...");
                                    numRetries++;
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e1) {
                                        logger.warning("retry interrupted");
                                    }
                                } catch (Exception e) {
                                    logger.severe("getJsConnector: returned " + ((null == o) ? "(null)" : o.toString()));
                                    numRetries++;
                                }
                            } while (null == javascriptConnector && numRetries < NUM_RETRIES_FOR_JS);

                            if (null == javascriptConnector) {
                                logger.severe(() -> "error loading " + MAPVIEW_HTML + ", JavaScript not ready.");
                            } else {
                                javascriptConnector.call("hello", "master");

                                initialized.set(true);
                                setMapTypeInMap();
                                setCenterInMap();
                                setZoomInMap();
                                logger.finer("initialized.");
                            }
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
                    Stream<String> lines = new BufferedReader(
                            new InputStreamReader(mapviewURL.openStream(), StandardCharsets.UTF_8)).lines()
            ) {
                String baseURL = mapviewURL.toExternalForm();
                String baseURLPath = baseURL.substring(0, baseURL.lastIndexOf('/') + 1);
                mapViewHtml = lines
                        .map(String::trim)
                        .map(line -> processHtmlLine(baseURLPath, line))
                        .flatMap(List::stream)
                        .collect(Collectors.joining("\n"));
//                logger.finer(mapViewHtml);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "loading " + mapviewURL.toExternalForm(), e);
            }
        }
        return Optional.ofNullable(mapViewHtml);
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
     * processes a line from the html file, adding the base url and replacing template values.
     *
     * @param baseURL
     *         the URL of the file
     * @param line
     *         the line to process, must be trimmed
     * @return a List with the processed strings
     */
    private List<String> processHtmlLine(String baseURL, String line) {
        // insert base url
        if ("<head>".equalsIgnoreCase(line)) {
            return Arrays.asList(line, "<base href=\"" + baseURL + "\">");
        }

        // check for replacement pattern
        Matcher matcher = htmlIncludePattern.matcher(line);
        if (matcher.matches()) {
            String resource = baseURL + matcher.group(1);
            if (CUSTOM_MAPVIEW_CSS.equals(matcher.group(1))) {
                if (customMapviewCssURL.isPresent()) {
                    logger.finer(
                            () -> "loading custom mapview css from " + customMapviewCssURL.get().toExternalForm());
                    try (Stream<String> lines = new BufferedReader(
                            new InputStreamReader(customMapviewCssURL.get().openStream(), StandardCharsets.UTF_8))
                            .lines()
                    ) {
                        return lines
                                .filter(l -> !l.contains("<"))
                                .collect(Collectors.toList());
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "loading resource " + resource, e);
                    }
                }
            } else {
                logger.finer(() -> "loading from " + resource);
                try (Stream<String> lines = new BufferedReader(
                        new InputStreamReader(new URL(resource).openStream(), StandardCharsets.UTF_8))
                        .lines()
                ) {
                    return lines.collect(Collectors.toList());
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "loading resource " + resource, e);
                }
            }
        }

        // return the line
        return Collections.singletonList(line);
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
     * removes a CoordinateLine from the map. If it was not added or the MapView is not yet initialized, nothing
     * happens
     *
     * @param coordinateLine
     *         the CoordinateLine to add
     * @return this object
     * @throws java.lang.NullPointerException
     *         if argument is null
     */
    public MapView removeCoordinateLine(CoordinateLine coordinateLine) {
        if (!getInitialized()) {
            logger.warning(MAP_VIEW_NOT_YET_INITIALIZED);
        } else {
            removeCoordinateLineWithId(requireNonNull(coordinateLine).getId());
        }
        return this;
    }

    /**
     * removes the CoordinateLinewith the given id. if no such element is found, nothing happens.
     *
     * @param id
     *         id of the coordinate line, may not be null
     */
    private void removeCoordinateLineWithId(String id) {
        // sync on the map as the cleaner thread accesses this as well
        synchronized (coordinateLines) {
            if (coordinateLines.containsKey(id)) {
                logger.fine(() -> "removing coordinate line " + id);

                javascriptConnector.call("hideCoordinateLine", id);
                javascriptConnector.call("removeCoordinateLine", id);

                // if the coordinateLine was not gc'ed we need to unregister the listeners
                CoordinateLine coordinateLine = coordinateLines.get(id).get();
                CoordinateLineListener coordinateLineListener = coordinateLineListeners.get(id);
                if (null != coordinateLine && null != coordinateLineListener) {
                    coordinateLine.visibleProperty().removeListener(coordinateLineListener.getVisibileChangeListener());
                }

                coordinateLineListeners.remove(id);
                coordinateLines.remove(id);
            }
        }
    }

    /**
     * removes the given label from the map and deregisters the change listeners. If the label was not in the map or the
     * MapView is not yet initialized, nothing happens.
     *
     * @param mapLabel
     *         label to remove
     * @return this object
     * @throws java.lang.NullPointerException
     *         if mapLabel is null
     */
    public MapView removeLabel(MapLabel mapLabel) {
        if (!getInitialized()) {
            logger.warning(MAP_VIEW_NOT_YET_INITIALIZED);
        } else {
            if (!requireNonNull(mapLabel).getMarker().isPresent()) {
                removeMapCoordinateElement(mapLabel);
            }
        }
        return this;
    }

    /**
     * removes a MapCoordinateElement from the map. If no such element is found, nothing happens.
     *
     * @param mapCoordinateElement
     *         the element to remove
     */
    private void removeMapCoordinateElement(MapCoordinateElement mapCoordinateElement) {
        removeMapCoordinateElementWithId(requireNonNull(mapCoordinateElement).getId());
    }

    /**
     * remove a MapCoordinateElement with a given id from the map.  If no such element is found, nothing happens.
     *
     * @param id
     *         the id of the element to remove.
     */
    private void removeMapCoordinateElementWithId(String id) {
        // sync on the map as the cleaner thread accesses this as well
        synchronized (mapCoordinateElements) {
            if (mapCoordinateElements.containsKey(id)) {
                logger.fine(() -> "removing element " + id);

                javascriptConnector.call("hideMapObject", id);
                javascriptConnector.call("removeMapObject", id);

                // if the element was not gc'ed we need to unregister the listeners so we dont' react to events from
                // removed elements
                MapCoordinateElement element = mapCoordinateElements.get(id).get();
                MapCoordinateElementListener markerListener = mapCoordinateElementListeners.get(id);
                if (null != element && null != markerListener) {
                    element.positionProperty().removeListener(markerListener.getCoordinateChangeListener());
                    element.visibleProperty().removeListener(markerListener.getVisibileChangeListener());
                }

                mapCoordinateElementListeners.remove(id);
                mapCoordinateElements.remove(id);
                logger.finer(() -> "removed element " + id);
            }
        }
    }

    /**
     * removes the given marker from the map and deregisters the change listeners. If the marker was not in the map or
     * the MapView is not yet initialized, nothing happens.
     *
     * @param marker
     *         marker to remove
     * @return this object
     * @throws java.lang.NullPointerException
     *         if marker is null
     */
    public MapView removeMarker(Marker marker) {
        if (!getInitialized()) {
            logger.warning(MAP_VIEW_NOT_YET_INITIALIZED);
        } else {
            requireNonNull(marker).getMapLabel().ifPresent(this::removeMapCoordinateElement);
            removeMapCoordinateElement(marker);
        }
        return this;
    }

    /**
     * sets the Bing Maps API Key.
     *
     * @param apiKey
     *         api key
     * @return this object
     */
    public MapView setBingMapsApiKey(final String apiKey) {
        if (null != apiKey && !apiKey.isEmpty()) {
            bingMapsApiKey = Optional.of(apiKey);
        } else {
            bingMapsApiKey = Optional.empty();
        }
        return this;
    }

    /**
     * sets the URL for the custom mapview css file.
     *
     * @param url
     *         css url
     * @throws NullPointerException
     *         if url is null
     */
    public void setCustomMapviewCssURL(URL url) {
        requireNonNull(url);
        customMapviewCssURL = Optional.of(url);
    }

    /**
     * sets the center and zoom of the map so that the given extent is visible.
     *
     * @param extent
     *         extent to show, if null, nothing is changed
     * @return this object
     * @throws java.lang.NullPointerException
     *         when extent is null
     */
    public MapView setExtent(Extent extent) {
        if (!getInitialized()) {
            logger.warning(MAP_VIEW_NOT_YET_INITIALIZED);
        } else {
            requireNonNull(extent);
            logger.finer(
                    () -> "setting extent in OpenLayers map: " + extent + ", animation: " +
                            animationDuration.get());
            javascriptConnector.call("setExtent", extent.getMin().getLatitude(), extent.getMin().getLongitude(),
                    extent.getMax().getLatitude(), extent.getMax().getLongitude(), animationDuration.get());
        }
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
// ------------------------------ FIELDS ------------------------------

        private final Logger logger = Logger.getLogger(JavaConnector.class.getCanonicalName());

// -------------------------- OTHER METHODS --------------------------

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
            if (null != href && !href.isEmpty()) {
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
            fireEvent(new MapViewEvent(MapViewEvent.MAP_CLICKED, coordinate));
        }

        /**
         * called when a marker was clicked. the coordinates are EPSG:4326 (WGS) values.
         *
         * @param name
         *         name of the marker
         * @param lat
         *         latitude where the click occured
         * @param lon
         *         longitude where the click occured.
         */
        public void markerClicked(final String name) {
            logger.finer(() -> "JS reports marker " + name + " clicked");
            fireEvent(new MapViewEvent(MapViewEvent.MARKER_CLICKED, name));
        }

        /**
         * called when the user changed the zoom with the controls in the map.
         *
         * @param newZoom
         *         new zoom value
         */
        public void zoomChanged(double newZoom) {
            logger.finer(() -> "JS reports zoom value " + newZoom);
            lastZoomFromMap.set(newZoom);
            setZoom(newZoom);
        }
    }
}
