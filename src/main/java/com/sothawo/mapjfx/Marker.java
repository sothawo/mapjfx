/*
 Copyright 2015-2021 Peter-Josef Meisch (pj.meisch@sothawo.com)

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

import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.*;

/**
 * A marker in the map. A marker has a position where it is displayed, an image URL for the image to be displayed and
 * offset values for x and y. The image should be in a format supporting transparency that can be rendered by JavaFX8
 * WebView. The image is rendered with it's top left point at the coordinate. This can be adjusted by setting the pixel
 * offset values, x positive to the right, y positive down.<br><br>
 *
 * The image URL and offset values can only be set at construction time, the coordinate is a JavaFX property. The Marker
 * has a visibility property which must be set to true to make the marker visible. With this property it is possible to
 * hide the marker without completely removing it from the map.<br><br>
 *
 * A marker has a unique (within class existence in the VM) id of the form "marker-NNN" where NNN is a consecutive
 * number assigned on creation.
 *
 * A Marker may have an attached Label. If it has one, the Label is shown/hidden/moved/removed together with the Marker.
 * Any attempt to do these operations directly on the Label are ignored.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public final class Marker extends MapCoordinateElement {
// ------------------------------ FIELDS ------------------------------

    private final static AtomicLong nextId = new AtomicLong(1);
    /** the id */
    private final String id;
    /** the image URL */
    private final URL imageURL;

    /** the optional attached Label. */
    private Optional<MapLabel> optMapLabel = Optional.empty();


// -------------------------- STATIC METHODS --------------------------

    /**
     * return a provided Marker with the given color.
     *
     * @param provided
     *     desired color
     * @return Marker
     * @throws NullPointerException
     *     when provided is null
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
     *     the image URL
     */
    public Marker(URL imageURL) {
        this(imageURL, 0, 0);
    }

    /**
     * constructs a Marker with the given values.
     *
     * @param imageURL
     *     image URL
     * @param offsetX
     *     horizontal pixel offset
     * @param offsetY
     *     vertical pixel offset
     * @throws java.lang.NullPointerException
     *     if imageURL is null
     */
    public Marker(URL imageURL, int offsetX, int offsetY) {
        super(offsetX, offsetY);
        this.id = "marker-" + nextId.getAndIncrement();
        this.imageURL = requireNonNull(imageURL);
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    @Override
    public Marker setRotation(Integer rotation) {
        super.setRotation(rotation);
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    public URL getImageURL() {
        return imageURL;
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
            "} " + super.toString();
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * attaches the MapLabel to this Marker
     *
     * @param mapLabel
     *     the MapLabel to attach
     * @return this object
     * @throws NullPointerException
     *     of mapLabel is null
     */
    public Marker attachLabel(MapLabel mapLabel) {
        optMapLabel = Optional.of(requireNonNull(mapLabel));
        mapLabel.setMarker(this);
        mapLabel.visibleProperty().bind(visibleProperty());
        mapLabel.positionProperty().bind(positionProperty());
        return this;
    }

    /**
     * detaches an attached Label.
     *
     * @return this object
     */
    public Marker detachLabel() {
        optMapLabel.ifPresent(mapLabel -> {
            mapLabel.setMarker(null);
            mapLabel.visibleProperty().unbind();
            mapLabel.positionProperty().unbind();
        });
        optMapLabel = Optional.empty();
        return this;
    }

    public Optional<MapLabel> getMapLabel() {
        return optMapLabel;
    }

    @Override
    public Marker setPosition(Coordinate position) {
        return (Marker) super.setPosition(position);
    }

    @Override
    public Marker setVisible(boolean visible) {
        return (Marker) super.setVisible(visible);
    }

// -------------------------- ENUMERATIONS --------------------------

    /**
     * provided Markers.
     */
    public enum Provided {
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
