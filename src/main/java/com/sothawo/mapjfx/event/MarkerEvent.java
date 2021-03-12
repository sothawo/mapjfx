/*
 Copyright 2016-2021 Peter-Josef Meisch (pj.meisch@sothawo.com)

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
package com.sothawo.mapjfx.event;

import com.sothawo.mapjfx.Marker;
import javafx.event.Event;
import javafx.event.EventType;

import static java.util.Objects.requireNonNull;

/**
 * Event class for events concerning markers.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class MarkerEvent extends Event {

    /** base event type */
    public static final EventType<MarkerEvent> ANY = new EventType<>("MARKER_EVENT_ANY");

    /** marker clicked in map */
    public static final EventType<MarkerEvent> MARKER_CLICKED = new EventType<>(ANY, "MARKER_CLICKED");

    /** marker doubleclicked in map */
    public static final EventType<MarkerEvent> MARKER_DOUBLECLICKED = new EventType<>(ANY, "MARKER_DOUBLECLICKED");

    /** marker rightclicked in map */
    public static final EventType<MarkerEvent> MARKER_RIGHTCLICKED = new EventType<>(ANY, "MARKER_RIGHTCLICKED");

    /** marker mousedown in map */
    public static final EventType<MarkerEvent> MARKER_MOUSEDOWN = new EventType<>(ANY, "MARKER_MOUSEDOWN");

    /** marker mouseup in map */
    public static final EventType<MarkerEvent> MARKER_MOUSEUP = new EventType<>(ANY, "MARKER_MOUSEUP");

    /** marker entered in map */
    public static final EventType<MarkerEvent> MARKER_ENTERED = new EventType<>(ANY, "MARKER_ENTERED");

    /** marker exited in map */
    public static final EventType<MarkerEvent> MARKER_EXITED = new EventType<>(ANY, "MARKER_EXITED");

    /** the marker for this event. */
    private final Marker marker;

    public MarkerEvent(EventType<? extends MarkerEvent> eventType, Marker marker) {
        super(eventType);
        this.marker = requireNonNull(marker);
    }

    public Marker getMarker() {
        return marker;
    }
}
