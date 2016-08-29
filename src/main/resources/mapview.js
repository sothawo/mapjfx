console.log = function(msg) {
  javaConnector.debug(msg);
};

/*******************************************************************************************************************
 * predefined map layers
 */

// Source for the coordinateLine features
var sourceFeatures = new ol.source.Vector({
    features: []
});
// layer for the featuress
var layerFeatures = new ol.layer.Vector({
    source: sourceFeatures
});

// layer groups for the different map styles
var layersOSM = new ol.layer.Group({
    layers: [
        new ol.layer.Tile({
            source: new ol.source.OSM()
        }),
        layerFeatures
    ]
});

/*******************************************************************************************************************
 * global variables
 */

// to store coordinateLine objects with a name
var coordinateLines = {};

// to store the marker and label objects with a name
var mapObjects = {};

// the Bing Maps API Key
var bingMapsApiKey = '';

/*******************************************************************************************************************
 map and handlers
 */
var map = new ol.Map({
    target: 'map',
    layers: layersOSM,
    view: new ol.View({
        zoom: 1
    })
});

map.on('singleclick', function (evt) {
    var coordinate = cToWGS84(evt.coordinate);
    // lat/lon reversion
    javaConnector.singleClickAt(coordinate[1], coordinate[0]);
});

var anchorsPatched = false;
map.on('postrender', function (evt) {
    if (!anchorsPatched) {
        var anchors = document.getElementById('map').getElementsByTagName('a');
        for (var i = 0; i < anchors.length; ++i) {
            var anchor = anchors[i];
            href = anchor.href;
            // only patch if not already a javascript link
            if (href && href.lastIndexOf('javascript', 0) !== 0) {
                javaConnector.debug('patching anchor for ' + href);
                anchor.href = 'javascript:javaConnector.showLink("' + href + '");';
                anchorsPatched = true;
            }
        }
    }
});

/*******************************************************************************************************************
 view and handlers
 */
var view = map.getView();
view.on('change:center', function (evt) {
    center = cToWGS84(evt.target.get('center'));
    // lat/lon reversion
    javaConnector.centerMovedTo(center[1], center[0]);
});

view.on('change:resolution', function (evt) {
    javaConnector.zoomChanged(view.getZoom());
});

/*******************************************************************************************************************
 * Connector object for the java application with the functions to be called. there is only this instance, so no
 * prototype is used
 */
var jsConnector = {

    /**
     * sets the center of the map
     *
     * @param {number} latitude value in WGS84
     * @param {number} longitude value in WGS84
     * @param {number} animation duration in ms
     */
    setCenter: function (lat, lon, animationDuration) {
        // transform uses x/y coordinates, thats lon/lat
        var newCenter = cFromWGS84([lon, lat]);
        var oldCenter = view.getCenter();
        if (oldCenter && animationDuration > 1) {
            var anim = ol.animation.pan({
                duration: animationDuration,
                source: oldCenter
            });
            map.beforeRender(anim);
        }
        view.setCenter(newCenter);
    },

    /**
     * sets the zoom of the map
     *
     * @param {number} zoom level
     * @param {number} animation duration in ms
     */
    setZoom: function (zoom, animationDuration) {
        if (zoom != view.getZoom()) {
            var res = view.getResolution();
            if (res && animationDuration > 1) {
                var anim = ol.animation.zoom({
                    resolution: res,
                    duration: animationDuration
                });
                map.beforeRender(anim);
            }
            view.setZoom(zoom);
        }
    },

    /**
     * sets the extent of the map
     *
     * @param {number} minimum latitude value in WGS84
     * @param {number} minimum longitude value in WGS84
     * @param {number} maximum latitude value in WGS84
     * @param {number} maximum longitude value in WGS84
     * @param {number} animation duration in ms
     */
    setExtent: function (minLat, minLon, maxLat, maxLon, animationDuration) {
        // lat/lon reversion
        var extent = eFromWGS84([minLon, minLat, maxLon, maxLat]);
        if (animationDuration > 1) {
            var animPan = ol.animation.pan({
                duration: animationDuration,
                source: view.getCenter()
            });
            var animZoom = ol.animation.zoom({
                resolution: view.getResolution(),
                duration: animationDuration
            });
            map.beforeRender(animPan, animZoom);
        }
        view.fit(extent, map.getSize());
    },

    /**
     * sets the map type
     *
     * @param {string} the new map type
     */
    setMapType: function (newType) {
        // reset the patched flag; the new layer can have different attributions
        anchorsPatched = false;
        if (newType == 'OSM') {
            map.setLayerGroup(layersOSM);
        } else if (newType == 'BINGMAPS_ROAD') {
            map.setLayerGroup(new ol.layer.Group({
                layers: [
                    new ol.layer.Tile({
                        source: new ol.source.BingMaps({
                            imagerySet: 'Road',
                            key: bingMapsApiKey
                        })
                    }),
                    layerFeatures
                ]
            }));
        } else if (newType == 'BINGMAPS_AERIAL') {
            map.setLayerGroup(new ol.layer.Group({
                layers: [
                    new ol.layer.Tile({
                        source: new ol.source.BingMaps({
                            imagerySet: 'Aerial',
                            key: bingMapsApiKey
                        })
                    }),
                    layerFeatures
                ]
            }));
        }
    },

    /**
     * get a coordinateLine with a name; create one, if not yet available
     *
     * @param {string} the name of the coordinateLine
     *
     * @return {CoordinateLine} the object
     */
    getCoordinateLine: function (name) {
        var coordinateLine = coordinateLines[name];
        if (!coordinateLine) {
            coordinateLine = new CoordinateLine();
            coordinateLines[name] = coordinateLine;
            javaConnector.debug("created CoordinateLine object named " + name);
        }
        return coordinateLine;
    },


    /**
     * shows a coordinateLine.
     *
     * @param {string} the name of the coordinateLine
     */
    showCoordinateLine: function (name) {
        var coordinateLine = coordinateLines[name];
        if (coordinateLine && !coordinateLine.getOnMap()) {
            sourceFeatures.addFeature(coordinateLine.getFeature());
            javaConnector.debug("showed CoordinateLine object named " + name);
            coordinateLine.setOnMap(true);
        }
    },

    /**
     * hides a coordinateLine.
     *
     * @param {string} the name of the coordinateLine
     */
    hideCoordinateLine: function (name) {
        var coordinateLine = coordinateLines[name];
        if (coordinateLine && coordinateLine.getOnMap()) {
            sourceFeatures.removeFeature(coordinateLine.getFeature());
            javaConnector.debug("hid CoordinateLine object named " + name);
            coordinateLine.setOnMap(false);
        }
    },

    /**
     * removes a coordinateLine.
     *
     * @param {string} the name of the coordinateLine
     */
    removeCoordinateLine: function (name) {
        if (coordinateLines[name]) {
            this.hideCoordinateLine(name);
            delete coordinateLines[name];
            javaConnector.debug("deleted CoordinateLine object named " + name);
        }
    },

    /**
     * adds a marker to the map
     * @param {string} the name of the marker. must be unique within all markers and labels.
     * @param {string} url the url of the marker's image
     * @param {number} the latitude of the marker's position
     * @param {number} the longitude of the marker's position
     * @param {number} x-offset of the top left point of the image to the coordinate
     * @param {number} y-offset of the top left point of the image to the coordinate
     */
    addMarker: function (name, url, latitude, longitude, offsetX, offsetY) {
        var marker = mapObjects[name];
        if (!marker) {
            marker = new MapObject(cFromWGS84([longitude, latitude]));
            javaConnector.debug('created Marker object named ' + name);

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
            imgElement.onload = function () {
                window.javaConnector.debug('image loaded from ' + url);
            };
            imgElement.onerror = function () {
                window.javaConnector.debug('image load error from ' + url);
            };
            imgElement.src = url;
            javaConnector.debug('started loading img from ' + url);

            imgElement.onclick = function() {
                window.javaConnector.markerClicked(name);
            };

            var overlay = new ol.Overlay({
                offset: [offsetX, offsetY],
                position: undefined,
                element: imgElement
            });
            marker.setOverlay(overlay);
            map.addOverlay(overlay);

            mapObjects[name] = marker;
        }
    },

    /**
     * adds a label to the map
     * @param {string} the name of the Label. must be unique within all markers and labels.
     * @param {string} text the text of the Label
     * @param {string} the css class for the label
     * @param {number} the latitude of the label's position
     * @param {number} the longitude of the label's position
     * @param {number} x-offset of the top left point of the image to the coordinate
     * @param {number} y-offset of the top left point of the image to the coordinate
     */
    addLabel: function (name, text, cssClass, latitude, longitude, offsetX, offsetY) {
        var label = mapObjects[name];
        if (!label) {
            label = new MapObject(cFromWGS84([longitude, latitude]));
            javaConnector.debug('created Label object named ' + name);

            // add a new <div> element to <div id='labels'>
            var labelsElement = document.getElementById('labels');
            var labelElement = document.createElement('div');
            labelsElement.appendChild(labelElement);

            labelElement.setAttribute('id', name);
            labelElement.setAttribute("class", "mapview-label " + cssClass);
            labelElement.innerHTML = text;

            var overlay = new ol.Overlay({
                offset: [offsetX, offsetY],
                position: undefined,
                element: labelElement
            });
            label.setOverlay(overlay);
            map.addOverlay(overlay);

            mapObjects[name] = label;
        }
    },

    /**
     * moves a MapObject to a new position.
     * @param name the name of the MapObject to move
     * @param latitude new latitude
     * @param longitude new longitude
     */
    moveMapObject: function (name, latitude, longitude) {
        var mapObject = mapObjects[name];
        if (mapObject) {
            mapObject.setPosition(cFromWGS84([longitude, latitude]));
            if (mapObject.getOnMap()) {
                var overlay = mapObject.getOverlay();
                if (overlay) {
                    overlay.setPosition(mapObject.getPosition());
                }
            }
            javaConnector.debug('moved ' + name);
        }
    },

    /**
     * removes a MapObject from the map
     * @param {string} the name of the MapObject
     */
    removeMapObject: function (name) {
        var mapObject = mapObjects[name];
        if (mapObject) {
            this.hideMapObject(mapObject);
            var overlay = mapObject.getOverlay();
            map.removeOverlay(overlay);
            var element = overlay.getElement();
            if (element) {
                delete element;
            }
            delete overlay;
            delete mapObjects[name];
            javaConnector.debug('removed ' + name);
        }
    },

    /**
     * hides a MapObject from the map. the overlay is set to an undefined position which removes it from the map
     * @param {string} the name of the MapObject
     */
    hideMapObject: function (name) {
        var mapObject = mapObjects[name];
        if (mapObject && mapObject.getOnMap()) {
            var overlay = mapObject.getOverlay();
            if (overlay) {
                overlay.setPosition(undefined);
            }
            mapObject.setOnMap(false);
            javaConnector.debug("hid " + name);
        }
    },


    /**
     * shows a MapObject.
     * @param the name of the MapObject to show
     */
    showMapObject: function (name) {
        var mapObject = mapObjects[name];
        if (mapObject && !mapObject.getOnMap()) {
            var overlay = mapObject.getOverlay();
            if (overlay) {
                overlay.setPosition(mapObject.getPosition());
            }
            mapObject.setOnMap(true);
            javaConnector.debug("showed " + name);
        }
    },

    /**
     * sets the bing maps api key
     * @param apiKey the api key
     */
    setBingMapsApiKey: function (apiKey) {
        bingMapsApiKey = apiKey;
    }
};

/**
 * @return the one and only jsConnector object
 */
function getJsConnector() {
    return jsConnector;
}

