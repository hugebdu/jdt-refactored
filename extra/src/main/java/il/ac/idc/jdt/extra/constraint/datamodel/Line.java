package il.ac.idc.jdt.extra.constraint.datamodel;
import  il.ac.idc.jdt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Innak
 * Date: 6/1/12
 * Time: 7:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class Line {

    private Point p1;
    private Point p2;

    public Point getP1() {
        return p1;
    }

    public void setP1(Point p1) {
        this.p1 = p1;
    }

    public Point getP2() {
        return p2;
    }

    public void setP2(Point p2) {
        this.p2 = p2;
    }

    public Line(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public boolean isConnectedToLine(Point p11, Point p22) {
        if (p1.equals(p11) || p1.equals(p22)) {
            return true;
        }

        if (p2.equals(p11) || p2.equals(p22)) {
            return true;
        }

       return false;
    }
}
