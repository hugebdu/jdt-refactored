package il.ac.idc.jdt.extra.constraint;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import il.ac.idc.jdt.DelaunayTriangulation;
import il.ac.idc.jdt.Point;
import il.ac.idc.jdt.Triangle;
import il.ac.idc.jdt.extra.constraint.datamodel.Line;
import il.ac.idc.jdt.extra.constraint.datamodel.PointsXComparator;
import il.ac.idc.jdt.extra.constraint.datamodel.PointsYComparator;
import il.ac.idc.jdt.extra.constraint.datamodel.Polygon;
import il.ac.idc.jdt.extra.constraint.helper.HelperMethods;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
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
    private List<Point> allPoints = Lists.newArrayList();
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

    public void addConstraints(List<Line> lines) {
        for (Line line : lines) {
            addConstraint(line);
        }
    }

    public void addConstraint(Line line) {
        validateLine(line);

        List<Line> lines = splitLineByPointsOnLine(line);
        for (Line lineSegment : lines) {
            dealWithSingleLine(lineSegment);
        }
    }

    private void dealWithSingleLine(Line line) {
        for (Polygon p:polygons) {
            List<Line> linesFromPolygon = p.getLinesFromPolygon();
            if (linesFromPolygon.contains(line)) {
                return;
            }
        }
        //stage 1
        List<Polygon> effectedPolygons = findEffectedPolygons(line.getP1());
        Polygon firstPolygon = getFirstPolygonInDirectionOfP2(line, effectedPolygons).iterator().next();

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

        //stage 2
        Polygon side1 = new Polygon();
        Polygon side2 = new Polygon();
        HelperMethods.dividePolygonByLine(line, merged, side1, side2);

        mapOfPolygons.remove(merged.getKey());
        polygons.remove(merged);

        mapOfPolygons.put(side1.getKey(), side1);
        mapOfPolygons.put(side2.getKey(), side2);
        polygons.add(side1) ;
        polygons.add(side2) ;

        //stage 3
        List<Polygon> newTriangles = Lists.newArrayList();
        divideToConstrainedPolygons(side1, newTriangles);
        divideToConstrainedPolygons(side2, newTriangles);


        mapOfPolygons.remove(side1.getKey());
        mapOfPolygons.remove(side1.getKey());
        polygons.remove(side1);
        polygons.remove(side2);

        for (Polygon p : newTriangles) {
            mapOfPolygons.put(p.getKey(), p);
            polygons.add(p);
        }
    }

    /**
     * In case there are original points in triangulation that are on the requested constraint line split the long
     * constraint line into segments according to the points on the line.
     * If not additional point found on the line the return a list with the line itself
     * @param line
     */
    private  List<Line> splitLineByPointsOnLine(Line line) {
        Point2D p1 = new Point2D.Double();
        p1.setLocation(line.getP1().getX(), line.getP1().getY());

        Point2D p2 = new Point2D.Double();
        p2.setLocation(line.getP2().getX(), line.getP2().getY());

        Line2D l = new Line2D.Double();
        l.setLine(p1, p2);

        List<Point> pointsOnLongSegment = Lists.newArrayList();
        pointsOnLongSegment.add(line.getP1());
        pointsOnLongSegment.add(line.getP2());
        for (Point p:allPoints) {
            if (!p.equals(line.getP1()) && !p.equals(line.getP2())) {
                if (l.ptSegDist(p.getX(), p.getY()) == 0.0D) {
                      pointsOnLongSegment.add(p);
                }
            }
        }
        List<Line> linesToReturn = Lists.newArrayList();

        if (line.getP1().getY() == line.getP2().getY()) {
            Collections.sort(pointsOnLongSegment, new PointsXComparator());
        } else {
            Collections.sort(pointsOnLongSegment, new PointsYComparator());
        }
        for (int i=0; i< pointsOnLongSegment.size()-1; i++) {
            linesToReturn.add(new Line(pointsOnLongSegment.get(i), pointsOnLongSegment.get(i+1)));
        }

        return linesToReturn;
    }

    private void validateLine(Line line) {
        if (!allPoints.contains(line.getP1()) || !allPoints.contains(line.getP2())) {
            throw new IllegalArgumentException("Constraint [" + line + " ] must be a subset of the original points");
        }
    }

    private void divideToConstrainedPolygons(Polygon polygonToSplit, List<Polygon> newTriangles) {
        if (polygonToSplit.isTriangle()) {
            newTriangles.add(polygonToSplit);
        } else {
            List<Polygon> side1AdjacentPolygons = polygonToSplit.getAdjacentPolygons();
            //try to start from different points
            boolean foundSplit = false;
            for (int j = 0; j < side1AdjacentPolygons.size()-1; j++) {
                if (!foundSplit) {
                    for (int i = j+2; i < side1AdjacentPolygons.size(); i++) {
                        Point p1 = polygonToSplit.getPoints().get(j);
                        Point p2 = polygonToSplit.getPoints().get(i);

                        Line splitCandidate = new Line(p1, p2);
                        if (HelperMethods.isLineInsidePolygon(polygonToSplit, splitCandidate, getBoundingBox().maxX())){
                            Polygon newSide1 = new Polygon();
                            Polygon newSide2 = new Polygon();
                            HelperMethods.dividePolygonByLine(splitCandidate, polygonToSplit, newSide1, newSide2);
                            divideToConstrainedPolygons(newSide1, newTriangles);
                            divideToConstrainedPolygons(newSide2, newTriangles);
                            foundSplit = true;
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
        }
    }


    /**
     * returns the polygons that intersects with the given line in order from p1 to p2
     * @param line
     * @param effectedPolygonsP1
     * @return
     */
    private List<Polygon> getFirstPolygonInDirectionOfP2(Line line, List<Polygon> effectedPolygonsP1) {
        List<Polygon> polygonInTheRightDirection = Lists.newArrayList();
        for (Polygon polygon : effectedPolygonsP1) {
            if (polygon != null) {
                List<Point> points = polygon.getPoints();
                for (int i=0; i< points.size(); i++) {
                    int curr = i;
                    int next = (i + 1) < points.size() ? (i+1) : 0;
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

    /**
     * fills polygon in sight from the currentPolygon(directed form sideP1) along the line
     * @param line
     * @param currentPolygon
     * @param polygonsInSight
     */
    private void fillPolygonsInTheRightDirection(Line line, Polygon currentPolygon, List<Polygon> polygonsInSight) {
        List<Polygon> intersectingPolygons = getFirstPolygonInDirectionOfP2(line, currentPolygon.getAdjacentPolygons());
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
        Polygon polygon = HelperMethods.mergeTwoPolygons(polygonToMerge, rootToMergeInto);
        return polygon;
    }

    public ConstrainedDelaunayTriangulation(Point[] ps) {
        this(Arrays.asList(ps));
    }
    public ConstrainedDelaunayTriangulation(Collection<Point> points) {
        super(points);
        allPoints.addAll(points);
        List<Triangle> triangulation = getTriangulation();
        mapOfPolygons = HelperMethods.fromTrianglesToPolygons(triangulation);
        polygons = new CopyOnWriteArrayList<Polygon>(mapOfPolygons.values());
    }

    public Collection<Polygon> getPolygons() {
        return polygons;
    }
}
