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

// to store coordinateLine objects with a name
var coordinateLines = {};

// to store the marker objects with a name
var markers = {};

alert('beforer map');

/*******************************************************************************************************************
  map and handlers
 */
var map = new ol.Map({
    /*
    // hack needed for ol 3.1.1, not necessary in 3.4.0
    controls: [
        new ol.control.Zoom({
            zoomInLabel: '+',
            // en-dash instead of standard \u2212 minus, this renders as '2' since ol 3.1.1
            zoomOutLabel: '\u2013'
        }),
        new ol.control.Attribution
    ],
    */
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

alert('before jsConnector');

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
        view.fit(extent, map.getSize());
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
        var coordinateLine = coordinateLines[name];
        if(coordinateLine && !coordinateLine.getOnMap()) {
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
    hideCoordinateLine: function(name) {
        var coordinateLine = coordinateLines[name];
        if(coordinateLine && coordinateLine.getOnMap()) {
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
    removeCoordinateLine: function(name) {
        if(coordinateLines[name]) {
            this.hideCoordinateLine(name);
            delete coordinateLines[name];
            javaConnector.debug("deleted CoordinateLine object named " + name);
        }
    },

    /** adds a marker to the map
     * @param {string} the name of the marker
     * @param {string} url the url of the marker's image
     * @param {number} the latitude of the marker's position
     * @param {number} the longitude of the marker's position
     * @param {number} x-offset of the top left point of the mage to the coordinate
     * @param {number} y-offset of the top left point of the mage to the coordinate
     */
    addMarker: function(name, url, latitude, longitude, offsetX, offsetY) {
        var marker = markers[name];
        if(!marker) {
            marker = new Marker(name, cFromWGS84([longitude,latitude]));
            javaConnector.debug('created Marker object named ' + name);

            // add a new <img> element to <div id='markers'>
            var markersElement = document.getElementById('markers');
            var imgElement = document.createElement('img');
            markersElement.appendChild(imgElement);

            imgElement.setAttribute('id', name);
            imgElement.setAttribute('alt', name);
            imgElement.setAttribute('draggable', 'false');
            imgElement.ondragstart = function() {
                return false;
            }
            imgElement.onload = function() {
                javaConnector.debug('image loaded from ' + url);
            };
            imgElement.onerror = function() {
                javaConnector.debug('image load error from ' + url);
            };
            imgElement.src = url;
            javaConnector.debug('started loading img from ' + url);
            
            var overlay = new ol.Overlay({
                offset: [offsetX, offsetY],
                position: undefined,
                element: imgElement
            });
            marker.setOverlay(overlay);
			map.addOverlay(overlay);
			
            markers[name] = marker;
        }
    },

    /**
     * moves a marker to a new position
     * @param {string} the name of the marker
     * @param {number} the latitude of the new position
     * @param {number} the longitude of the new position
     */
    moveMarker: function(name,latitude,longitude) {
        var marker = markers[name];
        if(marker){
	        marker.setPosition(cFromWGS84([longitude,latitude]));
	        if(marker.getOnMap()) {
	            var overlay = marker.getOverlay();
	            if(overlay) {
	                overlay.setPosition(marker.getPosition());
	            }
            }
            javaConnector.debug('moved marker ' + name);
        }
    },

    /**
     * removes a marker from the map
     * @param {string} the name of the marker
     */
    removeMarker: function(name) {
        var marker = markers[name];
        if(marker) {
            this.hideMarker(name);
            var overlay = marker.getOverlay();
            map.removeOverlay(overlay);
            var imgElement = overlay.getElement();
            if(imgElement){
                delete imgElement;
            }
            delete overlay;            
            delete markers[name];
            javaConnector.debug("deleted Marker object named " + name);
        }
    },

    /**
     * hides a marker from the map. the overlay is deleted from the map - not from the marker object -  and the img
     * element is moved to the markers div which is not displayed
     * @param {string} the name of the marker
     */
    hideMarker: function(name) {
        var marker = markers[name];
        if(marker && marker.getOnMap()) {
            var overlay = marker.getOverlay();
            if(overlay) {
                overlay.setPosition(undefined);
            }
            marker.setOnMap(false);
            javaConnector.debug("hid marker " + name);
        }
    },

    /**
     * shows a marker on the map
     * @param {string} the name of the marker
     */
    showMarker: function(name) {
        var marker = markers[name];
        if(marker && !marker.getOnMap()) {
            var overlay = marker.getOverlay();
            if(overlay) {
                overlay.setPosition(marker.getPosition());
            }
            marker.setOnMap(true);
            javaConnector.debug("showed marker " + name);
        }
    }
}

alert('after jsCOnnector');

/**
 * @return the one and only jsConnector object
 */
function getJsConnector() {
    return jsConnector;
}

