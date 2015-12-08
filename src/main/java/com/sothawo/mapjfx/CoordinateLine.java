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

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * A sequence of Coordinate objects which is drawn on the map as a line connecting the coordinates. It has a visible
 * property that enables to switch the visibility on the map off without removing the CoordinateLine frmthe map.
 * Invisible CoordinateLines can easily be switched to visiblae again.
 *
 * @author P.J.Meisch (pj.meisch@jaroso.de)
 */
public class CoordinateLine extends MapElement {
// ------------------------------ FIELDS ------------------------------

    /** default color: dodgerblue slightly transparent */
    public static final Color DEFAULT_COLOR = Color.web("#32CD32", 0.7);
    /** default width 3 */
    public static final int DEFAULT_WIDTH = 3;

    /** counter for creating the id */
    private final static AtomicLong nextId = new AtomicLong(1);
    /** unique id for this object */
    private final String id;
    /** the coordinates of the line */
    private final List<Coordinate> coordinates = new ArrayList<>();
    /** color of the line */
    private Color color;
    /** width of the line */
    private int width;

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
        // slightly transparent limegreen
        this.color = DEFAULT_COLOR;
        this.width = DEFAULT_WIDTH;
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

// --------------------- GETTER / SETTER METHODS ---------------------

    public Color getColor() {
        return color;
    }

    public String getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

// ------------------------ CANONICAL METHODS ------------------------

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

    @Override
    public String toString() {
        return "CoordinateLine{" +
                "id='" + id + '\'' +
                ", coordinates=" + coordinates +
                ", color=" + color +
                ", width=" + width +
                "} " + super.toString();
    }
// -------------------------- OTHER METHODS --------------------------

    /**
     * @return the coordinates as stream. The coordinates are only available as stream, this prevents modification of
     * the internal list.
     */
    public Stream<Coordinate> getCoordinateStream() {
        return coordinates.stream();
    }

    /**
     * sets the new color. when changing color, the CoordinateLine must be removed and re-added to the map in order to
     * make the change visible.
     *
     * @param color
     *         the new color
     * @return this object
     * @throws java.lang.NullPointerException
     *         when color is null
     */
    public CoordinateLine setColor(Color color) {
        this.color = requireNonNull(color);
        return this;
    }

    @Override
    public CoordinateLine setVisible(boolean visible) {
        return (CoordinateLine) super.setVisible(visible);
    }

    /**
     * sets the width
     *
     * @param width
     *         the new width
     * @return this object
     */
    public CoordinateLine setWidth(int width) {
        this.width = width;
        return this;
    }
}
