package gdb.variables;

import gdb.core.CommandManager;
import gdb.core.Parser.GdbResult;
import gdb.core.Parser.ResultHandler;
import gdb.variables.GdbVar.ChangeListener;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class LocalVariables extends JPanel {
	private JTree tree;
	private CommandManager commandManager = null;
	private DefaultMutableTreeNode root;
	private DefaultTreeModel model;
	
	public LocalVariables() {
		setLayout(new BorderLayout());
		
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);
		JButton modify = new JButton("Modify");
		modify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreePath tp = tree.getSelectionPath();
				if (tp == null)
					return;
				Object [] path = tp.getPath();
				GdbVar v = (GdbVar)(path[1]);
				if (v != null)
					v.contextRequested();
			}
		});
		tb.add(modify);
		add(tb, BorderLayout.NORTH);
		
		tree = new JTree();
		root = new DefaultMutableTreeNode("Locals");
		model = new DefaultTreeModel(root);
		tree.setModel(model);
		tree.setRootVisible(false);
		JScrollPane locals = new JScrollPane(tree);
		add(locals);
		tree.addMouseListener(new VarTreeMouseListener());
	}

	public void setCommandManager(CommandManager cm) {
		commandManager = cm;
	}
	public void update() {
		for (int i = 0; i < root.getChildCount(); i++)
			((GdbVar)root.getChildAt(i)).update();
	}
	public void update(int frame) {
		for (int i = 0; i < root.getChildCount(); i++)
			((GdbVar)root.getChildAt(i)).done();
		root.removeAllChildren();
		commandManager.add("-stack-list-arguments 0 " + frame + " " + frame,
				new StackArgumentsResultHandler());
		commandManager.add("-stack-list-locals 0", new LocalsResultHandler());
	}
	public void sessionEnded() {
		root.removeAllChildren();
		model.reload(root);
	}

	private class StackArgumentsResultHandler implements ResultHandler {
		@SuppressWarnings("unchecked")
		public void handle(String msg, GdbResult res) {
			if (! msg.equals("done"))
				return;
			Object frameArgs = res.getValue("stack-args/0/frame/args");
			if (frameArgs == null)
				return;
			if (frameArgs instanceof Vector) {
				Vector<Object> args = (Vector<Object>)frameArgs;
				for (int i = 0; i < args.size(); i++) {
					Hashtable<String, Object> hash =
						(Hashtable<String, Object>)args.get(i);
					String name = hash.get("name").toString();
					GdbVar v = new GdbVar(name);
					v.setChangeListener(new ChangeListener() {
						public void updated(GdbVar v) {
							model.reload(v);
						}
						public void changed(GdbVar v) {
							update();
						}
					});
					root.add(v);
				}
			}
		}
	}
	
	private class LocalsResultHandler implements ResultHandler {
		@SuppressWarnings("unchecked")
		public void handle(String msg, GdbResult res) {
			if (! msg.equals("done"))
				return;
			Object locals = res.getValue("locals");
			if (locals == null)
				return;
			if (locals instanceof Vector) {
				Vector<Object> localsVec = (Vector<Object>)locals;
				for (int i = 0; i < localsVec.size(); i++) {
					Object local = localsVec.get(i);
					if (local instanceof Hashtable) {
						Hashtable<String, Object> localHash =
							(Hashtable<String, Object>)local;
						String name = localHash.get("name").toString();
						GdbVar v = new GdbVar(name);
						v.setChangeListener(new ChangeListener() {
							public void updated(GdbVar v) {
								model.reload(v);
							}
							public void changed(GdbVar v) {
								update();
							}
						});
						root.add(v);
					}
				}
			}
			updateTree();
		}
	}

	public DefaultMutableTreeNode createTreeNode(String name, String value) {
		return new DefaultMutableTreeNode(name + " = " + value);
	}

	public void updateTree() {
		model.reload(root);
	}
}
