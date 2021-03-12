/*
 Copyright 2014-2021 Peter-Josef Meisch (pj.meisch@sothawo.com)

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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Assertions.*;

@SuppressWarnings("RedundantArrayCreation")
public class ExtentTest implements TestBase {

    @Test
    public void createWithArray() throws Exception {
        Extent extent = Extent.forCoordinates(new Coordinate[]{coordKarlsruheCastle, coordKarlsruheHarbour,
                                                               coordKarlsruheStation});
        // min latitude
        assertThat(extent.getMin().getLatitude()).isEqualTo(coordKarlsruheStation.getLatitude());
        // min longitude
        assertThat(extent.getMin().getLongitude()).isEqualTo(coordKarlsruheHarbour.getLongitude());
        // max latitude
        assertThat(extent.getMax().getLatitude()).isEqualTo(coordKarlsruheHarbour.getLatitude());
        // max longitude
        assertThat(extent.getMax().getLongitude()).isEqualTo(coordKarlsruheCastle.getLongitude());
    }

    @Test
    public void createWithArrayEllipsis() throws Exception {
        Extent extent = Extent.forCoordinates(coordKarlsruheCastle, coordKarlsruheHarbour, coordKarlsruheStation);
        // min latitude
        assertThat(extent.getMin().getLatitude()).isEqualTo(coordKarlsruheStation.getLatitude());
        // min longitude
        assertThat(extent.getMin().getLongitude()).isEqualTo(coordKarlsruheHarbour.getLongitude());
        // max latitude
        assertThat(extent.getMax().getLatitude()).isEqualTo(coordKarlsruheHarbour.getLatitude());
        // max longitude
        assertThat(extent.getMax().getLongitude()).isEqualTo(coordKarlsruheCastle.getLongitude());
    }

    @Test
    public void createWithArrayWithOneElement() throws Exception {
        assertThatThrownBy(() -> {
            Extent.forCoordinates(new Coordinate[]{coordKarlsruheCastle});
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void createWithCollection() throws Exception {
        Collection<Coordinate> col = new ArrayList<>();
        col.add(coordKarlsruheCastle);
        col.add(coordKarlsruheHarbour);
        col.add(coordKarlsruheStation);
        Extent extent = Extent.forCoordinates(col);

        // min latitude
        assertThat(extent.getMin().getLatitude()).isEqualTo(coordKarlsruheStation.getLatitude());
        // min longitude
        assertThat(extent.getMin().getLongitude()).isEqualTo(coordKarlsruheHarbour.getLongitude());
        // max latitude
        assertThat(extent.getMax().getLatitude()).isEqualTo(coordKarlsruheHarbour.getLatitude());
        // max longitude
        assertThat(extent.getMax().getLongitude()).isEqualTo(coordKarlsruheCastle.getLongitude());
    }

    @Test
    public void createWithCollectionWithOneElement() throws Exception {
        assertThatThrownBy(() -> {
            Collection<Coordinate> col = new ArrayList<>();
            col.add(coordKarlsruheCastle);
            Extent.forCoordinates(col);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void createWithEmptyArray() throws Exception {
        assertThatThrownBy(() -> {
            Extent.forCoordinates(new Coordinate[]{});
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void createWithEmptyCollection() throws Exception {
        assertThatThrownBy(() -> {
            Extent.forCoordinates(new ArrayList<>());
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void createWithNullArray() throws Exception {
        assertThatThrownBy(() -> {
            Extent.forCoordinates((Coordinate[]) null);
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void createWithNullCollection() throws Exception {
        assertThatThrownBy(() -> {
            Extent.forCoordinates((Collection<Coordinate>) null);
        }).isInstanceOf(NullPointerException.class);
    }
}
