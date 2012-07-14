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
    boolean isFirst = true;
    private Set<Polygon> polygons = new HashSet<Polygon>();
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
        Polygon merged = cleanPolygonInLineDirection(line);
        if (merged == null) {
            throw new RuntimeException("Something went wrong in cleaning all polygons is constraint direction");
        };
        //stage 2
        DivideMergedPolygonByConstraint divideMergedPolygonByConstraint = new DivideMergedPolygonByConstraint(line, merged).invoke();
        Polygon side1 = divideMergedPolygonByConstraint.getSide1();
        Polygon side2 = divideMergedPolygonByConstraint.getSide2();

        //stage 3 - change this method of you want to implement some other strategy to divide polygons
        divideSidesToTriangles(side1, side2);
    }

    private void divideSidesToTriangles(Polygon side1, Polygon side2) {
        List<Polygon> newTriangles = Lists.newArrayList();
        divideToConstrainedPolygons(side1, newTriangles);
        divideToConstrainedPolygons(side2, newTriangles);


        mapOfPolygons.remove(side1.getKey());
        mapOfPolygons.remove(side2.getKey());

        polygons.remove(side1);
        polygons.remove(side2);

        for (Polygon p : newTriangles) {
            mapOfPolygons.put(p.getKey(), p);
            polygons.add(p);
        }
    }

    private Polygon cleanPolygonInLineDirection(Line line) {
        List<Polygon> effectedPolygons = findEffectedPolygons(line.getP1());
        Polygon firstPolygon = getPolygonsInDirectionToP2(line, effectedPolygons).iterator().next();

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

        mapOfPolygons.remove(merged.getKey());
        polygons.remove(merged);
        return merged;
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
                if (HelperMethods.isPointOnTheLine(line, p)) {
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

    /**
     * Go over possible lines (different pairs of point from the polygon). if line is inside the polygon then divide
     * the polygon by this line. do the same algorithm on each of the new created sides till all sides are triangles
     * @param polygonToSplit
     * @param newTriangles
     */
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
                        if (!p1.equals(p2)) {
                            Line splitCandidate = new Line(p1, p2);
                            if (HelperMethods.isLineInsidePolygon(polygonToSplit, splitCandidate, getBoundingBox())){
                                Polygon newSide1 = new Polygon();
                                Polygon newSide2 = new Polygon();
                                HelperMethods.dividePolygonByLine(splitCandidate, polygonToSplit, newSide1, newSide2);
                                divideToConstrainedPolygons(newSide1, newTriangles);
                                divideToConstrainedPolygons(newSide2, newTriangles);

                                foundSplit = true;
                                break;
                            }
                        } else {

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
    private List<Polygon> getPolygonsInDirectionToP2(Line line, List<Polygon> effectedPolygonsP1) {
        List<Polygon> polygonInTheRightDirection = Lists.newArrayList();
        for (Polygon polygon : effectedPolygonsP1) {
            if (polygon != null) {
                List<Line> linesFromPolygon = polygon.getLinesFromPolygon();
                for (Line lineFromPolygon : linesFromPolygon) {
                    if (!line.isConnectedToLine(lineFromPolygon.getP1(), lineFromPolygon.getP2())) {
                        if (areLinesIntersect(line, lineFromPolygon)) {
                            polygonInTheRightDirection.add(polygon);
                            break;
                        } else if (HelperMethods.isPointOnTheLine(line, lineFromPolygon.getP1()) || HelperMethods.isPointOnTheLine(line, lineFromPolygon.getP2())) {
                            polygonInTheRightDirection.add(polygon);
                            break;
                        }
                    }
                }
            }
        }

        return polygonInTheRightDirection;
    }

    private boolean areLinesIntersect(Line l1, Line l2) {
        return Line2D.linesIntersect(l1.getP1().getX(), l1.getP1().getY(), l1.getP2().getX(), l1.getP2().getY(),
                l2.getP1().getX(), l2.getP1().getY(), l2.getP2().getX(), l2.getP2().getY());
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

        List<Polygon> intersectingPolygons = getPolygonsInDirectionToP2(line, currentPolygon.getAdjacentPolygons());
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
        polygons = new HashSet<Polygon>(mapOfPolygons.values());
    }

    public Collection<Polygon> getPolygons() {
        return polygons;
    }

    private class DivideMergedPolygonByConstraint {
        private Line line;
        private Polygon merged;
        private Polygon side1;
        private Polygon side2;

        public DivideMergedPolygonByConstraint(Line line, Polygon merged) {
            this.line = line;
            this.merged = merged;
        }

        public Polygon getSide1() {
            return side1;
        }

        public Polygon getSide2() {
            return side2;
        }

        public DivideMergedPolygonByConstraint invoke() {
            side1 = new Polygon();
            side2 = new Polygon();
            HelperMethods.dividePolygonByLine(line, merged, side1, side2);

            mapOfPolygons.put(side1.getKey(), side1);
            mapOfPolygons.put(side2.getKey(), side2);
            polygons.add(side1) ;
            polygons.add(side2) ;
            return this;
        }
    }
}