package il.ac.idc.jdt.extra.constraint.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import il.ac.idc.jdt.Point;
import il.ac.idc.jdt.extra.constraint.datamodel.Line;
import il.ac.idc.jdt.extra.constraint.datamodel.Polygon;
import il.ac.idc.jdt.Triangle;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Innak
 * Date: 5/29/12
 * Time: 10:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class Converter {
    /**
     * Helps in converting from one data structure to another
     * @param triangles
     * @return
     */
    public static Map<Set<Point>, Polygon> fromTrianglesToPolygons(List<Triangle> triangles) {
        List<Polygon> polygons = Lists.newArrayList();
        Map<Set<Point>, Polygon> mapOfPolygons = Maps.newHashMap();

        Triangle someTriangle = triangles.iterator().next();
        populatePolygons(someTriangle, polygons, mapOfPolygons);
        return mapOfPolygons;
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

    public static Polygon populatePolygons(Triangle triangle, List<Polygon> polygons, Map<Set<Point>, Polygon> mapOfPolygons) {
        if (!triangle.isHalfplane()) {
            if (triangle == null) {
                return null;
            }

            HashSet<Point> keyOfCurrentTriangle = Sets.newHashSet(triangle.getA(), triangle.getB(), triangle.getC());

            if (isNewPolygon(mapOfPolygons, keyOfCurrentTriangle)) {
                Polygon polygon = new Polygon();
                polygon.addPoints(triangle.getA(), triangle.getB(), triangle.getC());
                polygons.add(polygon);
                mapOfPolygons.put(keyOfCurrentTriangle, polygon);

                polygon.addPolygon(populatePolygons(triangle.getAbTriangle(), polygons, mapOfPolygons), 0);
                polygon.addPolygon(populatePolygons(triangle.getBcTriangle(), polygons, mapOfPolygons), 1);
                polygon.addPolygon(populatePolygons(triangle.getCaTriangle(), polygons, mapOfPolygons), 2);
                return polygon;

            } else {
                Polygon fullPolygon = mapOfPolygons.get(keyOfCurrentTriangle);
                return fullPolygon;
            }
        }

        return null;
    }

    private static boolean isNewPolygon(Map<Set<Point>, Polygon> mapOfPolygons, HashSet<Point> keyOfCurrentTriangle) {
        return !mapOfPolygons.containsKey(keyOfCurrentTriangle);
    }
}
