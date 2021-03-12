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

/*******************************************************************************************************************
 * Connector object for the java application with the functions to be called.
 * @param javaConnector the javaConnector object
 */

function JSMapView(javaConnector) {
    this.map = {};
    this.sourceFeatures = {};
    this.layerFeatures = {};
    this.coordinateLines = {};
    this.circles = {};
    this.mapObjects = {};
    this.mapType = '';
    this.javaConnector = javaConnector;
    this.anchorsPatched = false;
    this.bingMapsApiKey = '';
    this.wmsParams = {};
    this.xyzParams = {};
    this.projections = new Projections();
}

JSMapView.prototype.toString = function () {
    return 'JSMapView with javaConnector ' + this.javaConnector;
};

/**
 * initializes the JSMapView and the map.
 * @param config JSON string with configuraiton settings
 */
JSMapView.prototype.init = function (config) {

    var configuration = JSON.parse(config);
    console.log(config);

    this.projections.mapjfx = 'EPSG:4326';
    this.projections.openlayers = configuration.projection;

    // Source for the coordinateLine features
    this.sourceFeatures = new ol.source.Vector({
        features: []
    });
    // layer for the featuress
    this.layerFeatures = new ol.layer.Vector({
        source: this.sourceFeatures
    });


    this.map = new ol.Map({
        target: 'map',
        layers: new ol.layer.Group({
            layers: []
        }),
        view: new ol.View({
            zoom: 1,
            projection: configuration.projection
        }),
        controls: ol.control.defaults({
            zoom: configuration.showZoomControls
        }),
        interactions: ol.interaction.defaults({
            doubleClickZoom: configuration.interactive,
            dragPan: configuration.interactive,
            keyboardZoom: configuration.interactive,
            mouseWheelZoom: configuration.interactive
        })
    });

    var view = this.map.getView();


    this.map.on('pointermove',
        (function (evt) {
            var coordinate = this.projections.cToWGS84(evt.coordinate);
            // lat/lon reversion
            this.javaConnector.pointerMovedTo(coordinate[1], coordinate[0]);
        }).bind(this));

    this.map.on('singleclick',
        (function (evt) {
            var coordinate = this.projections.cToWGS84(evt.coordinate);
            // lat/lon reversion
            this.javaConnector.singleClickAt(coordinate[1], coordinate[0]);
        }).bind(this));

    this.map.on('postrender',
        (function (evt) {
            if (!this.anchorsPatched) {
                var anchors = document.getElementById('map').getElementsByTagName('a');
                for (var i = 0; i < anchors.length; ++i) {
                    var anchor = anchors[i];
                    var href = anchor.href;
                    // only patch if not already a javascript link
                    if (href && href.lastIndexOf('javascript', 0) !== 0) {
                        this.javaConnector.debug('patching anchor for ' + href);
                        anchor.href = 'javascript:_javaConnector.showLink("' + href + '");';
                        this.anchorsPatched = true;
                    }
                }
            }
        }).bind(this));

    view.on('change:center',
        (function (evt) {
            var center = this.projections.cToWGS84(evt.target.get('center'));
            // lat/lon reversion
            this.javaConnector.centerMovedTo(center[1], center[0]);
            this.reportExtent();
        }).bind(this));

    view.on('change:resolution', (function (evt) {
        this.javaConnector.zoomChanged(view.getZoom());
        this.reportExtent();
    }).bind(this));

    this.map.on('change:size', (function (evt) {
        this.reportExtent();
    }).bind(this));

    // a DragBox interaction
    var dragBox = new ol.interaction.DragBox({
        condition: ol.events.condition.platformModifierKeyOnly
    });
    dragBox.on('boxend', (function () {
        var extent = this.projections.eToWGS84(dragBox.getGeometry().getExtent());
        this.javaConnector.extentSelected(extent[1], extent[0], extent[3], extent[2]);
    }).bind(this));

    this.map.addInteraction(dragBox);

    this.setMapType('OSM');
};

/**
 * sets the center of the map
 *
 * @param {number} lat value in WGS84
 * @param {number} lon value in WGS84
 * @param {number} animationDuration duration in ms
 */
JSMapView.prototype.setCenter = function (lat, lon, animationDuration) {
    var view = this.map.getView();
    // transform uses x/y coordinates, thats lon/lat
    var newCenter = this.projections.cFromWGS84([lon, lat]);
    var oldCenter = view.getCenter();
    if (oldCenter && animationDuration > 1) {
        view.animate({
            center: newCenter,
            duration: animationDuration
        });
    } else {
        view.setCenter(newCenter);
    }
};

/**
 * sets the zoom of the map
 *
 * @param {number} zoom level
 * @param {number} animationDuration duration in ms
 */
JSMapView.prototype.setZoom = function (zoom, animationDuration) {
    var view = this.map.getView();
    if (zoom !== view.getZoom()) {
        if (animationDuration > 1) {
            view.animate({
                zoom: zoom,
                duration: animationDuration
            });
        } else {
            view.setZoom(zoom);
        }
    }
};

/**
 * sets the extent of the map
 *
 * @param {number} minLat latitude value in WGS84
 * @param {number} minLon longitude value in WGS84
 * @param {number} maxLat latitude value in WGS84
 * @param {number} maxLon longitude value in WGS84
 * @param {number} animationDuration duration in ms
 */
JSMapView.prototype.setExtent = function (minLat, minLon, maxLat, maxLon, animationDuration) {
    var view = this.map.getView();
    // lat/lon reversion
    var extent = this.projections.eFromWGS84([minLon, minLat, maxLon, maxLat]);
    if (animationDuration > 1) {
        view.fit(extent, {duration: animationDuration});
    } else {
        view.fit(extent);
    }
};

/**
 * sets the constrainextent of the map
 *
 * @param {number} minLat latitude value in WGS84
 * @param {number} minLon longitude value in WGS84
 * @param {number} maxLat latitude value in WGS84
 * @param {number} maxLon longitude value in WGS84
 */
JSMapView.prototype.constrainExtent = function (minLat, minLon, maxLat, maxLon) {
    var view = this.map.getView();
    // lat/lon reversion
    var extent = this.projections.eFromWGS84([minLon, minLat, maxLon, maxLat]);
    var properties = view.getProperties();
    properties["extent"] = extent;
    properties["smoothExtentConstraint"] = false;
    this.map.setView(new ol.View(properties));
};

/**
 * clears the constrainExtent of the view.
 */
JSMapView.prototype.clearConstrainExtent = function () {
    var view = this.map.getView();
    var properties = view.getProperties();
    delete properties.extent;
    this.map.setView(new ol.View(properties));
};


JSMapView.prototype.getMapType = function () {
    return this.mapType;
};

/**
 * sets the map type
 *
 * @param {string} newType the new map type
 */
JSMapView.prototype.setMapType = function (newType) {
    // reset the patched flag; the new layer can have different attributions
    this.anchorsPatched = false;
    var mapTypeChanged = true;

    if (newType === 'OSM') {
        this.map.setLayerGroup(new ol.layer.Group({
                layers: [
                    new ol.layer.Tile({
                        source: new ol.source.OSM({
                            projection: new ol.proj.Projection(this.projections.openlayers)
                        })
                    }),
                    this.layerFeatures
                ]
            })
        );
    } else if (newType === 'BINGMAPS_ROAD') {
        this.map.setLayerGroup(new ol.layer.Group({
            layers: [
                new ol.layer.Tile({
                    source: new ol.source.BingMaps({
                        imagerySet: 'RoadOnDemand',
                        key: this.bingMapsApiKey,
                        projection: new ol.proj.Projection(this.projections.openlayers)
                    })
                }),
                this.layerFeatures
            ]
        }));
    } else if (newType === 'BINGMAPS_AERIAL') {
        this.map.setLayerGroup(new ol.layer.Group({
            layers: [
                new ol.layer.Tile({
                    source: new ol.source.BingMaps({
                        imagerySet: 'Aerial',
                        key: this.bingMapsApiKey,
                        projection: new ol.proj.Projection(this.projections.openlayers)
                    })
                }),
                this.layerFeatures
            ]
        }));
    } else if (newType === 'BINGMAPS_AERIAL_WITH_LABELS') {
        this.map.setLayerGroup(new ol.layer.Group({
            layers: [
                new ol.layer.Tile({
                    source: new ol.source.BingMaps({
                        imagerySet: 'AerialWithLabelsOnDemand',
                        key: this.bingMapsApiKey,
                        projection: new ol.proj.Projection(this.projections.openlayers)
                    })
                }),
                this.layerFeatures
            ]
        }));
    } else if (newType === 'BINGMAPS_CANVAS_GRAY') {
        this.map.setLayerGroup(new ol.layer.Group({
            layers: [
                new ol.layer.Tile({
                    source: new ol.source.BingMaps({
                        imagerySet: 'CanvasGray',
                        key: this.bingMapsApiKey,
                        projection: new ol.proj.Projection(this.projections.openlayers)
                    })
                }),
                this.layerFeatures
            ]
        }));
    } else if (newType === 'BINGMAPS_CANVAS_DARK') {
        this.map.setLayerGroup(new ol.layer.Group({
            layers: [
                new ol.layer.Tile({
                    source: new ol.source.BingMaps({
                        imagerySet: 'CanvasDark',
                        key: this.bingMapsApiKey,
                        projection: new ol.proj.Projection(this.projections.openlayers)
                    })
                }),
                this.layerFeatures
            ]
        }));
    } else if (newType === 'BINGMAPS_CANVAS_LIGHT') {
        this.map.setLayerGroup(new ol.layer.Group({
            layers: [
                new ol.layer.Tile({
                    source: new ol.source.BingMaps({
                        imagerySet: 'CanvasLight',
                        key: this.bingMapsApiKey,
                        projection: new ol.proj.Projection(this.projections.openlayers)
                    })
                }),
                this.layerFeatures
            ]
        }));
    } else if (newType === 'STAMEN_WC') {
        this.map.setLayerGroup(new ol.layer.Group({
            layers: [
                new ol.layer.Tile({
                    source: new ol.source.Stamen({
                        layer: 'watercolor',
                        projection: new ol.proj.Projection(this.projections.openlayers)
                    })
                }),
                new ol.layer.Tile({
                    source: new ol.source.Stamen({
                        layer: 'terrain-labels',
                        projection: new ol.proj.Projection(this.projections.openlayers)
                    })
                }),
                this.layerFeatures
            ]
        }));
    } else if (newType === 'WMS' && this.wmsParams.getUrl().length > 0) {
        this.map.setLayerGroup(new ol.layer.Group({
            layers: [
                new ol.layer.Tile({
                    source: new ol.source.TileWMS({
                        url: this.wmsParams.getUrl(),
                        params: this.wmsParams.getParams(),
                        serverType: 'geoserver'
                    })
                }),
                this.layerFeatures
            ]
        }));
    } else if (newType === 'XYZ' && this.xyzParams.url.length > 0) {
        this.map.setLayerGroup(new ol.layer.Group({
            layers: [
                new ol.layer.Tile({
                    source: new ol.source.XYZ(this.xyzParams),
                    projection: new ol.proj.Projection(this.projections.openlayers)
                }),
                this.layerFeatures
            ]
        }));
    } else {
        mapTypeChanged = false;
    }

    if (mapTypeChanged) {
        this.mapType = newType;
    }
};

/**
 * get a coordinateLine with a name; create one, if not yet available
 *
 * @param {string} name the name of the coordinateLine
 *
 * @return {CoordinateLine} the object
 */
JSMapView.prototype.getCoordinateLine = function (name) {
    var coordinateLine = this.coordinateLines[name];
    if (!coordinateLine) {
        coordinateLine = new CoordinateLine(this.projections);
        this.coordinateLines[name] = coordinateLine;
        this.javaConnector.debug("created CoordinateLine object named " + name);
    }
    return coordinateLine;
};


/**
 * shows a coordinateLine.
 *
 * @param {string} name the name of the coordinateLine
 */
JSMapView.prototype.showCoordinateLine = function (name) {
    this.javaConnector.debug("should show CoordinateLine object named " + name);
    var coordinateLine = this.coordinateLines[name];
    if (coordinateLine && !coordinateLine.getOnMap()) {
        this.sourceFeatures.addFeature(coordinateLine.getFeature());
        this.javaConnector.debug("showed CoordinateLine object named " + name);
        coordinateLine.setOnMap(true);
    }
};

/**
 * hides a coordinateLine.
 *
 * @param {string} name the name of the coordinateLine
 */
JSMapView.prototype.hideCoordinateLine = function (name) {
    this.javaConnector.debug("should hide CoordinateLine object named " + name);
    var coordinateLine = this.coordinateLines[name];
    if (coordinateLine && coordinateLine.getOnMap()) {
        this.sourceFeatures.removeFeature(coordinateLine.getFeature());
        this.javaConnector.debug("hid CoordinateLine object named " + name);
        coordinateLine.setOnMap(false);
    }
};

/**
 * removes a coordinateLine.
 *
 * @param {string} name the name of the coordinateLine
 */
JSMapView.prototype.removeCoordinateLine = function (name) {
    this.javaConnector.debug("should delete CoordinateLine object named " + name);
    if (this.coordinateLines[name]) {
        this.hideCoordinateLine(name);
        delete this.coordinateLines[name];
        this.javaConnector.debug("deleted CoordinateLine object named " + name);
    }
};

/**
 * adds a marker to the map
 * @param {string} name the name of the marker. must be unique within all markers and labels.
 * @param {string} url the url of the marker's image
 * @param {number} latitude the latitude of the marker's position
 * @param {number} longitude the longitude of the marker's position
 * @param {number} offsetX x-offset of the top left point of the image to the coordinate
 * @param {number} offsetY y-offset of the top left point of the image to the coordinate
 */
JSMapView.prototype.addMarker = function (name, url, latitude, longitude, offsetX, offsetY) {
    var marker = this.mapObjects[name];
    if (!marker) {
        marker = new MapObject(this.projections.cFromWGS84([longitude, latitude]));
        this.javaConnector.debug('created Marker object named ' + name);

        // add a new <img> element to <div id='markers'>
        var markersElement = document.getElementById('markers');
        var imgElement = document.createElement('img');
        markersElement.appendChild(imgElement);

        imgElement.setAttribute('id', name);
        imgElement.setAttribute('alt', name);
        imgElement.setAttribute('draggable', 'false');
        imgElement.ondragstart = function () {
            return false;
        };
        imgElement.onload = (function () {
            this.javaConnector.debug('image loaded from ' + url);
        }).bind(this);
        imgElement.onerror = (function () {
            this.javaConnector.debug('image load error from ' + url);
        }).bind(this);
        imgElement.src = url;
        this.javaConnector.debug('started loading img from ' + url);

        imgElement.onmousedown = (function (evt) {
            this.javaConnector.markerMouseDown(name);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        imgElement.onmouseup = (function (evt) {
            this.javaConnector.markerMouseUp(name);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        imgElement.onclick = (function (evt) {
            this.javaConnector.markerClicked(name);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        imgElement.ondblclick = (function (evt) {
            this.javaConnector.markerDoubleClicked(name);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        imgElement.oncontextmenu = (function (evt) {
            this.javaConnector.markerRightClicked(name);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        imgElement.onmouseover = (function (evt) {
            this.javaConnector.markerEntered(name);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        imgElement.onmouseout = (function (evt) {
            this.javaConnector.markerExited(name);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        imgElement.onwheel = (function (evt) {
            this.javaConnector.wheelEvent(evt.deltaY);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        imgElement.onmousemove = (function (evt) {
            var coordinate = this.projections.cToWGS84(this.map.getEventCoordinate(evt));
            // lat/lon reversion
            this.javaConnector.pointerMovedTo(coordinate[1], coordinate[0]);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);

        var overlay = new ol.Overlay({
            offset: [offsetX, offsetY],
            position: undefined,
            element: imgElement,
            stopEvent: true
        });
        marker.setOverlay(overlay);
        marker.setElement(imgElement);
        this.map.addOverlay(overlay);
        this.mapObjects[name] = marker;
    }
};

/**
 * adds a label to the map
 * @param {string} name the name of the Label. must be unique within all markers and labels.
 * @param {string} text the text of the Label
 * @param {string} cssClass the css class for the label
 * @param {number} latitude the latitude of the label's position
 * @param {number} longitude the longitude of the label's position
 * @param {number} offsetX x-offset of the top left point of the image to the coordinate
 * @param {number} offsetY y-offset of the top left point of the image to the coordinate
 */
JSMapView.prototype.addLabel = function (name, text, cssClass, latitude, longitude, offsetX, offsetY) {
    var label = this.mapObjects[name];
    if (!label) {
        label = new MapObject(this.projections.cFromWGS84([longitude, latitude]));
        this.javaConnector.debug('created Label object named ' + name);

        // add a new <div> element to <div id='labels'>
        var labelsElement = document.getElementById('labels');
        var labelElement = document.createElement('div');
        labelsElement.appendChild(labelElement);

        labelElement.setAttribute('id', name);
        labelElement.setAttribute("class", "mapview-label " + cssClass);
        labelElement.innerHTML = text;

        labelElement.onmousedown = (function (evt) {
            this.javaConnector.labelMouseDown(name);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        labelElement.onmouseup = (function (evt) {
            this.javaConnector.labelMouseUp(name);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        labelElement.onclick = (function (evt) {
            this.javaConnector.labelClicked(name);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        labelElement.ondblclick = (function (evt) {
            this.javaConnector.labelDoubleClicked(name);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        labelElement.oncontextmenu = (function (evt) {
            this.javaConnector.labelRightClicked(name);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        labelElement.onmouseover = (function (evt) {
            this.javaConnector.labelEntered(name);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        labelElement.onmouseout = (function (evt) {
            this.javaConnector.labelExited(name);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        labelElement.onwheel = (function (evt) {
            this.javaConnector.wheelEvent(evt.deltaY);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);
        labelElement.onmousemove = (function (evt) {
            var coordinate = this.projections.cToWGS84(this.map.getEventCoordinate(evt));
            // lat/lon reversion
            this.javaConnector.pointerMovedTo(coordinate[1], coordinate[0]);
            evt.stopPropagation();
            evt.preventDefault();
        }).bind(this);

        var overlay = new ol.Overlay({
            offset: [offsetX, offsetY],
            position: undefined,
            element: labelElement,
            stopEvent: true
        });
        label.setOverlay(overlay);
        label.setElement(labelElement);
        this.map.addOverlay(overlay);
        this.mapObjects[name] = label;
    }
};

/**
 * moves a MapObject to a new position.
 * @param name the name of the MapObject to move
 * @param latitude new latitude
 * @param longitude new longitude
 */
JSMapView.prototype.moveMapObject = function (name, latitude, longitude) {
    var mapObject = this.mapObjects[name];
    if (mapObject) {
        mapObject.setPosition(this.projections.cFromWGS84([longitude, latitude]));
        if (mapObject.getOnMap()) {
            var overlay = mapObject.getOverlay();
            if (overlay) {
                overlay.setPosition(mapObject.getPosition());
            }
        }
        this.javaConnector.debug('moved ' + name);
    }
};

/**
 * removes a MapObject from the map
 * @param {string} name the name of the MapObject
 */
JSMapView.prototype.removeMapObject = function (name) {
    this.javaConnector.debug('should remove ' + name);
    var mapObject = this.mapObjects[name];
    if (mapObject) {
        this.hideMapObject(mapObject);
        var overlay = mapObject.getOverlay();
        if (overlay) {
            this.map.removeOverlay(overlay);
            var element = overlay.getElement();
            if (element) {
                delete element;
            }
            delete overlay;
        }
        delete this.mapObjects[name];
        this.javaConnector.debug('removed ' + name);
    }
};

/**
 * hides a MapObject from the map. the overlay is set to an undefined position which removes it from the map
 * @param {string} name the name of the MapObject
 */
JSMapView.prototype.hideMapObject = function (name) {
    this.javaConnector.debug("should hide " + name);
    var mapObject = this.mapObjects[name];
    if (mapObject && mapObject.getOnMap()) {
        var overlay = mapObject.getOverlay();
        if (overlay) {
            overlay.setPosition(undefined);
        }
        mapObject.setOnMap(false);
        this.javaConnector.debug("hid " + name);
    }
};


/**
 * rotates the html element of a MapObject using CSS.
 * @param {string} name the name of the MapObject
 * @param angle number the rotation angle in degrees
 */
JSMapView.prototype.rotateMapObject = function (name, angle) {
    this.javaConnector.debug("should rotate " + name + " to " + angle);
    let mapObject = this.mapObjects[name];
    if (mapObject) {
        let element = mapObject.getElement();
        let previousTransform = element.style["transform"];
        let newTransform = previousTransform.replace(/rotate\(.*?\)/, '').trim() + ' rotate(' + angle + 'deg)';
        element.style["transform"] = newTransform;
    }
};

/**
 * shows a MapObject.
 * @param name the name of the MapObject to show
 */
JSMapView.prototype.showMapObject = function (name) {
    this.javaConnector.debug("should show " + name);
    var mapObject = this.mapObjects[name];
    if (mapObject && !mapObject.getOnMap()) {
        var overlay = mapObject.getOverlay();
        if (overlay) {
            overlay.setPosition(mapObject.getPosition());
        }
        mapObject.setOnMap(true);
        this.javaConnector.debug("showed " + name);
    }
};

JSMapView.prototype.setLabelCss = function (name, cssClass) {
    this.javaConnector.debug("should css of " + name + " to " + cssClass);
    var mapLabel = this.mapObjects[name];
    if (mapLabel) {
        var overlay = mapLabel.getOverlay();
        if (overlay) {
            var element = overlay.getElement();
            if (element) {
                element.setAttribute("class", "mapview-label " + cssClass);
            }
        }
    }
};

/**
 * sets the bing maps api key
 * @param apiKey the api key
 */
JSMapView.prototype.setBingMapsApiKey = function (apiKey) {
    this.bingMapsApiKey = apiKey;
};

/**
 * sets the XYZ Params object
 * @param params the params object
 */
JSMapView.prototype.setXYZParams = function (params) {
    this.xyzParams = JSON.parse(params);
};

/**
 * sets the WMS Params object
 * @param params the params object
 */
JSMapView.prototype.setWMSParams = function (params) {
    this.wmsParams = params;
};

/**
 * creates a new WMSParams object.
 */
JSMapView.prototype.newWMSParams = function () {
    this.wmsParams = new WMSParams();
};

/**
 * sets the url of the current wmsParams object.
 * @param url {string} the new url
 */
JSMapView.prototype.setWMSParamsUrl = function (url) {
    this.wmsParams.setUrl(url);
};

/**
 * adds a key value pair to the current wmsParams object.
 * @param key {string} the key
 * @param value {string} the value
 */
JSMapView.prototype.addWMSParamsParams = function (key, value) {
    this.wmsParams.addParam(key, value);
};

/**
 * handle contextmenu click by converting the coordinate and passing it to Java.
 * @param browserEvent the browser event
 */
JSMapView.prototype.contextmenu = function (browserEvent) {
    var coordinate = this.projections.cToWGS84(this.map.getEventCoordinate(browserEvent));
    // lat/lon reversion
    this.javaConnector.contextClickAt(coordinate[1], coordinate[0]);
};

JSMapView.prototype.reportExtent = function () {
    try {
        var extent = this.projections.eToWGS84(this.map.getView().calculateExtent(this.map.getSize()));
        this.javaConnector.extentChanged(extent[1], extent[0], extent[3], extent[2]);
    } catch (e) {
        // ignore
    }
};

var _jsMapView;

/**
 * creates the global JSMapView object
 * @param config JSON string with configuraiton settings
 * @return the global JSMapView object.
 */
function createJSMapView(config) {
    var jsMapView = new JSMapView(_javaConnector);
    jsMapView.init(config);
    _jsMapView = jsMapView;
    return getJSMapView();
}

/**
 * @return the global JSMapView object.
 */
function getJSMapView() {
    return _jsMapView;
}


//==================================================================================================================
// Map circle
//==================================================================================================================

JSMapView.prototype.getCircle = function (name) {
    var circle = this.circles[name];
    if (!circle) {
        circle = new MapCircle(this.projections, this.map);
        this.circles[name] = circle;
        this.javaConnector.debug("created Circle object named " + name);
    }
    return circle;
};


/**
 * shows a circle.
 *
 * @param {string} name the name of the circle
 */
JSMapView.prototype.showCircle = function (name) {
    this.javaConnector.debug("should show Circle object named " + name);
    var circle = this.circles[name];
    if (circle && !circle.getOnMap()) {
        var feature = circle.getFeature()

        this.javaConnector.debug(" - center: " + circle.getCenter());
        this.javaConnector.debug(" - radius: " + circle.getRadius());
        this.javaConnector.debug(" - feature: " + JSON.stringify(feature));

        this.sourceFeatures.addFeature(feature);
        this.javaConnector.debug("showed Circle object named " + name);
        circle.setOnMap(true);
    }
};

/**
 * hides a circle.
 *
 * @param {string} name the name of the circle
 */
JSMapView.prototype.hideCircle = function (name) {
    this.javaConnector.debug("should hide Circle object named " + name);
    var circle = this.circles[name];
    if (circle && circle.getOnMap()) {
        this.sourceFeatures.removeFeature(circle.getFeature());
        this.javaConnector.debug("hid Circle object named " + name);
        circle.setOnMap(false);
    }
};

/**
 * removes a circle.
 *
 * @param {string} name the name of the circle
 */
JSMapView.prototype.removeCircle = function (name) {
    this.javaConnector.debug("should delete Circle object named " + name);
    if (this.circles[name]) {
        this.hideCircle(name);
        delete this.circles[name];
        this.javaConnector.debug("deleted Circle object named " + name);
    }
};


