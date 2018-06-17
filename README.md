# mapjfx

mapjfx provides a JavaFX8 region containing a map, allowing to zoom, pan, and use markers.

It uses [OpenLayers](http://openlayers.org) as the map technology.
More Information about the project can be found at [the sothawo website](http://www.sothawo.com/projects/mapjfx/).

## license

 Copyright 2014-2018 Peter-Josef Meisch (pj.meisch@sothawo.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

## building the library

this project is built using mvn. The library jar is created by running `mvn package`

## build status on travis-ci

[![Build Status](https://travis-ci.org/sothawo/mapjfx.svg?branch=master)](https://travis-ci.org/sothawo/mapjfx)

## running the test application

the project contains a test application which is mostly used during development. The source is found in the test
sources (src/test/java). It is compiled and executed with the following command (no need to build the library first):

`mvn test exec:java -Dexec.mainClass="com.sothawo.mapjfx.TestApp" -Dexec.classpathScope=test`

The test application does not contain all the features, to have that, check
 [mapjfx-demo](http://www.sothawo.com/projects/mapjfx-demo/)

## version history

## current

## 1.18.0

* add MAP_POINTER_MOVED event
* OfflineCache is now a Singleton, so one aplication can have multiple maps and have them use a common cache.

## 1.17.0

* report changes in the map's extent
* use OpenLayers 4.6.5

### 1.16.1

* use java 9 on travis for building (source and target class version is still set to 1.8)
* add Automatic-Module-Name to generated manifest

### 1.16.0

* added stamen watercolor type as additional map type
* moved deployment to travis build

### 1.15.0

* use OpenLayers 4.6.4

### 1.14.0

* added MARKER_ENTERED, MARKER_EXITED, MAPLABEL_ENTERED, MAPLABEL_EXITED events 
(thanks to [skinkie](https://github.com/skinkie))
* MapLabel.setCssClass(String) now is observed and changes a MapLabel's style directly on the map. 
* use OpenLayers 4.4.0

### 1.13.2

* use OpenLayers 4.3.2

### 1.13.1

* use OpenLayers 4.2.0

### 1.13.0

* add possibility to defined filters for excluding URLs from caching

### 1.12.2

* do not use offline cache as default.

### 1.12.1

* fixes to the caching code

### 1.12.0

* added extent selection by cmd-drag (Mac OSX) / ctrl-drag  (Windows)

### 1.11.0

* use OpenLayers 4.0.1

### 1.10.0

* added support for WMS servers

### 1.9.0

* use OpenLayers 3.20.1
* added mousedown, mouseup, doubleclick and rightclick events to labels and markers

### 1.8.2

* use OpenLayers 3.19.2

### 1.8.1

* use OpenLayers 3.18.2

### 1.8.0

* added events for clicks on markers and labels.
* removed `CoordinateEvent`, replaced by the classes in the _com.sothawo.mapjfx.event_ package
* bugfixes and improvements

### 1.7.3

* use OpenLayers 3.17.1
* removed MapQuest see [ol issue #5484](https://github.com/openlayers/ol3/issues/5484)

### 1.7.2

* fixed faulty implementation of Extent fields.

### 1.7.1

* added possibility to clear the cache directory
* improved handling of openstreetmap.org tiles

### 1.7.0


* implemented caching of the files loaded from the web (map images) for faster access and offline usage.

### 1.6.2

* use OpenLayers 3.14.2

### 1.6.1

* use OpenLayers 3.13.1

### 1.6.0

* added Label objects to the MapView

### 1.5.1

* use OpenLayers 3.11.2

### 1.5.0

* new MapTypes supported: BingMaps Road and BingMaps Aerial (BingMaps API key needed, 
see [BingMaps Portal](https://www.bingmapsportal.com)

### 1.4.9

* use OpenLayers 3.10.0

### 1.4.8

* the fix in 1.4.7 did not work in Windows. Fixed in 1.4.8.

### 1.4.7

* fix for WebView not loading css and js files when html is loaded from a jar; introduced with JDK 1.8u60.

### 1.4.6

* update to OpenLayers 3.9.0

### 1.4.4

* update to OpenLayers 3.4.0

### 1.4.3

* fixed potential memory leaks
* internal reimplementation of Markers

### 1.4.2

* fix for interruption of 1.4.1 release process

### 1.4.1

* Bugfix: wenn MapType was set before the MapView was initialized, the setting was ignored

### 1.4.0

* display of lines/tracks

### 1.3.3

* fixed javadoc errors

### 1.3.2

* internal code review and cleanup
* switched to OpenLayers 3.1.1
* marker images cannot be dragged anymore

### 1.3.1

* open the links from the OpenLayers attributions in the default browser
* removed WebView context menu from MapView

### 1.3.0

* added marker support
* added singleclick feedback as JavaFX event with map coordinate

### 1.2.0

* set the map's extent, so that a collection of coordinates is visible
* API modifications for fluent interface
* possibility to switch between map types
* added MapType enum and property

### 1.1.1

* animationDuration type changed to int

### 1.1.0

* removed unnecessary code
* reworked MapView constructor to enable SceneBuilder compatibility
* removed slf4j-api dependency and switched to java logging to enable SceneBuilder compatibility

### 1.0.2

* readme correction

### 1.0.1

* javadoc corrections

### 1.0.0

#### new features:

* display a map in a JavaFX8 region
* center of map is provided as JavaFX property
* zoom of map is provided as JavaFX property
* optional animation of center and zoom changes when the properties are changed


