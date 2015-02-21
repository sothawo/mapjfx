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

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * Event class for coordinate events. At the moment the only event that is defined is a clickin the map. This results
 * in a correspondign CoordinateEvent being fired.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class CoordinateEvent extends Event {
// ------------------------------ FIELDS ------------------------------

    /** base event type */
    public static final EventType<CoordinateEvent> ANY = new EventType<>("ANY");

    /** coordinate clicked in map */
    public static final EventType<CoordinateEvent> MAP_CLICKED = new EventType<>(ANY, "MAP_CLICKED");

    /** the coordinate where the event happened */
    private final Coordinate coordinate;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * creates an CoordinateEvent of the given type and coordinate.
     *
     * @param eventType
     *         type
     * @param coordinate
     *         coordinate
     */
    public CoordinateEvent(EventType<? extends CoordinateEvent> eventType, Coordinate coordinate) {
        super(eventType);
        this.coordinate = coordinate;
    }

    /**
     * creates an CoordinateEvent of the given type and coordinate for the given source and target..
     *
     * @param source
     *         source
     * @param target
     *         target
     * @param eventType
     *         type
     * @param coordinate
     *         coordinate
     */
    public CoordinateEvent(Object source, EventTarget target,
                           EventType<? extends CoordinateEvent> eventType, Coordinate coordinate) {
        super(source, target, eventType);
        this.coordinate = coordinate;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * @return the ccordinate where the event happend.
     */
    public Coordinate getCoordinate() {
        return coordinate;
    }
}
