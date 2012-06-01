package il.ac.idc.jdt.extra.helper;

import com.google.common.collect.Lists;
import il.ac.idc.jdt.*;
import il.ac.idc.jdt.Point;
import il.ac.idc.jdt.extra.constraint.datamodel.*;
import il.ac.idc.jdt.extra.constraint.datamodel.Polygon;
import il.ac.idc.jdt.extra.constraint.helper.Converter;
import org.junit.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: Innak
 * Date: 5/30/12
 * Time: 12:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConverterTest {

    @Test
    public void testConvert() {
        Point p11 = new Point(1D, 1D, 1D);
        Point p22 = new Point(2D, 2D, 2D);
        Point p24 = new Point(2D, 4D, 3D);
        Point p34 = new Point(3D, 4D, 3D);
        Point p55 = new Point(5D, 5D, 4D);
        Point p41 = new Point(4D, 1D, 5D);
        Point p73 = new Point(7D, 3D, 6D);

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

        ArrayList<Triangle> triangles = Lists.newArrayList(t1, t2, t3, t4, t5, t6);

        Map<Set<Point>,Polygon> setPolygonMap = Converter.fromTrianglesToPolygons(triangles);
        assertEquals(6, setPolygonMap.size());
    }

}


