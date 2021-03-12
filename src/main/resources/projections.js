/*
   Copyright 2018-2021 Peter-Josef Meisch (pj.meisch@sothawo.com)

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

function Projections() {
    this.openlayers = 'EPSG:3857';
    this.mapjfx = 'EPSG:4326';
}

Projections.prototype.cToWGS84 = function (c) {
    return ol.proj.transform(c, this.openlayers, this.mapjfx)
};

Projections.prototype.cFromWGS84 = function (c) {
    return ol.proj.transform(c, this.mapjfx, this.openlayers)
};

Projections.prototype.eToWGS84 = function (e) {
    return ol.proj.transformExtent(e, this.openlayers, this.mapjfx)
};

Projections.prototype.eFromWGS84 = function (e) {
    return ol.proj.transformExtent(e, this.mapjfx, this.openlayers)
};

