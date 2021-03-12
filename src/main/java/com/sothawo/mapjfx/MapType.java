/*
 Copyright 2014-2021 Peter-Josef Meisch (pj.meisch@sothawo.com)

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
package com.sothawo.mapjfx;

/**
 * Enumeration defining the different map types.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 * @author Erik Jähne
 */
public enum MapType {
    /** OpenStreetMap. */
    OSM,
    /** Stamen Watercolor. */
    STAMEN_WC,
    /** BingMaps Road. */
    BINGMAPS_ROAD,
    /** BingMaps Road as a grayscale version */
    BINGMAPS_CANVAS_GRAY,
    /** BingMaps Road as a dark version*/
    BINGMAPS_CANVAS_DARK,
    /** BingMaps Road as a lighter version*/
    BINGMAPS_CANVAS_LIGHT,
    /** BingMaps Aerial. */
    BINGMAPS_AERIAL,
    /** BingMaps Aerial with labels. */
    BINGMAPS_AERIAL_WITH_LABELS,
    /** custom WMS server. */
    WMS,
    /** custom Map server with XYZ support. */
    XYZ
}
