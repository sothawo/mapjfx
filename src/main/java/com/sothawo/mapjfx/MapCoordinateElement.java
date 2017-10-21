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
package com.sothawo.mapjfx;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

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
    /** custom css style name. */
    protected SimpleStringProperty cssClass = new SimpleStringProperty("");

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

    public String getCssClass() {
        return cssClass.get();
    }

    /**
     * sets the cssClass for the Label
     *
     * @param cssClass
     *         class name
     * @return this object
     */
    public MapCoordinateElement setCssClass(final String cssClass) {
        this.cssClass.set((null == cssClass) ? "" : cssClass);
        return this;
    }

    public SimpleStringProperty cssClassProperty() {
        return cssClass;
    }

    /**
     * @return the marker's id
     */
    public abstract String getId() ;

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
