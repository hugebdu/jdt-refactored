package il.ac.idc.jdt.extra.constraint.datamodel;

import il.ac.idc.jdt.Point;

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: Innak
 * Date: 6/16/12
 * Time: 8:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class PointsYComparator implements Comparator<Point> {

    @Override
    public int compare(Point o1, Point o2) {
        if (o1.getY() < o2.getY()) {
            return -1;
        } else if (o1.getY() == o2.getY()) {
            return 0;
        }
        return 1;
    }
}
