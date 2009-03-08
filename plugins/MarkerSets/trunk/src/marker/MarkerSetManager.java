package marker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import marker.MarkerSetsPlugin.ChangeListener;
import marker.MarkerSetsPlugin.Event;

import org.gjt.sp.jedit.View;

@SuppressWarnings("serial")
public class MarkerSetManager extends JPanel {
	private View view;
	private DefaultTreeModel model;
	private JTree markers;
	private DefaultMutableTreeNode root;
	
	public MarkerSetManager(View view)
	{
		super(new BorderLayout());
		this.view = view;
		root = new DefaultMutableTreeNode();
		model = new DefaultTreeModel(root);
		markers = new JTree(model);
		markers.setRootVisible(false);
		markers.setShowsRootHandles(true);
		DefaultTreeCellRenderer renderer = new MarkerSetRenderer();
		markers.setCellRenderer(renderer);
		add(markers, BorderLayout.CENTER);
		updateTree();
		markers.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				TreePath tp = markers.getPathForLocation(e.getX(), e.getY());
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					tp.getLastPathComponent();
				Object obj = node.getUserObject();
				if (obj instanceof FileMarker) {
					FileMarker marker = (FileMarker) obj;
					marker.jump(MarkerSetManager.this.view);
				}
			}
		});
		MarkerSetsPlugin.addChangeListener(new ChangeListener() {
			public void changed(Event e, Object o) {
				updateTree();
			}
		});
	}
	
	public void updateTree()
	{
		root.removeAllChildren();
		Vector<String> names = MarkerSetsPlugin.getMarkerSetNames();
		for (String name: names)
		{
			MarkerSet ms = MarkerSetsPlugin.getMarkerSet(name);
			DefaultMutableTreeNode msNode = new DefaultMutableTreeNode(ms);
			root.add(msNode);
			Vector<FileMarker> children = ms.getMarkers();
			for (FileMarker marker: children)
				msNode.add(new DefaultMutableTreeNode(marker));
		}
		model.nodeStructureChanged(root);
		for (int i = 0; i < markers.getRowCount(); i++)
			markers.expandRow(i);
	}
	
	private class MarkerSetRenderer extends DefaultTreeCellRenderer
	{
		private HashMap<MarkerSet, MarkerSetIcon> icons;
		private int iconWidth, iconHeight;
		
		public MarkerSetRenderer() {
			icons = new HashMap<MarkerSet, MarkerSetIcon>();
			iconWidth = getOpenIcon().getIconWidth();
			iconHeight = getOpenIcon().getIconHeight();
			setOpenIcon(null);
			setClosedIcon(null);
			setLeafIcon(null);
		}
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object obj = node.getUserObject();
			MarkerSet ms = null;
			if (obj instanceof MarkerSet)
			{
				ms = (MarkerSet) obj;
				value = ms.getName();
			}
			Component c = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus);
			if (ms != null)
			{
				MarkerSetIcon icon = icons.get(ms);
				if (icon == null)
				{
					icon = new MarkerSetIcon(ms);
					icons.put(ms, icon);
				}
				setIcon(icon);
			}
			return c;
		}
		
		private class MarkerSetIcon implements Icon
		{
			private MarkerSet ms;
			public MarkerSetIcon(MarkerSet ms) {
				this.ms = ms;
			}
			public Color getColor() {
				return ms.getColor();
			}
			public int getIconHeight() {
				return iconHeight;
			}
			public int getIconWidth() {
				return iconWidth;
			}
			public void paintIcon(Component c, Graphics g, int x, int y) {
				Color prevColor = g.getColor();
				g.setColor(ms.getColor());
				g.fillRect(0, 0, iconWidth, iconHeight);
				g.setColor(prevColor);
				
			}
		}
	}
}
