# mapjfx

mapjfx provides a JavaFX region containing a map, allowing to zoom, pan, and use markers.

It uses [OpenLayers](http://openlayers.org) as the map technology.
More Information about the project can be found at [the sothawo website](http://www.sothawo.com/projects/mapjfx/).

## Want to support mapjfx?

Donate via **PayPal** [![Donate](https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=PKA5VXADATSVN&source=url)

## license

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

## building the library

this project is built using mvn. The library jar is created by running `mvn package`

## Java versions and branches

* branch 3.x uses Java 17
* branch 2.x uses Java 11 (not developed further)
* branch 1.x uses Java 8   (not developed further)

Pull requests should be based on _1.x_ for changes/backports in the Java 8 version and to _2.x_ for the current version.

Releases are based on main for the 3.x branch, on _main-2.x_ for the _2.x_ branch ad on _main-1.x_ for the _1.x_ 
branch.
 
## running the test application

the project contains a test application which is mostly used during development. The application can be started from 
within an IDE by using the `com.sothawo.mapjfx.app.TestAppLauncher` class. To get the logging, the IDE should add the provided dependency to the run classpath.

to run it from the commandline it is first necessary to build the library with all dependencies as a shaded jar and 
the run the test application:

     ./mvnw -P shaded package
     java -Dlogback.configurationFile=logback.xml -jar target/mapjfx.jar

The test application does not contain all the features, to have that, check
 [mapjfx-demo](http://www.sothawo.com/projects/mapjfx-demo/)

*Note:* To use the offline cache with Java 11 and up, it is necessary to add `--add-opens java.base/java.net=com.sothawo.mapjfx` to the java call.

## current version

## 3.1.0

* MAPJFX-102 - upgrade to OpenLayers 6.7.0 
* MAPJFX-101 - upgrade to Java 17

## 3.0.1

* fix copyright year formats

## 3.0.0

* use Java 15, JFX 15.0.1

## 2.15.2

* MAPJFX-93 - Initial marker rotation.

## 2.15.1

* MAPJFX-92 - Rotate the img/label element, not the overlay.

## 2.15.0

* use Openlayers 6.4.2
* add MapCircle objects
* updates assertj, junit and slf4j versions.

### 2.14.1

* MAPJFX-82 - Map pointer event not triggered when mouse moves over a Marker.

### 2.14.0

* MAPJFX-80 - Rotate markers.

### 2.13.1

* MAPJFX-78 - fix marker and label events.

### 2.13.0

* use Java 11.0.2 (stay on LTS version)

### 2.12.0

* built with Java 13
* MAPJFX-73 - Fixed module-info
* MAPJFX-74 - Updated copyright headers.
* MAPJFX-75 - Adjust visibility of classes that need not be public

### 2.11.0

* Additional Bing Maps options.

### 2.10.2

* MAPJFX-69 - Implement cacheFilters in addition to noCacheFilters

### 2.10.1

* MAPJFX-67 offline cache parallelism

### 2.10.0

* MAPJFX-62 implement constrainExtent
* MAPJFX-66 use OpenLayers 6.0.1

### 2.9.0

* add possibility to preload URLs in the offline cache
* fix URLStreamHandler implementation
* use Java 12

### 2.8.0

* MAPJFX-59 - Hiding zoom controls.
* MAPJFX-60: Configure mapjfx with configuration object.

### 2.7.3

* MAPJFX-52 - fix context menu regression.

### 2.7.2

* MAPJFX-56: Initialization fix.

### 2.7.1

* MAPJFX-54: fix that coordinate lines were not shown on WMS
* MAPJFX-53: fix that WMS maps are not shown

### 2.7.0

* added non-interactive map mode

### 2.6.0

* added `Coordinate.normalize()` method

### 2.5.3

* removed travis-ci integration

### 2.5.2

* adapt readme

### 2.5.1

* update coypright notices

### 2.5.0

* enable map zooming with scroll wheel on markers ands labels

### 2.4.0

* added slf4j api to the libarary and logback to the testapp

### 2.3.0

* add possibility to set the map projection (either WGS_84("EPSG:4326") or WEB_MERCATOR("EPSG:3857"))

### 2.2.0

* CoordinateLines can now be closed (polygon) and have a fill color

### 2.1.1

* use OpenLayers 5.3.0

### 2.1.0

* added MapView.close() method to cleanup resources and implement AutoCloseable.

### 2.0.0

* switch to OpenJDK 11 and OpenJFX 11

### 1.19.0

* add XYZ map source (contribution from [Erik Jaehne](https://github.com/s3erjaeh))

### 1.18.0

* add MAP_POINTER_MOVED event
* OfflineCache is now a Singleton, so one application can have multiple maps and have them use a common cache.

### 1.17.0

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
