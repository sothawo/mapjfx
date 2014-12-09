/*
 * (c) 2014 P.J. Meisch (pj.meisch@sothawo.com).
 */
package com.sothawo.mapjfx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;

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
        webView = new WebView();
        getChildren().add(webView);
        // TODO: load local code containing openlayers script
        webView.getEngine().load("http://openlayers.org/en/v3.0.0/examples/simple.html");

        // resize the webview with this region by observing the changes
        // TODO: can this be done by connecting the webview's properties to this object properties?
        widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                webView.setPrefWidth(newValue.doubleValue());
            }
        });
        heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                webView.setPrefHeight(newValue.doubleValue());
            }
        });
    }
}
