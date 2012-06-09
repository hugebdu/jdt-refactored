package il.ac.idc.jdt.extra.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import il.ac.idc.jdt.*;
import il.ac.idc.jdt.Point;
import il.ac.idc.jdt.extra.DataGeneratorHelper;
import il.ac.idc.jdt.extra.constraint.ConstrainedDelaunayTriangulation;
import il.ac.idc.jdt.extra.constraint.datamodel.*;
import il.ac.idc.jdt.extra.constraint.datamodel.Polygon;
import il.ac.idc.jdt.extra.constraint.helper.Converter;
import org.junit.Test;
import java.util.*;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: Innak
 * Date: 5/30/12
 * Time: 12:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConverterTest {
    private static Point p11;
    private static Point p22;
    private static Point p24;
    private static Point p34;
    private static Point p55;
    private static Point p41;
    private static Point p73;

    private static Point pp23;
    private static Point pp54;

    @Test
    public void testConvert() {
        ArrayList<Triangle> triangles = generateTriangelsForTriangulation();

        Map<Set<Point>,Polygon> setPolygonMap = Converter.fromTrianglesToPolygons(triangles);
        assertEquals(6, setPolygonMap.size());

        Set<Point> key = Sets.newHashSet(p22, p34, p24);
        Polygon polygonToMerge = setPolygonMap.get(key);
        Set<Point> key2 = Sets.newHashSet(p22, p55, p34);
        Polygon polygonRoot = setPolygonMap.get(key2);

        Converter.mergeTwoPolygons(polygonToMerge, polygonRoot);

        System.out.println(polygonRoot);
    }

    @Test
    public void testAddLine() {
        ConstrainedDelaunayTriangulation delaunayTriangulation = generateLeagelTriangulation();

        Point pp23 = new Point(2D, 3D, 2D);
        Point pp54 = new Point(5D, 4D, 3D);

        delaunayTriangulation.addConstraint(new Line(pp23, pp54));
        Collection<Polygon> polygons = delaunayTriangulation.getPolygons();
        System.out.println(polygons);
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
        pp23 = new Point(2D, 3D, 2D);
        Point pp44 = new Point(4D, 4D, 3D);
        pp54 = new Point(5D, 4D, 3D);
        Point pp63 = new Point(6D, 3D, 4D);
        Point pp51 = new Point(5D, 1D, 5D);
        Point pp31 = new Point(3D, 1D, 6D);
        Point pp43 = new Point(4D, 3D, 6D);

        ConstrainedDelaunayTriangulation t = new ConstrainedDelaunayTriangulation(Lists.newArrayList(pp12, pp23, pp44, pp54, pp63, pp51, pp31, pp43));

//        Triangle t1 = new Triangle(p23, p12, p31);
//        Triangle t2 = new Triangle(p23, p31, p43);
//        Triangle t3 = new Triangle(p23, p43, p44);
//        Triangle t4 = new Triangle(p44, p54, p43);
//        Triangle t5 = new Triangle(p54, p63, p43);
//        Triangle t6 = new Triangle(p43, p63, p51);
//        Triangle t7 = new Triangle(p43, p51, p31);
//
        return t;
    }
}


