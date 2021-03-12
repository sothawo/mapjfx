/*
 * Copyright 2021  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sothawo.mapjfx;

import javafx.scene.paint.Color;

import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.*;

/**
 * MapCircle object to show circle
 *
 * @author HW Kim(hanwoo.kim@forsitenz.com)
 */
public class MapCircle extends MapElement {

    /** default color: dodgerblue slightly transparent */
    public static final Color DEFAULT_COLOR = Color.web("#32CD32", 0.7);
    /** default color: dark orange transparent */
    public static final Color DEFAULT_FILL_COLOR = Color.web("#ff8c00", 0.3);
    /** default width 3 */
    public static final int DEFAULT_WIDTH = 3;

    /** counter for creating the id */
    private static final AtomicLong nextId = new AtomicLong(1);

    /** unique id for this object */
    private final String id;

    /** the coordinates of the center */
    private Coordinate centerCoord;

    private double radiusInMeter;

    /** color of the line */
    private Color color;

    /** fill color of the line, only relevant when the line is closed */
    private Color fillColor;

    /** width of the line */
    private int width;

    public MapCircle(Coordinate centerCoord, double radiusInMeter) {
        this.id = "mapcircle-" + nextId.getAndIncrement();

        this.centerCoord = centerCoord;
        this.radiusInMeter = radiusInMeter;

        this.color = DEFAULT_COLOR;
        this.fillColor = DEFAULT_FILL_COLOR;
        this.width = DEFAULT_WIDTH;
    }

    public Coordinate getCenter() {
        return this.centerCoord;
    }

    public double getRadius() {
        return this.radiusInMeter;
    }

    public Color getColor() {
        return color;
    }

    /**
     * sets the new color for stroke line. when changing color, the MapCircle must be removed and re-added to the map in order to
     * make the change visible.
     *
     * @param color
     *     the new color
     * @return this object
     * @throws NullPointerException
     *     when color is null
     */
    public MapCircle setColor(final Color color) {
        this.color = requireNonNull(color);
        return this;
    }

    public Color getFillColor() {
        return fillColor;
    }

    /**
     * sets the new fill color. when changing color, the MapCircle must be removed and re-added to the map in order to
     * make the change visible.
     *
     * @param color
     *     the new color
     * @return this object
     * @throws NullPointerException
     *     when color is null
     */
    public MapCircle setFillColor(final Color color) {
        this.fillColor = requireNonNull(color);
        return this;
    }

    public String getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    /**
     * sets the width
     *
     * @param width
     *     the new width
     * @return this object
     */
    public MapCircle setWidth(final int width) {
        this.width = width;
        return this;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MapCircle that = (MapCircle) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public String toString() {
        return "MapCircle{" +
            "id='" + id + '\'' +
            ", center=" + centerCoord +
            ", radius=" + radiusInMeter +
            ", color=" + color +
            ", fillCclor=" + fillColor +
            ", width=" + width +
            "} " + super.toString();
    }

    @Override
    public MapCircle setVisible(boolean visible) {
        return (MapCircle) super.setVisible(visible);
    }
}
