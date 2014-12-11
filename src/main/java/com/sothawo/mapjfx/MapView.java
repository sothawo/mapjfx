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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Map component.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class MapView extends Region {

    private static final Logger logger = LoggerFactory.getLogger(MapView.class);

    /** URL of the html code for the WebView */
    private static final String MAPVIEW_HTML = "/mapview.html";

// ------------------------------ FIELDS ------------------------------

    /** the WebView containing the OpenLayers Map */
    private final WebView webView;

// --------------------------- CONSTRUCTORS ---------------------------

    public MapView() {
        // initialize the WebView, resize it with this region by letting it observe the changes and add it as child
        webView = new WebView();
        webView.prefWidthProperty().bind(widthProperty());
        webView.prefHeightProperty().bind(heightProperty());
        getChildren().add(webView);

        URL mapviewUrl = getClass().getResource(MAPVIEW_HTML);
        if (null == mapviewUrl) {
            logger.error("resource not found: {}", MAPVIEW_HTML);
        } else {
            WebEngine engine = webView.getEngine();
            engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue,
                                    Worker.State newValue) {
                    logger.debug("WebEngine loader state  {} -> {}", oldValue, newValue);
                }
            });
            // load the html containing the OL code
            engine.load(mapviewUrl.toExternalForm());
        }
    }
}
