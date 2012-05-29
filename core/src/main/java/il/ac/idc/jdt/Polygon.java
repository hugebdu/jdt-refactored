package il.ac.idc.jdt;

import java.util.ArrayList;

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
    private ArrayList<Point> points;

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
    private ArrayList<Polygon> adjacentPolygons;


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

    public void addPoints(Point p1, Point p2, Point p3) {
        points.add(p1);
        points.add(p2);
        points.add(p3);

        size += 3;
    }

    public void addPolygon(Polygon polygon) {
        adjacentPolygons.add(polygon);
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
}
