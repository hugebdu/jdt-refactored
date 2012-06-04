package il.ac.idc.jdt.extra.constraint.datamodel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import il.ac.idc.jdt.Point;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Innak
 * Date: 5/29/12
 * Time: 10:17 PM
 * This data model holds a polygon as an arrayList of Points
 */
public class Polygon {

    /**
     * Points representing the polygon ordered clockwise
     */
    private List<Point> points = new CopyOnWriteArrayList<Point>();

    /**
     * The polygons match the points. They are adjacent to the points counterclockwise
     * if the polygon is
     *
     *    ******
     *   *      *
     *  **********
     *
     *  then for the top right corner the adjacent polygon is the one that shares the top line (counterclockwise from the point).
     *
     *  If the polygon is not convex then same polygon may be adjacent twice
     *  If there is no polygon since the line is a part of the convex hull then the list will hold null.
     */
    private List<Polygon> adjacentPolygons = new CopyOnWriteArrayList<Polygon>();
    /**
     *  This is an indicator helper to weather this polygon should be merged with a polygon neighbour
     */
    private boolean markForMerge;

    /**
     * This makes it easier to extract a polygon object based on it's point
     */
    private Map<Set<Point>, Polygon> polygonsMap;

    /**
     * Helper structure to know the index of a point in the list of points
     */
    private Map<Point, Integer> pointToIndexInList = Maps.newHashMap();
    /**
     * Helper structure to know the index of a polygon in the list of adjacentPolygons
     */
    private Map<Polygon, Integer> polygonToIndexInList = Maps.newHashMap();

    /**
     * holds amount of points
     */
    private int size;

    /**
     *
     * @param point
     * @param polygon
     */
    public void addPointAndPolygon(Point point, Polygon polygon) {
        points.add(point);
        adjacentPolygons.add(polygon);
        size++;
    }

    /**
     * This is to convert from Triangle to point constructor, points should be counterclockwise direction
     *
     * @param p1
     * @param p2
     * @param p3
     */
    public void addPoints(Point p1, Point p2, Point p3) {
        points.add(p1);
        points.add(p2);
        points.add(p3);

        size += 3;

        pointToIndexInList.put(p1, 0);
        pointToIndexInList.put(p2, 1);
        pointToIndexInList.put(p3, 2);
    }

    public void addPolygon(Polygon polygon, int index) {
        adjacentPolygons.add(index, polygon);
        polygonToIndexInList.put(polygon, index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Polygon polygon = (Polygon) o;

        if (points != null ? !points.equals(polygon.points) : polygon.points != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return points != null ? points.hashCode() : 0;
    }

    public boolean isTriangle() {
        return (size == 3);
    }

    /**
     * Rotated points and adjacent polygons to start from point p
     * @param p
     */
    public void rotateOrderByLeadingPoint(Point p) {
        List<Point> firstHalfPoints = Lists.newArrayList();
        List<Polygon> firstHalfPolygons = Lists.newArrayList();

        List<Point> pointsLedByP = Lists.newArrayList();
        List<Polygon> polygonsLedByP = Lists.newArrayList();

        boolean wasFound = false;

        for (int i=0; i<points.size(); i++) {
            if (wasFound) {
                pointsLedByP.add(points.get(i));
                polygonsLedByP.add(adjacentPolygons.get(i));
            } else {
                if (points.get(i).equals(p)) {
                    wasFound = true;
                    pointsLedByP.add(points.get(i));
                    polygonsLedByP.add(adjacentPolygons.get(i));
                } else {
                    firstHalfPoints.add(points.get(i));
                    firstHalfPolygons.add(adjacentPolygons.get(i));
                }
            }
        }

        pointsLedByP.addAll(firstHalfPoints);
        polygonsLedByP.addAll(firstHalfPolygons);

        points = pointsLedByP;
        adjacentPolygons = polygonsLedByP;
    }

    public boolean isMarkForMerge() {
        return markForMerge;
    }

    public void setMarkForMerge(boolean markForMerge) {
        this.markForMerge = markForMerge;
    }

    public List<Polygon> getAdjacentPolygons() {
        return adjacentPolygons;
    }

    public int getSize() {
        return size;
    }

    public List<Point> getPoints() {
        return points;
    }

    /**
     * Returns the index of the point if the list
     * @param p
     * @return index
     */
    public Integer getIndexOfPoint(Point p) {
        return pointToIndexInList.get(p);
    }

    public Integer getIndexByPolygon(Polygon polygon) {
        return polygonToIndexInList.get(polygon);
    }
}
