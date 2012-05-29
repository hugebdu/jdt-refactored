package il.ac.idc.jdt;

import com.google.common.collect.Lists;
import il.ac.idc.jdt.helper.Converter;

import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: daniels
 * Date: 5/24/12
 */

public class ConstrainedDelaunayTriangulation extends DelaunayTriangulation
{
    private List<Polygon> polygons = Lists.newArrayList();

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

    public void addSegment(Segment segmentConstraint) {

    }

    public ConstrainedDelaunayTriangulation(Point[] ps) {
        super(ps);
    }

    public ConstrainedDelaunayTriangulation(Collection<Point> points) {
        super(points);
        polygons = Converter.fromTrianglesToPolygons(getTriangulation());
    }
}
