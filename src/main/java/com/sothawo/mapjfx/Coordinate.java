/*
 Copyright 2014 Peter-Josef Meisch (pj.meisch@sothawo.com)

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
 * Coordinates with longitude and latitude values. Immutable objects.
 *
 * @author P.J.Meisch (pj.meisch@sothawo.com)
 */
public final class Coordinate {
// ------------------------------ FIELDS ------------------------------

    /** latitude value */
    private final Double latitude;
    /** longitude value */
    private final Double longitude;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * creates a coordinate.
     *
     * parameter order lat/lon according to EPSG:4326 spec.
     *
     * @param latitude
     *         latitude of the coordinate
     * @param longitude
     *         longitude of the coordinate
     * @throws java.lang.IllegalArgumentException
     *         if either value is null
     */
    public Coordinate(Double latitude, Double longitude) {
        if (null == latitude || null == longitude) {
            throw new IllegalArgumentException();
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coordinate that = (Coordinate) o;

        if (!latitude.equals(that.latitude)) return false;
        if (!longitude.equals(that.longitude)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = latitude.hashCode();
        result = 31 * result + longitude.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return '[' +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ']';
    }
}
