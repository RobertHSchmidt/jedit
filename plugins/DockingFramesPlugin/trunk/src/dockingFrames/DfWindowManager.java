package dockingFrames;

import java.awt.BorderLayout;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.gjt.sp.jedit.PerspectiveManager;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.View.ViewConfig;
import org.gjt.sp.jedit.gui.DockableWindowFactory;
import org.gjt.sp.jedit.gui.DockableWindowManager;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.extension.gui.dock.theme.eclipse.DefaultEclipseThemeConnector;
import bibliothek.gui.DockController;
import bibliothek.gui.DockStation;
import bibliothek.gui.DockUI;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.DefaultDockable;
import bibliothek.gui.dock.SplitDockStation;
import bibliothek.gui.dock.StackDockStation;
import bibliothek.gui.dock.dockable.DefaultDockableFactory;
import bibliothek.gui.dock.layout.PredefinedDockSituation;
import bibliothek.gui.dock.station.split.SplitDockPathProperty;
import bibliothek.gui.dock.themes.ThemeFactory;
import bibliothek.gui.dock.title.DockTitle;
import bibliothek.gui.dock.title.DockTitleVersion;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;

@SuppressWarnings("serial")
public class DfWindowManager extends DockableWindowManager {

	private static final String WEST = "West";
	private static final String EAST = "East";
	private static final String SOUTH = "South";
	private static final String NORTH = "North";
	private static final String CENTER = "center";
	private static final String MAIN = "main";

	private DockController controller;
	private SplitDockStation center;
	private StackDockStation north, south, west, east;
	private Map<String, DockStation> stations;
	private Factory factory;
	private Dockable mainPanel;
	private PredefinedDockSituation situation;
	private String theme;
	private Map<String, Dockable> created = new HashMap<String, Dockable>();
	
	
	public DfWindowManager(View view, DockableWindowFactory instance,
			ViewConfig config) {
		super(view, instance, config);
		setLayout(new BorderLayout());
		situation = new PredefinedDockSituation();
        stations = new HashMap<String, DockStation>();
		controller = new DockController();
		setTheme(DfOptionPane.getThemeName());
        center = new SplitDockStation();
        stations.put(CENTER, center);
        add(center.getComponent(), BorderLayout.CENTER);
        north = createStackDockStation(NORTH);
        south = createStackDockStation(SOUTH);
        east = createStackDockStation(EAST);
        west = createStackDockStation(WEST);
        controller.add(center);
        controller.getProperties().set(EclipseTheme.THEME_CONNECTOR,
        	new DefaultEclipseThemeConnector() {
                @Override
                public TitleBar getTitleBarKind(Dockable dockable) {
                    if (dockable == mainPanel)
                        return TitleBar.NONE_BORDERED;
                    return super.getTitleBarKind(dockable);
                }
            });
		situation.put(CENTER, center);
        factory = new Factory();
        situation.add(factory);
        PerspectiveManager.setPerspectiveDirty(true);
	}

	private void setTheme(String name) {
        ThemeFactory[] themes = DockUI.getDefaultDockUI().getThemes();
        for (ThemeFactory t: themes) {
        	if (t.getName().equals(name)) {
        		controller.setTheme(t.create());
        		theme = name;
        		break;
        	}
        }
	}
	public PredefinedDockSituation getDockSituation() {
		return situation;
	}
	
	public Factory getDockFactory() {
		return factory;
	}
	
	private StackDockStation createStackDockStation(String title)
	{
		StackDockStation s = new StackDockStation();
		controller.add(s);
		s.setTitleText(title);
		situation.put(title, s);
		return s;
	}
	
	@Override
	public void applyDockingLayout(DockingLayout docking) {
		if (docking != null) {
			DfDockingLayout layout = (DfDockingLayout) docking;
			String filename = layout.getPersistenceFilename();
			if (filename != null) {
				XElement root = null;
				try {
					root = XIO.readUTF(new FileInputStream(filename));
					situation.readXML(root);
					return;
				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				}
			}
		}		
		super.applyDockingLayout(docking);
		dropDockingAreas();
	}

	public Map<String, DockStation> getStationMap() {
		return stations;
	}
	
	@Override
	protected void dockingPositionChanged(String dockableName,
			String oldPosition, String newPosition) {
		showDockableWindow(dockableName);
	}

	@Override
	public void closeCurrentArea() {
		// TODO Auto-generated method stub

	}

	@Override
	public JComponent floatDockableWindow(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DockingArea getBottomDockingArea() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DockingLayout getDockingLayout(ViewConfig config) {
		DfDockingLayout layout = new DfDockingLayout(this);
		return layout;
	}

	@Override
	public DockingArea getLeftDockingArea() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DockingArea getRightDockingArea() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DockingArea getTopDockingArea() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void hideDockableWindow(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDockableWindowDocked(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDockableWindowVisible(String name) {
		JComponent c = getDockable(name);
		if (c == null)
			return false;
		return c.isVisible();
	}

	@Override
	protected void propertiesChanged() {
		super.propertiesChanged();
		String selectedTheme = DfOptionPane.getThemeName();
		if (! selectedTheme.equals(theme))
			setTheme(selectedTheme);
	}

	@Override
	public void setMainPanel(JPanel panel) {
		mainPanel = new MainDockable(panel);
		center.drop(mainPanel);
		situation.put(MAIN, mainPanel);
	}

	private void dropDockingArea(StackDockStation s,
		SplitDockPathProperty.Location location, double size)
	{
        if (s.getDockableCount() == 0)
        	return;
		SplitDockPathProperty p = new SplitDockPathProperty();
        p.add(location, size);
    	stations.get(CENTER).drop(s, p);
	}
	private void dropDockingAreas() {
        dropDockingArea(north, SplitDockPathProperty.Location.TOP, 0.2);
        dropDockingArea(south, SplitDockPathProperty.Location.BOTTOM, 0.2);
        dropDockingArea(west, SplitDockPathProperty.Location.LEFT, 0.2);
        dropDockingArea(east, SplitDockPathProperty.Location.RIGHT, 0.2);
	}

	@Override
	public void showDockableWindow(String name) {
		Dockable d = created.get(name);
		if (d != null) {
			if (d.getController() != null) {
				DockStation station = d.getDockParent();
				if (station != null)
					station.setFrontDockable(d);
				else
					d.getComponent().setVisible(true);
				return;
			} else {
				// Windows has been closed
				created.remove(d);
			}
		}
		d = createDefaultDockable(name);
		if (d == null)
			return;
		StackDockStation s;
		String position = getDockablePosition(name); 
		if (position.equals(DockableWindowManager.TOP))
			s = north;
		else if (position.equals(DockableWindowManager.BOTTOM))
			s = south;
		else if (position.equals(DockableWindowManager.RIGHT))
			s = east;
		else
			s = west;
		s.drop(d);
	}

	private JEditDockable createDefaultDockable(String name) {
		JComponent window = getDockable(name);
		if (window == null)
			window = createDockable(name);
		if (window == null)
			return null;
		String title = getDockableTitle(name);
		JEditDockable d = new JEditDockable(name, title, window);
		created.put(name, d);
		return d;
	}

	private static class MainDockable extends DefaultDockable {
		public MainDockable(JPanel panel) {
			super(panel, (String)null);
		}

		@Override
		public DockTitle getDockTitle(DockTitleVersion version) {
			return null;
		}
	}
	private static class JEditDockable extends DefaultDockable {
		String name;
		public JEditDockable(String name, String title, JComponent window) {
			super(window, title);
			this.name = name;
			setFactoryID("jEdit");
		}
		public String getName() {
			return name;
		}
	}
	private class Factory extends DefaultDockableFactory {

		@Override
		public String getID() {
			return "jEdit";
		}

		@Override
		public Object getLayout(DefaultDockable element,
				Map<Dockable, Integer> children)
		{
			if (element instanceof JEditDockable) {
				JEditDockable d = (JEditDockable) element;
				return d.getName();
			}
			return super.getLayout(element, children);
		}

		@Override
		public DefaultDockable layout(Object layout, Map<Integer, Dockable> children)
		{
			String name = (String) layout;
			return createDefaultDockable(name);
		}

		@Override
		public DefaultDockable layout(Object layout) {
			return layout(layout, null);
		}

		@Override
		public Object read(XElement element) {
			XElement child = element.getElement("Dockable");
			if (child == null)
				return super.read(element);
			return child.getString("name");
		}

		@Override
		public void write(Object layout, XElement element) {
			XElement child = new XElement("Dockable");
			child.addString("name", (String)layout);
			element.addElement(child);
		}
		
	}
}
