/**
 * @constructor
 */
function MapCircle(projections, map) {
    this.coordinate = projections.cFromWGS84([0, 0]);
    this.feature = null;
    this.onMap = false;
    // default color opaque red
    this.color = [255, 0, 0, 1];
    // default fill color transparent yellow
    this.fillColor = [255, 255, 0, 0.3];
    // default width 3
    this.width = 3;
    this.radius = 0.0;
    this.projections = projections;
    this.map = map;

}

/**
 * @returns {array} the centre coordinate of this circle. Coordinates are in longitude/latitude order.
 */
MapCircle.prototype.getCenter = function () {
    return this.coordinate;
}

/**
 * set centre coordinate of this circle
 * @param {number} latitude value in WGS84
 * @param {number} longitude value in WGS84
 */
MapCircle.prototype.setCenter = function (latitude, longitude) {
    // lat/lon reversion
    this.coordinate = this.projections.cFromWGS84([longitude, latitude]);
}

/**
 * finishes construction of the object and builds the OL Feature based in the centre coordinate and radius that were set.
 */
MapCircle.prototype.seal = function () {
    this.feature = new ol.Feature({
                   		geometry: new ol.geom.Point(this.coordinate)
                   	});

    this.map.getView().on('change:resolution', (function (evt) {
        this.addCircleToFeature();
    }).bind(this));

    this.addCircleToFeature()
};

MapCircle.prototype.addCircleToFeature = function() {
    const circle = new ol.style.Circle({
        fill: new ol.style.Fill({color: this.fillColor}),
        stroke: new ol.style.Stroke({color: this.color, width: this.width}),
        points: 4,
        radius: this.radius / this.map.getView().getResolution(),
        angle: Math.PI / 4
    });

    const style = new ol.style.Style({
        image: circle
    });

    this.feature.setStyle( style );
};

/**
 * gets the feature for OpenLayers map
 * @return {ol.Feature}
 */
MapCircle.prototype.getFeature = function () {
    return this.feature;
};

/**
 * sets the flag wether the feature is shown on the map
 *
 * @param flag
 */
MapCircle.prototype.setOnMap = function (flag) {
    this.onMap = flag;
};

/**
 * gets the flag wether the feature is visible on the map
 * @return {boolean}
 */
MapCircle.prototype.getOnMap = function () {
    return this.onMap;
};

/**
 * sets the color of the stroke line.
 *
 * @param {number} red 0..255
 * @param {number} green 0..255
 * @param {number} blue 0..255
 * @param {number} alpha 0..1
 */
MapCircle.prototype.setColor = function (red, green, blue, alpha) {
    this.color = [red, green, blue, alpha];
};

/**
 * sets the fill color of the circle.
 *
 * @param {number} red 0..255
 * @param {number} green 0..255
 * @param {number} blue 0..255
 * @param {number} alpha 0..1
 */
MapCircle.prototype.setFillColor = function (red, green, blue, alpha) {
    this.fillColor = [red, green, blue, alpha];
};

/**
 * sets the width of the stroke line
 *
 * @param width
 */
MapCircle.prototype.setWidth = function (width) {
    this.width = width;
};

/**
 * sets the radius of this circle.
 * @param flag
 */
MapCircle.prototype.setRadius = function (radius) {
    this.radius = radius;
};

MapCircle.prototype.getRadius = function () {
    return this.radius;
};
