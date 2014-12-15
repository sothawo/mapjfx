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

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test application.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class Showcase extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Showcase.class);

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void start(Stage primaryStage) throws Exception {
        Coordinate coordKaCastle = new Coordinate(49.013517, 8.404435);

        logger.info("starting showcase...");
        BorderPane borderPane = new BorderPane();

        // MapView in the center
        MapView mapView = new MapView();
        borderPane.setCenter(mapView);
        // add listener for mapView initialization
        mapView.initializedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    logger.debug("mapView initialized");
                }
            }
        });

        // on top some buttons with coordinates
        Button btn = new Button();
        btn.setText("Karlsruhe castle");
        btn.setOnAction(event -> mapView.setCenter(coordKaCastle));
        borderPane.setTop(btn);

        // at the bottom some infos

        // now initialize the mapView
        logger.debug("initializing mapView...");
        mapView.initialize();

        // show the whole thing
        Scene scene = new Scene(borderPane, 800, 600);

        primaryStage.setTitle("sothawo mapjfx showcase");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        logger.debug("application started.");
    }

// --------------------------- main() method ---------------------------

    public static void main(String[] args) {
        launch(args);
    }
}
