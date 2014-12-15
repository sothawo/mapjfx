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
 * Map component. To use the MapView, construct it and add it to your scene. Then the #initializedProperty should be
 * observed as well as bindings/observations to other properties should be established. Then the #initialize() Method
 * must be called. When the MapView is initialized and ready to be used, the #initializedProperty is set to true.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public final class MapView extends Region {
// ------------------------------ FIELDS ------------------------------

    private static final Logger logger = LoggerFactory.getLogger(MapView.class);

    /** URL of the html code for the WebView */
    private static final String MAPVIEW_HTML = "/mapview.html";

    /** readonly property that informs if this MapView is fully initialized */
    private final ReadOnlyBooleanWrapper initialized = new ReadOnlyBooleanWrapper(false);

    /** the WebEngine of the WebView containing the OpenLayers Map */
    private final WebEngine webEngine;

// --------------------------- CONSTRUCTORS ---------------------------

    public MapView() {
        // instantiate the WebView, resize it with this region by letting it observe the changes and add it as child
        WebView webView = new WebView();
        webEngine = webView.getEngine();
        webView.prefWidthProperty().bind(widthProperty());
        webView.prefHeightProperty().bind(heightProperty());
        getChildren().add(webView);
    }

// -------------------------- OTHER METHODS --------------------------

    public boolean getInitialized() {
        return initialized.get();
    }

    /**
     * initializes the MapView. The internal HTML file is loaded into the contained WebView and the necessary setup is
     * made for communication between this object and the Javascript elements on the web page.
     */
    public void initialize() {
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
                        initialized.set(true);
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
     * @return the readonly initialized property.
     */
    public ReadOnlyBooleanProperty initializedProperty() {
        return initialized.getReadOnlyProperty();
    }
}
