package il.ac.idc.jdt.gui2;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import il.ac.idc.jdt.extra.constraint.ConstrainedDelaunayTriangulation;
import il.ac.idc.jdt.extra.constraint.datamodel.Line;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static il.ac.idc.jdt.gui2.CanvasPanel.ClearEvent;

/**
 * Created by IntelliJ IDEA.
 * User: daniels
 * Date: 6/9/12
 */
public class SegmentsPanel extends JPanel
{
    private static final Dimension PREFERRED_SIZE = new Dimension(250, 600);

    final EventBus eventBus;
    
    final JList list;
    final DefaultListModel model = new DefaultListModel();

    boolean editingEnabled = true;

    public SegmentsPanel(EventBus eventBus)
    {
        this.eventBus = eventBus;
        setPreferredSize(PREFERRED_SIZE);
        
        add(new JLabel("Segments:"));
        list = new JList(model);
        list.setBorder(new BevelBorder(BevelBorder.LOWERED));
        list.setPreferredSize(new Dimension(230, 200));
        list.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.isPopupTrigger())
                    doPopupMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (e.isPopupTrigger())
                    doPopupMenu(e);
            }

            private void doPopupMenu(MouseEvent e)
            {
                JPopupMenu menu = popupMenu();
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        add(list);
    }

    public JPopupMenu popupMenu()
    {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(removeSelectedMenuItem());
        popupMenu.add(new JPopupMenu.Separator());
        popupMenu.add(removeAllMenuItem());
        return popupMenu;
    }

    private JMenuItem removeAllMenuItem()
    {
        boolean hasItems = !SegmentsPanel.this.model.isEmpty();
        JMenuItem item = new JMenuItem(onRemoveAllSegments());
        item.setEnabled(hasItems && isSegmentsEditingEnabled());
        return item;
    }

    private Action onRemoveAllSegments()
    {
        return new AbstractAction("Remove all segments")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                model.clear();
                eventBus.post(new AllSegmentsRemovedEvent(SegmentsPanel.this));
            }
        };
    }

    private JMenuItem removeSelectedMenuItem()
    {
        Object[] selectedValues = list.getSelectedValues();
        boolean selected = selectedValues != null && selectedValues.length > 0;
        JMenuItem item = new JMenuItem(onRemoveSelectedSegment());
        item.setEnabled(selected && isSegmentsEditingEnabled());
        return item;
    }

    private boolean isSegmentsEditingEnabled()
    {
        return editingEnabled;
    }

    private Action onRemoveSelectedSegment()
    {
        return new AbstractAction("Remove selected segment(s)")
        {
            @SuppressWarnings("SuspiciousToArrayCall")
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Object[] selectedValues = list.getSelectedValues();

                for (Object selectedValue : selectedValues)
                    model.removeElement(selectedValue);

                Line[] selectedLines = newArrayList(selectedValues).toArray(new Line[selectedValues.length]);
                eventBus.post(new SelectedSegmentsRemovedEvent(SegmentsPanel.this, newHashSet(selectedLines)));
            }
        };
    }

    @Subscribe
    public void onSegmentAdded(SegmentAddedEvent event)
    {
        model.addElement(event.line);
    }

    @Subscribe
    public void onClear(ClearEvent event)
    {
        model.clear();
        this.editingEnabled = true;
    }

    @Subscribe
    public void onTriangulationCalculated(TriangulationCalculatedEvent event)
    {
        editingEnabled = false;
    }

    @Subscribe
    public void onTriangulationReset(TriangulationResetEvent event)
    {
        editingEnabled = true;
    }
    
    public static class SelectedSegmentsRemovedEvent extends EventObject
    {
        final Set<Line> removedSegments;

        public SelectedSegmentsRemovedEvent(Object source, Set<Line> removedSegments)
        {
            super(source);
            this.removedSegments = removedSegments;
        }
    }
    
    public static class AllSegmentsRemovedEvent extends EventObject
    {
        public AllSegmentsRemovedEvent(Object source)
        {
            super(source);
        }
    }
    
    public static class SegmentAddedEvent extends EventObject
    {
        final Line line;

        public SegmentAddedEvent(Object source, Line line)
        {
            super(source);
            this.line = line;
        }
    }

    public static class TriangulationResetEvent extends EventObject
    {
        public TriangulationResetEvent(Object source)
        {
            super(source);
        }
    }

    public static class TriangulationCalculatedEvent extends EventObject
    {
        final ConstrainedDelaunayTriangulation triangulation;

        public TriangulationCalculatedEvent(Object source, ConstrainedDelaunayTriangulation triangulation)
        {
            super(source);
            this.triangulation = triangulation;
        }
    }
}
