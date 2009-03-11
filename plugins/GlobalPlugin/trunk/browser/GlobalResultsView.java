package browser;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import marker.FileMarker;
import marker.tree.SourceLinkTree;
import marker.tree.SourceLinkTree.SourceLinkParentNode;
import options.GlobalOptionPane;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.util.Log;

@SuppressWarnings("serial")
public class GlobalResultsView extends JPanel implements
	DefaultFocusComponent, GlobalDockableInterface
{
	private View view;
	private String param;
	private JTextField symbolTF;
	private JLabel statusLbl;
	private SourceLinkTree tree;
	
	public GlobalResultsView(final View view, String param)
	{
		super(new BorderLayout());
		this.view = view;
		this.param = param;
		JPanel symbolPanel = new JPanel(new BorderLayout());
		symbolPanel.add(new JLabel("Search for:"), BorderLayout.WEST);
		symbolTF = new JTextField(40);
		symbolTF.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					show(view, symbolTF.getText());
			}
		});
		symbolPanel.add(symbolTF, BorderLayout.CENTER);
		JPanel toolbar = new JPanel(new BorderLayout(5, 0));
		statusLbl = new JLabel("");
		toolbar.add(statusLbl, BorderLayout.EAST);
		symbolPanel.add(toolbar, BorderLayout.EAST);
		add(symbolPanel, BorderLayout.NORTH);
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setClosedIcon(null);
		renderer.setOpenIcon(null);
		renderer.setLeafIcon(null);
		tree = new SourceLinkTree(view);
		tree.setCellRenderer(renderer);
		add(new JScrollPane(tree), BorderLayout.CENTER);
	}
	
	protected String getParam()	{
		return param;
	}
	
	private String getBufferDirectory() {
		File file = new File(view.getBuffer().getPath());
		return file.getParent();
	}

	private String makeBold(String s) {
		return "<html><body><b>" + s + "</body></html>";
	}

	private class SearchNode extends DefaultMutableTreeNode {
		String search;
		public SearchNode(String search) {
			this.search = search;
		}
		public String toString() {
			int occurs = getLeafCount();
			int files = getChildCount();
			return makeBold(search + " (" + occurs + " occurrences in " + files +
				" files)");
		}
	}
	
	private class RecordQuery implements Runnable {
		String identifier;
		String workingDirectory;
		public RecordQuery(String identifier, String workingDirectory)
		{
			this.identifier = identifier;
			this.workingDirectory = workingDirectory;
		}
		public void run() {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					statusLbl.setText("Working...");
				}
			});
			long start = System.currentTimeMillis();
			final Vector<GlobalRecord> refs =
				GlobalLauncher.instance().runRecordQuery(getParam() +
					" " + identifier, workingDirectory);
			DefaultMutableTreeNode rootNode = new SearchNode(identifier);
			SourceLinkParentNode parent =
				tree.addSourceLinkParent(rootNode);
			for (int i = 0; i < refs.size(); i++)
			{
				GlobalRecord rec = refs.get(i);
				String file = rec.getFile();
				int line = rec.getLine();
				parent.addSourceLink(new FileMarker(file, line, ""));
			}
			if (refs.size() == 1 && GlobalOptionPane.isJumpImmediately())
				new GlobalReference(refs.get(0)).jump(view);
			long end = System.currentTimeMillis();
			Log.log(Log.DEBUG, this.getClass(), "GlobalResultsView(" + getParam() +
				", " + identifier + "' took " + (end - start) * .001 + " seconds.");
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					statusLbl.setText(refs.size() + " results");
					for (int i = 0; i < tree.getRowCount(); i++)
						tree.expandRow(i);
				}
			});
		}
	}
	public void show(View view, String identifier) {
		symbolTF.setText(identifier);
		RecordQuery query = new RecordQuery(identifier,	getBufferDirectory());
		GlobalPlugin.runInBackground(query);
	}
	
	public void focusOnDefaultComponent() {
		tree.requestFocus();
	}
}
