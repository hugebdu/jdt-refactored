package il.ac.idc.jdt.extra.constraint;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import il.ac.idc.jdt.DelaunayTriangulation;
import il.ac.idc.jdt.Point;
import il.ac.idc.jdt.Segment;
import il.ac.idc.jdt.Triangle;
import il.ac.idc.jdt.extra.constraint.datamodel.Line;
import il.ac.idc.jdt.extra.constraint.datamodel.Polygon;
import il.ac.idc.jdt.extra.constraint.helper.Converter;

import java.awt.geom.Line2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        List<Polygon> effectedPolygons = findEffectedPolygons(line.getP1());
        Polygon firstPolygon = getIntersectingPolygons(line, effectedPolygons).iterator().next();

        List<Polygon> polygonsInSight = new CopyOnWriteArrayList<Polygon>();
        fillPolygonsInTheRightDirection(line, firstPolygon, polygonsInSight);

        Polygon merged = null;
        for (int i = 0; i < polygonsInSight.size(); i++) {
            if (i==0) {
                merged = mergeTwoPolygons(polygonsInSight.get(i), polygonsInSight.get(i+1));
            } else if ((i+1) < polygonsInSight.size()) {
                merged = mergeTwoPolygons(merged, polygonsInSight.get(i+1));
            }
        }


    }

    /**
     * returns the polygons that intersects with the given line in order from p1 to p2
     * @param line
     * @param effectedPolygonsP1
     * @return
     */
    private List<Polygon> getIntersectingPolygons(Line line, List<Polygon> effectedPolygonsP1) {
        List<Polygon> polygonInTheRightDirection = Lists.newArrayList();
        for (Polygon polygon : effectedPolygonsP1) {
            if (polygon != null) {
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
        }
        return polygonInTheRightDirection;
    }

    private boolean areLinesIntersect(Line line, List<Point> points, int curr, int next) {
        return Line2D.linesIntersect(line.getP1().getX(), line.getP1().getY(), line.getP2().getX(), line.getP2().getY(),
                points.get(curr).getX(), points.get(curr).getY(), points.get(next).getX(), points.get(next).getY());
    }

    private void fillPolygonsInTheRightDirection(Line line, Polygon currentPolygon, List<Polygon> polygonsInSight) {
        List<Polygon> intersectingPolygons = getIntersectingPolygons(line, currentPolygon.getAdjacentPolygons());
        for (Polygon intersectingPolygon : intersectingPolygons) {
            if (!polygonsInSight.contains(intersectingPolygon)) {
                polygonsInSight.add(intersectingPolygon);
                fillPolygonsInTheRightDirection(line, intersectingPolygon, polygonsInSight);
            }
        }
    }

    private Polygon mergeTwoPolygons(Polygon polygonToMerge, Polygon rootToMergeInto) {
        mapOfPolygons.remove(polygonToMerge.getKey());
        mapOfPolygons.remove(rootToMergeInto.getKey());

        polygons.remove(polygonToMerge);
        polygons.remove(rootToMergeInto);

        Polygon p = mergeTwoPolygonsLogic(polygonToMerge, rootToMergeInto);

        mapOfPolygons.put(p.getKey(), p);
        polygons.add(p);

        return p;
    }

    private Polygon mergeTwoPolygonsLogic(Polygon polygonToMerge, Polygon rootToMergeInto) {
        Polygon polygon = Converter.mergeTwoPolygons(polygonToMerge, rootToMergeInto);
        return polygon;
    }

    public ConstrainedDelaunayTriangulation(Point[] ps) {
        super(ps);
    }
    public ConstrainedDelaunayTriangulation(Collection<Point> points) {
        super(points);
        List<Triangle> triangulation = getTriangulation();
        mapOfPolygons = Converter.fromTrianglesToPolygons(triangulation);
        polygons = new CopyOnWriteArrayList<Polygon>(mapOfPolygons.values());
    }

    public Collection<Polygon> getPolygons() {
        return polygons;
    }
}
