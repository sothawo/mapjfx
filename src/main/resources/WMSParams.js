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
 * WMSParams contain the necessary information to load data from a WMS tile server.
 */

/**
 * @constructor
 *
 */
function WMSParams() {
    this.url = '';
    this.params = {};
}

/**
 * sets the url.
 *
 * @param u {string}
 */
WMSParams.prototype.setUrl = function (u) {
    this.url = u;
};

/**
 * gets the url.
 * @return {string}
 */
WMSParams.prototype.getUrl = function () {
    return this.url;
};

/**
 * adds an parameter
 * @param key {string} parameter key
 * @param  value {string} parameter value
 */
WMSParams.prototype.addParam = function (key, value) {
    this.params[key] = value;
};

/**
 * gets the params object.
 * @return {object} the params object
 */
WMSParams.prototype.getParams = function () {
    return this.params;
};
