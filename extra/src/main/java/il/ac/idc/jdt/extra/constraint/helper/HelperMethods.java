package il.ac.idc.jdt.extra.constraint.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import il.ac.idc.jdt.BoundingBox;
import il.ac.idc.jdt.Point;
import il.ac.idc.jdt.extra.constraint.datamodel.Line;
import il.ac.idc.jdt.extra.constraint.datamodel.PointsXComparator;
import il.ac.idc.jdt.extra.constraint.datamodel.PointsYComparator;
import il.ac.idc.jdt.extra.constraint.datamodel.Polygon;
import il.ac.idc.jdt.Triangle;
import org.javatuples.Pair;

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
        List<Point> intersectionPoints = getIntersectionPoints(polygonToMerge, rootToMergeInto);

        Polygon mergedPolygon = new Polygon();
        //remove the intersection point to avoid adding it twice
        if (intersectionPoints.size()==2) {
            System.out.println("twoPoints");
            orderIntersectionPoints(intersectionPoints, rootToMergeInto);
        }

        Point leadingIntersectionPoint = intersectionPoints.get(0);
        Polygon.PointsPolygons rotateOrderByLeadingPoint = rootToMergeInto.getRotateOrderByLeadingPoint(leadingIntersectionPoint, false);
        mergedPolygon.addPointsAndPolygons(rotateOrderByLeadingPoint.getPoints(), rotateOrderByLeadingPoint.getPolygons());

        //remove the common point not to add it twice
        mergedPolygon.removeByIndex(0);
        Polygon.PointsPolygons rotated = polygonToMerge.getRotateOrderByLeadingPoint(leadingIntersectionPoint, true);

        //remove the second point since it is of the non convex edge not to count it anymore
        if (intersectionPoints.size() == 2) {
            mergedPolygon.removeByIndex(0);

            int lastIndex = rotated.getPoints().size() - 1;
            rotated.getPoints().remove(lastIndex);
            rotated.getPolygons().remove(lastIndex);
        }

        mergedPolygon.getPoints().addAll(0, rotated.getPoints());
        mergedPolygon.getAdjacentPolygons().addAll(0, rotated.getPolygons());

        //update the neighbours
        updateNeighboursWithMergedPolygon(polygonToMerge, rootToMergeInto, mergedPolygon);

        return mergedPolygon;





//        Point intersectionPoint = getIntersectionPoints(polygonToMerge, rootToMergeInto).iterator().next();
//        Polygon.PointsPolygons rotateOrderByLeadingPoint = rootToMergeInto.getRotateOrderByLeadingPoint(intersectionPoint, false);
//        Integer indexOfPoint = 0;
//
//        Polygon.PointsPolygons rotated = polygonToMerge.getRotateOrderByLeadingPoint(intersectionPoint, true);
//
//        List<Polygon> polygons = Lists.newArrayList(rootToMergeInto.getAdjacentPolygons());
//        List<Point> points = Lists.newArrayList(rootToMergeInto.getPoints());
//        Polygon mergedPolygon = new Polygon(rotateOrderByLeadingPoint.getPoints(), rotateOrderByLeadingPoint.getPolygons());
//
//        //remove the intersection point to avoid adding it twice
//        mergedPolygon.removeByIndex(indexOfPoint);
//        //merge the two polygons
//
//        mergedPolygon.getPoints().addAll(indexOfPoint, rotated.getPoints());
//        mergedPolygon.getAdjacentPolygons().addAll(indexOfPoint, rotated.getPolygons());
//
//        updateNeighboursWithMergedPolygon(polygonToMerge, rootToMergeInto, mergedPolygon);
//
//        return mergedPolygon;


//        Point intersectionPoint = getIntersectionPoints(polygonToMerge, rootToMergeInto).iterator().next();
//        Integer indexOfPoint = rootToMergeInto.getIndexOfPoint(intersectionPoint);
//
//        Polygon.PointsPolygons rotated = polygonToMerge.getRotateOrderByLeadingPoint(intersectionPoint, true);
//
//        List<Polygon> polygons = Lists.newArrayList(rootToMergeInto.getAdjacentPolygons());
//        List<Point> points = Lists.newArrayList(rootToMergeInto.getPoints());
//        Polygon mergedPolygon = new Polygon(points, polygons);
//
//        //remove the intersection point to avoid adding it twice
//        mergedPolygon.removeByIndex(indexOfPoint);
//        //merge the two polygons
//
//        mergedPolygon.getPoints().addAll(indexOfPoint, rotated.getPoints());
//        mergedPolygon.getAdjacentPolygons().addAll(indexOfPoint, rotated.getPolygons());
//
//        updateNeighboursWithMergedPolygon(polygonToMerge, rootToMergeInto, mergedPolygon);
//
//        return mergedPolygon;
    }

    /**
     * Orders the Points in place so that the first point will be the first intersection point clockwise.
     * @param intersectionPoints
     * @param mergedPolygon
     */
    private static void orderIntersectionPoints(List<Point> intersectionPoints, Polygon mergedPolygon) {
        Integer firstIndex = mergedPolygon.getIndexOfPoint(intersectionPoints.get(0));
        Integer secondIndex = mergedPolygon.getIndexOfPoint(intersectionPoints.get(1));

        if ((secondIndex - firstIndex) != 1) {
            swichPoints(intersectionPoints);
        }
    }

    private static void swichPoints(List<Point> intersectionPoints) {
        Point temp = intersectionPoints.get(0);
        intersectionPoints.set(0, intersectionPoints.get(1));
        intersectionPoints.set(1, temp);
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


    private static List<Point> getIntersectionPoints(Polygon polygonToMerge, Polygon rootToMergeInto) {
        List<Point> intersectionPoints = Lists.newArrayList();
        List<Polygon> surroundingPolygons = rootToMergeInto.getAdjacentPolygons();
        for(int i=0; i<rootToMergeInto.getSize(); i++) {
            Polygon polygon = surroundingPolygons.get(i);
            if (polygon != null) {
                if (polygon.equals(polygonToMerge)) {
                    intersectionPoints.add(rootToMergeInto.getPoints().get(i));
                }
            }
        }

        if (intersectionPoints.isEmpty()) {
            throw new RuntimeException("illegal input - no intersection between two polygons");
        } else {
            if (intersectionPoints.size() == 2) {
                System.out.println("found two intersection points");
            }  if (intersectionPoints.size() > 2) {
                throw new RuntimeException("illegal input - more then two intersection points");
            }
            return intersectionPoints;
        }
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


    public static boolean isLineInsidePolygon(Polygon polygon, Line suggestedLine, BoundingBox boundingBox) {
//        if (polygon.doesLineCrossPolygon(suggestedLine)) {
//            return false;
//        } else {
//            Point descriptorPointOfLine = createDescriptorPointOfLine(suggestedLine);
//            List<Line> linesFromPolygon = polygon.getLinesFromPolygon();
//            //if somehow we managed to send line to split by which is one of the original polygon lines
//            if (linesFromPolygon.contains(suggestedLine)) {
//                return false;
//            }
//            for (Line line : linesFromPolygon) {
//                boolean pointOnTheLine = isPointOnTheLine(line, descriptorPointOfLine);
//                if (pointOnTheLine) {
//                    return false;
//                }
//            }
//
//            Line lineFromPoint;
//            boolean lineVertical = isLineVertical(suggestedLine);
//            lineFromPoint = new Line(descriptorPointOfLine, new Point(boundingBox.getWidth(), descriptorPointOfLine.getY()));
//
//            List<Point> intersectionPoints = Lists.newArrayList(descriptorPointOfLine);
//
//            for (Line lineFromPolygon : linesFromPolygon) {
//                if (lineFromPolygon != null) {
//                    Point intersectionPoint = findIntersectionPoint(lineFromPolygon, lineFromPoint);
//                    if (intersectionPoint != null) {
//                        intersectionPoints.add(intersectionPoint);
//                    }
//                }
//            }
//
//            int numOfCrossAbove = getNumberOfCrossAboveIntersectionPoint(descriptorPointOfLine, intersectionPoints, lineVertical);
//            return (numOfCrossAbove%2==1);
//        }

        if (polygon.doesLineCrossPolygon(suggestedLine)) {
            return false;
        } else {
            Point descriptorPointOfLine = createDescriptorPointOfLine(suggestedLine);
            Line lineFromPoint;
            boolean lineVertical = isLineVertical(suggestedLine);
            if (lineVertical) {
                lineFromPoint = new Line(descriptorPointOfLine, new Point(boundingBox.getWidth(), descriptorPointOfLine.getY()));
            } else {
                lineFromPoint = new Line(descriptorPointOfLine, new Point(descriptorPointOfLine.getX(), boundingBox.getHeight()));
            }

            List<Point> intersectionPoints = Lists.newArrayList(descriptorPointOfLine);
            List<Line> linesFromPolygon = polygon.getLinesFromPolygon();
            //if somehow we managed to send line to split by which is one of the original polygon lines
            if (linesFromPolygon.contains(suggestedLine)) {
                return false;
            }
            for (Line lineFromPolygon : linesFromPolygon) {
                if (lineFromPolygon != null) {
                    Point intersectionPoint = findIntersectionPoint(lineFromPolygon, lineFromPoint);
                    if (intersectionPoint != null) {
                        intersectionPoints.add(intersectionPoint);
                    }
                }
            }

            int numOfCrossAbove = getNumberOfCrossAboveIntersectionPoint(descriptorPointOfLine, intersectionPoints, lineVertical);
            return (numOfCrossAbove%2==1);
        }
    }

    private static boolean isLineVertical(Line line) {
        return line.getP1().getX() == line.getP2().getX();
    }

    private static int getNumberOfCrossAboveIntersectionPoint(Point pointForVerticalLine, List<Point> intersectionPoints, boolean isVertical) {
        int numOfCrossAbove = 0;
        if (isVertical) {
            Collections.sort(intersectionPoints, new PointsXComparator());
        } else {
            Collections.sort(intersectionPoints, new PointsYComparator());
        }
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
     * @param polygonLine
     * @param descriptorLine
     * @return
     */
    public static Point findIntersectionPoint(Line polygonLine, Line descriptorLine) {

        if (polygonLine.isConnectedToLine(descriptorLine.getP1(), descriptorLine.getP2())) {
            System.out.println("should not get here since we deal with this option by checking of the point is on one of the lines");
            return null;
        }

        double x1 = polygonLine.getP1().getX();
        double x2 = polygonLine.getP2().getX();
        double y1 = polygonLine.getP1().getY();
        double y2 = polygonLine.getP2().getY();

        double x3 = descriptorLine.getP1().getX();
        double x4 = descriptorLine.getP2().getX();
        double y3 = descriptorLine.getP1().getY();
        double y4 = descriptorLine.getP2().getY();
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

    /**
     * check the intersection of the line (xi, yi) with line((x1,y1)(x2,y2)). If the intersection point is exactly on the
     * line (xi, yi) then count it only if the line ((x1,y1)(x2,y2)) is from the left side of (xi, yi)
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @param xi
     * @param yi
     * @return
     */
    private static boolean isIntersectionPointOnOneOfTheLines(double x1, double x2, double y1, double y2, double xi, double yi) {
        return ((xi <= x1 && xi > x2) || (xi > x1 && xi <= x2)) &&
            ((yi <= y1 && yi >= y2) || (yi >= y1 && y1 <= y2));
    }

    private static boolean isIntersectionPointOnOneOfTheLinesWithNoRegardToOrder(double x1, double x2, double y1, double y2, double xi, double yi) {
        return ((xi <= x1 && xi >= x2) || (xi >= x1 && xi <= x2)) &&
                ((yi <= y1 && yi >= y2) || (yi >= y1 && y1 <= y2));
    }

    public static boolean isPointOnTheLine(Line l, Point p, boolean isRegardToOrder) {
        if (isPointOnVerticalOrHorisontalLine(l, p)) {
            return true;
        }

        double slope = (double)(l.getP1().getY() - l.getP2().getY())/(l.getP1().getX() - l.getP2().getX());
        double extra = (-1*slope*l.getP1().getX()) + (l.getP1().getY());

        double yValue = slope * p.getX() + extra;

        //this means that the point is on the formula representing the line, still need to check if it is on the line
        if (yValue == p.getY()) {
            if (isRegardToOrder) {
                return isIntersectionPointOnOneOfTheLines(l.getP1().getX(), l.getP2().getX(), l.getP1().getY(), l.getP2().getY(), p.getX(), p.getY());
            } else {
                return isIntersectionPointOnOneOfTheLinesWithNoRegardToOrder(l.getP1().getX(), l.getP2().getX(), l.getP1().getY(), l.getP2().getY(), p.getX(), p.getY());
            }
        }  else {
            return false;
        }
    }

    public static boolean isPointOnTheLine(Line l, Point p) {
        return isPointOnTheLine(l, p, true);
    }



    private static boolean isPointOnVerticalOrHorisontalLine(Line l, Point p) {
        if (isLineVertical(l)) {
            if (p.getX() == l.getP1().getX()) {
                if ((p.getY() <= l.getP1().getY() && p.getY() >= l.getP2().getY()) ||
                        (p.getY() <= l.getP2().getY() && p.getY() >= l.getP1().getY())) {
                    return true;
                }
            }
        }  else if (l.getP1().getY() == l.getP2().getY()) {
            if (p.getY() == l.getP1().getY()) {
                if ((p.getX() <= l.getP1().getX() && p.getX() >= l.getP2().getX()) ||
                        (p.getX() <= l.getP2().getX() && p.getX() >= l.getP1().getX())) {
                    return true;
                }
            }
        }
        return false;
    }
}
