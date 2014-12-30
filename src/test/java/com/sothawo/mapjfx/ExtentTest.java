package com.sothawo.mapjfx;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class ExtentTest {
// ------------------------------ FIELDS ------------------------------

    // max longitude
    private static final Coordinate coordKarlsruheCastle = new Coordinate(49.013517, 8.404435);
    // min longitude, max latitude
    private static final Coordinate coordKarlsruheHarbour = new Coordinate(49.015511, 8.323497);
    // min latitude
    private static final Coordinate coordKarlsruheStation = new Coordinate(48.993284, 8.402186);

// -------------------------- OTHER METHODS --------------------------

    @Test
    public void createWithArray() throws Exception {
        Extent extent = Extent.forCoordinates(new Coordinate[]{coordKarlsruheCastle, coordKarlsruheHarbour,
                                                               coordKarlsruheStation});
        // min latitude
        assertEquals(coordKarlsruheStation.getLatitude(), extent.getMin().getLatitude());
        // min longitude
        assertEquals(coordKarlsruheHarbour.getLongitude(), extent.getMin().getLongitude());
        // max latitude
        assertEquals(coordKarlsruheHarbour.getLatitude(), extent.getMax().getLatitude());
        // max longitude
        assertEquals(coordKarlsruheCastle.getLongitude(), extent.getMax().getLongitude());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithArrayWithOneElement() throws Exception {
        Extent.forCoordinates(new Coordinate[]{coordKarlsruheCastle});
    }

    @Test
    public void createWithCollection() throws Exception {
        Collection<Coordinate> col = new ArrayList<>();
        col.add(coordKarlsruheCastle);
        col.add(coordKarlsruheHarbour);
        col.add(coordKarlsruheStation);
        Extent extent = Extent.forCoordinates(col);

        // min latitude
        assertEquals(coordKarlsruheStation.getLatitude(), extent.getMin().getLatitude());
        // min longitude
        assertEquals(coordKarlsruheHarbour.getLongitude(), extent.getMin().getLongitude());
        // max latitude
        assertEquals(coordKarlsruheHarbour.getLatitude(), extent.getMax().getLatitude());
        // max longitude
        assertEquals(coordKarlsruheCastle.getLongitude(), extent.getMax().getLongitude());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithCollectionWithOneElement() throws Exception {
        Collection<Coordinate> col = new ArrayList<>();
        col.add(coordKarlsruheCastle);
        Extent.forCoordinates(col);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithEmptyArray() throws Exception {
        Extent.forCoordinates(new Coordinate[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithEmptyCollection() throws Exception {
        Extent.forCoordinates(new ArrayList<Coordinate>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullArray() throws Exception {
        Extent.forCoordinates((Coordinate[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWithNullCollection() throws Exception {
        Extent.forCoordinates((Collection<Coordinate>) null);
    }
}
