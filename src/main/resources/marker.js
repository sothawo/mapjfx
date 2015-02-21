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
 */
function Marker() {
    this.coordinate = [];
    this.onMap = false;
}

/**
 * @returns {array} the coordinate of this Marker. Coordinates are in longitude/latitude order.
 */
Marker.prototype.getCoordinate = function() {
    return this.coordinate;
};

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
