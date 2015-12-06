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
 * Label object. A Label object contains a coordinate which is an array of two numbers.
 * Internally the coordinates are stored in longitude/latitude order, as this is the order expected by OpenLayers.
 * It has a name, and an overlay for the label to be shown in the OL map.
 */

/**
 * @constructor
 *
 * @param {string} name of the label
  * @param {array[number]} position in OL coordinates (lon/lat)
*/
function Label(nameParam, positionParam) {
    this.onMap = false;
    this.name = nameParam;
    this.overlayText = {};
    this.labelElement = {};
    this.position = positionParam;
}

/**
 * @returns {string} the name of the Label.
 */
Label.prototype.getName = function() {
    return this.name;
}

/**
 * sets the flag wether the label is shown on the map
 *
 * @param {boolean}
 */
Label.prototype.setOnMap = function(flag) {
    this.onMap = flag;
}

/**
 * gets the flag wether the label is visible on the map
 * @return {boolean}
 */
Label.prototype.getOnMap = function() {
    return this.onMap;
}

/**
 * sets the label's position
 * @param {array[number]} position in OL coordinates (lon/lat)
 */
Label.prototype.setPosition = function(coords) {
	this.position = coords;
}

/**
 * @return {array[number]} the label's position
 */
Label.prototype.getPosition = function() {
	return this.position;
}

/**
 * sets the Label's text overlay
 * @param {ol.Overlay} the overlay
 */
Label.prototype.setOverlayText = function(o) {
    this.overlayText = o;
}

/**
 * @retuns {ol.Overlay} the label's image overlay
 */
Label.prototype.getOverlayText = function() {
    return this.overlayText;
}

/**
 * sets the img element
 * @param {Node} label element
 */
Label.prototype.setLabelElement = function(el) {
    this.labelElement = el;
}

/**
 * @returns {Node} the label element
 */
Label.prototype.getLabelElement = function() {
    return this.labelElement;
}
