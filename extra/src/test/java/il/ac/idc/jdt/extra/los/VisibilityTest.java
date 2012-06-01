package il.ac.idc.jdt.extra.los;

import il.ac.idc.jdt.DelaunayTriangulation;
import il.ac.idc.jdt.IOParsers;
import il.ac.idc.jdt.Point;
import il.ac.idc.jdt.extra.los.Section;
import il.ac.idc.jdt.extra.los.Visibility;
import org.junit.Test;

import il.ac.idc.jdt.extra.constraint.helper.Converter;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class VisibilityTest {

	public void shouldSeeTwoPointsSameLine() {
		Point[] points = new Point[] { new Point(0, 0, 1), new Point(1, 1, 1), new Point(1, 0, 0), new Point(0, 1, 0) };

		DelaunayTriangulation dt = new DelaunayTriangulation(points);
		Visibility v = new Visibility();
		Section section = v.computeSection(dt, new Point(0, 0, 1), new Point(1, 1, 1));
		assertThat("Has simple visibility", v.isVisible(section));
	}

	public void shouldSeeTwoPointsCuttingTriangle() {
		// DT creates two triangles:
		// t1 [1,1,1][1.5,1,0][1.1,2,0]
		// t2 [1.1,2,0][1.5,1,0][2,2,1]
		// and therefore visibility should be between [1,1,0] and [2,2,0]
		Point[] points = new Point[] { new Point(1, 1, 1), new Point(2, 2, 1), new Point(1.5, 1, 0), new Point(1.1, 2, 0) };

		DelaunayTriangulation dt = new DelaunayTriangulation(points);
		System.out.println(dt.getTriangulation());
		Visibility v = new Visibility();
		Section section = v.computeSection(dt, new Point(1, 1, 1), new Point(2, 2, 1));
		assertThat("Doens't have simple visibility", v.isVisible(section));

	}

	public void shouldNotSeeTwoPoints() {
		// DT creates two triangles:
		// t1 [1,1,0][1.5,1,1][1.1,2,1]
		// t2 [1.1,2,1][1.5,1,1][2,2,0]
		// and therefore no visibility should be between [1,1,0] and [2,2,0]
		Point[] points = new Point[] { new Point(1, 1, 0), new Point(2, 2, 0), new Point(1.5, 1, 1), new Point(1.1, 2, 1) };

		DelaunayTriangulation dt = new DelaunayTriangulation(points);
		System.out.println(dt.getTriangulation());
		Visibility v = new Visibility();
		Section section = v.computeSection(dt, new Point(1, 1, 0), new Point(2, 2, 0));
		assertThat("Doens't have simple visibility", !v.isVisible(section));

	}


    @Test
    public void someTest() throws IOException {
        List<Point> points = IOParsers.readPoints("C:\\Users\\Innak\\workspace\\GitRepo\\ex4\\jdt-refactored\\core\\src\\test\\resources\\inputs\\t1_1000.tsin");
        DelaunayTriangulation delaunayTriangulation = new DelaunayTriangulation(points);
        Point p1 = new Point(4013100.0D, 606720.0D, 7D);
        Point p2 = new Point(4013640.0D, 610320.0, 8D);

        Visibility v = new Visibility();
        Section section = v.computeSection(delaunayTriangulation, p1, p2);
        System.out.println("f");
    }

}
