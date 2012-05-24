package il.ac.idc.jdt;

import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: daniels
 * Date: 5/24/12
 */
public class Segment
{
    private List<Point> points = newLinkedList();

    public List<Point> getPoints()
    {
        return points;
    }

    public void setPoints(List<Point> points)
    {
        this.points = points;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Segment segment = (Segment) o;

        if (points != null ? !points.equals(segment.points) : segment.points != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return points != null ? points.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return "Segment{" +
                "points=" + points +
                '}';
    }
}
