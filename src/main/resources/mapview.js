/*******************************************************************************************************************
 * predefined map layers
 */

// Source for the coordinateLine features
var sourceFeatures = new ol.source.Vector({
                             features: []
                     });
// layer for the featuress
var layerFeatures = new ol.layer.Vector({
    source: sourceFeatures,
    style: new ol.style.Style({
        stroke: new ol.style.Stroke({
                  width: 3,
                  color: [255, 0, 0, 1]
                })
    })
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

var layersMQ = new ol.layer.Group({
    layers: [
        new ol.layer.Tile({
            source: new ol.source.MapQuest({layer: 'osm'})
        }),
        layerFeatures
    ]
  });

/*******************************************************************************************************************
 * global variables
 */

// to store the marker names with the overlays to be able to remove them
var markerOverlays = {};

// to store coordinateLine objects with a name
var coordinateLines = {};

/*******************************************************************************************************************
  map and handlers
 */
var map = new ol.Map({
    controls: [
        new ol.control.Zoom({
            zoomInLabel: '+',
            // en-dash instead of standard \u2212 minus, this renders as '2' since ol 3.1.1
            zoomOutLabel: '\u2013'
        }),
        new ol.control.Attribution
    ],
    target: 'map',
    layers: layersOSM,
    view: new ol.View({
        zoom: 1
    })
});

map.on('singleclick', function(evt){
  var coordinate = cToWGS84(evt.coordinate);
    // lat/lon reversion
    javaConnector.singleClickAt(coordinate[1], coordinate[0]);
});

var anchorsPatched = false;
map.on('postrender', function(evt) {
    if(!anchorsPatched) {
        var anchors = document.getElementById('map').getElementsByTagName('a');
        for(var i = 0; i < anchors.length; ++i) {
            var anchor = anchors[i];
            href = anchor.href;
            // only patch if not already a javascript link
            if(href && href.lastIndexOf('javascript', 0) !== 0) {
              javaConnector.debug('patching anchor for ' + href);
              anchor.href='javascript:javaConnector.showLink("' + href +'");';
              anchorsPatched =true;
            }
        }
    }
});

/*******************************************************************************************************************
  view and handlers
 */
var view = map.getView();
view.on('change:center', function(evt) {
    center = cToWGS84(evt.target.get('center'));
    // lat/lon reversion
    javaConnector.centerMovedTo(center[1], center[0]);
});

view.on('change:resolution', function(evt) {
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
    setCenter: function(lat, lon, animationDuration) {
        // transform uses x/y coordinates, thats lon/lat
        var newCenter = cFromWGS84([lon, lat]);
        var oldCenter = view.getCenter();
        if(oldCenter && animationDuration > 1) {
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
    setZoom: function(zoom, animationDuration) {
        if(zoom != view.getZoom()) {
            var res = view.getResolution();
            if(res && animationDuration > 1) {
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
    setExtent: function(minLat, minLon, maxLat, maxLon, animationDuration) {
        // lat/lon reversion
        var extent = eFromWGS84([minLon, minLat, maxLon, maxLat]);
        if(animationDuration > 1) {
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
        view.fitExtent(extent, map.getSize());
    },
    /**
     * sets the map type
     *
     * @param {string} the new map type
     */
    setMapType: function(newType) {
        // reset the patched flag; the new layer can have different attributions
        anchorsPatched = false;
        if(newType == 'OSM') {
            map.setLayerGroup(layersOSM);
        } else if (newType == 'MAPQUEST_OSM') {
            map.setLayerGroup(layersMQ);
        }
    },

    /**
     * get a coordinateLine with a name; create one, if not yet available
     *
     * @param {string} the name of the coordinateLine
     *
     * @return {CoordinateLine} the object
     */
     getCoordinateLine: function(name) {
        var coordinateLine = coordinateLines[name];
        if(!coordinateLine) {
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
     showCoordinateLine: function(name) {
        if(coordinateLines[name]) {
            sourceFeatures.addFeature(coordinateLines[name].getFeature());
            javaConnector.debug("showed CoordinateLine object named " + name);
        }
     },

    /**
     * hides a coordinateLine.
     *
     * @param {string} the name of the coordinateLine
     */
     hideCoordinateLine: function(name) {
        if(coordinateLines[name]) {
            sourceFeatures.removeFeature(coordinateLines[name].getFeature());
            javaConnector.debug("hid CoordinateLine object named " + name);
        }
     },

    /**
     * removes a coordinateLine.
     *
     * @param {string} the name of the coordinateLine
     */
     removeCoordinateLine: function(name) {
        if(coordinateLines[name]) {
            delete coordinateLines[name];
            javaConnector.debug("deleted CoordinateLine object named " + name);
        }
     },
}

/**
 * @return the on and only jsConnector object
 */
function getJsConnector() {
    return jsConnector;
}

/*******************************************************************************************************************
  functions
 */


function addMarkerWithURL(name, url, latitude, longitude, offsetX, offsetY) {
    var img = document.getElementById(name);
    if(!img) {
        var markers = document.getElementById('markers');
        img = document.createElement('img');
        img.setAttribute('id', name);
        img.setAttribute('alt', name);
        img.setAttribute('draggable', 'false');
        img.ondragstart = function() {
            return false;
        }

        // create an image that does the rest when finished loading
        var newImg = new Image;
        newImg.onload = function() {
            javaConnector.debug('image loaded from ' + url);
            img.src = this.src;

            markers.appendChild(img);

            var overlay = new ol.Overlay({
                offset: [offsetX, offsetY],
                position: cFromWGS84([longitude,latitude]),
                element: img
            });
            markerOverlays[name] = overlay;
            map.addOverlay(overlay);
        };
        newImg.onerror = function() {
            javaConnector.debug('image load error');
        };
        newImg.src = url;
    }
}

function removeMarker(name) {
    var overlay = markerOverlays[name];
    if(overlay) {
        map.removeOverlay(overlay);
        delete markerOverlays[name];
    }
    var img = document.getElementById(name);
    if(img){
       img.parentNode.removeChild(img);
    }
}

function moveMarker(name,latitude,longitude) {
    var overlay = markerOverlays[name];
    if(overlay) {
        overlay.setPosition(cFromWGS84([longitude,latitude]));
    }
}
