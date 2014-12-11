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

import javafx.scene.layout.Region;
import javafx.scene.web.WebView;

import java.net.URL;

/**
 * Map component.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class MapView extends Region {
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

        // load the html containing the OL code
        URL mapviewUrl = getClass().getResource("/mapview.html");
        webView.getEngine().load(mapviewUrl.toExternalForm());
    }
}
