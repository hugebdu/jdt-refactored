package il.ac.idc.jdt.gui2;

import com.google.common.eventbus.Subscribe;
import il.ac.idc.jdt.Point;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.util.EventObject;

import static il.ac.idc.jdt.gui2.Main.TriangulationStartedEvent;
import static il.ac.idc.jdt.gui2.SegmentsPanel.TriangulationCalculatedEvent;
import static il.ac.idc.jdt.gui2.SegmentsPanel.TriangulationResetEvent;

/**
 * Created by IntelliJ IDEA.
 * User: daniels
 * Date: 6/9/12
 */
public class StatusPanel extends JPanel
{
    public static final int HEIGHT = 16;
    final JLabel label;
    final JLabel label2;

    public StatusPanel()
    {
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        setLayout(new BorderLayout());
        
        label = new JLabel("");
        label.setHorizontalAlignment(SwingConstants.LEFT);
        add(label, BorderLayout.LINE_START);
        
        label2 = new JLabel("");
        label2.setHorizontalAlignment(SwingConstants.RIGHT);
        add(label2, BorderLayout.LINE_END);
    }

    @Subscribe
    public void onTriangulationCalculated(TriangulationCalculatedEvent e)
    {
        label2.setText(String.format("Running time (ms): %d", e.runtimeInMs));
    }

    @Subscribe
    public void onMouseOverOPoint(MouseOverPointEvent event)
    {
        if (event.point == null)
            label.setText("");
        else
            label.setText(event.point.toString());
    }

    @Subscribe
    public void onTriangulationReset(TriangulationResetEvent e)
    {
        label2.setText("");
    }

    @Subscribe
    public void onTriangulationStarted(TriangulationStartedEvent e)
    {
        label2.setText("");
    }
    
    public static class MouseOverPointEvent extends EventObject
    {
        final Point point;

        public MouseOverPointEvent(Object source, Point point)
        {
            super(source);
            this.point = point;
        }
    }
}
