package il.ac.idc.jdt.gui2;

import com.google.common.eventbus.Subscribe;
import il.ac.idc.jdt.Point;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import java.util.EventObject;

/**
 * Created by IntelliJ IDEA.
 * User: daniels
 * Date: 6/9/12
 */
public class StatusPanel extends JPanel
{
    public static final int HEIGHT = 16;
    final JLabel label;

    public StatusPanel()
    {
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        label = new JLabel("");
        label.setHorizontalAlignment(SwingConstants.LEFT);
        add(label);
    }

    @Subscribe
    public void onMouseOverOPoint(MouseOverPointEvent event)
    {
        if (event.point == null)
            label.setText("");
        else
            label.setText(event.point.toString());
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
