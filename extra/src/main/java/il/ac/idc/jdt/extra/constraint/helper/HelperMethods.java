package il.ac.idc.jdt.extra.constraint.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import il.ac.idc.jdt.Point;
import il.ac.idc.jdt.extra.constraint.datamodel.Line;
import il.ac.idc.jdt.extra.constraint.datamodel.PointsYComparator;
import il.ac.idc.jdt.extra.constraint.datamodel.Polygon;
import il.ac.idc.jdt.Triangle;
import org.javatuples.Pair;

import java.awt.geom.Line2D;
import java.util.*;

import static com.google.common.collect.Lists.newLinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: Innak
 * Date: 5/29/12
 * Time: 10:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class HelperMethods {
    /**
     * Helps in converting from one data structure to another
     * @param triangles
     * @return
     */
    public static Map<Set<Point>, Polygon> fromTrianglesToPolygons(List<Triangle> triangles) {
        Triangle someTriangle = triangles.iterator().next();
        return populatePolygons(someTriangle);
    }


    public static Polygon mergeTwoPolygons(Polygon polygonToMerge, Polygon rootToMergeInto) {
        Point intersectionPoint = getIntersectionPoint(polygonToMerge, rootToMergeInto);
        Integer indexOfPoint = rootToMergeInto.getIndexOfPoint(intersectionPoint);

        Polygon.PointsPolygons rotated = polygonToMerge.getRotateOrderByLeadingPoint(intersectionPoint, true);

        List<Polygon> polygons = Lists.newArrayList(rootToMergeInto.getAdjacentPolygons());
        List<Point> points = Lists.newArrayList(rootToMergeInto.getPoints());
        Polygon mergedPolygon = new Polygon(points, polygons);

        //remove the intersection point to avoid adding it twice
        mergedPolygon.removeByIndex(indexOfPoint);
        //merge the two polygons

        mergedPolygon.getPoints().addAll(indexOfPoint, rotated.getPoints());
        mergedPolygon.getAdjacentPolygons().addAll(indexOfPoint, rotated.getPolygons());

        updateNeighboursWithMergedPolygon(polygonToMerge, rootToMergeInto, mergedPolygon);

        return mergedPolygon;
    }

    public static void dividePolygonByLine(Line line, Polygon merged, Polygon side1, Polygon side2) {
        Polygon.PointsPolygons rotateOrderByLeadingPoint = merged.getRotateOrderByLeadingPoint(line.getP1(), false);
        List<Point> mergedPoints = rotateOrderByLeadingPoint.getPoints();
        List<Polygon> mergedPolygons = rotateOrderByLeadingPoint.getPolygons();

        List<Point> side1Points = Lists.newArrayList();
        List<Polygon> side1Polygons = Lists.newArrayList();
        int secondIntersection;
        for (secondIntersection = 0; secondIntersection < mergedPoints.size(); secondIntersection++) {
            if (!gotToOtherSide(line, mergedPoints, secondIntersection)) {
                side1Points.add(mergedPoints.get(secondIntersection));
                side1Polygons.add(mergedPolygons.get(secondIntersection));
            }  else {
                side1Points.add(mergedPoints.get(secondIntersection));
                side1Polygons.add(mergedPolygons.get(secondIntersection));
                break;
            }
        }

        List<Point> side2Points = Lists.newArrayList();
        List<Polygon> side2Polygons = Lists.newArrayList();
        for (int firstIndexInSecondPolygon = secondIntersection; firstIndexInSecondPolygon < mergedPoints.size(); firstIndexInSecondPolygon++) {
            side2Points.add(mergedPoints.get(firstIndexInSecondPolygon));
            side2Polygons.add(mergedPolygons.get(firstIndexInSecondPolygon));
        }

        side2Points.add(mergedPoints.get(0));
        side2Polygons.add(mergedPolygons.get(0));

        side1Polygons.set(side1Polygons.size()-1, side2);
        side2Polygons.set(side2Polygons.size()-1, side1);

        side1.addPointsAndPolygons(side1Points, side1Polygons);
        side2.addPointsAndPolygons(side2Points, side2Polygons);

        updateAdjacent(merged, side1, side1.getAdjacentPolygons());
        updateAdjacent(merged, side2, side2.getAdjacentPolygons());

    }


    private static boolean gotToOtherSide(Line line, List<Point> mergedPoints, int i) {
        return mergedPoints.get(i).equals(line.getP2());
    }

    private static Point getIntersectionPoint(Polygon polygonToMerge, Polygon rootToMergeInto) {
        List<Polygon> surroundingPolygons = rootToMergeInto.getAdjacentPolygons();
        for(int i=0; i<rootToMergeInto.getSize(); i++) {
            Polygon polygon = surroundingPolygons.get(i);
            if (polygon != null) {
                if (polygon.equals(polygonToMerge)) {
                    return rootToMergeInto.getPoints().get(i);
                }
            }
        }

        throw new RuntimeException("illegal input");
    }

    private static void updateNeighboursWithMergedPolygon(Polygon polygonToMerge, Polygon rootToMergeInto, Polygon merged) {
        List<Polygon> adjacentPolygons = polygonToMerge.getAdjacentPolygons();
        updateAdjacent(polygonToMerge, merged, adjacentPolygons);

        adjacentPolygons = rootToMergeInto.getAdjacentPolygons();
        updateAdjacent(rootToMergeInto, merged, adjacentPolygons);
    }

    /**
     *
     * @param polygonToMerge - the previous polygon that acted as neighbour
     * @param merged - the polygon to update to
     * @param adjacentPolygons - the polygons that should be updated
     */
    private static void updateAdjacent(Polygon polygonToMerge, Polygon merged, List<Polygon> adjacentPolygons) {
        for (Polygon adjacentPolygon : adjacentPolygons) {
            if (adjacentPolygon != null) {
                Integer indexOfMergedPolygonInNeighbours = adjacentPolygon.getIndexByPolygon(polygonToMerge);
                if (indexOfMergedPolygonInNeighbours != null) {
                    adjacentPolygon.setPolygon(indexOfMergedPolygonInNeighbours, merged);
                }
            }
        }
    }

    public static Map<Set<Point>, Polygon>  populatePolygons(Triangle triangle) {
        List<Polygon> polygons = Lists.newArrayList();
        Map<Set<Point>, Polygon> mapOfPolygons = Maps.newHashMap();
        Queue<Pair<Triangle, Pair<Polygon, Integer>>> queue = newLinkedList();
        queue.add(Pair.<Triangle, Pair<Polygon, Integer>>with(triangle, null));
        while (!queue.isEmpty()) {
            Pair<Triangle, Pair<Polygon, Integer>> pair = queue.poll();
            triangle = pair.getValue0();
            Polygon polygon = null;
            if (!triangle.isHalfplane()) {
                HashSet<Point> keyOfCurrentTriangle = Sets.newHashSet(triangle.getA(), triangle.getB(), triangle.getC());
                if (isNewPolygon(mapOfPolygons, keyOfCurrentTriangle)) {
                    polygon = new Polygon();
                    polygon.addPoints(triangle.getA(), triangle.getB(), triangle.getC());
                    polygons.add(polygon);
                    mapOfPolygons.put(keyOfCurrentTriangle, polygon);

                    queue.add(Pair.<Triangle, Pair<Polygon, Integer>>with(triangle.getAbTriangle(), Pair.<Polygon, Integer>with(polygon, 0)));
                    queue.add(Pair.<Triangle, Pair<Polygon, Integer>>with(triangle.getBcTriangle(), Pair.<Polygon, Integer>with(polygon, 1)));
                    queue.add(Pair.<Triangle, Pair<Polygon, Integer>>with(triangle.getCaTriangle(), Pair.<Polygon, Integer>with(polygon, 2)));
                } else {
                    polygon = mapOfPolygons.get(keyOfCurrentTriangle);
                }
            }

            Pair<Polygon, Integer> callerPolygonInfoPair = pair.getValue1();
            if (callerPolygonInfoPair != null) {
                Polygon callerPolygon = callerPolygonInfoPair.getValue0();
                Integer targetIndex = callerPolygonInfoPair.getValue1();
                callerPolygon.addPolygon(polygon, targetIndex);
            }
        }

        return mapOfPolygons;
    }

    private static boolean isNewPolygon(Map<Set<Point>, Polygon> mapOfPolygons, HashSet<Point> keyOfCurrentTriangle) {
        return !mapOfPolygons.containsKey(keyOfCurrentTriangle);
    }


    public static boolean isLineInsidePolygon(Polygon polygon, Line line, double maxHeight) {
        if (polygon.doesLineCrossPolygon(line)) {
            return false;
        } else {
            Point pointForVerticalLine = createDescriptorPointOfLine(line);
            Line verticalLineFromPoint = new Line(pointForVerticalLine, new Point(pointForVerticalLine.getX(), maxHeight));

            List<Point> intersectionPoints = Lists.newArrayList(pointForVerticalLine);
            List<Line> linesFromPolygon = polygon.getLinesFromPolygon();
            //if somehow we managed to send line to split by which is one of the original polygon lines
            if (linesFromPolygon.contains(line)) {
                return false;
            }
            for (Line lineFromPolygon : linesFromPolygon) {
                if (lineFromPolygon != null) {
                    Point intersectionPoint = findIntersectionPoint(lineFromPolygon, verticalLineFromPoint);
                    if (intersectionPoint != null) {
                        intersectionPoints.add(intersectionPoint);
                    }
                }
            }

            int numOfCrossAbove = getNumberOfCrossAboveIntersectionPoint(pointForVerticalLine, intersectionPoints);
            return (numOfCrossAbove%2==1);
        }
    }

    private static int getNumberOfCrossAboveIntersectionPoint(Point pointForVerticalLine, List<Point> intersectionPoints) {
        int numOfCrossAbove = 0;
        Collections.sort(intersectionPoints, new PointsYComparator());
        for (int i = 0; i < intersectionPoints.size(); i++) {
            if (intersectionPoints.get(i).equals(pointForVerticalLine)) {
                numOfCrossAbove = intersectionPoints.size() - i + 1;
            }
        }
        return numOfCrossAbove;
    }

    private static Point createDescriptorPointOfLine(Line line) {
        return new Point((line.getP1().getX() + line.getP2().getX())/2, (line.getP1().getY() + line.getP2().getY())/2);
    }

    /**
     * If Lines intersect return the point of intersection
     * If lines share the same point return null;
     * If the intersection is not on the lines then return null also
     * If parallel return null
     * @param l1
     * @param l2
     * @return
     */
    public static Point findIntersectionPoint(Line l1, Line l2) {

        if (l1.isConnectedToLine(l2.getP1(), l2.getP2())) {
            return null;
        }

        double x1 = l1.getP1().getX();
        double x2 = l1.getP2().getX();
        double y1 = l1.getP1().getY();
        double y2 = l1.getP2().getY();

        double x3 = l2.getP1().getX();
        double x4 = l2.getP2().getX();
        double y3 = l2.getP1().getY();
        double y4 = l2.getP2().getY();
        double d = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
        if (d == 0) return null;

        double xi = ((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d;
        double yi = ((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d;

        if (isIntersectionPointOnOneOfTheLines(x1, x2, y1, y2, xi, yi)) {
            return new Point(xi,yi);
        } else {
            return null;
        }
    }

    private static boolean isIntersectionPointOnOneOfTheLines(double x1, double x2, double y1, double y2, double xi, double yi) {
        return ((xi <= x1 && xi >= x2) || (xi >= x1 && xi <= x2)) &&
            ((yi <= y1 && yi >= y2) || (yi >= y1 && y1 <= y2));
    }
}
