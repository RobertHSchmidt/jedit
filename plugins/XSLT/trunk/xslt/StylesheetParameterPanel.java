/* *  StylesheetPanel.java - GUI panel for list of XSL parameters * *  Copyright (C) 2003 Robert McKinnon * *  This program is free software; you can redistribute it and/or *  modify it under the terms of the GNU General Public License *  as published by the Free Software Foundation; either version 2 *  of the License, or any later version. * *  This program is distributed in the hope that it will be useful, *  but WITHOUT ANY WARRANTY; without even the implied warranty of *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the *  GNU General Public License for more details. * *  You should have received a copy of the GNU General Public License *  along with this program; if not, write to the Free Software *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. */package xslt;import org.gjt.sp.jedit.jEdit;import org.gjt.sp.util.Log;import javax.swing.*;import javax.swing.event.ListSelectionEvent;import javax.swing.event.ListSelectionListener;import javax.swing.event.TableModelEvent;import javax.swing.event.TableModelListener;import javax.swing.table.TableModel;import java.awt.*;import java.awt.event.ActionEvent;import java.awt.event.ActionListener;import java.util.HashMap;import java.util.LinkedList;import java.util.Map;/** * GUI panel for list of XSL parameters * * @author Robert McKinnon - robmckinnon@users.sourceforge.net */public class StylesheetParameterPanel extends JPanel implements ActionListener, ListSelectionListener, TableModelListener {  private static final String PARAMETER_ADD = "parameters.add";  private static final String PARAMETER_REMOVE = "parameters.remove";  private StylesheetParameterTableModel parameterTableModel;  private JTable parameterTable;  private JButton addParameterButton;  private JButton removeParameterButton;  private static final String PARAMETER_NAME = "xslt.parameter.name";  private static final String PARAMETER_VALUE = "xslt.parameter.value";  private boolean isResetting = false;  /**   * Constructor for the XSLTProcessor object.   */  public StylesheetParameterPanel() {    super(new GridBagLayout());    this.parameterTableModel = initParameterTableModel();    this.parameterTableModel.addTableModelListener(this);    this.parameterTable = initParameterTable(this.parameterTableModel);    this.addParameterButton = XSLTProcessor.initButton(PARAMETER_ADD, this, true);    this.removeParameterButton = XSLTProcessor.initButton(PARAMETER_REMOVE, this, false);    String label = jEdit.getProperty("xslt.parameters.label");    JScrollPane tablePane = new JScrollPane(this.parameterTable);    StylesheetPanel.initPanel(this, label, tablePane, this.addParameterButton, this.removeParameterButton);  }  public void actionPerformed(ActionEvent e) {    String action = e.getActionCommand();    if(action == PARAMETER_ADD) {      addParameter();    } else if(action == PARAMETER_REMOVE) {      removeParameter(this.parameterTable.getSelectedRow());    }  }  public void valueChanged(ListSelectionEvent e) {    boolean selectionExists = this.parameterTable.getSelectedRow() != -1;    this.removeParameterButton.setEnabled(selectionExists);  }  public void tableChanged(TableModelEvent e) {    int row = e.getFirstRow();    logEvent(e.getType(), row);    if(!isResetting) {      storeParameters();    }  }  public void setParameters(String[] names, String[] values) {    this.isResetting = true;    this.parameterTableModel.clear();    for(int i = 0; i < names.length; i++) {      this.parameterTableModel.addParameter(names[i], values[i]);    }    this.isResetting = false;    storeParameters();  }  private void logEvent(int event, int row) {    String eventText = "";    switch(event) {      case TableModelEvent.UPDATE:        eventText = "update ";        break;      case TableModelEvent.INSERT:        eventText = "insert ";        break;      case TableModelEvent.DELETE:        eventText = "delete ";    }    Log.log(Log.DEBUG, this, eventText + "row " + row);  }  private void storeParameters() {    LinkedList nameList = new LinkedList();    LinkedList valueList = new LinkedList();    for(int i = 0; i < this.parameterTableModel.getRowCount(); i++) {      String parameterName = this.parameterTableModel.getParameterName(i);      if(!parameterName.equals("")) {        nameList.add(parameterName);        valueList.add(this.parameterTableModel.getParameterValue(i));      }    }    PropertyUtil.setEnumeratedProperty(PARAMETER_NAME, nameList, jEdit.getProperties());    PropertyUtil.setEnumeratedProperty(PARAMETER_VALUE, valueList, jEdit.getProperties());  }  /**   * Returns a map with parameter name as the key, and parameter value as the value.   */  public Map getParametersMap() {    Object[] names = PropertyUtil.getEnumeratedProperty(PARAMETER_NAME, jEdit.getProperties()).toArray();    Object[] values = PropertyUtil.getEnumeratedProperty(PARAMETER_VALUE, jEdit.getProperties()).toArray();    Map parameterMap = new HashMap();    for(int i = 0; i < names.length; i++) {      parameterMap.put(names[i], values[i]);    }    return parameterMap;  }  public int getParametersCount() {    int rowCount = this.parameterTableModel.getRowCount();    if(rowCount > 0 && getParameterName(rowCount - 1).equals("")) {      rowCount--;    }    return rowCount;  }  public String getParameterName(int index) {    return this.parameterTableModel.getParameterName(index);  }  public String getParameterValue(int index) {    return this.parameterTableModel.getParameterValue(index);  }  private StylesheetParameterTableModel initParameterTableModel() {    StylesheetParameterTableModel model = new StylesheetParameterTableModel();    Object[] names = PropertyUtil.getEnumeratedProperty(PARAMETER_NAME, jEdit.getProperties()).toArray();    Object[] values = PropertyUtil.getEnumeratedProperty(PARAMETER_VALUE, jEdit.getProperties()).toArray();    for(int i = 0; i < names.length; i++) {      model.addParameter((String)names[i], (String)values[i]);    }    return model;  }  private JTable initParameterTable(TableModel model) {    JTable table = new JTable(model);    table.getSelectionModel().addListSelectionListener(this);    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);    DefaultCellEditor defaultCellEditor = ((DefaultCellEditor)table.getDefaultEditor(String.class));    Log.log(Log.DEBUG, this, "" + defaultCellEditor.getClickCountToStart());    defaultCellEditor.setClickCountToStart(1);    return table;  }  private void addParameter() {    this.parameterTableModel.addParameter("", "");    int lastRow = this.parameterTableModel.getRowCount() - 1;    this.parameterTable.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);    this.parameterTable.getSelectionModel().setSelectionInterval(lastRow, lastRow);    this.parameterTable.requestFocus();  }  private void removeParameter(int selectedRow) {    if(selectedRow != -1) {      this.parameterTableModel.removeParameter(selectedRow);      if(selectedRow != 0 && selectedRow < this.parameterTableModel.getRowCount()) {        this.parameterTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);        this.parameterTable.requestFocus();      }    }  }}