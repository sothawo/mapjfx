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

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.requireNonNull;

/**
 * A label in the map. A label has a position where it is displayed, a text to be displayed and offset values for x and
 * y. The label is rendered with it's top left point at the coordinate. This can be adjusted by setting the pixel offset
 * values, x positive to the right, y positive down.<br><br>
 *
 * The label text and offset values can only be set at construction time, the coordinate is a JavaFX property. The Label
 * has a visibility property which must be set to true to make the label visible. With this property it is possible to
 * hide the label without completely removing it from the map.<br><br>
 *
 * A label has a unique (within class existence in the VM) id of the form "label-NNN" where NNN is a consecutive number
 * assigned on creation.
 *
 * A label on the map has a css class namedn mapview-label. In addition to that an additional class for the lable can be
 * set with the #setCssClass(String) method. This method must be called before a Label object is added to a MapView. If
 * it is changed afterwards, the Label must be removed and readded.
 *
 * A Label may be attached to a Marker. If it is, the Label is shown/hidden/moved/removed together with the Marker. Any
 * attempt to do these operations directly on the Label are ignored.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class MapLabel extends MapCoordinateElement {
// ------------------------------ FIELDS ------------------------------

    private final static AtomicLong nextId = new AtomicLong(1);

    /** the id */
    private final String id;

    /** the label text */
    private final String text;

    /** custom css style name. */
    private String cssClass = "";

    /** the Marker this Label is attached to. */
    private Optional<Marker> optMarker = Optional.empty();

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * creates a Label with the given text and the offset values set to 0, 0.
     *
     * @param text
     *         label text
     */
    public MapLabel(String text) {
        this(text, 0, 0);
    }

    /**
     * creates a Label with the given text and offset values.
     *
     * @param text
     *         label text
     * @param offsetX
     *         horizontal offset, positive to the right
     * @param offsetY
     *         vertical offset, positive down
     * @throws NullPointerException
     *         if text is null
     * @throws IllegalArgumentException
     *         if text is empty
     */
    public MapLabel(String text, int offsetX, int offsetY) {
        super(offsetX, offsetY);
        this.text = requireNonNull(text);
        if (text.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.id = "label-" + nextId.getAndIncrement();
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getCssClass() {
        return cssClass;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapLabel mapLabel = (MapLabel) o;
        return Objects.equals(id, mapLabel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Label{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                "} " + super.toString();
    }

// -------------------------- OTHER METHODS --------------------------

    public Optional<Marker> getMarker() {
        return optMarker;
    }

    /**
     * sets the cssClass for the Label
     *
     * @param cssClass
     *         class name
     * @return this object
     */
    public MapLabel setCssClass(String cssClass) {
        this.cssClass = (null == cssClass) ? "" : cssClass;
        return this;
    }

    /**
     * sets the Marker this Label is attached to. Package scope as it should only be called from the Marker.
     */
    void setMarker(Marker marker) {
        optMarker = Optional.ofNullable(marker);
    }

    @Override
    public MapLabel setPosition(Coordinate position) {
        return (MapLabel) super.setPosition(position);
    }

    @Override
    public MapLabel setVisible(boolean visible) {
        return (MapLabel) super.setVisible(visible);
    }

}
