/*
   Copyright 2015 Peter-Josef Meisch (pj.meisch@sothawo.com)

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

/**
 * CoordinateLine object. A CoordinateLine object contains an array of coordinates which in turn are an array of two numbers.
 * Internally the coordinates are stored in longitude/latitude order, as this is the order expected by OpenLayers.
 */

/**
 * @constructor
 */
function CoordinateLine() {
    this.coordinates = [];
    this.feature = null;
    this.onMap = false;
}

/**
 * @returns {array} the coordinates of this CoordinateLine. Coordinates are in longitude/latitude order.
 */
CoordinateLine.prototype.getCoordinates = function() {
    return this.coordinates;
};

/**
 * adds a coordinate to the coordinates array
 * @param {number} latitude value in WGS84
 * @param {number} longitude value in WGS84
 */
CoordinateLine.prototype.addCoordinate = function(latitude, longitude) {
    // lat/lon reversion
    this.coordinates.push(cFromWGS84([longitude, latitude]));
};

/**
 * finishes construction of the object and builds the OL Feature based in the coordinates that were set
 */
CoordinateLine.prototype.seal = function() {
    this.feature = new ol.Feature(new ol.geom.LineString(this.coordinates));
};

/**
 * gets the feature for OpenLayers map
 * @return {ol.Feature}
 */
CoordinateLine.prototype.getFeature = function() {
    return this.feature;
};

/**
 * sets the flag wether the feature is shown on the map
 *
 * @param {boolean}
 */
CoordinateLine.prototype.setOnMap = function(flag) {
    this.onMap = flag;
};

/**
 * gets the flag wether the feature is visible on the map
 * @return {boolean}
 */
CoordinateLine.prototype.getOnMap = function() {
    return this.onMap;
};
