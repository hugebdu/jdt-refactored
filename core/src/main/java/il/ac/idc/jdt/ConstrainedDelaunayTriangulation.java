package il.ac.idc.jdt;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: daniels
 * Date: 5/24/12
 */

public class ConstrainedDelaunayTriangulation extends DelaunayTriangulation
{
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
}
