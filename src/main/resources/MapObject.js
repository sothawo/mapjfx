/*
 Copyright 2015-2017 Peter-Josef Meisch (pj.meisch@sothawo.com)

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
 * MapObject: an object that can be hidden and reshown on the map. An MapObject object combines an OL overlay with a position
 * which is an array of two numbers. Internally the coordinates are stored in longitude/latitude order, as this is the order
 * expected by OpenLayers. It has a name, and an overlay for the element to be shown in the OL map.
 */

/**
 * @constructor
 *
 * @param {string} name of the label
 * @param {array[number]} position in OL coordinates (lon/lat)
 */
function MapObject(positionParam) {
    this.onMap = false;
    this.overlay = {};
    this.element = {};
    this.position = positionParam;
}

/**
 * sets the flag wether the label is shown on the map
 *
 * @param {boolean}
 */
MapObject.prototype.setOnMap = function (flag) {
    this.onMap = flag;
};

/**
 * gets the flag wether the label is visible on the map
 * @return {boolean}
 */
MapObject.prototype.getOnMap = function () {
    return this.onMap;
}

/**
 * sets the label's position
 * @param {array[number]} position in OL coordinates (lon/lat)
 */
MapObject.prototype.setPosition = function (coords) {
    this.position = coords;
}

/**
 * @return {array[number]} the label's position
 */
MapObject.prototype.getPosition = function () {
    return this.position;
}

/**
 * sets the MapObject's overlay
 * @param {ol.Overlay} the overlay
 */
MapObject.prototype.setOverlay = function (o) {
    this.overlay = o;
}

/**
 * @retuns {ol.Overlay} the label's image overlay
 */
MapObject.prototype.getOverlay = function () {
    return this.overlay;
}
