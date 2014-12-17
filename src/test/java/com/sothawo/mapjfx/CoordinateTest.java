package com.sothawo.mapjfx;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoordinateTest {
// -------------------------- OTHER METHODS --------------------------

    @Test
    public void equals() throws Exception {
        Coordinate coordinate1 = new Coordinate(12.345, 67.89);
        Coordinate coordinate2 = new Coordinate(Double.valueOf(12.345), Double.valueOf(67.89));
        assertEquals(coordinate1, coordinate2);
    }

    @Test
    public void getLatitude() throws Exception {
        Coordinate coordinate = new Coordinate(12.345, 67.89);
        assertEquals((Double) 12.345, coordinate.getLatitude());
    }

    @Test
    public void getLongitude() throws Exception {
        Coordinate coordinate = new Coordinate(12.345, 67.89);
        assertEquals((Double) 67.89, coordinate.getLongitude());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullLatitude() throws Exception {
        new Coordinate(null, 12.345);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullLongitude() throws Exception {
        new Coordinate(12.345, null);
    }

    @Test
    public void nearPrecision5() throws Exception {
        Coordinate coordinate1 = new Coordinate(1.23456789012, 67.890123456789);
        Coordinate coordinate2 = new Coordinate(1.23456689012, 67.890123456789);
        assertTrue(coordinate1.isNear(coordinate2, 5));

        coordinate1 = new Coordinate(1.23456789012, 67.890123456789);
        coordinate2 = new Coordinate(1.23456789012, 67.890124456789);
        assertTrue(coordinate1.isNear(coordinate2, 5));
    }
    @Test
    public void notNearPrecision6() throws Exception {
        Coordinate coordinate1 = new Coordinate(1.23456789012, 67.890123456789);
        Coordinate coordinate2 = new Coordinate(1.23456689012, 67.890123456789);
        assertFalse(coordinate1.isNear(coordinate2, 6));

        coordinate1 = new Coordinate(1.23456789012, 67.890123456789);
        coordinate2 = new Coordinate(1.23456789012, 67.890124456789);
        assertFalse(coordinate1.isNear(coordinate2, 6));
    }
}
