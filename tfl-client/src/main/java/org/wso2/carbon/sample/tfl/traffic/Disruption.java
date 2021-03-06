/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.sample.tfl.traffic;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.util.ArrayList;

public class Disruption {
    private String id;
    private String state;
    private String severity;
    private String location;
    private String comments;
    private String coordinates = null;

    public static GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
    public boolean isMultiPolygon = true;
    public final static double tolerance = 0.0005;
    private ArrayList<Coordinate> coords = new ArrayList<Coordinate>();

    public Disruption() {

    }

    public Disruption(String id, String severity, String location, String comments) {
        this.id = id;
        this.state = severity;
        this.comments = comments;
        this.location = location;
    }

    public void setCoordsPoly(String coords) {
        isMultiPolygon = false;
        StringBuilder sb = new StringBuilder();
        if (coords != null) {
            String[] temp = coords.split(",");
            sb.append("{ 'type': 'Polygon',  'coordinates': [[");
            for (int i = 0; i < temp.length - 1; i += 2) {
                if (i != 0) {
                    sb.append(",");
                }
                sb.append("[").append(Double.parseDouble(temp[i])).append(",").append(Double.parseDouble(temp[i + 1])).append("]");
            }
            sb.append("]]  }");
        } else {
            sb.append("{  'type': 'Polygon',  'coordinates': []  }");
        }
        coordinates = sb.toString();
    }

    public void setCoordsPoly(Coordinate[] coords) {
        StringBuilder sb = new StringBuilder();
        if (coords.length != 0) {
            sb.append("{  'type': 'Polygon',  'coordinates': [[");

            for (int i = 0; i < coords.length; i++) {
                if (i != 0) {
                    sb.append(",");
                }
                sb.append("[").append(coords[i].x).append(",").append(coords[i].y).append("]");
            }
            sb.append("]]  }");
        } else {
            sb.append("{  'type': 'Polygon',  'coordinates': []  }");
        }
        coordinates = sb.toString();
    }

    public void addCoordsLane(String co) {
        String[] temp = co.split(",");
        if (temp.length != 4) {
            //System.out.println(co);
            return;
        }
        try {
            double x1, x2, y1, y2;
            x1 = Double.parseDouble(temp[0]);
            y1 = Double.parseDouble(temp[1]);
            x2 = Double.parseDouble(temp[2]);
            y2 = Double.parseDouble(temp[3]);

            double f = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow(y1 - y2, 2));
            coords.add(new Coordinate(Double.parseDouble(temp[0]) - tolerance * (y2 - y1) / f, Double.parseDouble(temp[1]) - tolerance * (x1 - x2) / f));
            coords.add(new Coordinate(Double.parseDouble(temp[0]) + tolerance * (y2 - y1) / f, Double.parseDouble(temp[1]) + tolerance * (x1 - x2) / f));
            coords.add(new Coordinate(Double.parseDouble(temp[2]) + tolerance * (y2 - y1) / f, Double.parseDouble(temp[3]) + tolerance * (x1 - x2) / f));
            coords.add(new Coordinate(Double.parseDouble(temp[2]) - tolerance * (y2 - y1) / f, Double.parseDouble(temp[3]) - tolerance * (x1 - x2) / f));

            //coords.add(new Coordinate(Double.parseDouble(temp[0]), Double.parseDouble(temp[1])));
            //coords.add(new Coordinate(Double.parseDouble(temp[2]), Double.parseDouble(temp[3])));
        } catch (NumberFormatException e) {
            System.out.println("NFE " + co);
        }
    }

    public void end() {
        if (isMultiPolygon) {
            Coordinate[] c = new Coordinate[coords.size()];
            c = coords.toArray(c);
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            ConvexHull ch = new ConvexHull(c, geometryFactory);
            setCoordsPoly(ch.getConvexHull().getCoordinates());
        }

    }

    @Override
    public String toString() {
        return "{'id': " + id + ", \n"
                + "'properties': { \n"
                + " 'timeStamp': " + System.currentTimeMillis() + ", \n"
                + " 'state': '" + severity + "', \n"
                + " 'information': " + "'Location- " + location + " Comments- " + comments + "'" + "\n"
                + " }, \n"
                + "'geometry' : " + coordinates + "\n}";
    }

    public String toJson() {
        return "{'id': " + id + ", "
                + "'properties': { "
                + " 'timeStamp': " + System.currentTimeMillis() + ", "
                + " 'state': '" + severity + "', "
                + " 'information': " + "'Location- " + location + " Comments- " + comments + "'"
                + " }, "
                + "'geometry' : " + coordinates + "}";
    }

    public String getCsvHeader() {
        return "id, timestamp, state, location, comments, coordinates";
    }

    public String toCsv() {
        return id + ", " + System.currentTimeMillis() + ", " + severity + ", " + location + ", "
                + comments + ", " + coordinates;
    }

    public void setComments(String comments) {
        this.comments = comments.replaceAll("'", "").replaceAll("\"", "");
    }

    public void setLocation(String location) {
        this.location = location.replaceAll("'", "").replaceAll("\"", "");
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getSeverity() {
        return severity;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
