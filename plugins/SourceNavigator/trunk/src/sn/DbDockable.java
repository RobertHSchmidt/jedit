package sn;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.View;

import sn.DbAccess.RecordHandler;

import com.sleepycat.db.DatabaseEntry;

@SuppressWarnings("serial")
public class DbDockable extends JPanel {

	private static final char FIND_FIELD_SEP = '?';
	private static final String SN_SEP = "\\?";

	public class SourceLink {
		String path;
		int line;
		int offset;
		public SourceLink(String path, int line, int offset) {
			this.path = path;
			this.line = line;
			this.offset = offset;
		}
		public void jumpTo(View view) {
			SourceNavigatorPlugin.jumpTo(view, path, line, offset);
		}
	}
	
	public class DbTableModel extends AbstractTableModel {
		
		String [] columns;
		int fileColumn;
		int lineColumn;
		String baseDir;
		Vector<String []> elements;
		
		public DbTableModel(String columnsString) {
			columns = columnsString.split(SN_SEP);
			fileColumn = lineColumn = -1;	// None by default
			for (int i = 0; i < columns.length; i++) {
				if (columns[i].equals("File"))
					fileColumn = i;
				else if (columns[i].equals("Line"))
					lineColumn = i;
			}
			elements = new Vector<String []>();
		}
		public void setBaseDir(String baseDir) {
			this.baseDir = baseDir;
		}
		public void clear() {
			elements.clear();
		}
		@Override
		public int getColumnCount() {
			return columns.length;
		}
		@Override
		public int getRowCount() {
			return elements.size();
		}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex < 0 || rowIndex >= getRowCount() ||
				columnIndex < 0 || columnIndex >= getColumnCount())
				return null;
			return elements.get(rowIndex)[columnIndex];
		}
		public SourceLink getSourceLink(int selectedRow) {
			if (fileColumn < 0)
				return null;
			String file = (String) getValueAt(selectedRow, fileColumn);
			if (file == null)
				return null;
			int line = 0;
			int offset = 0;
			if (lineColumn >= 0) {
				String lineStr = (String) getValueAt(selectedRow, lineColumn);
				if (lineStr == null)
					return null;
				try {
					String [] pos = lineStr.split("\\.");
					line = Integer.valueOf(pos[0]);
					offset = (pos.length > 1) ? Integer.valueOf(pos[1]) : 0;
				} catch (Exception e) {
					return null;
				}
			}
			if (! MiscUtilities.isAbsolutePath(file))
				file = baseDir + "/" + file;
			return new SourceLink(file, line, offset);
		}
		public void addElement(String [] columns) {
			// Pad with empty strings if there's a need
			if (columns.length < getColumnCount()) {
				String [] newColumns = new String[getColumnCount()];
				int i;
				for (i = 0; i < columns.length; i++)
					newColumns[i] = columns[i];
				for (; i < getColumnCount(); i++)
					newColumns[i] = "";
				columns = newColumns;
			}
			elements.add(columns);
		}
		@Override
		public String getColumnName(int column) {
			return columns[column];
		}
	}
	
	private View view;
	private DbTableModel model;
	private JTable table;
	private JTextField text;
	private String db;
	
	public DbDockable(View view, String db, String columns)
	{
		super(new BorderLayout());
		this.view = view;
		this.db = db;
		model = new DbTableModel(columns);
		table = new JTable();
		table.setModel(model);
		table.setAutoCreateRowSorter(true);
		//table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		//table.setAutoResizeColumns(true);
		//table.setAutoResizeWithHeaders(true);
		table.setRowSelectionAllowed(true);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					int sel = table.getSelectedRow();
					SourceLink link = (SourceLink)
						model.getSourceLink(table.convertRowIndexToModel(sel));
					if (link != null)
						link.jumpTo(DbDockable.this.view);
				}
			}
		});
		add(new JScrollPane(table), BorderLayout.CENTER);
		JPanel p = new JPanel(new BorderLayout());
		JLabel l = new JLabel("Find:");
		p.add(l, BorderLayout.WEST);
		final JCheckBox prefix = new JCheckBox("Prefix");
		p.add(prefix, BorderLayout.EAST);
		text = new JTextField(40);
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					find(text.getText(), prefix.isSelected());
			}
		});
		p.add(text, BorderLayout.CENTER);
		add(p, BorderLayout.NORTH);
	}
	
	private class DbRecordHandler implements RecordHandler {
		private String [] keyStrings;
		private boolean prefix;
		public boolean handle(DatabaseEntry key, DatabaseEntry data) {
			String [] s = breakToStrings(key);
			// Check that the record matches the search key
			if (keyStrings != null) {
				for (int i = 0; i < keyStrings.length; i++) {
					if (! s[i].equals(keyStrings[i])) {
						if (! prefix || i < keyStrings.length - 1)
							return false;
						if (! s[i].startsWith(keyStrings[i]))
							return false;
					}
				}
			}
			model.addElement(s);
			return true;
		}
		private void setSearchKey(String key, boolean prefixKey) {
			keyStrings = key.split(SN_SEP);
			prefix = prefixKey;
		}
		private String [] breakToStrings(DatabaseEntry e) {
			byte [] bytes = e.getData();
			int nStrings = model.getColumnCount();
			String [] strings = new String[nStrings];
			int start = 0;
			int index = 0;
			for (int i = 0; i < bytes.length && index < nStrings; i++) {
				if (bytes[i] <= 1) {
					strings[index++] = new String(bytes, start, i - start);
					start = i + 1;
				}
			}
			if (index < nStrings)
				strings[index] = new String(bytes, start, bytes.length - start - 1);
			return strings;
		}
	}
	
	private void find(String text, boolean prefixKey) {
		model.clear();
		DbAccess dba = new DbAccess(db);
		model.setBaseDir(dba.getDir());
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		DbRecordHandler handler = new DbRecordHandler();
		if (text == null || text.length() == 0) {
			// Get all records in the table
			dba.lookup(key, data, handler);
		} else {
			// Get records starting with the prefix
			byte [] bytes = text.getBytes();
			byte [] keyBytes = new byte[bytes.length + (prefixKey ? 1 : 0)];
			int i;
			for (i = 0; i < bytes.length; i++)
				keyBytes[i] = (bytes[i] == FIND_FIELD_SEP) ? 1	: bytes[i];
			if (prefixKey)
				keyBytes[i] = 1;
			key.setData(keyBytes);
			handler.setSearchKey(text, prefixKey);
			dba.lookup(key, data, handler);
		}
		model.fireTableDataChanged();
	}
}
