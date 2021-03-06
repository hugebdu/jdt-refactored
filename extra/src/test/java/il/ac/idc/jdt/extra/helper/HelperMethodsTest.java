package il.ac.idc.jdt.extra.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import il.ac.idc.jdt.BoundingBox;
import il.ac.idc.jdt.Point;
import il.ac.idc.jdt.Triangle;
import il.ac.idc.jdt.extra.constraint.ConstrainedDelaunayTriangulation;
import il.ac.idc.jdt.extra.constraint.datamodel.Line;
import il.ac.idc.jdt.extra.constraint.datamodel.Polygon;
import il.ac.idc.jdt.extra.constraint.helper.HelperMethods;
import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: Innak
 * Date: 5/30/12
 * Time: 12:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class HelperMethodsTest {
    private static Point p11;
    private static Point p22;
    private static Point p24;
    private static Point p34;
    private static Point p55;
    private static Point p41;
    private static Point p73;

    @Test
    public void testConvert() {
        ArrayList<Triangle> triangles = generateTriangelsForTriangulation();

        Map<Set<Point>,Polygon> setPolygonMap = HelperMethods.fromTrianglesToPolygons(triangles);
        assertEquals(6, setPolygonMap.size());

        Set<Point> key = Sets.newHashSet(p22, p34, p24);
        Polygon polygonToMerge = setPolygonMap.get(key);
        Set<Point> key2 = Sets.newHashSet(p22, p55, p34);
        Polygon polygonRoot = setPolygonMap.get(key2);

        Polygon polygon = HelperMethods.mergeTwoPolygons(polygonToMerge, polygonRoot);

        assertEquals(polygon.getSize(), 4);
    }

    @Test
    public void testAddLine() {
        testAddConstraint(2D, 3D, 5D, 4D, 6);
        testAddConstraint(3D, 1D, 6D, 3D, 6);
        testAddConstraint(1D, 2D, 5D, 4D, 4);
        testAddConstraint(5D, 1D, 4D, 4D, 5);
    }

    private void testAddConstraint(double x1, double y1, double x2, double y2, int sizeToAssert) {
        ConstrainedDelaunayTriangulation delaunayTriangulation = generateLeagelTriangulation();
        Point p1 = new Point(x1, y1, 2D);
        Point p2 = new Point(x2, y2, 3D);

        delaunayTriangulation.addConstraint(new Line(p1, p2));
        Collection<Polygon> polygons = delaunayTriangulation.getPolygons();
        assertEquals(sizeToAssert, polygons.size());
    }

    public static ArrayList<Triangle> generateTriangelsForTriangulation() {
        p11 = new Point(1D, 1D, 1D);
        p22 = new Point(2D, 2D, 2D);
        p24 = new Point(2D, 4D, 3D);
        p34 = new Point(3D, 4D, 3D);
        p55 = new Point(5D, 5D, 4D);
        p41 = new Point(4D, 1D, 5D);
        p73 = new Point(7D, 3D, 6D);

        Triangle t1 = new Triangle(p11, p22, p24);
        Triangle t2 = new Triangle(p22, p34, p24);
        Triangle t3 = new Triangle(p22, p55, p34);
        Triangle t4 = new Triangle(p22, p41, p55);
        Triangle t5 = new Triangle(p41, p73, p55);
        Triangle t6 = new Triangle(p22, p11, p41);

        t1.setAbTriangle(t6);
        t1.setBcTriangle(t2);
        t1.setCanext(null);

        t2.setAbTriangle(t3);
        t2.setBcTriangle(null);
        t2.setCanext(t1);

        t3.setAbTriangle(t4);
        t3.setBcTriangle(null);
        t3.setCanext(t2);

        t4.setAbTriangle(t6);
        t4.setBcTriangle(t5);
        t4.setCanext(t3);

        t5.setAbTriangle(null);
        t5.setBcTriangle(null);
        t5.setCanext(t4);

        t6.setAbTriangle(t1);
        t6.setBcTriangle(null);
        t6.setCanext(t4);

        return Lists.newArrayList(t1, t2, t3, t4, t5, t6);
    }

    public static ConstrainedDelaunayTriangulation generateLeagelTriangulation() {
        Point pp12 = new Point(1D, 2D, 1D);
        Point pp23 = new Point(2D, 3D, 2D);
        Point pp44 = new Point(4D, 4D, 3D);
        Point pp54 = new Point(5D, 4D, 3D);
        Point pp63 = new Point(6D, 3D, 4D);
        Point pp51 = new Point(5D, 1D, 5D);
        Point pp31 = new Point(3D, 1D, 6D);
        Point pp43 = new Point(4D, 3D, 6D);

        ConstrainedDelaunayTriangulation t = new ConstrainedDelaunayTriangulation(Lists.newArrayList(pp12, pp23, pp44, pp54, pp63, pp51, pp31, pp43));
//
        return t;
    }


    @Test
    public void testIntersectionPoint() {
        Point p1 = new Point(1D, 1D);
        Point p2 = new Point(2D, 2D);

        Point p3 = new Point(0D, 1D);
        Point p4 = new Point(5D, 1D);

        checkIntersection(p1, p2, p3, p4, 1D, 1D);


        p1 = new Point(0D, 0D);
        p2 = new Point(1D, 6D);

        p3 = new Point(1D, 3D);
        p4 = new Point(7D, 3D);

        checkIntersection(p1, p2, p3, p4, 0.5D, 3D);
    }


    @Test
    public void testIntersectionPointNotOnLine() {
        Point p1 = new Point(1D, 1D);
        Point p2 = new Point(2D, 2D);

        Point p3 = new Point(4D, 3D);
        Point p4 = new Point(5D, 3D);

        Line l1 = new Line(p1, p2);
        Line l2 = new Line(p3, p4);

        Point intersectionPoint = HelperMethods.findIntersectionPoint(l1, l2);
        assertNull(intersectionPoint);
    }

    @Test
    public void testIntersectionPointOnTipFromLeft() {
        Point p3 = new Point(2D, 1D);
        Point p4 = new Point(2D, 3D);
        Line l2 = new Line(p3, p4);

        Point p1 = new Point(1D, 1D);
        Point p2 = new Point(2D, 2D);
        Line l1 = new Line(p1, p2);

        Point intersectionPoint = HelperMethods.findIntersectionPoint(l1, l2);
        assertEquals(p2, intersectionPoint);

        p1 = new Point(2D, 2D);
        p2 = new Point(1D, 7D);
        l1 = new Line(p1, p2);

        intersectionPoint = HelperMethods.findIntersectionPoint(l1, l2);
        assertEquals(p1, intersectionPoint);

        p1 = new Point(2D, 2D);
        p2 = new Point(1D, 2D);
        l1 = new Line(p1, p2);

        intersectionPoint = HelperMethods.findIntersectionPoint(l1, l2);
        assertEquals(p1, intersectionPoint);
    }


    @Test
    public void testIntersectionPointOnTipFromRight() {
        Point p3 = new Point(2D, 1D);
        Point p4 = new Point(2D, 3D);
        Line l2 = new Line(p3, p4);

        Point p1 = new Point(2D, 2D);
        Point p2 = new Point(3D, 2D);
        Line l1 = new Line(p1, p2);

        Point intersectionPoint = HelperMethods.findIntersectionPoint(l1, l2);
        assertNull(intersectionPoint);

        p1 = new Point(2D, 2D);
        p2 = new Point(3D, 7D);
        l1 = new Line(p1, p2);

        intersectionPoint = HelperMethods.findIntersectionPoint(l1, l2);
        assertNull(intersectionPoint);

        p1 = new Point(4D, 1D);
        p2 = new Point(2D, 2D);
        l1 = new Line(p1, p2);

        intersectionPoint = HelperMethods.findIntersectionPoint(l1, l2);
        assertNull(intersectionPoint);
    }

    private void checkIntersection(Point p1, Point p2, Point p3, Point p4, double x, double y) {
        Line l1 = new Line(p1, p2);
        Line l2 = new Line(p3, p4);
        Point intersectionPoint = HelperMethods.findIntersectionPoint(l1, l2);
        assertEquals(x, intersectionPoint.getX());
        assertEquals(y, intersectionPoint.getY());
    }

    @Test
    public void testIsLineInsidePolygon() {
        Point p33 = new Point(3, 3);
        Point p13 = new Point(1, 3);
        Point p22 = new Point(2, 2);
        Point p11 = new Point(1, 1);
        Point p20 = new Point(2, 0);
        Point p30 = new Point(3, 0);

        List<Point> points = Lists.newArrayList(p33, p13, p22, p11, p20, p30);

        List<Polygon> polygons = Lists.newArrayList(null, null, null, null, null, null);
        Polygon pToTest = new Polygon(points, polygons);

        Line lineInside = new Line(p22, p30);
        BoundingBox boundingBox = new BoundingBox(0, 3, 0, 3, 0, 3);
        boolean lineInsidePolygon = HelperMethods.isLineInsidePolygon(pToTest, lineInside, boundingBox);
        assertTrue(lineInsidePolygon);

        lineInside = new Line(p33, p20);
        lineInsidePolygon = HelperMethods.isLineInsidePolygon(pToTest, lineInside, boundingBox);
        assertTrue(lineInsidePolygon);

        lineInside = new Line(p13, p20);
        lineInsidePolygon = HelperMethods.isLineInsidePolygon(pToTest, lineInside, boundingBox);
        assertFalse(lineInsidePolygon);

        lineInside = new Line(p13, p11);
        lineInsidePolygon = HelperMethods.isLineInsidePolygon(pToTest, lineInside, boundingBox);
        assertFalse(lineInsidePolygon);

        lineInside = new Line(p33, p11);
        lineInsidePolygon = HelperMethods.isLineInsidePolygon(pToTest, lineInside, boundingBox);
        assertFalse(lineInsidePolygon);
    }

    @Test
    public void testIsPointOnTheLine() {
        Point p1 = new Point(1, 1);
        Point p2 = new Point(2, 2);

        Line l1 = new Line(p1, p2);

        boolean pointOnTheLine = HelperMethods.isPointOnTheLine(l1, new Point(1.1D, 1.1D));
        assertTrue(pointOnTheLine);

        pointOnTheLine = HelperMethods.isPointOnTheLine(l1, new Point(1D, 1D));
        assertTrue(pointOnTheLine);

        pointOnTheLine = HelperMethods.isPointOnTheLine(l1, new Point(2D, 2D));
        assertTrue(pointOnTheLine);

        pointOnTheLine = HelperMethods.isPointOnTheLine(l1, new Point(1.9D, 1.9D));
        assertTrue(pointOnTheLine);

        pointOnTheLine = HelperMethods.isPointOnTheLine(l1, new Point(3D, 3D));
        assertTrue(!pointOnTheLine);

        pointOnTheLine = HelperMethods.isPointOnTheLine(l1, new Point(1.1D, 1.2D));
        assertTrue(!pointOnTheLine);

        Point p12 = new Point(1, 1);
        Point p22 = new Point(2, 1);

        Line l2 = new Line(p12, p22);

        pointOnTheLine = HelperMethods.isPointOnTheLine(l2, new Point(1.5D, 1D));
        assertTrue(pointOnTheLine);

        pointOnTheLine = HelperMethods.isPointOnTheLine(l2, new Point(3D, 1D));
        assertTrue(!pointOnTheLine);

    }

    public void testIsPointOnTheLineVerticalHorisontal() {
        Point p13 = new Point(1, 1);
        Point p23 = new Point(1, 2);

        Line l3 = new Line(p13, p23);

        boolean pointOnTheLine = HelperMethods.isPointOnTheLine(l3, new Point(1D, 1.5D));
        assertTrue(pointOnTheLine);

        pointOnTheLine = HelperMethods.isPointOnTheLine(l3, new Point(1D, 1D));
        assertTrue(pointOnTheLine);

        pointOnTheLine = HelperMethods.isPointOnTheLine(l3, new Point(1D, 2D));
        assertTrue(pointOnTheLine);

        pointOnTheLine = HelperMethods.isPointOnTheLine(l3, new Point(1D, 0.5D));
        assertTrue(!pointOnTheLine);

        pointOnTheLine = HelperMethods.isPointOnTheLine(l3, new Point(0D, 7D));
        assertTrue(!pointOnTheLine);

        Point p12 = new Point(1, 1);
        Point p22 = new Point(2, 1);

        Line l2 = new Line(p12, p22);

        pointOnTheLine = HelperMethods.isPointOnTheLine(l2, new Point(1.5D, 1D));
        assertTrue(pointOnTheLine);

        pointOnTheLine = HelperMethods.isPointOnTheLine(l2, new Point(1D, 1D));
        assertTrue(pointOnTheLine);

        pointOnTheLine = HelperMethods.isPointOnTheLine(l2, new Point(2D, 1D));
        assertTrue(pointOnTheLine);

        pointOnTheLine = HelperMethods.isPointOnTheLine(l2, new Point(3D, 1D));
        assertTrue(!pointOnTheLine);
    }

    @Test
    public void testMergeTwoPolygons() {

        Point p21 = new Point(2,1);
        Point p61 = new Point(6,1);
        Point p63 = new Point(6,3);
        Point p75 = new Point(7,5);
        Point p35 = new Point(3,5);
        Point p03 = new Point(0,3);
        Point p43 = new Point(4,3);


        List<Point> points = Lists.newArrayList(p21, p61, p63, p75, p35, p03, p43);

        ConstrainedDelaunayTriangulation triangulation = new ConstrainedDelaunayTriangulation(points);
        System.out.println(triangulation);
        triangulation.addConstraint(new Line(p75, p21));



        Polygon polygon1 = new Polygon();
        Polygon polygon2 = new Polygon();
        Polygon polygon3 = new Polygon();
        Polygon polygon4 = new Polygon();
        Polygon polygon5 = new Polygon();
        Polygon polygon6 = new Polygon();

        polygon1.addPointsAndPolygons(Lists.newArrayList(p63, p75, p35), Lists.newArrayList(null, null, polygon3));
        polygon2.addPointsAndPolygons(Lists.newArrayList(p61, p63, p21), Lists.newArrayList(null, polygon4, null));
        polygon3.addPointsAndPolygons(Lists.newArrayList(p63, p35, p43), Lists.newArrayList(polygon1, polygon5, polygon4));
        polygon4.addPointsAndPolygons(Lists.newArrayList(p21, p63, p43), Lists.newArrayList(polygon2, polygon3, polygon5));
        polygon5.addPointsAndPolygons(Lists.newArrayList(p43, p35, p21), Lists.newArrayList(polygon3, polygon6, polygon4));
        polygon6.addPointsAndPolygons(Lists.newArrayList(p35, p03, p21), Lists.newArrayList(null, null, polygon5));


        Polygon merged1 = HelperMethods.mergeTwoPolygons(polygon3, polygon4);
        assertEquals(4, merged1.getSize());

        Polygon merged2 = HelperMethods.mergeTwoPolygons(merged1, polygon5);
        assertEquals(3, merged2.getSize());

        List<Point> merged2Points = merged2.getPoints();
        assertTrue(merged2Points.contains(p63));
        assertTrue(merged2Points.contains(p35));
        assertTrue(merged2Points.contains(p21));
       // Polygon merged3 = HelperMethods.mergeTwoPolygons(polygon1, polygon2);
    }
}


