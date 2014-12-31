/*
 * (c) 2014 P.J. Meisch (pj.meisch@sothawo.com).
 */
package com.sothawo.mapjfx;

import java.util.Arrays;
import java.util.Collection;

/**
 * An extent defines an area by two coordinates: min latitude/min longitude and may latitude/max longitude
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
     * @throws java.lang.IllegalArgumentException
     *         when less than 2 coordinates or null are passed in
     */
    public static Extent forCoordinates(Coordinate... coordinates) {
        if (null == coordinates) {
            throw new IllegalArgumentException();
        }
        return forCoordinates(Arrays.asList(coordinates));
    }

    /**
     * creates the extent of the given coordinates.
     *
     * @param coordinates
     *         the coordinates
     * @throws java.lang.IllegalArgumentException
     *         when less than 2 coordinates or null are passed in
     */
    public static Extent forCoordinates(Collection<Coordinate> coordinates) {
        if (null == coordinates || coordinates.size() < 2) {
            throw new IllegalArgumentException();
        }
        double minLatitude = Double.MAX_VALUE;
        double maxLatitude = Double.MIN_VALUE;
        double minLongitude = Double.MAX_VALUE;
        double maxLongitude = Double.MIN_VALUE;

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
     */
    public Extent(Coordinate min, Coordinate max) {
        if (null == min || null == max) {
            throw new IllegalArgumentException();
        }
        this.min = min;
        this.max = max;
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

        if (!max.equals(extent.max)) return false;
        if (!min.equals(extent.min)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = min.hashCode();
        result = 31 * result + max.hashCode();
        return result;
    }
}
