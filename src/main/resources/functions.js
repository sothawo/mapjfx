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

// coordinate and extent transformation
function cToWGS84(c) {return ol.proj.transform(c, 'EPSG:3857', 'EPSG:4326')}
function cFromWGS84(c) {return ol.proj.transform(c, 'EPSG:4326', 'EPSG:3857')}
function eToWGS84(e) {return ol.proj.transformExtent(e, 'EPSG:3857', 'EPSG:4326')}
function eFromWGS84(e) {return ol.proj.transformExtent(e, 'EPSG:4326', 'EPSG:3857')}
