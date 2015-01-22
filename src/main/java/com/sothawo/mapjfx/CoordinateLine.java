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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * A sequence of Coordinate objects which is drawn on the map as a line connecting the coordinates.
 *
 * @author P.J.Meisch (pj.meisch@jaroso.de)
 */
public class CoordinateLine {
// ------------------------------ FIELDS ------------------------------

    /** counter for the id */
    private final static AtomicLong nextId = new AtomicLong(1);
    /** unique id for this object */
    private final String id;

    /** the coordinates of the line */
    private final List<Coordinate> coordinates = new ArrayList<>();

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Creates a CoordinateLine for the given coordinates.
     *
     * @param coordinates
     *         the coordinate objects are copied into an internal ist, so that modifying the passed argument list will
     *         not modify this object.
     * @throws java.lang.NullPointerException
     *         if coordinates is null
     */
    public CoordinateLine(List<? extends Coordinate> coordinates) {
        this.id = "coordinateline-" + nextId.getAndIncrement();
        requireNonNull(coordinates).stream().forEach(this.coordinates::add);
    }

    /**
     * Creates a CoordinateLine for the given coordinates.
     *
     * @param coordinates
     *         the coordinate objects are copied into an internal ist, so that modifying the passed argument list will
     *         not modify this object.
     * @throws java.lang.NullPointerException
     *         if coordinates is null
     */
    public CoordinateLine(Coordinate... coordinates) {
        this(Arrays.asList(requireNonNull(coordinates)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoordinateLine that = (CoordinateLine) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
// --------------------- GETTER / SETTER METHODS ---------------------

    public String getId() {
        return id;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public String toString() {
        return "CoordinateLine{" +
                "id='" + id + '\'' +
                ", #coordinates=" + coordinates.size() +
                '}';
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * @return the coordinates as stream.
     */
    public Stream<Coordinate> getCoordinateStream() {
        return coordinates.stream();
    }
}
