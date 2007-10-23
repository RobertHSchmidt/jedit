package db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.gjt.sp.jedit.jEdit;

public class TagDB {
	private Connection conn;
	private Set<String> columns;
	// Column types
	public static final String IDENTITY_TYPE = "IDENTITY";
	public static final String VARCHAR_TYPE = "VARCHAR";
	public static final String INTEGER_TYPE = "INTEGER";
	// Tags table
	public static final String TAGS_TABLE = "TAGS";
	public static final String TAGS_NAME = "NAME";
	public static final String TAGS_FILE_ID = "FILE_ID";
	public static final String TAGS_PATTERN = "PATTERN";
	public static final String TAGS_ATTR_PREFIX = "A_";
	public static final String TAGS_LINE = "A_LINE";
	// Files table
	public static final String FILES_TABLE = "FILES";
	public static final String FILES_ID = "ID";
	public static final String FILES_NAME = "FILE";
	// Origins table
	public static final String ORIGINS_TABLE = "ORIGINS";
	public static final String ORIGINS_ID = "ID";
	public static final String ORIGINS_NAME = "NAME";
	public static final String ORIGINS_TYPE = "TYPE";
	// Files/Origin map table
	public static final String MAP_TABLE = "MAP";
	public static final String MAP_FILE_ID = "FILE_ID";
	public static final String MAP_ORIGIN_ID = "ORIGIN_ID";
	// Origin types
	public static final String PROJECT_ORIGIN = "Project";
	public static final String DIR_ORIGIN = "Dir";

	public static final String PROJECT_COL = "K_PROJECT";
	
	public TagDB() {
		removeStaleLock();
        try {
			Class.forName("org.hsqldb.jdbcDriver");
			conn = DriverManager.getConnection("jdbc:hsqldb:file:" +
					getDBFilePath(), "sa", "");
        } catch (final Exception e) {
			e.printStackTrace();
		}
		createTables();
		getColumns();
	}

	// Check if a source file is in the DB
	public boolean hasSourceFile(String file) {
		return tableColumnContainsValue(FILES_TABLE, FILES_NAME, file);
	}
	
	// Returns the ID of a source file, or (-1) if source file not in DB
	public int getSourceFileID(String file) {
		return queryInteger(FILES_ID, "SELECT " + FILES_ID + " FROM " + FILES_TABLE +
			" WHERE " + FILES_NAME + "=" + quote(file), -1);
	}
	
	// Inserts a source file to the DB
	public void insertSourceFile(String file) {
		try {
			update("INSERT INTO " + FILES_TABLE + " (" + FILES_NAME +
				") VALUES (" + quote(file) + ")");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Delete tags from source file
	public void deleteTagsFromSourceFile(int fileId) {
		if (fileId < 0)
			return;
		deleteRowsWithValue(TAGS_TABLE, TAGS_FILE_ID, Integer.valueOf(fileId));
	}
	
	/*
	 * Insert a tag into the DB with a given file ID.
	 * If file ID is negative, the ID is looked up in the FILES table. If the file
	 * is not in the table, it is inserted.
	 */
	public void insertTag(Hashtable<String, String> info, int fileId) {
		if (fileId < 0) {
			System.err.println("insertTag called with fileId=-1");
			return;
		}
		info.remove(TAGS_FILE_ID);
		// Find missing columns and build the inserted value string
		StringBuffer valueStr = new StringBuffer();
		StringBuffer columnStr = new StringBuffer();
		Set<Entry<String,String>> entries = info.entrySet();
		Iterator<Entry<String, String>> it = entries.iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			String col = entry.getKey().toUpperCase();
			String val = entry.getValue();
			if (! columns.contains(col)) {
				try {
					update("ALTER TABLE " + TAGS_TABLE + " ADD " + col + " " +
						VARCHAR_TYPE + ";");
				} catch (SQLException e) {
					e.printStackTrace();
				}
				columns.add(col);
			}
			if (columnStr.length() > 0)
				columnStr.append(",");
			columnStr.append(col);
			if (valueStr.length() > 0)
				valueStr.append(",");
			valueStr.append(quote(val));
		}
		// Insert the record
		try {
			update("INSERT INTO " + TAGS_TABLE + " (" + TAGS_FILE_ID + "," +
				columnStr.toString() + ") VALUES (" + fileId + "," + valueStr.toString() +
				")");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Runs a query for the specified tag name
	public ResultSet queryTag(String tag) throws SQLException {
		String query = "SELECT * FROM " + TAGS_TABLE + "," + FILES_TABLE +
			" WHERE " + field(TAGS_TABLE, TAGS_NAME) + "=" + quote(tag) +
			" AND " + field(TAGS_TABLE, TAGS_FILE_ID) + "=" +
				field(FILES_TABLE, FILES_ID);
		return query(query);
	}
	// Runs a query for the specified tag name in the specified project
	public ResultSet queryTagInProject(String tag, String project) throws SQLException {
		String query = "SELECT * FROM " + TAGS_TABLE + "," + FILES_TABLE +
			" WHERE " + field(TAGS_TABLE, TAGS_NAME) + "=" + quote(tag) +
			" AND EXISTS " +
				"(SELECT " + MAP_FILE_ID + " FROM " + MAP_TABLE +
				" WHERE " + field(MAP_TABLE, MAP_FILE_ID) + "=" +
					field(FILES_TABLE, FILES_ID) +
				" AND " + field(MAP_TABLE, MAP_ORIGIN_ID) + "=" +
					"(SELECT " + ORIGINS_ID + " FROM " + ORIGINS_TABLE +
					" WHERE " + ORIGINS_NAME + "=" + quote(project) +
					" AND " + ORIGINS_TYPE + "=" + quote(PROJECT_ORIGIN) +
					"))";
		return query(query);
	}

	private String field(String table, String column) {
		return table + "." + column;
	}

	/*
	 * Check if the table contains a row with the specified value in the specified column.
	 * Used for checking if a buffer is in the DB. 
	 */
	private boolean tableColumnContainsValue(String table, String column, Object value) {
		try {
			ResultSet rs = query("SELECT TOP 1 " + column + " FROM " + table + " WHERE " +
				column + "=" + quote(value));
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * Runs a query and returns an integer value from the specified column of the first
	 * row in the query result, or defaultValue if the query result is empty.
	 */
	public int queryInteger(String column, String query, int defaultValue) {
		try {
			ResultSet rs = query(query);
			if (! rs.next())
				return defaultValue;
			return rs.getInt(column);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public Vector<String> getOrigins(String type) {
		return queryStringList(ORIGINS_NAME,
			"SELECT * FROM " + ORIGINS_TABLE + " WHERE " +
			ORIGINS_TYPE + "=" + quote(type));
	}
	public void updateOrigins(String type, Vector<String> values) {
		// Remove obsolete origins
		Vector<String> current = queryStringList(ORIGINS_NAME,
			"SELECT * FROM " + ORIGINS_TABLE + " WHERE " +
			ORIGINS_TYPE + "=" + quote(type));
		for (int i = 0; i < current.size(); i++) {
			String name = current.get(i);
			if (! values.contains(name))
				deleteOrigin(type, name);
		}
		// Add new origins
		for (int i = 0; i < values.size(); i++) {
			String name = values.get(i);
			if (! current.contains(name))
				try {
					insertOrigin(type, name);
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}
	
	public void setProject(String project) {
	}
	public void unsetProject() {
	}
	
	public synchronized void update(String expression) throws SQLException {
		//System.err.println("update: " + expression);
		Statement st = conn.createStatement();
		try {
			if (st.executeUpdate(expression) == -1)
	            System.err.println("db error : " + expression);
		} catch (SQLException e) {
			System.err.println("SQL update: " + expression);
			throw e;
		}
        st.close();
    }
	
	public synchronized ResultSet query(String expression) throws SQLException {
		//System.err.println("query: " + expression);
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(expression);
        st.close();
        return rs;
    }
	
	public Vector<Integer> queryIntegerList(String column, String query) {
		Vector<Integer> values = new Vector<Integer>();
		try {
			ResultSet rs = query(query);
			while (rs.next())
				values.add(Integer.valueOf(rs.getInt(column)));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return values;
	}
	public Vector<String> queryStringList(String column, String query) {
		Vector<String> values = new Vector<String>();
		try {
			ResultSet rs = query(query);
			while (rs.next())
				values.add(rs.getString(column));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return values;
	}
	
	public void shutdown()
	{
		try {
			Statement st = conn.createStatement();
	        st.execute("SHUTDOWN");
	        conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String quote(Object value) {
		if (value instanceof String)
			return "'" + ((String)value).replaceAll("'", "''") + "'";
		return value.toString();
	}
	
	public void deleteRowsWithValue(String table, String column, Object value) {
		try {
			query("DELETE FROM " + table + " WHERE " + column + "=" + quote(value));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void deleteRowsWithValueList(String table, String column,
			Vector list) {
		StringBuffer set = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			if (i > 0)
				set.append(",");
			set.append(quote(list.get(i)));
		}
		try {
			query("DELETE FROM " + table + " WHERE " + column + " IN (" +
				set.toString() + ")");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteRowsWithValues(Hashtable<String, String> values) {
		StringBuffer where = new StringBuffer();
		Iterator<Entry<String, String>> it = values.entrySet().iterator();
		boolean first = true;
		while (it.hasNext()) {
			if (first)
				first = false;
			else
				where.append(" AND ");
			Entry<String, String> entry = it.next();
			where.append(entry.getKey());
			where.append("=");
			where.append(quote(entry.getValue()));
		}
		try {
			query("DELETE FROM TAGS WHERE " + where);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteRowsWithValuePrefix(String column, String prefix) {
		try {
			query("DELETE FROM TAGS WHERE " + column + " LIKE " +
				quote(prefix + "%"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	static public String attr2col(String attributeName) {
		return TAGS_ATTR_PREFIX + attributeName.toUpperCase();
	}
	static public String col2attr(String columnName) {
		return columnName.substring(TAGS_ATTR_PREFIX.length()).toLowerCase();
	}
	
	private void getColumns() {
		columns = new HashSet<String>();
		try {
			ResultSet rs = query("SELECT * FROM TAGS WHERE NAME=''");
			ResultSetMetaData meta = rs.getMetaData();
			int cols = meta.getColumnCount();
			for (int i = 0; i < cols; i++)
				columns.add(meta.getColumnName(i + 1));
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}
	
	private String getDBFilePath() {
		return jEdit.getSettingsDirectory() + "/CtagsInterface/tagdb";
	}

	private void removeStaleLock() {
		File lock = new File(getDBFilePath() + ".lck");
		if (lock.exists())
			lock.delete();
	}

	private void createIndex(String index, String table, String column)
	throws SQLException {
		update("CREATE INDEX " + index + " ON " + table + "(" + column + ")");
	}
	
	private void createTable(String table, String [] columns)
	throws SQLException {
		StringBuffer st = new StringBuffer("CREATE CACHED TABLE ");
		st.append(table);
		st.append("(");
		for (int i = 0; i < columns.length; i += 2) {
			if (i > 0)
				st.append(", ");
			st.append(columns[i]);
			st.append(" ");
			st.append(columns[i + 1]);
		}
		st.append(")");
		update(st.toString());
	}

	private void createTables() {
		try {
			// Create Tags table
			createTable(TAGS_TABLE, new String [] {
				TAGS_NAME, VARCHAR_TYPE,
				TAGS_FILE_ID, INTEGER_TYPE,
				TAGS_PATTERN, VARCHAR_TYPE
			});
			createIndex("TAGS_NAME", TAGS_TABLE, TAGS_NAME);
			createIndex("TAGS_FILE", TAGS_TABLE, TAGS_FILE_ID);
			// Create Files table
			createTable(FILES_TABLE, new String [] {
				FILES_ID, IDENTITY_TYPE,
				FILES_NAME, VARCHAR_TYPE
			});
			createIndex("FILES_NAME", FILES_TABLE, FILES_NAME);
			// Create Origins table
			createTable(ORIGINS_TABLE, new String [] {
				ORIGINS_ID, IDENTITY_TYPE,
				ORIGINS_NAME, VARCHAR_TYPE,
				ORIGINS_TYPE, VARCHAR_TYPE
			});
			// Create Map table
			createTable(MAP_TABLE, new String [] {
				MAP_FILE_ID, INTEGER_TYPE,
				MAP_ORIGIN_ID, INTEGER_TYPE
			});
			createIndex("MAP_FILE_ID", MAP_TABLE, MAP_FILE_ID);
			createIndex("MAP_ORIGIN_ID", MAP_TABLE, MAP_ORIGIN_ID);
		} catch (SQLException e) {
			// Table already exists
		}
	}
	
	private void insertOrigin(String type, String name) throws SQLException {
		update("INSERT INTO " + ORIGINS_TABLE + " (" +
			ORIGINS_TYPE + "," + ORIGINS_NAME + ") VALUES (" +
			type + "," + name + ")");
	}
	private void deleteOrigin(String type, String name) {
		// TODO: Remove origin from origins table, remove all files
		// originated in the removed origin only, and remove all tags
		// from the removed files.
		int originId = queryInteger(ORIGINS_ID,
			"SELECT * FROM " + ORIGINS_TABLE + " WHERE " +
			ORIGINS_TYPE + "=" + quote(type) + " AND " +
			ORIGINS_NAME + "=" + quote(name), -1);
		if (originId < 0)
			return;
		String fileIds = "SELECT FILE_ID FROM MAP WHERE ORIGIN_ID=" +
			quote(originId);
		String deleteTags = "DELETE FROM TAGS WHERE FILE_ID IN (" +
			fileIds + ")";
		try {
			query(deleteTags);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
