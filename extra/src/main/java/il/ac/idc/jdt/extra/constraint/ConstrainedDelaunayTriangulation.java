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

/**
 * Created by IntelliJ IDEA.
 * User: daniels
 * Date: 5/24/12
 */

public class ConstrainedDelaunayTriangulation extends DelaunayTriangulation
{
    private Collection<Polygon> polygons = Lists.newArrayList();
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
        int indexOfPolygonToMergeInRootPolygon = getIndexOfPolygonToMergeInRootPolygon(polygonToMerge, rootToMergeInto);
        Point connectionPoint1 = rootToMergeInto.getPoints().get(indexOfPolygonToMergeInRootPolygon);

        //TODO: complete merge
    }




    private int getIndexOfPolygonToMergeInRootPolygon(Polygon polygonToMerge, Polygon rootToMergeInto) {
        ArrayList<Polygon> surroundingPolygons = rootToMergeInto.getAdjacentPolygons();
        ArrayList<Point> points = rootToMergeInto.getPoints();
        for(int i=0; i<rootToMergeInto.getSize(); i++) {
            if (surroundingPolygons.get(i).equals(polygonToMerge)) {
                return i;
            }

        }

        throw new RuntimeException("illegal input");
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
