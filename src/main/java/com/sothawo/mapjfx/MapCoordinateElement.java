/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.mapjfx;

import javafx.beans.property.SimpleObjectProperty;

/**
 * Common base class for elements on the map that have a defined position on the map.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public abstract class MapCoordinateElement extends MapElement {
// ------------------------------ FIELDS ------------------------------

    /** the coordinate */
    protected final SimpleObjectProperty<Coordinate> position = new SimpleObjectProperty<>();
    /** horizontal offset */
    protected final int offsetX;
    /** the vertical offset */
    protected final int offsetY;

// --------------------------- CONSTRUCTORS ---------------------------

    public MapCoordinateElement() {
        this(0, 0);
    }

    public MapCoordinateElement(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public String toString() {
        return "MapCoordinateElement{" +
                "position=" + position +
                ", offsetX=" + offsetX +
                ", offsetY=" + offsetY +
                "} " + super.toString();
    }

// -------------------------- OTHER METHODS --------------------------

    public Coordinate getPosition() {
        return position.get();
    }

    public SimpleObjectProperty<Coordinate> positionProperty() {
        return position;
    }

    /**
     * sets the marker's new position
     *
     * @param position
     *         new position
     * @return this object
     */
    public MapCoordinateElement setPosition(Coordinate position) {
        this.position.set(position);
        return this;
    }
}
