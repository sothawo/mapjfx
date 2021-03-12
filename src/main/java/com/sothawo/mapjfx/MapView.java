/*
 Copyright 2015-2021 Peter-Josef Meisch (pj.meisch@sothawo.com)

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

import com.sothawo.mapjfx.event.ClickType;
import com.sothawo.mapjfx.event.MapLabelEvent;
import com.sothawo.mapjfx.event.MapViewEvent;
import com.sothawo.mapjfx.event.MarkerEvent;
import com.sothawo.mapjfx.offline.OfflineCache;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.event.EventType;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.*;

/**
 * Map component. To use the MapView, construct it and add it to your scene. Then the  {@link #initialized} property
 * should be observed as well as bindings/observations to other properties should be established. <br><br>
 *
 * After that, the {@link #initialize()} method or one of it's overloads must be called.
 * When the MapView is initialized and ready to be used,
 * the {@link #initialized} property is set to true.<br><br>
 *
 * No map is displayed until {@link #setCenter(Coordinate)} is called.<br><br>
 *
 * The MapView does it's logging using java logging with level FINER.<br><br>
 *
 * All the setters return the MapView itself to enable fluent programming.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 * @author Erik Jähne
 */
@SuppressWarnings("UnusedDeclaration")
public final class MapView extends Region implements AutoCloseable {

    /** minimal zoom level, OL defines this as 0. */
    public static final int MIN_ZOOM = 0;
    /** maximal zoom level, OL defines this as 28. */
    public static final int MAX_ZOOM = 28;
    /** initial zoom value for the map. */
    public static final int INITIAL_ZOOM = 14;

    /** Logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(MapView.class);

    /** URL of the html code for the WebView. */
    private static final String MAPVIEW_HTML = "/mapview.html";
    private static final String MAP_VIEW_NOT_YET_INITIALIZED = "MapView not yet initialized";

    /** number of retries if Javascript object is not ready. */
    private static final int NUM_RETRIES_FOR_JS = 10;

    /** marker for custom_mapview.css. */
    private static final String CUSTOM_MAPVIEW_CSS = "custom_mapview.css";
    /** readonly property that informs if this MapView is fully initialized. */
    private final ReadOnlyBooleanWrapper initialized = new ReadOnlyBooleanWrapper(false);
    /** flag that is set to true after the technical infrastructure is set up, but before center, zoom etc are set. */
    private final AtomicBoolean mapViewReady = new AtomicBoolean(false);
    /** used to store the last coordinate that was reported by the map to prevent setting it again in the map. */
    private final AtomicReference<Coordinate> lastCoordinateFromMap = new AtomicReference<>();
    /** used to store the last zoom value that was reported by the map to prevent setting it again in the map. */
    private final AtomicReference<Long> lastZoomFromMap = new AtomicReference<>();
    /**
     * a map from the names of MapCoordinateElements in the map to WeakReferences of the Objects. When
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
    private final OfflineCache offlineCache = OfflineCache.INSTANCE;
    /** the connector object in the web page; field to prevent it being gc'ed. */
    private final JavaConnector javaConnector = new JavaConnector();
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
    private JSObject jsMapView;
    /** Pattern to find resources to include in the local html file. */
    private Pattern htmlIncludePattern = Pattern.compile("^#(.+)#$");
    /** Bing Maps API Key. */
    private Optional<String> bingMapsApiKey = Optional.empty();
    /** URL for custom mapview css. */
    private Optional<URL> customMapviewCssURL = Optional.empty();
    /** optional WMS server parameters. */
    private Optional<WMSParam> wmsParam = Optional.empty();
    /** optional XYZ server parameters. */
    private Optional<XYZParam> xyzParam = Optional.empty();
    /** the thread to clean weak references. */
    private Thread weakRefCleaner;

    private final Map<String, WeakReference<MapCircle>> mapCircles = new HashMap<>();

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

    /**
     * should be called when the MapViewis no longer needed. Cleans up internal resources.
     */
    public void close() {
        stopWeakRefCleaner();
    }

    /**
     * initializes the JavaFX properties.
     */
    private void initProperties() {
        center = new SimpleObjectProperty<>();
        center.addListener((observable, oldValue, newValue) -> {
            // check if this is the same value that was just reported from the map using object equality
            if (newValue != lastCoordinateFromMap.get()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("center changed from {} to {}", oldValue, newValue);
                }
                setCenterInMap();
            }
        });

        zoom = new SimpleDoubleProperty(INITIAL_ZOOM);
        zoom.addListener((observable, oldValue, newValue) -> {
            // check if this is the same value that was just reported from the map using object equality
            final Long rounded = Math.round((Double) newValue);
            if (!Objects.equals(rounded, lastZoomFromMap.get())) {
                if (logger.isTraceEnabled()) {
                    logger.trace("zoom changed from {} to {}", oldValue, rounded);
                }
                setZoomInMap();
            }
        });

        animationDuration = new SimpleIntegerProperty(0);

        mapType = new SimpleObjectProperty<>(MapType.OSM);
        mapType.addListener((observable, oldValue, newValue) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("map type changed from {} to {}", oldValue, newValue);
            }
            if (!checkApiKey(newValue)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("no api key defined for map type {}", newValue);
                }
                mapType.set(oldValue);
            }
            if (MapType.WMS == newValue) {
                boolean wmsValid = false;
                if (wmsParam.isPresent()) {
                    String url = wmsParam.get().getUrl();
                    if (null != url && !url.isEmpty()) {
                        wmsValid = true;
                    }
                }
                if (!wmsValid) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("no wms params defined for map type {}", newValue);
                    }
                    mapType.set(oldValue);
                }
            }
            if (MapType.XYZ.equals(newValue)) {
                boolean xyzValid = false;
                if (xyzParam.isPresent()) {
                    String url = xyzParam.get().getUrl();
                    if (null != url && !url.isEmpty()) {
                        xyzValid = true;
                    }
                }
                if (!xyzValid) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("no xyz params defined for map type {}", newValue);
                    }
                    mapType.set(oldValue);
                }
            }
            setMapTypeInMap();
        });
    }

    /**
     * defines and starts the thread watching the weak reference queue(s)
     */
    private synchronized void startWeakRefCleaner() {
        weakRefCleaner = new Thread(() -> {
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
                                if (logger.isTraceEnabled()) {
                                    logger.trace("need to cleanup gc'ed coordinate line {}", k);
                                }
                            }
                        });
                    }
                    // run on the JavaFX thread, as removeCoordinateLineWithId() calls methods from the WebView
                    Platform.runLater(() -> coordinateLinesToRemove.forEach(this::removeCoordinateLineWithId));

                    // clean up the coordinateLines entries
                    final Set<String> mapCirclesToRemove = new HashSet<>();
                    synchronized (mapCircles) {
                        mapCircles.forEach((k, v) -> {
                            if (null == v.get()) {
                                mapCirclesToRemove.add(k);
                                if (logger.isTraceEnabled()) {
                                    logger.trace("need to cleanup gc'ed map circle {}", k);
                                }
                            }
                        });
                    }
                    // run on the JavaFX thread, as removeCoordinateLineWithId() calls methods from the WebView
                    Platform.runLater(() -> mapCirclesToRemove.forEach(this::removeMapCircleWithId));

                    // clean up the MapCoordinateElement entries
                    final Set<String> mapCoordinateElementsToRemove = new HashSet<>();
                    synchronized (mapCoordinateElements) {
                        mapCoordinateElements.forEach((k, v) -> {
                            if (null == v.get()) {
                                mapCoordinateElementsToRemove.add(k);
                                if (logger.isTraceEnabled()) {
                                    logger.trace("need to cleanup gc'ed element {}", k);
                                }
                            }
                        });
                    }
                    // run on the JavaFX thread, as removeCoordinateLineWithId() calls methods from the WebView
                    Platform.runLater(
                        () -> mapCoordinateElementsToRemove.forEach(this::removeMapCoordinateElementWithId));
                } catch (InterruptedException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("thread interrupted");
                    }
                    running = false;
                }
            }
        });
        weakRefCleaner.setName("MapView-WeakRef-Cleaner");
        weakRefCleaner.setDaemon(true);
        weakRefCleaner.start();
    }

    private synchronized void stopWeakRefCleaner() {
        if (weakRefCleaner != null) {
            weakRefCleaner.interrupt();
            weakRefCleaner = null;
        }
    }

    /**
     * sets the value of the center property in the OL map.
     */
    private void setCenterInMap() {
        final Coordinate actCenter = getCenter();
        if (getInitialized() && null != actCenter) {
            if (logger.isTraceEnabled()) {
                logger.trace("setting center in OpenLayers map: {}, animation: {}", actCenter, animationDuration.get());
            }
            // using Double objects instead of primitives works here
            jsMapView
                .call("setCenter", actCenter.getLatitude(), actCenter.getLongitude(), animationDuration.get());
        }
    }

    /**
     * sets the value of the actual zoom property in the OL map.
     */
    private void setZoomInMap() {
        if (getInitialized()) {
            final int zoomInt = (int) getZoom();
            if (logger.isTraceEnabled()) {
                logger.trace("setting zoom in OpenLayers map: {}, animation: {}", zoomInt, animationDuration.get());
            }
            jsMapView.call("setZoom", zoomInt, animationDuration.get());
        }
    }

    /**
     * checks if the given map type needs an api key, and if so, if it is set.
     *
     * @param mapTypeToCheck
     *     the map type
     * @return true if either the map type does not need an api key or an api key was set.
     */
    private boolean checkApiKey(final MapType mapTypeToCheck) {
        switch (requireNonNull(mapTypeToCheck)) {
            case BINGMAPS_ROAD:
            case BINGMAPS_AERIAL:
            case BINGMAPS_AERIAL_WITH_LABELS:
            case BINGMAPS_CANVAS_DARK:
            case BINGMAPS_CANVAS_GRAY:
            case BINGMAPS_CANVAS_LIGHT:
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
            final String mapTypeName = getMapType().toString();
            if (logger.isDebugEnabled()) {
                logger.debug("setting map type in OpenLayers map: {}", mapTypeName);
            }
            bingMapsApiKey.ifPresent(apiKey -> jsMapView.call("setBingMapsApiKey", apiKey));
            wmsParam.ifPresent(wmsParam -> {
                jsMapView.call("newWMSParams");
                jsMapView.call("setWMSParamsUrl", wmsParam.getUrl());
                wmsParam.getParams().forEach((key, value) -> jsMapView.call("addWMSParamsParams", key, value));
            });
            xyzParam.ifPresent(xyzParam -> {
                jsMapView.call("setXYZParams", xyzParam.toJSON());
            });
            jsMapView.call("setMapType", mapTypeName);
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
     *     new center
     * @return this object
     */
    public MapView setCenter(final Coordinate center) {
        this.center.set(center);
        return this;
    }

    /**
     * @return true if the MapView is initialized.
     */
    public boolean getInitialized() {
        return mapViewReady.get() || initialized.get();
    }

    /**
     * @return the current zoom value.
     */
    public double getZoom() {
        return zoom.get();
    }

    /**
     * sets the zoom level. the zoom value is rounded to the next whole number using {@link Math#round(double)} and then
     * checked to be in the range between {@link #MIN_ZOOM} and {@link #MAX_ZOOM }. If the value is not in this range,
     * the call is ignored.
     *
     * @param zoom
     *     new zoom level
     * @return this object
     */
    public MapView setZoom(final double zoom) {
        final double rounded = Math.round(zoom);
        if (rounded < MIN_ZOOM || rounded > MAX_ZOOM) {
            return this;
        }
        this.zoom.set(rounded);
        return this;
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
     *     the new MapType
     * @return this object
     */
    public MapView setMapType(final MapType mapType) {
        this.mapType.set(mapType);
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
     *     the CoordinateLine to add
     * @return this object
     * @throws java.lang.NullPointerException
     *     if argument is null
     */
    public MapView addCoordinateLine(final CoordinateLine coordinateLine) {
        if (!getInitialized()) {
            if (logger.isWarnEnabled()) {
                logger.warn(MAP_VIEW_NOT_YET_INITIALIZED);
            }
        } else {
            // sync on the coordinatesLines map as the cleaner thread accesses this as well
            synchronized (coordinateLines) {
                final String id = requireNonNull(coordinateLine).getId();
                if (!coordinateLines.containsKey(id)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("adding coordinate line {}", coordinateLine);
                    }
                    final JSObject jsCoordinateLine = (JSObject) jsMapView.call("getCoordinateLine", id);
                    coordinateLine.getCoordinateStream().forEach(
                        (coord) -> jsCoordinateLine
                            .call("addCoordinate", coord.getLatitude(), coord.getLongitude()));
                    final javafx.scene.paint.Color color = coordinateLine.getColor();
                    jsCoordinateLine.call("setColor",
                        color.getRed() * 255, color.getGreen() * 255, color.getBlue() * 255,
                        color.getOpacity());
                    final javafx.scene.paint.Color fillColor = coordinateLine.getFillColor();
                    jsCoordinateLine.call("setFillColor",
                        fillColor.getRed() * 255, fillColor.getGreen() * 255, fillColor.getBlue() * 255,
                        fillColor.getOpacity());
                    jsCoordinateLine.call("setWidth", coordinateLine.getWidth());
                    jsCoordinateLine.call("setClosed", coordinateLine.isClosed());
                    jsCoordinateLine.call("seal");

                    final ChangeListener<Boolean> changeListener =
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
     *     the id of the CoordinateLine object
     */
    private void setCoordinateLineVisibleInMap(final String coordinateLineId) {
        if (null != coordinateLineId) {
            final WeakReference<CoordinateLine> coordinateLineWeakReference = coordinateLines.get(coordinateLineId);
            if (null != coordinateLineWeakReference) {
                final CoordinateLine coordinateLine = coordinateLineWeakReference.get();
                if (null != coordinateLine) {
                    if (coordinateLine.getVisible()) {
                        jsMapView.call("showCoordinateLine", coordinateLineId);
                    } else {
                        jsMapView.call("hideCoordinateLine", coordinateLineId);
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
     *     the label
     * @return this object
     * @throws java.lang.NullPointerException
     *     if marker is null
     */
    public MapView addLabel(final MapLabel mapLabel) {
        if (!getInitialized()) {
            if (logger.isWarnEnabled()) {
                logger.warn(MAP_VIEW_NOT_YET_INITIALIZED);
            }
        } else {
            if (null == requireNonNull(mapLabel).getPosition()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("label with no position was not added: {}", mapLabel);
                }
                return this;
            }
            final String id = mapLabel.getId();
            // synchronize on the mapCoordinateElements map as the cleaning thread accesses this as well
            synchronized (mapCoordinateElements) {
                // if the label is attached to a Marker, only add it when the marker is already added to the MapView
                if (mapLabel.getMarker().isPresent() && !mapCoordinateElements.containsKey(mapLabel.getMarker().get()
                    .getId())) {
                    return this;
                }
                if (!mapCoordinateElements.containsKey(id)) {
                    addMapCoordinateElement(mapLabel);
                    jsMapView.call("addLabel", id, mapLabel.getText(), mapLabel.getCssClass(),
                        mapLabel.getPosition().getLatitude(), mapLabel.getPosition().getLongitude(),
                        mapLabel.getOffsetX(), mapLabel.getOffsetY());
                    if (logger.isTraceEnabled()) {
                        logger.trace("add label in OpenLayers map {}", mapLabel);
                    }
                    setMarkerVisibleInMap(id);
                    setMapCoordinateElementRotation(id, mapLabel.getRotation());
                }
            }
        }
        return this;
    }

    /**
     * sets up the internal information about a MapCoordinate Element.
     *
     * @param mapCoordinateElement
     *     the MapCooordinate Element
     */
    private void addMapCoordinateElement(final MapCoordinateElement mapCoordinateElement) {
        final String id = mapCoordinateElement.getId();
        // create change listeners for the coordinate and the visibility and store them with the
        // marker's id.
        final ChangeListener<Coordinate> coordinateChangeListener =
            (observable, oldValue, newValue) -> moveMapCoordinateElementInMap(id);
        final ChangeListener<Boolean> visibileChangeListener =
            (observable, oldValue, newValue) -> setMarkerVisibleInMap(id);
        final ChangeListener<String> cssChangeListener = (observable, oldValue, newValue) -> setMapCoordinateElementCss(id,
            newValue);
        final ChangeListener<Number> rotationChangeListener = (observable, oldvalue, newValue) -> setMapCoordinateElementRotation(id, newValue);

        mapCoordinateElementListeners.put(id, new MapCoordinateElementListener(
            coordinateChangeListener,
            visibileChangeListener,
            cssChangeListener,
            rotationChangeListener));

        // observe the mapCoordinateElements position, visibility and cssClass with the listeners
        mapCoordinateElement.positionProperty().addListener(coordinateChangeListener);
        mapCoordinateElement.visibleProperty().addListener(visibileChangeListener);
        mapCoordinateElement.cssClassProperty().addListener(cssChangeListener);
        mapCoordinateElement.rotationProperty().addListener(rotationChangeListener);

        // keep a weak ref of the mapCoordinateELement
        mapCoordinateElements.put(id, new WeakReference<>(mapCoordinateElement, weakReferenceQueue));
    }

    /**
     * sets the css class for a MapCoordinateElement. Currently supported only for MapLabels.
     *
     * @param id
     *     the id of the element
     * @param cssclass
     *     the css class
     */
    private void setMapCoordinateElementCss(final String id, final String cssclass) {
        jsMapView.call("setLabelCss", id, cssclass);
    }


    /**
     * sets the rotation angle in degrees for a MapCoordinateElement.
     *
     * @param id
     *     the id of the element
     * @param rotation
     *     the rotation angle
     */
    private void setMapCoordinateElementRotation(final String id, final Number rotation) {
        jsMapView.call("rotateMapObject", id, rotation.intValue() % 360);
    }

    /**
     * sets the visibility of a MapCoordinateElement in the map.
     *
     * @param id
     *     the marker to show or hide
     */
    private void setMarkerVisibleInMap(final String id) {
        if (null != id) {
            final WeakReference<MapCoordinateElement> weakReference = mapCoordinateElements.get(id);
            if (null != weakReference) {
                final MapCoordinateElement mapCoordinateElement = weakReference.get();
                if (null != mapCoordinateElement) {
                    if (mapCoordinateElement.getVisible()) {
                        jsMapView.call("showMapObject", mapCoordinateElement.getId());
                    } else {
                        jsMapView.call("hideMapObject", mapCoordinateElement.getId());
                    }
                }
            }
        }
    }

    /**
     * adjusts the mapCoordinateElement's position in the map.
     *
     * @param id
     *     the id of the element to move
     */
    private void moveMapCoordinateElementInMap(final String id) {
        if (getInitialized() && null != id) {
            final WeakReference<MapCoordinateElement> weakReference = mapCoordinateElements.get(id);
            if (null != weakReference) {
                final MapCoordinateElement mapCoordinateElement = weakReference.get();
                if (null != mapCoordinateElement) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("move element in OpenLayers map to {}", mapCoordinateElement);
                    }
                    jsMapView.call("moveMapObject", mapCoordinateElement.getId(),
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
     *     the marker
     * @return this object
     * @throws java.lang.NullPointerException
     *     if marker is null
     */
    public MapView addMarker(final Marker marker) {
        if (!getInitialized()) {
            if (logger.isWarnEnabled()) {
                logger.warn(MAP_VIEW_NOT_YET_INITIALIZED);
            }
        } else {
            if (null == requireNonNull(marker).getPosition()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("marker with no position was not added: {}", marker);
                }
                return this;
            }
            final String id = marker.getId();
            // synchronize on the mapCoordinateElements map as the cleaning thread accesses this as well
            synchronized (mapCoordinateElements) {
                if (!mapCoordinateElements.containsKey(id)) {
                    addMapCoordinateElement(marker);
                    jsMapView.call("addMarker", id, marker.getImageURL().toExternalForm(),
                        marker.getPosition().getLatitude(), marker.getPosition().getLongitude(),
                        marker.getOffsetX(), marker.getOffsetY());

                    if (logger.isTraceEnabled()) {
                        logger.trace("add marker in OpenLayers map {}", marker);
                    }
                    setMarkerVisibleInMap(id);
                    setMapCoordinateElementRotation(id, marker.getRotation());
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
     *     where to load the image from, may not be null
     * @return the encoded image as data url
     */
    @SuppressWarnings("UnusedDeclaration")
    private String createDataURI(final URL imageURL) {
        return imgCache.computeIfAbsent(imageURL, url -> {
            String dataUrl = null;
            try (final InputStream isGuess = url.openStream();
                 final InputStream isConvert = url.openStream();
                 final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                final String contentType = URLConnection.guessContentTypeFromStream(isGuess);
                if (null != contentType) {
                    final byte[] chunk = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = isConvert.read(chunk)) > 0) {
                        os.write(chunk, 0, bytesRead);
                    }
                    os.flush();
                    dataUrl = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(os
                        .toByteArray());
                } else {
                    if (logger.isWarnEnabled()) {
                        logger.warn("could not get content type from {}", imageURL.toExternalForm());
                    }
                }
            } catch (final IOException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("error loading image", e);
                }
            }
            if (null == dataUrl) {
                if (logger.isWarnEnabled()) {
                    logger.warn("could not create data url from {}", imageURL.toExternalForm());
                }
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
     *     animation duration in ms
     * @return this object
     */
    public MapView setAnimationDuration(int animationDuration) {
        this.animationDuration.set(animationDuration);
        return this;
    }

    /**
     * calls {@link #initialize(Configuration)} with the default values.
     */
    public void initialize() {
        initialize(Configuration.builder().build());
    }

    /**
     * initializes the MapView. The internal HTML file is loaded into the contained WebView and the necessary setup is
     * made for communication between this object and the Javascript elements on the web page.
     *
     * @param configuration
     *     the initial configuration for the mapView object.
     */
    public void initialize(final Configuration configuration) {
        if (logger.isDebugEnabled()) {
            logger.debug("initializing...");
            logger.debug(configuration.toString());
        }

        // we could load the html via the URL, but then we run into problems loading local images or track files when
        // the mapView is embededded in a jar and loaded via jar: URI. If we load the page with loadContent, these
        // restrictions do not apply.
        loadMapViewHtml().ifPresent((html) -> {
            // instantiate the WebView, resize it with this region by letting it observe the changes and add it as child
            final WebView webView = new WebView();
            if (logger.isTraceEnabled()) {
                logger.trace("WebView created");
            }
            webEngine = webView.getEngine();
            webView.prefWidthProperty().bind(widthProperty());
            webView.prefHeightProperty().bind(heightProperty());
            getChildren().add(webView);
            // log versions after webEngine is available
            logVersions();

            // pass JS alerts to the logger
            webView.getEngine().setOnAlert(event -> {
                if (logger.isWarnEnabled()) {
                    logger.warn("JS alert: {}", event.getData());
                }
            });

            // watch for load changes
            webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                    if (logger.isTraceEnabled()) {
                        logger.trace("WebEngine loader state {} -> {}", oldValue, newValue);
                    }
                    if (Worker.State.SUCCEEDED == newValue) {
                        // set an interface object named 'javaConnector' in the web engine
                        final JSObject window = (JSObject) webEngine.executeScript("window");
                        window.setMember("_javaConnector", javaConnector);

                        // add JS console.log() redirector
                        webEngine.executeScript("console.log = function(msg) { _javaConnector.console(msg) }");

                        // get the Javascript connector object. Even if the html file is loaded, JS may not yet
                        // be ready, so prepare for an exception and retry
                        int numRetries = 0;
                        do {
                            final Object o;
                            try {
                                final String script = "createJSMapView('" + configuration.toJson() + "')";
                                if (logger.isDebugEnabled()) {
                                    logger.debug("calling JS \"" + script + '"');
                                }
                                o = webEngine.executeScript(script);
                                jsMapView = (JSObject) o;
                            } catch (final JSException e) {
                                if (logger.isWarnEnabled()) {
                                    logger.warn("JS not ready, retrying...");
                                }
                                numRetries++;
                                try {
                                    Thread.sleep(500);
                                } catch (final InterruptedException e1) {
                                    if (logger.isWarnEnabled()) {
                                        logger.warn("retry interrupted");
                                    }
                                }
                            } catch (final Exception e) {
                                if (logger.isWarnEnabled()) {
                                    logger.warn("getJSMapView: returned (null)");
                                }
                                numRetries++;
                            }
                        } while (null == jsMapView && numRetries < NUM_RETRIES_FOR_JS);

                        if (null == jsMapView) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("error loading {}, JavaScript not ready.", MAPVIEW_HTML);
                            }
                        } else {
                            mapViewReady.set(true);
                            setMapTypeInMap();
                            setCenterInMap();
                            setZoomInMap();
                            initialized.set(true);
                            if (logger.isDebugEnabled()) {
                                logger.debug("initialized.");
                            }
                        }
                    } else if (Worker.State.FAILED == newValue) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("error loading {}", MAPVIEW_HTML);
                        }
                    }
                }
            );
            // do the load
            if (logger.isDebugEnabled()) {
                logger.debug("load html into WebEngine");
            }
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
        final URL mapviewURL = getClass().getResource(MAPVIEW_HTML);
        if (null == mapviewURL) {
            if (logger.isWarnEnabled()) {
                logger.warn("resource not found: {}", MAPVIEW_HTML);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("loading from {}", mapviewURL.toExternalForm());
            }
            try (
                final Stream<String> lines = new BufferedReader(
                    new InputStreamReader(mapviewURL.openStream(), StandardCharsets.UTF_8)).lines()
            ) {
                final String baseURL = mapviewURL.toExternalForm();
                final String baseURLPath = baseURL.substring(0, baseURL.lastIndexOf('/') + 1);
                mapViewHtml = lines
                    .map(String::trim)
                    .map(line -> processHtmlLine(baseURLPath, line))
                    .flatMap(List::stream)
                    .collect(Collectors.joining("\n"));
//                logger.finer(mapViewHtml);
            } catch (final IOException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("loading {}", mapviewURL.toExternalForm(), e);
                }
            }
        }
        return Optional.ofNullable(mapViewHtml);
    }

    /**
     * log Java, JavaFX , OS and WebKit version.
     */
    private void logVersions() {
        if (logger.isDebugEnabled()) {
            logger.debug("Java Version:   {}", System.getProperty("java.runtime.version"));
            logger.debug("JavaFX Version: {}", System.getProperty("javafx.runtime.version"));
            logger.debug("OS:             {}, {}, {}", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
            logger.debug("User Agent:     {}", webEngine.getUserAgent());
        }
    }

    /**
     * processes a line from the html file, adding the base url and replacing template values.
     *
     * @param baseURL
     *     the URL of the file
     * @param line
     *     the line to process, must be trimmed
     * @return a List with the processed strings
     */
    private List<String> processHtmlLine(final String baseURL, final String line) {
        // insert base url
        if ("<head>".equalsIgnoreCase(line)) {
            return Arrays.asList(line, "<base href=\"" + baseURL + "\">");
        }

        // check for replacement pattern
        final Matcher matcher = htmlIncludePattern.matcher(line);
        if (matcher.matches()) {
            final String resource = baseURL + matcher.group(1);
            if (CUSTOM_MAPVIEW_CSS.equals(matcher.group(1))) {
                if (customMapviewCssURL.isPresent()) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("loading custom mapview css from {}", customMapviewCssURL.get().toExternalForm());
                    }
                    try (final Stream<String> lines = new BufferedReader(
                        new InputStreamReader(customMapviewCssURL.get().openStream(), StandardCharsets.UTF_8))
                        .lines()
                    ) {
                        return lines
                            .filter(l -> !l.contains("<"))
                            .collect(Collectors.toList());
                    } catch (final IOException e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("loading resource {}", resource, e);
                        }
                    }
                }
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("loading from {}", resource);
                }
                try (final Stream<String> lines = new BufferedReader(
                    new InputStreamReader(new URL(resource).openStream(), StandardCharsets.UTF_8))
                    .lines()
                ) {
                    return lines.collect(Collectors.toList());
                } catch (final IOException e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("loading resource {}", resource, e);
                    }
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
     *     the CoordinateLine to add
     * @return this object
     * @throws java.lang.NullPointerException
     *     if argument is null
     */
    public MapView removeCoordinateLine(final CoordinateLine coordinateLine) {
        if (!getInitialized()) {
            if (logger.isWarnEnabled()) {
                logger.warn(MAP_VIEW_NOT_YET_INITIALIZED);
            }
        } else {
            removeCoordinateLineWithId(requireNonNull(coordinateLine).getId());
        }
        return this;
    }

    /**
     * removes the CoordinateLinewith the given id. if no such element is found, nothing happens.
     *
     * @param id
     *     id of the coordinate line, may not be null
     */
    private void removeCoordinateLineWithId(final String id) {
        // sync on the map as the cleaner thread accesses this as well
        synchronized (coordinateLines) {
            if (coordinateLines.containsKey(id)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("removing coordinate line {}", id);
                }

                jsMapView.call("hideCoordinateLine", id);
                jsMapView.call("removeCoordinateLine", id);

                if (logger.isTraceEnabled()) {
                    logger.trace("removing coordinate line {}, after JS calls", id);
                }

                // if the coordinateLine was not gc'ed we need to unregister the listeners
                final CoordinateLine coordinateLine = coordinateLines.get(id).get();
                final CoordinateLineListener coordinateLineListener = coordinateLineListeners.get(id);
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
     *     label to remove
     * @return this object
     * @throws java.lang.NullPointerException
     *     if mapLabel is null
     */
    public MapView removeLabel(final MapLabel mapLabel) {
        if (!getInitialized()) {
            if (logger.isWarnEnabled()) {
                logger.warn(MAP_VIEW_NOT_YET_INITIALIZED);
            }
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
     *     the element to remove
     */
    private void removeMapCoordinateElement(final MapCoordinateElement mapCoordinateElement) {
        removeMapCoordinateElementWithId(requireNonNull(mapCoordinateElement).getId());
    }

    /**
     * remove a MapCoordinateElement with a given id from the map.  If no such element is found, nothing happens.
     *
     * @param id
     *     the id of the element to remove.
     */
    private void removeMapCoordinateElementWithId(final String id) {
        // sync on the map as the cleaner thread accesses this as well
        synchronized (mapCoordinateElements) {
            if (mapCoordinateElements.containsKey(id)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("removing element {}", id);
                }

                jsMapView.call("hideMapObject", id);
                jsMapView.call("removeMapObject", id);

                // if the element was not gc'ed we need to unregister the listeners so we dont' react to events from
                // removed elements
                final MapCoordinateElement element = mapCoordinateElements.get(id).get();
                final MapCoordinateElementListener mapCoordinateElementListener = mapCoordinateElementListeners.get(id);
                if (null != element && null != mapCoordinateElementListener) {
                    element.positionProperty().removeListener(mapCoordinateElementListener.getCoordinateChangeListener());
                    element.visibleProperty().removeListener(mapCoordinateElementListener.getVisibileChangeListener());
                    element.cssClassProperty().removeListener(mapCoordinateElementListener.getCssChangeListener());
                    element.rotationProperty().removeListener(mapCoordinateElementListener.getRotationChangeListener());
                }

                mapCoordinateElementListeners.remove(id);
                mapCoordinateElements.remove(id);
                if (logger.isDebugEnabled()) {
                    logger.debug("removed element {}", id);
                }
            }
        }
    }

    /**
     * removes the given marker from the map and deregisters the change listeners. If the marker was not in the map or
     * the MapView is not yet initialized, nothing happens.
     *
     * @param marker
     *     marker to remove
     * @return this object
     * @throws java.lang.NullPointerException
     *     if marker is null
     */
    public MapView removeMarker(final Marker marker) {
        if (!getInitialized()) {
            if (logger.isWarnEnabled()) {
                logger.warn(MAP_VIEW_NOT_YET_INITIALIZED);
            }
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
     *     api key
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
     * sets the WMS parameters.
     *
     * @param wmsParam
     *     WMS parameters
     * @return this object
     */
    public MapView setWMSParam(final WMSParam wmsParam) {
        this.wmsParam = Optional.ofNullable(wmsParam);
        return this;
    }

    /**
     * sets the XYZ parameters.
     *
     * @param xyzParam
     *     XYZ parameters
     * @return this object
     */
    public MapView setXYZParam(final XYZParam xyzParam) {
        this.xyzParam = Optional.ofNullable(xyzParam);
        return this;
    }

    /**
     * sets the URL for the custom mapview css file.
     *
     * @param url
     *     css url
     * @throws NullPointerException
     *     if url is null
     */
    public void setCustomMapviewCssURL(final URL url) {
        requireNonNull(url);
        customMapviewCssURL = Optional.of(url);
    }

    /**
     * sets the center and zoom of the map so that the given extent is visible.
     *
     * @param extent
     *     extent to show, if null, nothing is changed
     * @return this object
     * @throws java.lang.NullPointerException
     *     when extent is null
     */
    public MapView setExtent(final Extent extent) {
        if (!getInitialized()) {
            if (logger.isWarnEnabled()) {
                logger.warn(MAP_VIEW_NOT_YET_INITIALIZED);
            }
        } else {
            requireNonNull(extent);
            if (logger.isDebugEnabled()) {
                logger.debug("setting extent in OpenLayers map: {}, animation: ", extent, animationDuration.get());
            }
            jsMapView.call("setExtent", extent.getMin().getLatitude(), extent.getMin().getLongitude(),
                extent.getMax().getLatitude(), extent.getMax().getLongitude(), animationDuration.get());
        }
        return this;
    }

    /**
     * constrains the map to the given extent so that it is not possible to zoom out or pan if this would make some area outside of this extent visible.
     *
     * @param extent
     *     the constraining extent
     * @return this object
     * @throws java.lang.NullPointerException
     *     when extent is null
     */
    public MapView constrainExtent(final Extent extent) {
        if (!getInitialized()) {
            if (logger.isWarnEnabled()) {
                logger.warn(MAP_VIEW_NOT_YET_INITIALIZED);
            }
        } else {
            requireNonNull(extent);
            if (logger.isDebugEnabled()) {
                logger.debug("constraining extent in OpenLayers map: {}: ", extent);
            }
            jsMapView.call("constrainExtent", extent.getMin().getLatitude(), extent.getMin().getLongitude(),
                extent.getMax().getLatitude(), extent.getMax().getLongitude());
        }
        return this;
    }

    /**
     * clears a given constrainExtent of the map, allowing panning and zooming without restriction.
     *
     * @return this object
     */
    public MapView clearConstrainExtent() {
        if (!getInitialized()) {
            if (logger.isWarnEnabled()) {
                logger.warn(MAP_VIEW_NOT_YET_INITIALIZED);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("clearing constraining extent in OpenLayers map.");
            }
            jsMapView.call("clearConstrainExtent");
        }
        return this;
    }

    public SimpleDoubleProperty zoomProperty() {
        return zoom;
    }

    /**
     * Connector object. Methods of an object of this class are called from JS code in the web page.
     */
    public class JavaConnector {

        private final Logger logger = LoggerFactory.getLogger(JavaConnector.class);

        /**
         * called when the user has moved the map. the coordinates are EPSG:4326 (WGS) values. The arguments are double
         * primitives and no Double objects.
         *
         * @param lat
         *     new latitude value
         * @param lon
         *     new longitude value
         */
        public void centerMovedTo(double lat, double lon) {
            final Coordinate newCenter = new Coordinate(lat, lon);
            if (logger.isTraceEnabled()) {
                logger.trace("JS reports center value {}", newCenter);
            }
            lastCoordinateFromMap.set(newCenter);
            setCenter(newCenter);
        }

        /**
         * called when the user has moved the pointer (mouse).
         *
         * @param lat
         *     new latitude value
         * @param lon
         *     new longitude value
         */
        public void pointerMovedTo(double lat, double lon) {
            final Coordinate coordinate = new Coordinate(lat, lon);
            if (logger.isTraceEnabled()) {
                logger.trace("JS reports pointer move {}", coordinate);
            }
            fireEvent(new MapViewEvent(MapViewEvent.MAP_POINTER_MOVED, coordinate));
        }

        /**
         * called from the JS in the web page to output a message to the application's log.
         *
         * @param msg
         *     the message to log
         */
        public void debug(String msg) {
            if (logger.isDebugEnabled()) {
                logger.debug("JS: {}", msg);
            }
        }

        /**
         * called when something writes in the JS side to console.log()
         *
         * @param msg
         *     the message to log
         */
        public void console(String msg) {
            if (logger.isDebugEnabled()) {
                logger.debug("JS Console: {}", msg);
            }
        }

        /**
         * called when an a href in the map is clicked and shows the URL in the default browser.
         *
         * @param href
         *     the url to show
         */
        public void showLink(final String href) {
            if (null != href && !href.isEmpty()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("JS asks to browse to {}", href);
                }
                if (!Desktop.isDesktopSupported()) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("no desktop support for displaying {}", href);
                    }
                } else {
                    try {
                        Desktop.getDesktop().browse(new URI(href));
                    } catch (final IOException | URISyntaxException e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("can't display {}", href, e);
                        }
                    }
                }
            }
        }

        /**
         * called when the user has single-clicked in the map. the coordinates are EPSG:4326 (WGS) values.
         *
         * @param lat
         *     new latitude value
         * @param lon
         *     new longitude value
         */
        public void singleClickAt(double lat, double lon) {
            final Coordinate coordinate = new Coordinate(lat, lon);
            if (logger.isTraceEnabled()) {
                logger.trace("JS reports single click at {}", coordinate);
            }
            fireEvent(new MapViewEvent(MapViewEvent.MAP_CLICKED, coordinate));
        }

        /**
         * called when the user has context-clicked in the map. the coordinates are EPSG:4326 (WGS) values.
         *
         * @param lat
         *     new latitude value
         * @param lon
         *     new longitude value
         */
        public void contextClickAt(double lat, double lon) {
            final Coordinate coordinate = new Coordinate(lat, lon);
            if (logger.isTraceEnabled()) {
                logger.trace("JS reports context click at {}", coordinate);
            }
            fireEvent(new MapViewEvent(MapViewEvent.MAP_RIGHTCLICKED, coordinate));
        }

        /**
         * called when a marker was clicked.
         *
         * @param name
         *     name of the marker
         */
        public void markerClicked(final String name) {
            processMarkerClicked(name, ClickType.LEFT);
        }

        /**
         * calles when mouse is pressed on marker.
         *
         * @param name
         *     name of the marker
         */
        public void markerMouseDown(final String name) {
            processMarkerClicked(name, ClickType.MOUSEDOWN);
        }

        /**
         * calles when mouse is released on marker.
         *
         * @param name
         *     name of the marker
         */
        public void markerMouseUp(final String name) {
            processMarkerClicked(name, ClickType.MOUSEUP);
        }

        /**
         * called when a marker was doubleclicked.
         *
         * @param name
         *     name of the marker
         */
        public void markerDoubleClicked(final String name) {
            processMarkerClicked(name, ClickType.DOUBLE);
        }

        /**
         * called when a marker was doubleclicked.
         *
         * @param name
         *     name of the marker
         */
        public void markerRightClicked(final String name) {
            processMarkerClicked(name, ClickType.RIGHT);
        }

        /**
         * called when a marker was entered.
         *
         * @param name
         *     name of the marker
         */
        public void markerEntered(final String name) {
            processMarkerClicked(name, ClickType.ENTERED);
        }

        /**
         * called when a marker was exited.
         *
         * @param name
         *     name of the marker
         */
        public void markerExited(final String name) {
            processMarkerClicked(name, ClickType.EXITED);
        }

        /**
         * processes a marker click
         *
         * @param name
         *     name of the marker
         * @param clickType
         *     the type of click
         */
        private void processMarkerClicked(final String name, final ClickType clickType) {
            if (logger.isTraceEnabled()) {
                logger.trace("JS reports marker {} clicked {}", name, clickType);
            }
            synchronized (mapCoordinateElements) {
                if (mapCoordinateElements.containsKey(name)) {
                    final MapCoordinateElement mapCoordinateElement = mapCoordinateElements.get(name).get();
                    EventType<MarkerEvent> eventType = null;
                    switch (clickType) {
                        case LEFT:
                            eventType = MarkerEvent.MARKER_CLICKED;
                            break;
                        case DOUBLE:
                            eventType = MarkerEvent.MARKER_DOUBLECLICKED;
                            break;
                        case RIGHT:
                            eventType = MarkerEvent.MARKER_RIGHTCLICKED;
                            break;
                        case MOUSEDOWN:
                            eventType = MarkerEvent.MARKER_MOUSEDOWN;
                            break;
                        case MOUSEUP:
                            eventType = MarkerEvent.MARKER_MOUSEUP;
                            break;
                        case ENTERED:
                            eventType = MarkerEvent.MARKER_ENTERED;
                            break;
                        case EXITED:
                            eventType = MarkerEvent.MARKER_EXITED;
                            break;
                    }
                    fireEvent(new MarkerEvent(eventType, (Marker) mapCoordinateElement));
                }
            }
        }

        /**
         * called when a label was single clicked.
         *
         * @param name
         *     name of the lael
         */
        public void labelClicked(final String name) {
            processLabelClicked(name, ClickType.LEFT);
        }

        /**
         * called when mouse is pressed on label.
         *
         * @param name
         *     name of the label
         */
        public void labelMouseDown(final String name) {
            processLabelClicked(name, ClickType.MOUSEDOWN);
        }

        /**
         * called when mouse is released on label.
         *
         * @param name
         *     name of the label
         */
        public void labelMouseUp(final String name) {
            processLabelClicked(name, ClickType.MOUSEUP);
        }

        /**
         * called when a label was double clicked.
         *
         * @param name
         *     name of the lael
         */
        public void labelDoubleClicked(final String name) {
            processLabelClicked(name, ClickType.DOUBLE);
        }

        /**
         * called when a label was single clicked.
         *
         * @param name
         *     name of the lael
         */
        public void labelRightClicked(final String name) {
            processLabelClicked(name, ClickType.RIGHT);
        }

        /**
         * called when a label was entered.
         *
         * @param name
         *     name of the lael
         */
        public void labelEntered(final String name) {
            processLabelClicked(name, ClickType.ENTERED);
        }

        /**
         * called when a label was exited.
         *
         * @param name
         *     name of the lael
         */
        public void labelExited(final String name) {
            processLabelClicked(name, ClickType.EXITED);
        }

        /**
         * called when a label was clicked.
         *
         * @param name
         *     name of the lael
         * @param clickType
         *     the type of click
         */
        private void processLabelClicked(final String name, final ClickType clickType) {
            if (logger.isTraceEnabled()) {
                logger.trace("JS reports label {} clicked {}", name, clickType);
            }
            synchronized (mapCoordinateElements) {
                if (mapCoordinateElements.containsKey(name)) {
                    final MapCoordinateElement mapCoordinateElement = mapCoordinateElements.get(name).get();
                    if (mapCoordinateElement instanceof MapLabel) {
                        EventType<MapLabelEvent> eventType = null;
                        switch (clickType) {
                            case LEFT:
                                eventType = MapLabelEvent.MAPLABEL_CLICKED;
                                break;
                            case DOUBLE:
                                eventType = MapLabelEvent.MAPLABEL_DOUBLECLICKED;
                                break;
                            case RIGHT:
                                eventType = MapLabelEvent.MAPLABEL_RIGHTCLICKED;
                                break;
                            case MOUSEDOWN:
                                eventType = MapLabelEvent.MAPLABEL_MOUSEDOWN;
                                break;
                            case MOUSEUP:
                                eventType = MapLabelEvent.MAPLABEL_MOUSEUP;
                                break;
                            case ENTERED:
                                eventType = MapLabelEvent.MAPLABEL_ENTERED;
                                break;
                            case EXITED:
                                eventType = MapLabelEvent.MAPLABEL_EXITED;
                                break;
                        }
                        fireEvent(new MapLabelEvent(eventType, (MapLabel) mapCoordinateElement));
                    }
                }
            }
        }

        /**
         * called when the user changed the zoom with the controls in the map.
         *
         * @param newZoom
         *     new zoom value
         */
        public void zoomChanged(double newZoom) {
            final long roundedZoom = Math.round(newZoom);
            if (logger.isTraceEnabled()) {
                logger.trace("JS reports zoom value {}", roundedZoom);
            }
            lastZoomFromMap.set(roundedZoom);
            setZoom(roundedZoom);
        }

        /**
         * called when the user selected an extent by dragging the mouse with modifier pressed.
         *
         * @param latMin
         *     latitude of upper left corner
         * @param lonMin
         *     longitude of upper left corner
         * @param latMax
         *     latitude of lower right corner
         * @param lonMax
         *     longitude of lower right corner
         */
        public void extentSelected(double latMin, double lonMin, double latMax, double lonMax) {
            final Extent extent = Extent.forCoordinates(new Coordinate(latMin, lonMin), new Coordinate(latMax, lonMax));
            if (logger.isTraceEnabled()) {
                logger.trace("JS reports extend selected: {}", extent);
            }
            fireEvent(new MapViewEvent(MapViewEvent.MAP_EXTENT, extent));
        }

        /**
         * called when the map extent changed by changing the center or zoom of the map.
         *
         * @param latMin
         *     latitude of upper left corner
         * @param lonMin
         *     longitude of upper left corner
         * @param latMax
         *     latitude of lower right corner
         * @param lonMax
         *     longitude of lower right corner
         */
        public void extentChanged(double latMin, double lonMin, double latMax, double lonMax) {
            final Extent extent = Extent.forCoordinates(new Coordinate(latMin, lonMin), new Coordinate(latMax, lonMax));
            if (logger.isTraceEnabled()) {
                logger.trace("JS reports extend change: {}", extent);
            }
            fireEvent(new MapViewEvent(MapViewEvent.MAP_BOUNDING_EXTENT, extent));
        }

        /**
         * called when a wheel event is detected on a marker or a label
         *
         * @param deltaY
         *     the reported wheel delta.
         */
        public void wheelEvent(double deltaY) {
            if (logger.isTraceEnabled()) {
                logger.trace("JS reports wheel event: {}", deltaY);
            }
            setZoom(getZoom() - Math.signum(deltaY));
        }
    }

    //==================================================================================================================
    // Map circle
    //==================================================================================================================
    public MapView addMapCircle(final MapCircle mapCircle) {
        if (!getInitialized()) {
            if (logger.isWarnEnabled()) {
                logger.warn(MAP_VIEW_NOT_YET_INITIALIZED);
            }
        } else {
            // sync on the mapCircle map as the cleaner thread accesses this as well
            synchronized (this.mapCircles) {
                final String id = requireNonNull(mapCircle).getId();

                if (!this.mapCircles.containsKey(id)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("adding circle {}", mapCircle);
                    }
                    final JSObject jsCircle = (JSObject) jsMapView.call("getCircle", id);

                    logger.debug("  - setCenter: (" + mapCircle.getCenter().getLatitude() + ", " + mapCircle.getCenter().getLongitude() + ")");
                    jsCircle.call("setCenter", mapCircle.getCenter().getLatitude(), mapCircle.getCenter().getLongitude());

                    logger.debug("  - setRadius: " + mapCircle.getRadius());
                    jsCircle.call("setRadius", mapCircle.getRadius());

                    final javafx.scene.paint.Color color = mapCircle.getColor();
                    jsCircle.call("setColor",
                        color.getRed() * 255, color.getGreen() * 255, color.getBlue() * 255,
                        color.getOpacity());
                    final javafx.scene.paint.Color fillColor = mapCircle.getFillColor();
                    jsCircle.call("setFillColor",
                        fillColor.getRed() * 255, fillColor.getGreen() * 255, fillColor.getBlue() * 255,
                        fillColor.getOpacity());
                    jsCircle.call("setWidth", mapCircle.getWidth());
                    jsCircle.call("seal");

                    // store a weak reference to be able to remove the line from the map if the caller forgets to do so
                    mapCircles.put(id, new WeakReference<>(mapCircle, weakReferenceQueue));

                    setMapCircleVisibleInMap(id);
                }
            }
        }
        return this;
    }

    /**
     * shows or hides the mapCircle in the map according to it's visible property.
     */
    private void setMapCircleVisibleInMap(final String circleId) {
        if (null != circleId) {
            final WeakReference<MapCircle> mapCircleWeakReference = this.mapCircles.get(circleId);
            if (null != mapCircleWeakReference) {
                final MapCircle mapCircle = mapCircleWeakReference.get();
                if (null != mapCircle) {
                    if (mapCircle.getVisible()) {
                        jsMapView.call("showCircle", circleId);
                    } else {
                        jsMapView.call("hideCircle", circleId);
                    }
                }
            }
        }
    }

    public MapView removeMapCircle(final MapCircle mapCircle) {
        if (!getInitialized()) {
            if (logger.isWarnEnabled()) {
                logger.warn(MAP_VIEW_NOT_YET_INITIALIZED);
            }
        } else {
            removeMapCircleWithId(requireNonNull(mapCircle).getId());
        }
        return this;
    }

    /**
     * removes the MapCircle with the given id. if no such element is found, nothing happens.
     *
     * @param id
     *     id of the map circle, may not be null
     */
    private void removeMapCircleWithId(final String id) {
        // sync on the map as the cleaner thread accesses this as well
        synchronized (mapCircles) {
            if (mapCircles.containsKey(id)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("removing circle {}", id);
                }

                jsMapView.call("hideCircle", id);
                jsMapView.call("removeCircle", id);

                if (logger.isTraceEnabled()) {
                    logger.trace("removing circle {}, after JS calls", id);
                }

                mapCircles.remove(id);
            }
        }
    }
}
