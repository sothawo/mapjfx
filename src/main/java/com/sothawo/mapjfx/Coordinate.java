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
 * Coordinates with longitude and latitude values. and an optional label. Immutable objects.
 *
 * @author P.J.Meisch (pj.meisch@jaroso.de)
 */
public final class Coordinate {
// ------------------------------ FIELDS ------------------------------

    /** latitude value */
    private final Double latitude;
    /** longitude value */
    private final Double longitude;
    /** label */
    private final String label;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * creates a coordinate with no label
     *
     * parameter order lat/lon according to EPSG:4326 spec.
     *
     * @param latitude
     * @param longitude
     * @throws java.lang.IllegalArgumentException
     *         if either value is null
     */
    public Coordinate(Double latitude, Double longitude) {
        this(latitude, longitude, null);
    }

    /**
     * creates a coordinate with a label label
     *
     * parameter order lat/lon according to EPSG:4326 spec.
     *
     * @param latitude
     * @param longitude
     * @param label
     * @throws java.lang.IllegalArgumentException
     *         if either value is null
     */
    public Coordinate(Double latitude, Double longitude, String label) {
        if (null == latitude || null == longitude) {
            throw new IllegalArgumentException();
        }
        this.latitude = latitude;
        this.longitude = longitude;
        this.label = label;
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

        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (!latitude.equals(that.latitude)) return false;
        if (!longitude.equals(that.longitude)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = latitude.hashCode();
        result = 31 * result + longitude.hashCode();
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "[" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ((null != label) ? (", '" + label + '\'') : "") +
                ']';
    }
}
