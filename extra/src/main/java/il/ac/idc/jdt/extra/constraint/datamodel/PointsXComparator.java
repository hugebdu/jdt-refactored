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
public class PointsXComparator implements Comparator<Point> {

    @Override
    public int compare(Point o1, Point o2) {
        if (o1.getX() < o2.getX()) {
            return -1;
        } else if (o1.getX() == o2.getX()) {
            return 0;
        }
        return 1;
    }
}
