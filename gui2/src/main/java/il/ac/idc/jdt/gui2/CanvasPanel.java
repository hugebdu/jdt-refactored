package il.ac.idc.jdt.gui2;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Doubles;
import il.ac.idc.jdt.Point;
import il.ac.idc.jdt.extra.constraint.ConstrainedDelaunayTriangulation;
import il.ac.idc.jdt.extra.constraint.datamodel.*;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.vecmath.Point2i;
import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newHashSet;
import static il.ac.idc.jdt.gui2.SegmentsPanel.*;
import static il.ac.idc.jdt.gui2.StatusPanel.MouseOverPointEvent;
import static java.util.Arrays.asList;

/**
 * Created by IntelliJ IDEA.
 * User: daniels
 * Date: 5/27/12
 */
public class CanvasPanel extends JPanel
{
    private static final Ordering<Point> xOrdering = new Ordering<Point>()
    {
        @Override
        public int compare(Point left, Point right)
        {
            return Doubles.compare(left.getX(), right.getX());
        }
    };

    private static final Ordering<Point> yOrdering = new Ordering<Point>()
    {
        @Override
        public int compare(Point left, Point right)
        {
            return Doubles.compare(left.getY(), right.getY());
        }
    };


    private static final Color     POINT_COLOR                = Color.black;
    private static final Dimension DIMENSION                  = new Dimension(800, 600);
    private static final int       PADDING                    = 20;
    private static final double    SELECTION_GRID_HALF_RADIUS = 3;

    final EventBus eventBus;

    SelectionGrid selectionGrid = new SelectionGrid();
    Collection<Point> points;
    Set<Line> segments;
    Set<Line> lines;
    ConstrainedDelaunayTriangulation t;

    AffineTransform transform = new AffineTransform();
    private final MouseManager mouseManager;

    public CanvasPanel(EventBus eventBus)
    {
        this.eventBus = eventBus;

        setBackground(Color.white);
        setPreferredSize(DIMENSION);
        mouseManager = new MouseManager();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;

        paintPoints(graphics2D);
        paintSegments(graphics2D);
        paintLines(graphics2D);
    }

    private void paintSegments(Graphics2D g)
    {
        if (segments != null)
        {
            g.setColor(Color.green);
            for (Line line : segments)
            {
                Line2D line2d = toLine2d(line);
                g.draw(line2d);
            }
        }
    }

    private void paintLines(Graphics2D g)
    {
        if (lines != null)
        {
            g.setColor(Color.cyan);
            for (Line line : lines)
            {
                Line2D line2d = toLine2d(line);
                g.draw(line2d);
            }
        }
    }
    
    private Function<Line, Line2D> toLine2d()
    {
        return new Function<Line, Line2D>()
        {
            @Override
            public Line2D apply(Line input)
            {
                return toLine2d(input);
            }
        };
    }

    private Line2D toLine2d(Line line)
    {
        return new Line2D.Double(
                line.getP1().getX(), line.getP1().getY(),
                line.getP2().getX(), line.getP2().getY());
    }

    private void paintPoints(Graphics2D g)
    {
        g.setTransform(transform);

        if (points == null || points.isEmpty())
        {
            return;
        }

        g.setColor(POINT_COLOR);

        for (Point point : points)
            paintPoint(point, g);
    }

    private void paintPoint(Point point, Graphics2D g)
    {
        Point2i intPoint = toIntPoint(point);

        g.drawLine(intPoint.x, intPoint.y, intPoint.x, intPoint.y);

        if (point == mouseManager.getSelected())
        {
            Color prevColor = g.getColor();
            AffineTransform prevTransform = g.getTransform();
            
            g.setTransform(new AffineTransform());
            g.setColor(Color.red);

            Point2i transformedPoint = toTransformedIntPoint(point);

            Ellipse2D circle = new Ellipse2D.Double(
                    transformedPoint.x - 3,
                    transformedPoint.y - 3,
                    6,
                    6);

            g.draw(circle);

            g.setColor(prevColor);
            g.setTransform(prevTransform);
        }
    }

    private Point2i toIntPoint(Point point)
    {
        return new Point2i(
                (int) point.getX(),
                (int) point.getY());
    }

    public Collection<Point> getPoints()
    {
        return points;
    }

    public void setPoints(Point... points)
    {
        setPoints(asList(points));
    }

    public void setPoints(Collection<Point> points)
    {
        this.points = points;
        adjustAffineTransformation();
        initializeSelectionGrid();
    }

    private void initializeSelectionGrid()
    {
        selectionGrid.initialize();
    }

    Point2i toTransformedIntPoint(Point point)
    {
        Point2D source = new Point2D.Double(point.getX(), point.getY());
        Point2D target = new Point2D.Double();
        transform.transform(source, target);
        return new Point2i(
                (int) target.getX(),
                (int) target.getY());
    }

    void adjustAffineTransformation()
    {
        double maxX = xOrdering.max(points).getX();
        double minX = xOrdering.min(points).getX();

        double maxY = yOrdering.max(points).getY();
        double minY = yOrdering.min(points).getY();

        double xScaleFactor = (DIMENSION.getWidth() - PADDING) / (maxX - minX);
        double yScaleFactor = (DIMENSION.getHeight() - PADDING) / (maxY - minY);

        transform = new AffineTransform();
        transform.scale(xScaleFactor, yScaleFactor);

        double xScaledPadding = PADDING * (1 / xScaleFactor) / 2;
        double yScaledPadding = PADDING * (1 / yScaleFactor) / 2;

        transform.translate(xScaledPadding - minX, yScaledPadding - minY);
    }

    @Subscribe
    public void onLoadPoints(LoadPointsEvent event)
    {
        setPoints(event.points);
        repaint();
    }

    @Subscribe
    public void onSegmentAdded(SegmentAddedEvent event)
    {
        if (segments == null)
            segments = newHashSet();
        segments.add(event.line);

        t.addConstraint(event.line);
        if (lines != null) {
            lines.clear();
            lines.addAll(getLinesFromTriangulation());
        }
    }

    @Subscribe
    public void onLineAdded(LinesAddedEvent event)
    {
        this.t = event.triangulation;
        java.util.List<Line> linesToPaint = getLinesFromTriangulation();
        if (lines == null)
            lines = newHashSet();
        lines.addAll(linesToPaint);
    }

    private java.util.List<Line> getLinesFromTriangulation() {
        java.util.List<Line> linesToPaint = Lists.newArrayList();

        for (il.ac.idc.jdt.extra.constraint.datamodel.Polygon polygon : t.getPolygons()) {
            java.util.List<Point> points1 = polygon.getPoints();
            for (int i=0; i<points1.size(); i++) {
                if (i+1<points1.size()) {
                    Line ab = new Line(points1.get(i), points1.get(i+1));
                    linesToPaint.add(ab);
                }
            }

            Line ab = new Line(points1.get(0), points1.get(points1.size()-1));
            linesToPaint.add(ab);
        }
        System.out.println(linesToPaint);
        return linesToPaint;
    }

    @Subscribe
    public void onSelectedSegmentsRemoved(SelectedSegmentsRemovedEvent event)
    {
        segments.removeAll(event.removedSegments);
        repaint();
    }

    @Subscribe
    public void onAllSegmentsRemoved(AllSegmentsRemovedEvent event)
    {
        segments = newHashSet();
        repaint();
    }

    @Subscribe
    public void onClear(ClearEvent event)
    {
        points = newLinkedList();
        segments = newHashSet();
        mouseManager.reset();
        repaint();
        //TODO: implement
    }

    public static class LoadPointsEvent extends EventObject
    {
        final Collection<Point> points;

        public LoadPointsEvent(Object source, Collection<Point> points)
        {
            super(source);
            this.points = points;
        }
    }

    public static class ClearEvent extends EventObject
    {
        public ClearEvent(Object source)
        {
            super(source);
        }

    }

    class MouseManager
    {
        Point hovered = null;
        Point selected = null;

        public MouseManager()
        {
            CanvasPanel.this.addMouseMotionListener(motionListener());
            CanvasPanel.this.addMouseListener(mouseListener());
        }

        private MouseListener mouseListener()
        {
            return new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    if (hovered != null)
                    {
                        if (selected != null && !selected.equals(hovered))
                        {
                            if (segmentAllowed(selected, hovered))
                            {
                                eventBus.post(new SegmentAddedEvent(CanvasPanel.this, new Line(selected, hovered)));
                                selected = null;
                            }
                            else
                            {
                                JOptionPane.showMessageDialog(CanvasPanel.this,
                                        "Intersecting segments aren't allowed.",
                                        "Illegal input",
                                        JOptionPane.WARNING_MESSAGE);
                            }
                        }
                        else if (hovered.equals(selected))
                        {
                            // clear selection
                            selected = null;
                        }
                        else
                        {
                            selected = hovered;
                        }

                        repaint();
                    }
                }
            };
        }

        public Point getHovered()
        {
            return hovered;
        }

        public void setHovered(Point hovered)
        {
            this.hovered = hovered;
        }

        public Point getSelected()
        {
            return selected;
        }

        public void setSelected(Point selected)
        {
            this.selected = selected;
        }

        private MouseMotionListener motionListener()
        {
            return new MouseMotionAdapter()
            {
                @Override
                public void mouseMoved(MouseEvent e)
                {
                    Point point = selectionGrid.getPointOrNullAt(e.getX(), e.getY());

                    if (point != null)
                    {
                        if (hovered == null || !hovered.equals(point))
                        {
                            eventBus.post(new MouseOverPointEvent(CanvasPanel.this, point));
                            hovered = point;
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        }
                    }
                    else
                    {
                        if (hovered != null)
                        {
                            eventBus.post(new MouseOverPointEvent(CanvasPanel.this, null));
                            hovered = null;
                            setCursor(Cursor.getDefaultCursor());
                        }
                    }
                }
            };
        }


        public void reset()
        {
            hovered = null;
        }

    }

    private boolean segmentAllowed(Point p1, Point p2)
    {
        if (segments == null || segments.isEmpty())
            return true;
        
        Line line = new Line(p1, p2);
        if (segments.contains(line))
            return false;

        Line2D newSegment = new Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        
        for (Line2D existingSegment : Iterables.transform(segments, toLine2d()))
        {
            if (!haveSharedVertex(newSegment, existingSegment) && existingSegment.intersectsLine(newSegment))
                return false;
        }

        return true;
    }

    private boolean haveSharedVertex(Line2D line1, Line2D line2)
    {
        return  line1.getP1().equals(line2.getP1()) ||
                line1.getP1().equals(line2.getP2()) ||
                line1.getP2().equals(line2.getP1()) ||
                line1.getP2().equals(line2.getP2());
    }

    class SelectionGrid
    {
        WeightedPointHolder[][] grid;

        public void initialize()
        {
            grid = new WeightedPointHolder[DIMENSION.width][DIMENSION.height];

            for (Point point : points)
            {
                Point2i transformedPoint = toTransformedIntPoint(point);
                placeMarks(point,
                        transformedPoint.x,
                        transformedPoint.y
                );
            }
        }

        private void placeMarks(Point point, int x, int y)
        {
            Ellipse2D ellipse = constructEllipse(x, y);

            for (int i = (int) ellipse.getMinX(); i < ellipse.getMaxX(); i++)
            {
                for (int j = (int) ellipse.getMinY(); j < ellipse.getMaxY(); j++)
                {
                    if (isValidGridLocation(i, j) && ellipse.contains(i, j))
                    {
                        float weight = calculateWeight(i, j, x, y);

                        if (grid[i][j] == null || grid[i][j].weight < weight)
                        {
                            grid[i][j] = new WeightedPointHolder(weight, point);
                        }
                    }
                }
            }
        }

        private float calculateWeight(int i, int j, int x, int y)
        {
            float distance = (float) Math.sqrt(Math.pow((x - i), y) + Math.pow((y - j), 2));
            return (float) (SELECTION_GRID_HALF_RADIUS - distance);
        }

        boolean isValidGridLocation(int x, int y)
        {
            return x >= 0 && y >= 0 &&
                    x < DIMENSION.width && y < DIMENSION.height;
        }

        private Ellipse2D constructEllipse(int x, int y)
        {
            return new Ellipse2D.Double(
                    x - SELECTION_GRID_HALF_RADIUS,
                    y - SELECTION_GRID_HALF_RADIUS,
                    SELECTION_GRID_HALF_RADIUS * 2,
                    SELECTION_GRID_HALF_RADIUS * 2);
        }

        public Point getPointOrNullAt(int x, int y)
        {
            if (grid != null && grid[x][y] != null)
            {
                return grid[x][y].point;
            }

            return null;
        }

        class WeightedPointHolder
        {
            final Point point;
            final float weight;

            WeightedPointHolder(float weight, Point point)
            {
                this.weight = weight;
                this.point = point;
            }
        }
    }
}
