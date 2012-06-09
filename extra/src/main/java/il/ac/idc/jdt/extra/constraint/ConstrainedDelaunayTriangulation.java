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

import java.awt.geom.Line2D;
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

    /**
     * Returned all the polygons that this is one of their points
     * @param p
     */
    public List<Polygon> findEffectedPolygons(Point p) {
        List<Polygon> toReturn = Lists.newArrayList();
        for (Polygon polygon : polygons) {
            if (polygon.getPoints().contains(p)) {
                toReturn.add(polygon);
            }
        }

        return toReturn;
    }

    public void addConstraint(Line line) {

//        List<Polygon> effectedPolygonsP1 = findEffectedPolygons(line.getP1());
//        Polygon polygonInTheRightDirection1 = getIntersectingPolygons(line, effectedPolygonsP1);
//        Triangle triangle11 = getByPoints(polygonInTheRightDirection1.getPoints());
//
//        List<Polygon> effectedPolygonsP2 = findEffectedPolygons(line.getP2());
//        Polygon polygonInTheRightDirection2 = getIntersectingPolygons(line, effectedPolygonsP2);
//        Triangle triangle12 = getByPoints(polygonInTheRightDirection2.getPoints());
//
//        Section section = Visibility.computeSection(this, line.getP1(), triangle11, line.getP2(), triangle12);
//        List<Triangle> triangles = section.getTriangles();
//
//        for (Triangle triangle : triangles) {
//            Polygon polygon = mapOfPolygons.get(Sets.newHashSet(triangle.getA(), triangle.getB(), triangle.getC()));
//            polygon.setMarkForMerge(true);
//            for (Polygon adjacentPolygon:polygon.getAdjacentPolygons()) {
//                if(adjacentPolygon.isMarkForMerge()) {
//                    mergeTwoPolygons(adjacentPolygon, polygon);
//                }
//            }
//        }


    }

    private List<Polygon> getIntersectingPolygons(Line line, List<Polygon> effectedPolygonsP1) {
        List<Polygon> polygonInTheRightDirection = null;
        for (Polygon polygon : effectedPolygonsP1) {
            List<Point> points = polygon.getPoints();
            for (int i=0; i< points.size(); i++) {
                int curr = i;
                int next = (i + 1) < points.size()? (i+1) : 0;
                if (!line.isConnectedToLine(points.get(curr), points.get(next))) {
                    if (areLinesIntersect(line, points, curr, next)) {
                        polygonInTheRightDirection.add(polygon);
                    }
                }
            }

        }
        return polygonInTheRightDirection;
    }

    private boolean areLinesIntersect(Line line, List<Point> points, int curr, int next) {
        return Line2D.linesIntersect(line.getP1().getX(), line.getP1().getY(), line.getP2().getX(), line.getP2().getY(),
                points.get(curr).getX(), points.get(curr).getY(), points.get(next).getX(), points.get(next).getY());
    }

    private void fillPolygonsInTheRightDirection(Line line, Polygon currentPolygon, List<Polygon> polygonsInSight) {
     //   getIntersectingPolygons(currentPolygon.g)
      //  }
       // polygonsInSight.add(polygonInTheRightDirection);
    }

    private void mergeTwoPolygons(Polygon polygonToMerge, Polygon rootToMergeInto) {
        mapOfPolygons.remove(polygonToMerge.getKey());
        mapOfPolygons.remove(rootToMergeInto.getKey());


        mergeTwoPolygonsLogic(polygonToMerge, rootToMergeInto);

        polygons.remove(polygonToMerge);
        mapOfPolygons.put(rootToMergeInto.getKey(), rootToMergeInto);
    }

    private void mergeTwoPolygonsLogic(Polygon polygonToMerge, Polygon rootToMergeInto) {
        Converter.mergeTwoPolygons(polygonToMerge, rootToMergeInto);
    }

    public ConstrainedDelaunayTriangulation(Collection<Point> points) {
        super(points);
        List<Triangle> triangulation = getTriangulation();
        mapOfPolygons = Converter.fromTrianglesToPolygons(triangulation);
        polygons = mapOfPolygons.values();
    }

    public Collection<Polygon> getPolygons() {
        return polygons;
    }
}
