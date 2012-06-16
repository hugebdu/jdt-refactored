package il.ac.idc.jdt.gui2;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import il.ac.idc.jdt.DelaunayTriangulation;
import il.ac.idc.jdt.IOParsers;
import il.ac.idc.jdt.Point;
import il.ac.idc.jdt.Triangle;
import il.ac.idc.jdt.extra.constraint.ConstrainedDelaunayTriangulation;
import il.ac.idc.jdt.extra.constraint.datamodel.*;
import il.ac.idc.jdt.extra.constraint.datamodel.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static il.ac.idc.jdt.gui2.CanvasPanel.ClearEvent;
import static il.ac.idc.jdt.gui2.CanvasPanel.LoadPointsEvent;
import static il.ac.idc.jdt.gui2.SegmentsPanel.LinesAddedEvent;
import static java.lang.String.format;

/**
 * Created by IntelliJ IDEA.
 * User: daniels
 * Date: 5/27/12
 */
public class Main extends JFrame
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    CanvasPanel canvasPanel;
    StatusPanel statusPanel;
    SegmentsPanel segmentsPanel;
    
    final EventBus eventBus = new EventBus();
    final MenuDispatcher menuDispatcher = new MenuDispatcher();

    Main()
    {
        initFrame();
        eventBus.register(canvasPanel);
        eventBus.register(statusPanel);
        eventBus.register(segmentsPanel);
        doShow();
    }

    private void doShow()
    {
        setVisible(true);
    }

    private void initFrame()
    {
        setTitle("Delaunay Triangulation GUI");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // menu
        setMenuBar(menuBar(
                menu("File",
                        menuItem("Load...", loadFileAction()),
                        menuItem("Clear", clearAction()))));

        // canvas panel
        canvasPanel = new CanvasPanel(eventBus);
        add(canvasPanel, BorderLayout.LINE_START);

        // segments panel
        segmentsPanel = new SegmentsPanel(eventBus);
        add(segmentsPanel, BorderLayout.LINE_END);

        // status bar panel
        statusPanel = new StatusPanel();
        statusPanel.setPreferredSize(new Dimension(getWidth(), StatusPanel.HEIGHT));
        add(statusPanel, BorderLayout.PAGE_END);

        pack();
    }

    private Action clearAction()
    {
        return new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                eventBus.post(new ClearEvent(Main.this));
            }
        };
    }

    private MenuBar menuBar(Menu ... menus)
    {
        MenuBar menuBar = new MenuBar();
        for (Menu menu : menus)
            menuBar.add(menu);
        return menuBar;
    }
    
    private Menu menu(String text, MenuItem ... items)
    {
        Menu menu = new Menu(text);
        for (MenuItem item : items)
            menu.add(item);
        return menu;
    }
    
    private MenuItem menuItem(String text, Action action)
    {
        MenuItem menuItem = new MenuItem(text);
        menuItem.setActionCommand(text);
        menuItem.addActionListener(menuDispatcher);
        menuDispatcher.put(text, action);
        return menuItem;
    }

    private Action loadFileAction()
    {
        return new AbstractAction()
        {
            final JFileChooser fileChooser = createFileChooser();

            @Override
            public void actionPerformed(ActionEvent e)
            {
                int result = fileChooser.showOpenDialog(Main.this);
                if (result == JFileChooser.APPROVE_OPTION)
                    tryLoadingFile(fileChooser.getSelectedFile());
            }

            private JFileChooser createFileChooser()
            {
                JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
                fileChooser.setFileFilter(new FileNameExtensionFilter("TSIN files (*.tsin)", "tsin"));
                return fileChooser;
            }
        }; 
    }

    private void tryLoadingFile(File file)
    {
        try
        {
            List<Point> points = IOParsers.readPoints(file);
            eventBus.post(new LoadPointsEvent(this, points));
            ConstrainedDelaunayTriangulation constrainedDelaunayTriangulation = new ConstrainedDelaunayTriangulation(points);

            eventBus.post(new LinesAddedEvent(this, constrainedDelaunayTriangulation));
        }
        catch (Exception e)
        {
            String message = format("Failed to load points from file %s: %s", file.getAbsolutePath(), e.getMessage());
            JOptionPane.showMessageDialog(this,
                    message,
                    "Error loading TSIN file",
                    JOptionPane.ERROR_MESSAGE);
            LOGGER.warn(message, e);
        }
    }

    public static void main(String[] args)
    {
        Main main = new Main();
        main.doShow();
    }

    class MenuDispatcher extends HashMap<String, Action> implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            String actionCommand = e.getActionCommand();
            if (containsKey(actionCommand))
                get(actionCommand).actionPerformed(e);
        }
    }
}
