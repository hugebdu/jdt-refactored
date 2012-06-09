package il.ac.idc.jdt.extra.constraint.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import il.ac.idc.jdt.Point;
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


    public static void mergeTwoPolygons(Polygon polygonToMerge, Polygon rootToMergeInto) {

        Point intersectionPoint = getIntersectionPoint(polygonToMerge, rootToMergeInto);

        Integer indexOfPoint = rootToMergeInto.getIndexOfPoint(intersectionPoint);

        //remove the two intersection point to avoid adding it twice
        rootToMergeInto.removeByIndex(indexOfPoint);

        polygonToMerge.rotateOrderByLeadingPoint(intersectionPoint);

        updateNeighboursWithMergedPolygon(polygonToMerge, rootToMergeInto);
        //remove the last point since it is common with the root polygon - so not to add it twice
        polygonToMerge.removeByIndex(polygonToMerge.getSize() - 1);
        //merge the two polygons
        rootToMergeInto.getPoints().addAll(indexOfPoint, polygonToMerge.getPoints());
        rootToMergeInto.getAdjacentPolygons().addAll(indexOfPoint, polygonToMerge.getAdjacentPolygons());

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

    private static void updateNeighboursWithMergedPolygon(Polygon polygonToMerge, Polygon rootToMergeInto) {
        List<Polygon> adjacentPolygons = polygonToMerge.getAdjacentPolygons();
        for (Polygon adjacentPolygon : adjacentPolygons) {
            if (adjacentPolygon != null) {
                Integer indexOfMergedPolygonInNeighbours = adjacentPolygon.getIndexByPolygon(polygonToMerge);
                adjacentPolygon.setPolygon(indexOfMergedPolygonInNeighbours, rootToMergeInto);
            }
        }
    }

    public static Polygon populatePolygons(Triangle triangle, List<Polygon> polygons, Map<Set<Point>, Polygon> mapOfPolygons) {
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

    private static boolean isNewPolygon(Map<Set<Point>, Polygon> mapOfPolygons, HashSet<Point> keyOfCurrentTriangle) {
        return !mapOfPolygons.containsKey(keyOfCurrentTriangle);
    }

    private static Polygon getPolygon(List<Polygon> polygons, Polygon keyPolygon) {
        for (Polygon polygon : polygons) {
            if (polygon.equals(keyPolygon)) {
                return polygon;
            }
        }
        return null;
    }
}
