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

import javafx.beans.property.SimpleObjectProperty;

import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A marker in the map. A marker has a position where it is displayed, an image URL for the image to be displayed and
 * offset values for x and y. The image should be in a format supporting transparency that can be rendered by JavaFX8
 * WebView. The image is rendered with it's top left point at the coordinate. This can be adjusted by setting the pixel
 * offset values, x positive to the right, y positive down.<br><br>
 *
 * The image URL and offset values can only be set at construction time, the coordinate is a JavaFX property.<br><br>
 *
 * A marker has a unique (within class existence in the VM) id of the form "marker-NNN" where NNN is a consecutive
 * number assigned on creation.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class Marker {
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
    private SimpleObjectProperty<Coordinate> position = new SimpleObjectProperty<>();

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * constructs an object with the given URL and offset values set to 0.
     *
     * @param imageURL
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
     */
    public Marker(URL imageURL, int offsetX, int offsetY) {
        if (null == imageURL) {
            throw new IllegalArgumentException();
        }
        this.id = "marker-" + nextId.getAndIncrement();

        this.imageURL = imageURL;
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

        if (!id.equals(marker.id)) return false;

        return true;
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
                '}';
    }

// -------------------------- OTHER METHODS --------------------------

    public Coordinate getPosition() {
        return position.get();
    }

    public SimpleObjectProperty<Coordinate> positionProperty() {
        return position;
    }

    public void setPosition(Coordinate position) {
        this.position.set(position);
    }
}
