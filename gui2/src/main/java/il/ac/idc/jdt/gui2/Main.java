package il.ac.idc.jdt.gui2;

import com.google.common.collect.Iterables;
import com.google.common.eventbus.EventBus;
import il.ac.idc.jdt.IOParsers;
import il.ac.idc.jdt.Point;
import il.ac.idc.jdt.extra.constraint.ConstrainedDelaunayTriangulation;
import il.ac.idc.jdt.extra.constraint.datamodel.Line;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static il.ac.idc.jdt.gui2.CanvasPanel.ClearEvent;
import static il.ac.idc.jdt.gui2.CanvasPanel.LoadPointsEvent;
import static il.ac.idc.jdt.gui2.SegmentsPanel.TriangulationCalculatedEvent;
import static il.ac.idc.jdt.gui2.SegmentsPanel.TriangulationResetEvent;
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
        centerOnScreen();
        setVisible(true);
    }

    private void centerOnScreen()
    {
        int widthWindow = getWidth();
        int heightWindow = getHeight();

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int X = (screen.width / 2) - (widthWindow / 2); // Center horizontally.
        int Y = (screen.height / 2) - (heightWindow / 2); // Center vertically.

        setBounds(X, Y, widthWindow, heightWindow);
    }

    private void initFrame()
    {
        setTitle("Delaunay Triangulation GUI");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // menu
        setJMenuBar(menuBar(
                menu("File",
                        menuItem(loadFileAction()),
                        menuItem(clearAction())),
                menu("Triangulation",
                        menuItem(runTriangulationAction()),
                        menuItem(resetTriangulationAction()))));

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

    private Action resetTriangulationAction()
    {
        return new AbstractAction("Reset")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                eventBus.post(new TriangulationResetEvent(Main.this));
            }
        };
    }

    private Action runTriangulationAction()
    {
        return new AbstractAction("Run")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                TriangulationDataSource dataSource = getTriangulationDataSource();
                if (!isEmpty())
                {
                    Main.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    new TriangulationWorker(dataSource).execute();
                }
            }

            private boolean isEmpty()
            {
                TriangulationDataSource dataSource = getTriangulationDataSource();
                if (dataSource == null)
                    return true;

                return Iterables.isEmpty(dataSource.getPoints());
            }
        };
    }

    private TriangulationDataSource getTriangulationDataSource()
    {
        return canvasPanel;
    }

    private Action clearAction()
    {
        return new AbstractAction("Clear")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                eventBus.post(new ClearEvent(Main.this));
            }
        };
    }

    private JMenuBar menuBar(JMenu ... menus)
    {
        JMenuBar menuBar = new JMenuBar();
        for (JMenu menu : menus)
            menuBar.add(menu);
        return menuBar;
    }
    
    private JMenu menu(String text, JMenuItem ... items)
    {
        JMenu menu = new JMenu(text);
        for (JMenuItem item : items)
            menu.add(item);
        return menu;
    }
    
    private JMenuItem menuItem(Action action)
    {
        return new JMenuItem(action);
    }

    private Action loadFileAction()
    {
        return new AbstractAction("Load...")
        {
            final JFileChooser fileChooser = createFileChooser();

            @Override
            public void actionPerformed(ActionEvent e)
            {
                int result = fileChooser.showOpenDialog(Main.this);
                if (result == JFileChooser.APPROVE_OPTION)
                {
                    eventBus.post(new ClearEvent(Main.this));
                    tryLoadingFile(fileChooser.getSelectedFile());
                }
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

    class TriangulationWorker extends SwingWorker<ConstrainedDelaunayTriangulation, Object>
    {
        final TriangulationDataSource dataSource;

        TriangulationWorker(TriangulationDataSource dataSource)
        {
            this.dataSource = dataSource;
        }

        @Override
        protected ConstrainedDelaunayTriangulation doInBackground() throws Exception
        {
            ConstrainedDelaunayTriangulation triangulation = new ConstrainedDelaunayTriangulation(
                    newHashSet(dataSource.getPoints()));

            for (Line constraint : dataSource.getSegments())
                triangulation.addConstraint(constraint);
            
            return triangulation;
        }

        @Override
        protected void done()
        {
            try
            {
                eventBus.post(new TriangulationCalculatedEvent(Main.this, get()));
            }
            catch (Exception e)
            {
                String message = "Failed running constrained triangulation";
                JOptionPane.showMessageDialog(Main.this,
                        message,
                        "Error running triangulation",
                        JOptionPane.ERROR_MESSAGE);
                LOGGER.warn(message, e);
            }
            finally
            {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }
    
    public interface TriangulationDataSource
    {
        Iterable<Point> getPoints();
        Iterable<Line> getSegments();
    }
}
