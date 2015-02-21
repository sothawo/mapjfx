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
 * Marker object. A CoordinateLine object contains a coordinate which is an array of two numbers.
 * Internally the coordinates are stored in longitude/latitude order, as this is the order expected by OpenLayers.
 * It has a name, an url for the image to be displayed and offset values for x and y to position the image in
 reference to the coordinate.
 */

/**
 * @constructor
 *
 * @param {string} name of the marker
 */
function Marker(nameParam) {
    this.onMap = false;
    this.name = nameParam;
    this.overlay = {};
    this.imgElement = {};
}

/**
 * @returns {string} the name of the Marker.
 */
Marker.prototype.getName = function() {
    return this.name;
}

/**
 * sets the flag wether the marker is shown on the map
 *
 * @param {boolean}
 */
Marker.prototype.setOnMap = function(flag) {
    this.onMap = flag;
};

/**
 * gets the flag wether the marker is visible on the map
 * @return {boolean}
 */
Marker.prototype.getOnMap = function() {
    return this.onMap;
};

/**
 * sets the Markewrs's overlay
 * @param {ol.Overlay} the overlay
 */
Marker.prototype.setOverlay = function(o) {
    this.overlay = o;
};

/**
 * @retuns {ol.Overlay} the marker's overlay
 */
Marker.prototype.getOverlay = funtion() {
    return this.overlay;
};

/**
 * sets the img element
 * @param {Node} image element
 */
Marker.prototype.setImgElement = function(el) {
    this.imgElement = el;
};

/**
 * @returns {Node} the image element
 */
Marker.prototype.getImgElement = function() {
    return this.imgElement;
};
