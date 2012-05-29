package il.ac.idc.jdt.helper;

import com.google.common.collect.Lists;
import il.ac.idc.jdt.Polygon;
import il.ac.idc.jdt.Triangle;

import java.util.List;

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
    public static List<Polygon> fromTrianglesToPolygons(List<Triangle> triangles) {
        List<Polygon> polygons = Lists.newArrayList();

        Triangle someTriangle = triangles.iterator().next();
        populatePolygons(someTriangle, polygons);
        return polygons;
    }

    public static Polygon populatePolygons(Triangle triangle, List<Polygon> polygons) {
        if (triangle == null) {
            return null;
        }

        Polygon polygon = new Polygon();
        polygon.addPoints(triangle.getA(), triangle.getB(), triangle.getC());
        if (!polygons.contains(polygon)) {
            polygons.add(polygon);
            polygon.addPolygon(populatePolygons(triangle.getAbTriangle(), polygons));
            polygon.addPolygon(populatePolygons(triangle.getBcTriangle(), polygons));
            polygon.addPolygon(populatePolygons(triangle.getCaTriangle(), polygons));
        } else {
            Polygon fullPolygon = getPolygon(polygons, polygon);
            return fullPolygon;
        }

        return polygon;
    }

    //TODO: move to map
    private static Polygon getPolygon(List<Polygon> polygons, Polygon keyPolygon) {
        for (Polygon polygon : polygons) {
            if (polygon.equals(keyPolygon)) {
                return polygon;
            }
        }
        return null;
    }
}
