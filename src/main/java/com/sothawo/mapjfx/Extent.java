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
package com.sothawo.mapjfx;

import java.util.Arrays;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

/**
 * An extent defines an area by two coordinates: min latitude/min longitude and max latitude/max longitude.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public final class Extent {
// ------------------------------ FIELDS ------------------------------

    /** the coordinate with the min values */
    private final Coordinate min;
    /** the coordinate with the max values */
    private final Coordinate max;

// -------------------------- STATIC METHODS --------------------------

    /**
     * creates the extent of the given coordinates.
     *
     * @param coordinates
     *         the coordinates
     * @return Extent for the coorinates
     * @throws java.lang.IllegalArgumentException
     *         when less than 2 coordinates or null are passed in
     * @throws NullPointerException
     *         when coordinates is null
     */
    public static Extent forCoordinates(Coordinate... coordinates) {
        return forCoordinates(Arrays.asList(requireNonNull(coordinates)));
    }

    /**
     * creates the extent of the given coordinates.
     *
     * @param coordinates
     *         the coordinates
     * @return Extent for the coorinates
     * @throws java.lang.IllegalArgumentException
     *         when less than 2 coordinates or null are passed in
     * @throws NullPointerException
     *         when coordinates is null
     */
    public static Extent forCoordinates(Collection<? extends Coordinate> coordinates) {
        requireNonNull(coordinates);
        if (coordinates.size() < 2) {
            throw new IllegalArgumentException();
        }
        double minLatitude = Double.MAX_VALUE;
        double maxLatitude = -Double.MAX_VALUE;
        double minLongitude = Double.MAX_VALUE;
        double maxLongitude = -Double.MAX_VALUE;

        for (Coordinate coordinate : coordinates) {
            minLatitude = Math.min(minLatitude, coordinate.getLatitude());
            maxLatitude = Math.max(maxLatitude, coordinate.getLatitude());
            minLongitude = Math.min(minLongitude, coordinate.getLongitude());
            maxLongitude = Math.max(maxLongitude, coordinate.getLongitude());
        }
        return new Extent(new Coordinate(minLatitude, minLongitude), new Coordinate(maxLatitude, maxLongitude));
    }

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * private constructor. For construction use the factory methods.
     *
     * @param min
     *         coordinate with min lat/lon value
     * @param max
     *         coordinate with max lat/lon value
     * @throws java.lang.NullPointerException
     *         if either argument is null
     */
    private Extent(Coordinate min, Coordinate max) {
        this.min = requireNonNull(min);
        this.max = requireNonNull(max);
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public Coordinate getMax() {
        return max;
    }

    public Coordinate getMin() {
        return min;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Extent extent = (Extent) o;

        return max.equals(extent.max) && min.equals(extent.min);
    }

    @Override
    public int hashCode() {
        int result = min.hashCode();
        result = 31 * result + max.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Extent{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }
}
