package il.ac.idc.jdt.extra.constraint.datamodel;

import il.ac.idc.jdt.Point;

/**
 * Created with IntelliJ IDEA.
 * User: Innak
 * Date: 6/1/12
 * Time: 7:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class ThreePointKey {

    private Point p1;
    private Point p2;
    private Point p3;


    public ThreePointKey(Point p1, Point p2, Point p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    public Point getP1() {
        return p1;
    }

    public Point getP2() {
        return p2;
    }

    public Point getP3() {
        return p3;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThreePointKey that = (ThreePointKey) o;

        if (p1 != null ? !p1.equals(that.p1) : that.p1 != null) return false;
        if (p2 != null ? !p2.equals(that.p2) : that.p2 != null) return false;
        if (p3 != null ? !p3.equals(that.p3) : that.p3 != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = p1 != null ? p1.hashCode() : 0;
        result = 31 * result + (p2 != null ? p2.hashCode() : 0);
        result = 31 * result + (p3 != null ? p3.hashCode() : 0);
        return result;
    }
}
