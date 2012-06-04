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
