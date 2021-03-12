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
package com.sothawo.mapjfx.app;

import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MapLabelEvent;
import com.sothawo.mapjfx.event.MapViewEvent;
import com.sothawo.mapjfx.event.MarkerEvent;
import com.sothawo.mapjfx.offline.OfflineCache;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Test application.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class TestApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(TestApp.class);

    /** some coordinates from around town */
    private static final Coordinate coordKarlsruheCastle = new Coordinate(49.013517, 8.404435);
    private static final Coordinate coordKarlsruheHarbour = new Coordinate(49.015511, 8.323497);
    private static final Coordinate coordKarlsruheStation = new Coordinate(48.993284, 8.402186);
    private static final Extent extentAll =
        Extent.forCoordinates(coordKarlsruheHarbour, coordKarlsruheCastle, coordKarlsruheStation);

    private static final CoordinateLine coordinateLine =
        new CoordinateLine(coordKarlsruheCastle, coordKarlsruheHarbour, coordKarlsruheStation)
            .setVisible(true)
            .setColor(Color.DODGERBLUE)
            .setWidth(7)
            .setClosed(true)
            .setFillColor(Color.web("lawngreen", 0.5));

    private static final Marker marker;

    private static final MapCircle circle;

    private static final MapLabel mapLabel;

    private static final WMSParam wmsParam;

    private static final XYZParam xyzParam;

    static {
        marker = Marker.createProvided(Marker.Provided.BLUE)
            .setPosition(coordKarlsruheCastle)
            .setRotation(90)
            .setVisible(true);

        mapLabel = new MapLabel("blau!")
            .setCssClass("blue-label")
            .setPosition(coordKarlsruheCastle)
            .setRotation(90)
            .setVisible(true);

        marker.attachLabel(mapLabel);

        circle = new MapCircle(coordKarlsruheCastle, 1_000).setVisible(true);

//        wmsParam = new WMSParam()
//                .setUrl("http://irs.gis-lab.info/?")
//                .addParam("layers", "landsat")
//                .addParam("REQUEST", "GetTile");

//        wmsParam = new WMSParam()
//                .setUrl("http://geonode.wfp.org:80/geoserver/ows")
//                .addParam("layers", "geonode:admin_2_gaul_2015");

        wmsParam = new WMSParam()
            .setUrl("http://ows.terrestris.de/osm/service")
            .addParam("layers", "OSM-WMS");

        xyzParam = new XYZParam()
            .withUrl("https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x})")
            .withAttributions("'Tiles &copy; <a href=\"https://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer\">ArcGIS</a>'");
    }

    /** the MapView */
    private MapView mapView;

    /** api keys for bing maps. */
    private TextField bingApiKey;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        logger.info("starting devtest program...");
        final BorderPane borderPane = new BorderPane();

        // MapView in the center with an initial coordinate (optional)
        // the MapView is created first as the other elements reference it
        mapView = new MapView();
        // animate pan and zoom with 500ms
        mapView.setAnimationDuration(500);
        borderPane.setCenter(mapView);

        // at the top some buttons
        final Pane topPane = createTopPane();
        borderPane.setTop(topPane);

        // at the bottom some infos
        borderPane.setBottom(createBottomPane());

        // add WMSParam
        mapView.setWMSParam(wmsParam);

        //add XYZParam
        mapView.setXYZParam(xyzParam);

        // listen to MapViewEvent MAP_CLICKED
        mapView.addEventHandler(MapViewEvent.MAP_CLICKED, event -> {
            logger.info("MAP_CLICKED event at {}", event.getCoordinate().normalize());
            event.consume();
            if (marker.getVisible()) {
                marker.setPosition(event.getCoordinate());
            }
            if (mapLabel.getVisible()) {
                mapLabel.setPosition(event.getCoordinate());
            }
        });

        // listen to MapViewEvent MAP_RIGHTCLICKED
        mapView.addEventHandler(MapViewEvent.MAP_RIGHTCLICKED, event -> {
            logger.info("MAP_RIGHTCLICKED event at {}", event.getCoordinate());
            event.consume();
        });

        // listen to MapViewEvent MAP_EXTENT
        mapView.addEventHandler(MapViewEvent.MAP_EXTENT, event -> {
            logger.info("MAP_EXTENT event: {}", event.getExtent());
            mapView.setExtent(event.getExtent());
            event.consume();
        });

        // listen to MapViewEvent MAP_BOUNDING_EXTENT
        mapView.addEventHandler(MapViewEvent.MAP_BOUNDING_EXTENT, event -> {
            logger.info("MAP_BOUNDING_EXTENT event: {}", event.getExtent());
            event.consume();
        });

        // listen to MARKER_CLICKED event.
        mapView.addEventHandler(MarkerEvent.MARKER_CLICKED, event -> {
            Marker marker = event.getMarker();
            logger.info("MARKER_CLICKED event: {}", marker);
            event.consume();
            marker.setRotation(marker.getRotation() + 5);
        });

        // listen to MARKER_MOUSEDOWN event.
        mapView.addEventHandler(MarkerEvent.MARKER_MOUSEDOWN, event -> {
            logger.info("MARKER_MOUSEDOWN event: {}", event.getMarker());
            event.consume();
        });
        // listen to MARKER_MOUSEUP event.
        mapView.addEventHandler(MarkerEvent.MARKER_MOUSEUP, event -> {
            logger.info("MARKER_MOUSEUP event: {}", event.getMarker());
            event.consume();
        });
        // listen to MARKER_DOUBLECLICKED event.
        mapView.addEventHandler(MarkerEvent.MARKER_DOUBLECLICKED, event -> {
            logger.info("MARKER_DOUBLECLICKED event: {}", event.getMarker());
            event.consume();
        });
        // listen to MARKER_RIGHTCLICKED event.
        mapView.addEventHandler(MarkerEvent.MARKER_RIGHTCLICKED, event -> {
            logger.info("MARKER_RIGHTCLICKED event: {}", event.getMarker());
            event.consume();
        });
        // listen to MARKER_ENTERED event.
        mapView.addEventHandler(MarkerEvent.MARKER_ENTERED, event -> {
            logger.info("MARKER_ENTERED event: {}", event.getMarker());
            event.consume();
        });
        // listen to MARKER_EXITED event.
        mapView.addEventHandler(MarkerEvent.MARKER_EXITED, event -> {
            logger.info("MARKER_EXITED event: {}", event.getMarker());
            event.consume();
        });
        // listen to MAPLABEL_MOUSEDOWN event.
        mapView.addEventHandler(MapLabelEvent.MAPLABEL_MOUSEDOWN, event -> {
            logger.info("MAPLABEL_MOUSEDOWN event: {}", event.getMapLabel());
            event.consume();
        });
        // listen to MAPLABEL_MOUSEUP event.
        mapView.addEventHandler(MapLabelEvent.MAPLABEL_MOUSEUP, event -> {
            logger.info("MAPLABEL_MOUSEUP event: {}", event.getMapLabel());
            event.consume();
        });
        // listen to MAPLABEL_CLICKED event.
        mapView.addEventHandler(MapLabelEvent.MAPLABEL_CLICKED, event -> {
            logger.info("MAPLABEL_CLICKED event: {}", event.getMapLabel());
            event.consume();
        });
        // listen to MAPLABEL_RIGHTCLICKED event.
        mapView.addEventHandler(MapLabelEvent.MAPLABEL_RIGHTCLICKED, event -> {
            logger.info("MAPLABEL_RIGHTCLICKED event: {}", event.getMapLabel());
            event.consume();
        });
        // listen to MAPLABEL_DOUBLECLICKED event.
        mapView.addEventHandler(MapLabelEvent.MAPLABEL_DOUBLECLICKED, event -> {
            logger.info("MAPLABEL_DOUBLECLICKED event:{}", event.getMapLabel());
            event.consume();
        });
        // listen to MAPLABEL_ENTERED event.
        mapView.addEventHandler(MapLabelEvent.MAPLABEL_ENTERED, event -> {
            logger.info("MAPLABEL_ENTERED event: {}", event.getMapLabel());
            event.consume();
            event.getMapLabel().setCssClass("green-label");
        });
        // listen to MAPLABEL_EXITED event.
        mapView.addEventHandler(MapLabelEvent.MAPLABEL_EXITED, event -> {
            logger.info("MAPLABEL_EXITED event: {}", event.getMapLabel());
            event.consume();
            event.getMapLabel().setCssClass("blue-label");
        });
        // listen to MAP_POINTER_MOVED event
        mapView.addEventHandler(MapViewEvent.MAP_POINTER_MOVED, event -> {
            logger.info("MAP_POINTER_MOVED event: {}", event.getCoordinate());
            event.consume();
        });

        initOfflineCache();
        // add listener for mapView initialization state
        mapView.initializedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // a map is only displayed when an initial coordinate is set
                mapView.setCenter(coordKarlsruheHarbour);
                mapView.setExtent(extentAll);
//                mapView.setZoom(0);

                // add two markers without keeping a ref to them, they should disappear from the map when gc'ed
                mapView.addMarker(Marker.createProvided(Marker.Provided.GREEN).setPosition(coordKarlsruheHarbour)
                    .setVisible(true));
                mapView.addMarker(
                    Marker.createProvided(Marker.Provided.ORANGE).setPosition(coordKarlsruheStation).setVisible(
                        true));

                // add a coordinate line to be gc'ed
                mapView.addCoordinateLine(
                    new CoordinateLine(coordKarlsruheHarbour, coordKarlsruheStation, coordKarlsruheCastle)
                        .setVisible(true)
                        .setColor(Color.FUCHSIA).setWidth(5));

                // add a label to be gc'ed
                mapView.addLabel(new MapLabel("clean me up").setPosition(coordKarlsruheStation)
                    .setVisible(true));

                // add normal circle and a circle to be gc'ed
                mapView.addMapCircle(circle);
                mapView.addMapCircle(new MapCircle(coordKarlsruheHarbour, 100).setVisible(true));

                topPane.setDisable(false);
            }
        });

        // set custom css url
        mapView.setCustomMapviewCssURL(getClass().getResource("/custom_mapview.css"));

        // now initialize the mapView
        mapView.setMapType(MapType.XYZ);
//        mapView.initialize();
        mapView.initialize(Configuration.builder()
//            .showZoomControls(false)
            .build());

        // show the whole thing
        final Scene scene = new Scene(borderPane, 1200, 800);

        primaryStage.setTitle("sothawo mapjfx devtest program");
        primaryStage.setScene(scene);
        primaryStage.show();

        logger.debug("application started.");
    }

    @Override
    public void stop() throws Exception {
        mapView.close();
        super.stop();
    }

    private void initOfflineCache() {
        final OfflineCache offlineCache = OfflineCache.INSTANCE;
        offlineCache.setCacheDirectory(FileSystems.getDefault().getPath("tmpdata/cache"));
        offlineCache.setActive(true);
        offlineCache.setNoCacheFilters(Collections.singletonList(".*\\.sothawo\\.com/.*"));

        LinkedList<String> urls = new LinkedList<>();
        urls.add("https://c.tile.openstreetmap.org/14/8572/5626.png");
        urls.add("https://b.tile.openstreetmap.org/14/8571/5626.png");
        urls.add("https://a.tile.openstreetmap.org/14/8572/5625.png");
        urls.add("https://c.tile.openstreetmap.org/14/8571/5625.png");
        urls.add("https://b.tile.openstreetmap.org/14/8570/5625.png");
        urls.add("https://a.tile.openstreetmap.org/14/8572/5625.png");
        urls.add("https://a.tile.openstreetmap.org/14/8570/5626.png");
        urls.add("https://a.tile.openstreetmap.org/14/8571/5627.png");
        urls.add("https://a.tile.openstreetmap.org/14/8573/5626.png");
        urls.add("https://a.tile.openstreetmap.org/14/8574/5627.png");
        urls.add("https://b.tile.openstreetmap.org/14/8571/5626.png");
        urls.add("https://b.tile.openstreetmap.org/14/8573/5625.png");
        urls.add("https://b.tile.openstreetmap.org/14/8572/5627.png");
        urls.add("https://b.tile.openstreetmap.org/14/8574/5626.png");
        urls.add("https://c.tile.openstreetmap.org/14/8572/5626.png");
        urls.add("https://c.tile.openstreetmap.org/14/8570/5627.png");
        urls.add("https://c.tile.openstreetmap.org/14/8574/5625.png");
        urls.add("https://c.tile.openstreetmap.org/14/8573/5627.png");

        offlineCache.preloadURLs(urls, 2);
    }

    /**
     * creates the bottom pane with status labels.
     *
     * @return Pane
     */
    private Pane createBottomPane() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(5, 5, 5, 5));
        hbox.setSpacing(10);

        // label for showing the map's center
        Label labelCenter = new Label();
        hbox.getChildren().add(labelCenter);
        // add an observer for the map's center property to adjust the corresponding label
        mapView.centerProperty().addListener((observable, oldValue, newValue) -> {
            labelCenter.setText(newValue == null ? "" : ("center: " + newValue.toString()));
        });

        // label for showing the map's zoom
        Label labelZoom = new Label();
        hbox.getChildren().add(labelZoom);
        // add an observer to adjust the label
        mapView.zoomProperty().addListener((observable, oldValue, newValue) -> {
            labelZoom.setText(null == newValue ? "" : ("zoom: " + newValue.toString()));
        });
        return hbox;
    }

    /**
     * creates the top pane with the different location buttons.
     *
     * @return Pane
     */
    private Pane createTopPane() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(5, 5, 5, 5));

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(5, 5, 5, 5));
        hbox.setSpacing(5);
        vbox.getChildren().add(hbox);

        Button btn = new Button();
        btn.setText("Karlsruhe castle");
        btn.setOnAction(event -> mapView.setCenter(coordKarlsruheCastle));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("Karlsruhe harbour");
        btn.setOnAction(event -> mapView.setCenter(coordKarlsruheHarbour));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("Karlsruhe station");
        btn.setOnAction(event -> mapView.setCenter(coordKarlsruheStation));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("all");
        btn.setOnAction(event -> mapView.setExtent(extentAll));
        hbox.getChildren().add(btn);

        Slider slider = new Slider(MapView.MIN_ZOOM, MapView.MAX_ZOOM, MapView.INITIAL_ZOOM);
        slider.setBlockIncrement(1);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setSnapToTicks(true);
        slider.setMajorTickUnit(MapView.MAX_ZOOM / 4);
        slider.setMinorTickCount((MapView.MAX_ZOOM / 4) - 1);
        slider.valueProperty().bindBidirectional(mapView.zoomProperty());
        slider.setSnapToTicks(true);
        HBox.setHgrow(slider, Priority.ALWAYS);
        hbox.getChildren().add(slider);

        hbox = new HBox();
        hbox.setPadding(new Insets(5, 5, 5, 5));
        hbox.setSpacing(5);
        vbox.getChildren().add(hbox);

        btn = new Button();
        btn.setText("OSM");
        btn.setOnAction(evt -> mapView.setMapType(MapType.OSM));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("ST");
        btn.setOnAction(evt -> mapView.setMapType(MapType.STAMEN_WC));
        hbox.getChildren().add(btn);

        LinkedList<MenuItem> bingOptions = new LinkedList<>();
        MenuItem item = new MenuItem("Bing Roads");
        item.setOnAction(evt -> {
            mapView.setBingMapsApiKey(bingApiKey.getText());
            mapView.setMapType(MapType.BINGMAPS_ROAD);
        });
        bingOptions.add(item);

        item = new MenuItem("Bing Aerial");
        item.setOnAction(evt -> {
            mapView.setBingMapsApiKey(bingApiKey.getText());
            mapView.setMapType(MapType.BINGMAPS_AERIAL);
        });
        bingOptions.add(item);

        item = new MenuItem("Bing Aerial with Labels");
        item.setOnAction(evt -> {
            mapView.setBingMapsApiKey(bingApiKey.getText());
            mapView.setMapType(MapType.BINGMAPS_AERIAL_WITH_LABELS);
        });
        bingOptions.add(item);

        item = new MenuItem("Bing Roads - dark");
        item.setOnAction(evt -> {
            mapView.setBingMapsApiKey(bingApiKey.getText());
            mapView.setMapType(MapType.BINGMAPS_CANVAS_DARK);
        });
        bingOptions.add(item);

        item = new MenuItem("Bing Roads - grayscale");
        item.setOnAction(evt -> {
            mapView.setBingMapsApiKey(bingApiKey.getText());
            mapView.setMapType(MapType.BINGMAPS_CANVAS_GRAY);
        });
        bingOptions.add(item);

        item = new MenuItem("Bing Roads - light");
        item.setOnAction(evt -> {
            mapView.setBingMapsApiKey(bingApiKey.getText());
            mapView.setMapType(MapType.BINGMAPS_CANVAS_LIGHT);
        });
        bingOptions.add(item);


        MenuButton menuButton = new MenuButton("Bing", null, bingOptions.toArray(new MenuItem[0]));
        hbox.getChildren().add(menuButton);


        btn = new Button();
        btn.setText("WMS");
        btn.setOnAction(evt -> mapView.setMapType(MapType.WMS));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("XYZ");
        btn.setOnAction(evt -> mapView.setMapType(MapType.XYZ));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("add marker");
        btn.setOnAction(evt -> mapView.addMarker(marker));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("toggle marker visibility");
        btn.setOnAction(evt -> marker.setVisible(!marker.getVisible()));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("remove marker");
        btn.setOnAction(evt -> mapView.removeMarker(marker));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("add Track");
        btn.setOnAction(evt -> mapView.addCoordinateLine(coordinateLine));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("remove Track");
        btn.setOnAction(evt -> mapView.removeCoordinateLine(coordinateLine));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("toggle Track visibilty");
        btn.setOnAction(evt -> coordinateLine.setVisible(!coordinateLine.getVisible()));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("GC");
        btn.setOnAction(evt -> {
            System.gc();
        });
        hbox.getChildren().add(btn);

        hbox = new HBox();
        hbox.setPadding(new Insets(5, 5, 5, 5));
        hbox.setSpacing(5);
        hbox.getChildren().add(new Label("Bing Maps API Key:"));
        bingApiKey = new TextField();
        hbox.getChildren().add(bingApiKey);

        btn = new Button();
        btn.setText("add label");
        btn.setOnAction(evt -> mapView.addLabel(mapLabel));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("toggle label visibility");
        btn.setOnAction(evt -> mapLabel.setVisible(!mapLabel.getVisible()));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("remove label");
        btn.setOnAction(evt -> mapView.removeLabel(mapLabel));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("clear cache");
        btn.setOnAction(evt -> {
            try {
                mapView.getOfflineCache().clear();
            } catch (IOException e) {
                logger.warn("could not clear cache", e);
            }
        });
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("set constrain");
        btn.setOnAction(evt -> mapView.constrainExtent(extentAll));
        hbox.getChildren().add(btn);

        btn = new Button();
        btn.setText("clear constrain");
        btn.setOnAction(evt -> mapView.clearConstrainExtent());
        hbox.getChildren().add(btn);

        vbox.getChildren().add(hbox);

        vbox.setDisable(true);

        return vbox;
    }
}
