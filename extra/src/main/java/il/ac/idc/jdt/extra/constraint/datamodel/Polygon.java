package il.ac.idc.jdt.extra.constraint.datamodel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import il.ac.idc.jdt.Point;

import java.awt.geom.Line2D;
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
        return (getSize() == 3);
    }

    /**
     * Rotated points and adjacent polygons to start from point p
     * @param p
     */
    public PointsPolygons getRotateOrderByLeadingPoint(Point p, boolean isWithoutLastPoint) {
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

//        for (int i=0; i<pointsLedByP.size(); i++) {
//            pointToIndexInList.put(pointsLedByP.get(i), i);
//        }
//
//        for (int i=0; i<polygonsLedByP.size(); i++) {
//            polygonToIndexInList.put(polygonsLedByP.get(i), i);
//        }

        if (isWithoutLastPoint) {
            pointsLedByP.remove(polygonsLedByP.size()-1);
            polygonsLedByP.remove(polygonsLedByP.size()-1);
        }

        PointsPolygons pointsPolygons = new PointsPolygons();
        pointsPolygons.points = pointsLedByP;
        pointsPolygons.polygons = polygonsLedByP;
        return pointsPolygons;
    }

    public List<Polygon> getAdjacentPolygons() {
        return adjacentPolygons;
    }

    public int getSize() {
        return points.size();
    }

    public List<Point> getPoints() {
        return points;
    }

    public ImmutableList<Line> getHull()
    {
        if (points == null || points.size() < 2)
            return ImmutableList.of();

        ImmutableList.Builder<Line> builder = ImmutableList.builder();
        
        Point previousPoint = null;

        for (Point point : points)
        {
            if (previousPoint == null)
            {
                previousPoint = point;
            }
            else
            {
                builder.add(new Line(previousPoint, point));
                previousPoint = point;
            }
        }

        builder.add(new Line(previousPoint, getPoints().get(0)));

        return builder.build();
    }

    public void removeByIndex(int i) {
        Point removedPoint = points.remove(i);
        pointToIndexInList.remove(removedPoint);

        Polygon removedPolygon = adjacentPolygons.remove(i);
        polygonToIndexInList.remove(removedPolygon);
    }

    /**
     * Returns true if there is an intersection point petween the two lines. If they just connected by one end then
     * return false
     * @param line
     * @return
     */
    public boolean doesLineCrossPolygon(Line line) {
        List<Line> linesFromPolygon = getLinesFromPolygon();
        for (Line lineFromPolygon : linesFromPolygon) {
            if (!line.isConnectedToLine(lineFromPolygon.getP1(), lineFromPolygon.getP2())) {
                if (Line2D.linesIntersect(line.getP1().getX(), line.getP1().getY(), line.getP2().getX(), line.getP2().getY(),
                        lineFromPolygon.getP1().getX(), lineFromPolygon.getP1().getY(), lineFromPolygon.getP2().getX(), lineFromPolygon.getP2().getY())) {
                    return true;
                }
            }
        }

        return false;
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

    public void setPolygon(int index, Polygon polygon) {
        Polygon set = adjacentPolygons.set(index, polygon);
        polygonToIndexInList.remove(set);
        polygonToIndexInList.put(polygon, index);
    }

    public Set<Point> getKey() {
        return Sets.newHashSet(points);
    }

    public Polygon(List<Point> points, List<Polygon> adjacentPolygons) {
        this.points = points;
        this.adjacentPolygons = adjacentPolygons;

        Set<Point> set = Sets.newHashSet();
        set.addAll(points);

        if (set.size() < points.size()) {
            throw new IllegalArgumentException("Inserted two of the same points!!!");
        }

        updateIndexes();
    }

    public void addPointsAndPolygons(List<Point> points, List<Polygon> adjacentPolygons) {
        this.points = points;
        this.adjacentPolygons = adjacentPolygons;

        Set<Point> set = Sets.newHashSet();
        set.addAll(points);

        if (set.size() < points.size()) {
            throw new IllegalArgumentException("can't be smaller");
        }

        updateIndexes();
    }

    public Polygon() {

    }

    public void insertInIndex(int index, List<Point> points, List<Polygon> polygons) {
        this.points.addAll(index, points);
        this.adjacentPolygons.addAll(index, polygons);

        Set<Point> set = Sets.newHashSet();
        set.addAll(points);

        if (set.size() < points.size()) {
            throw new IllegalArgumentException("can't be smaller");
        }

        updateIndexes();
    }

    private void updateIndexes() {
        for (int i=0; i<points.size(); i++) {
            pointToIndexInList.put(points.get(i), i);
            polygonToIndexInList.put(adjacentPolygons.get(i), i);
        }
    }

    public List<Line> getLinesFromPolygon() {
        List<Line> linesToPaint = Lists.newArrayList();
        List<Point> points = getPoints();
        for (int i = 0; i < points.size(); i++) {
            if (i + 1 < points.size())
            {
                Line ab = new Line(points.get(i), points.get(i + 1));
                linesToPaint.add(ab);
            }
        }

        Line line = new Line(points.get(0), points.get(points.size() - 1));
        linesToPaint.add(line);
        return linesToPaint;
    }

    public class PointsPolygons {
        private List<Point> points = Lists.newArrayList();
        private List<Polygon> polygons = Lists.newArrayList();

        public List<Point> getPoints() {
            return points;
        }

        public List<Polygon> getPolygons() {
            return polygons;
        }
    }

    @Override
    public String toString() {
        return "Polygon{" +
                "points=" + points;

    }
}
