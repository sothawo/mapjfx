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
package com.sothawo.mapjfx;

/**
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
public enum Projection {
    EPSG_3857("EPSG:3857"),
    WEB_MERCATOR("EPSG:3857"),
    EPSG_4326("EPSG:4326"),
    WGS_84("EPSG:4326");

    private final String olName;

    Projection(final String name) {
        olName = name;
    }

    public String getOlName() {
        return olName;
    }}
