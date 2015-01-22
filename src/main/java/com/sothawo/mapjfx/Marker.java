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
import javafx.beans.property.SimpleObjectProperty;

import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.requireNonNull;

/**
 * A marker in the map. A marker has a position where it is displayed, an image URL for the image to be displayed and
 * offset values for x and y. The image should be in a format supporting transparency that can be rendered by JavaFX8
 * WebView. The image is rendered with it's top left point at the coordinate. This can be adjusted by setting the pixel
 * offset values, x positive to the right, y positive down.<br><br>
 *
 * The image URL and offset values can only be set at construction time, the coordinate is a JavaFX property. The Marker
 * has a visibilty property whch must be set to true to make the marker visible. With this property it is possible to
 * hide the marker without completely removing it from the map.<br><br>
 *
 * A marker has a unique (within class existence in the VM) id of the form "marker-NNN" where NNN is a consecutive
 * number assigned on creation.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public final class Marker {
// ------------------------------ FIELDS ------------------------------

    private final static AtomicLong nextId = new AtomicLong(1);
    /** the id */
    private final String id;
    /** the image URL */
    private final URL imageURL;
    /** horizontal offset */
    private final int offsetX;
    /** the vertical offset */
    private final int offsetY;
    /** the coordinate */
    private final SimpleObjectProperty<Coordinate> position = new SimpleObjectProperty<>();
    /** vivible property */
    private final SimpleBooleanProperty visible = new SimpleBooleanProperty(false);

// -------------------------- STATIC METHODS --------------------------

    /**
     * return a provided Marker with the given color.
     *
     * @param provided
     *         desired color
     * @return Marker
     * @throws NullPointerException
     *         when provided is null
     */
    public static Marker createProvided(Provided provided) {
        requireNonNull(provided);
        return new Marker(Marker.class.getResource("/markers/" + provided.getFilename()), provided.getOffsetX(),
                provided.getOffsetY());
    }

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * constructs an object with the given URL and offset values set to 0.
     *
     * @param imageURL
     *         the image URL
     */
    public Marker(URL imageURL) {
        this(imageURL, 0, 0);
    }

    /**
     * constructs a Marker with the given values.
     *
     * @param imageURL
     *         image URL
     * @param offsetX
     *         horizontal pixel offset
     * @param offsetY
     *         vertical pixel offset
     * @throws java.lang.NullPointerException
     *         if imageURL is null
     */
    public Marker(URL imageURL, int offsetX, int offsetY) {
        this.id = "marker-" + nextId.getAndIncrement();
        this.imageURL = requireNonNull(imageURL);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * @return the marker's id
     */
    public String getId() {
        return id;
    }

    public URL getImageURL() {
        return imageURL;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Marker marker = (Marker) o;

        return id.equals(marker.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Marker{" +
                "id='" + id + '\'' +
                ", imageURL=" + imageURL +
                ", offsetX=" + offsetX +
                ", offsetY=" + offsetY +
                ", position=" + getPosition() +
                ", visible=" + getVisible() +
                '}';
    }

    public Coordinate getPosition() {
        return position.get();
    }

    public boolean getVisible() {
        return visible.get();
    }

// -------------------------- OTHER METHODS --------------------------

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
    public Marker setPosition(Coordinate position) {
        this.position.set(position);
        return this;
    }

    /**
     * sets the visibilty of the marker
     *
     * @param visible
     *         visibilty
     * @return this object
     */
    public Marker setVisible(boolean visible) {
        this.visible.set(visible);
        return this;
    }

    public SimpleBooleanProperty visibleProperty() {
        return visible;
    }

// -------------------------- ENUMERATIONS --------------------------

    /**
     * provided Markers. contains the filename and the offsets as well.
     */
    public static enum Provided {
        BLUE("blue_map_marker.png", -32, -64),
        GREEN("green_map_marker.png", -32, -64),
        ORANGE("orange_map_marker.png", -32, -64),
        RED("red_map_marker.png", -32, -64);

// ------------------------------ FIELDS ------------------------------

        /** the filename of the marker image */
        private final String filename;
        /** offset x */
        private final int offsetX;
        /** offset y */
        private final int offsetY;

// --------------------------- CONSTRUCTORS ---------------------------

        Provided(String filename, int offsetX, int offsetY) {
            this.filename = filename;
            this.offsetX = offsetX;

            this.offsetY = offsetY;
        }

// --------------------- GETTER / SETTER METHODS ---------------------

        public String getFilename() {
            return filename;
        }

        public int getOffsetX() {
            return offsetX;
        }

        public int getOffsetY() {
            return offsetY;
        }
    }
}
