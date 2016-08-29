/*
 Copyright 2016 Peter-Josef Meisch (pj.meisch@sothawo.com)

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

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * Event class for MapView events.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class MapViewEvent extends Event {
    /** base event type */
    public static final EventType<MapViewEvent> ANY = new EventType<>("ANY");

    /** coordinate clicked in map */
    public static final EventType<MapViewEvent> MAP_CLICKED = new EventType<>(ANY, "MAP_CLICKED");

    /** marker clicked in map */
    public static final EventType<MapViewEvent> MARKER_CLICKED = new EventType<>(ANY, "MARKER_CLICKED");

    /** the coordinate where the event happened, only set on MAP_CLICKED event */
    private final Coordinate coordinate;

    /** the name of the object that was clicked; only set in case of marker click and label click events. */
    private final String name;

    /**
     * creates an CoordinateEvent of the given type and coordinate.
     *
     * @param eventType
     *         type
     * @param coordinate
     *         coordinate
     */
    public MapViewEvent(EventType<? extends MapViewEvent> eventType, Coordinate coordinate) {
        this(eventType, coordinate, null);
    }

    /**
     * creates an CoordinateEvent of the given type and name for a clicked object.
     *
     * @param eventType
     *         type
     * @param coordinate
     *         coordinate
     * @param name
     *         the name of the clicke dobject
     */
    public MapViewEvent(EventType<? extends MapViewEvent> eventType, Coordinate coordinate, final String name) {
        super(eventType);
        this.coordinate = coordinate;
        this.name = name;
    }

    /**
     * creates an CoordinateEvent of the given type and coordinate for a clicked object.
     *
     * @param eventType
     *         type
     * @param name
     *         the name of the clicke dobject
     */
    public MapViewEvent(EventType<? extends MapViewEvent> eventType, final String name) {
        this(eventType, null, name);
    }

    /**
     * gets the name of the clicked element.
     *
     * @return element name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the coordinate where the event happend.
     */
    public Coordinate getCoordinate() {
        return coordinate;
    }
}
