package il.ac.idc.jdt.extra.constraint;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import il.ac.idc.jdt.DelaunayTriangulation;
import il.ac.idc.jdt.Point;
import il.ac.idc.jdt.Segment;
import il.ac.idc.jdt.Triangle;
import il.ac.idc.jdt.extra.constraint.datamodel.Line;
import il.ac.idc.jdt.extra.constraint.datamodel.Polygon;
import il.ac.idc.jdt.extra.constraint.helper.Converter;
import il.ac.idc.jdt.extra.los.Section;
import il.ac.idc.jdt.extra.los.Visibility;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: daniels
 * Date: 5/24/12
 */

public class ConstrainedDelaunayTriangulation extends DelaunayTriangulation
{
    private Collection<Polygon> polygons = new CopyOnWriteArrayList<Polygon>();
    private Map<Set<Point>, Polygon> mapOfPolygons = Maps.newHashMap();


    public ConstrainedDelaunayTriangulation(Point[] ps, Segment[] constraints)
    {
        super(ps);
        //TODO: validate constraints segments are based upon ps points
    }

    public ConstrainedDelaunayTriangulation(Collection<Point> points, Collection<Segment> constraints)
    {
        super(points);
        //TODO: validate constraints segments are based upon ps points
    }

    public void addConstraint(Line line) {
        Section section = Visibility.computeSection(this, line.getP1(), line.getP2());
        List<Triangle> triangles = section.getTriangles();

        for (Triangle triangle : triangles) {
            Polygon polygon = mapOfPolygons.get(Sets.newHashSet(triangle.getA(), triangle.getB(), triangle.getC()));
            polygon.setMarkForMerge(true);
            for (Polygon adjacentPolygon:polygon.getAdjacentPolygons()) {
                if(adjacentPolygon.isMarkForMerge()) {
                    mergeTwoPolygons(adjacentPolygon, polygon);
                }
            }
        }


    }

    private void mergeTwoPolygons(Polygon polygonToMerge, Polygon rootToMergeInto) {
        mapOfPolygons.remove(polygonToMerge.getKey());
        mapOfPolygons.remove(rootToMergeInto.getKey());


        mergeTwoPolygonsLogic(polygonToMerge, rootToMergeInto);

        polygons.remove(polygonToMerge);
        mapOfPolygons.put(rootToMergeInto.getKey(), rootToMergeInto);
    }

    public void mergeTwoPolygonsLogic(Polygon polygonToMerge, Polygon rootToMergeInto) {
        Converter.mergeTwoPolygons(polygonToMerge, rootToMergeInto);
    }



    public ConstrainedDelaunayTriangulation(Point[] ps) {
        super(ps);
    }

    public ConstrainedDelaunayTriangulation(Collection<Point> points) {
        super(points);
        mapOfPolygons = Converter.fromTrianglesToPolygons(getTriangulation());
        polygons = mapOfPolygons.values();
    }
}
