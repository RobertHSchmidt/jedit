/* *  XSLTProcessor.java - GUI for performing XSL Transformations * *  Copyright (C) 2002 Greg Merrill *                2002, 2003 Robert McKinnon * *  This program is free software; you can redistribute it and/or *  modify it under the terms of the GNU General Public License *  as published by the Free Software Foundation; either version 2 *  of the License, or any later version. * *  This program is distributed in the hope that it will be useful, *  but WITHOUT ANY WARRANTY; without even the implied warranty of *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the *  GNU General Public License for more details. * *  You should have received a copy of the GNU General Public License *  along with this program; if not, write to the Free Software *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. */package xslt;import org.gjt.sp.jedit.*;import org.gjt.sp.util.Log;import javax.swing.*;import java.awt.*;import java.awt.event.ActionEvent;import java.awt.event.ActionListener;import java.io.IOException;import java.net.URL;import java.text.MessageFormat;import java.util.Date;import java.util.Map;/** * GUI for performing XSL Transformations. * * @author Greg Merrill * @author Robert McKinnon - robmckinnon@users.sourceforge.net */public class XSLTProcessor extends JPanel implements ActionListener {  private static final String SOURCE_SELECT = "source.select";  private static final String RESULT_NAME = "result.name";  private static final String SAVE_SETTINGS = "settings.save";  private static final String LOAD_SETTINGS = "settings.load";  private static final String TRANSFORM = "transform";  private View view;  private JTextField sourceDocumentTextField;  private JTextField resultDocumentTextField;  private JButton selectButton;  private JButton nameResultButton;  private JButton saveSettingsButton;  private JButton loadSettingsButton;  private StylesheetPanel stylesheetPanel;  private StylesheetParameterPanel parameterPanel;  private JButton transformButton;  /**   * Constructor for the XSLTProcessor object.   *   *@param theView   */  public XSLTProcessor(View theView) {    super(new GridBagLayout());    this.view = theView;    this.sourceDocumentTextField = initFileTextField("XSLTProcessor.lastSource", "XSLTProcessor.source.pleaseSelect");    this.resultDocumentTextField = initFileTextField("XSLTProcessor.lastResult", "XSLTProcessor.result.pleaseName");    this.selectButton = initButton(SOURCE_SELECT, this, true);    this.nameResultButton = initButton(RESULT_NAME, this, true);    this.stylesheetPanel = new StylesheetPanel(view, this);    this.parameterPanel = new StylesheetParameterPanel();    this.loadSettingsButton = initButton(LOAD_SETTINGS, this, true);    this.saveSettingsButton = initButton(SAVE_SETTINGS, this, true);    this.transformButton = initButton(TRANSFORM, this, stylesheetPanel.stylesheetsExist());    addSourceComponents();    addStylesheetComponents();    addResultComponents();    addTransformComponents();  }  /**   * Returns transform button   *   *@return  The transformButton value   */  public JButton getTransformButton() {    return transformButton;  }  private JTextField initFileTextField(String lastProperty, String descriptionProperty) {    JTextField textField = new JTextField();    textField.setEditable(false);    String lastSource = jEdit.getProperty(lastProperty);    if(lastSource == null) {      textField.setText(jEdit.getProperty(descriptionProperty));    } else {      textField.setText(lastSource);    }    return textField;  }  static JButton initButton(String buttonType, ActionListener actionListener, boolean isEnabled) {    String toolTipText = jEdit.getProperty("XSLTProcessor." + buttonType + ".button.tooltip");    String iconName = jEdit.getProperty("XSLTProcessor." + buttonType + ".button.icon");    URL url = XSLTProcessor.class.getResource(iconName);    JButton button = new JButton(new ImageIcon(url));    button.setActionCommand(buttonType);    button.setToolTipText(toolTipText);    button.addActionListener(actionListener);    button.setEnabled(isEnabled);    Dimension dimension = new Dimension(30, 30);    if(buttonType.equals("transform")) {      dimension.setSize(74, 30);    }    button.setMinimumSize(dimension);    button.setPreferredSize(dimension);    return button;  }  static GridBagConstraints getConstraints(int gridy, Insets insets) {    GridBagConstraints constraints = new GridBagConstraints();    constraints.gridy = gridy;    constraints.insets = insets;    return constraints;  }  private void addSourceComponents() {    GridBagConstraints constraints = getConstraints(0, new Insets(4, 4, 0, 4));    constraints.anchor = GridBagConstraints.WEST;    add(new JLabel(jEdit.getProperty("XSLTProcessor.source.label")), constraints);    constraints = getConstraints(1, new Insets(4, 4, 4, 4));    constraints.weightx = 5;    constraints.fill = GridBagConstraints.HORIZONTAL;    add(sourceDocumentTextField, constraints);    constraints = getConstraints(1, new Insets(2, 2, 4, 2));    constraints.anchor = GridBagConstraints.EAST;    add(selectButton, constraints);  }  private void addStylesheetComponents() {    JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, stylesheetPanel, parameterPanel);    splitPane1.setOneTouchExpandable(true);//    splitPane1.setDividerLocation(150);    JSplitPane splitPane = splitPane1;    GridBagConstraints constraints = getConstraints(2, new Insets(4, 4, 4, 4));    constraints.gridheight = 5;    constraints.gridwidth = 2;    constraints.weightx = constraints.weighty = 5;    constraints.fill = GridBagConstraints.BOTH;    add(splitPane, constraints);  }  private void addTransformComponents() {    GridBagConstraints constraints = getConstraints(8, new Insets(4, 4, 4, 4));    constraints.gridwidth = 2;    constraints.anchor = GridBagConstraints.CENTER;    JPanel panel = new JPanel();    panel.add(this.loadSettingsButton);    panel.add(this.saveSettingsButton);    panel.add(this.transformButton);    add(panel, constraints);//    add(this.saveSettingsButton, constraints);//    add(this.transformButton, constraints);  }  private void addResultComponents() {    GridBagConstraints constraints = getConstraints(9, new Insets(4, 4, 0, 4));    constraints.anchor = GridBagConstraints.WEST;    add(new JLabel(jEdit.getProperty("XSLTProcessor.result.label")), constraints);    constraints = getConstraints(10, new Insets(4, 4, 4, 4));    constraints.weightx = 5;    constraints.fill = GridBagConstraints.HORIZONTAL;    add(this.resultDocumentTextField, constraints);    constraints = getConstraints(10, new Insets(2, 2, 4, 2));    constraints.anchor = GridBagConstraints.EAST;    add(this.nameResultButton, constraints);  }  public void actionPerformed(ActionEvent e) {    String action = e.getActionCommand();    if(action == TRANSFORM) {      transform();    } else if(action == SOURCE_SELECT) {      chooseFile(true);    } else if(action == RESULT_NAME) {      chooseFile(false);    } else if(action == LOAD_SETTINGS) {      loadSettings();    } else if(action == SAVE_SETTINGS) {      saveSettings();    }  }  public View getView() {    return view;  }  public StylesheetPanel getStylesheetPanel() {    return stylesheetPanel;  }  public StylesheetParameterPanel getParameterPanel() {    return parameterPanel;  }  public String getSourceFile() {    return this.sourceDocumentTextField.getText();  }  public String getResultFile() {    return this.resultDocumentTextField.getText();  }  public void setResultFile(String resultFile) {    resultDocumentTextField.setText(resultFile);    jEdit.setProperty("XSLTProcessor.lastResult", resultFile);  }  public void setSourceFile(String sourceFile) {    sourceDocumentTextField.setText(sourceFile);    jEdit.setProperty("XSLTProcessor.lastSource", sourceFile);  }  public void setStylesheets(String[] stylesheets) {    this.stylesheetPanel.setStylesheets(stylesheets);  }  public void setStylesheetParameters(String[] names, String[] values) {    this.parameterPanel.setParameters(names, values);  }  private void loadSettings() {    try {      XsltSettings settings = new XsltSettings(this);      settings.loadFromFile();    } catch(Exception e) {      Log.log(Log.ERROR, this, e.toString());    }  }  private void saveSettings() {    try {      XsltSettings settings = new XsltSettings(this);      settings.writeToFile();    } catch(IOException e) {      Log.log(Log.ERROR, this, e.toString());    }  }  private void chooseFile(boolean isSourceFile) {    String path = null;    if(isSourceFile && getSourceFile() != null && !getSourceFile().equals("")) {      path = MiscUtilities.getParentOfPath(getSourceFile());    } else if(!isSourceFile && !getResultFile().equals(jEdit.getProperty("XSLTProcessor.result.pleaseName"))) {      path = MiscUtilities.getParentOfPath(getResultFile());    }    String[] selections = GUIUtilities.showVFSFileDialog(view, path, JFileChooser.OPEN_DIALOG, false);    if(selections != null) {      if(isSourceFile) {        setSourceFile(selections[0]);      } else {        setResultFile(selections[0]);      }    }    Container topLevelAncestor = XSLTProcessor.this.getTopLevelAncestor();    if(topLevelAncestor instanceof JFrame) {      ((JFrame) topLevelAncestor).toFront();    }  }  private void transform() {    Date start = new Date();    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));    String stylesheetFileName = null;    Object[] stylesheets = stylesheetPanel.getStylesheets();    Map parameterMap = this.parameterPanel.getParametersMap();    try {      String docBeingTransformed = XSLTUtilities.transform(getSourceFile(), stylesheets, parameterMap);      Buffer newBuffer = jEdit.newFile(view);      newBuffer.insert(0, docBeingTransformed);      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));      if(!getResultFile().equals(jEdit.getProperty("XSLTProcessor.result.pleaseName"))) {        newBuffer.save(view, getResultFile());      }      view.getTextArea().setCaretPosition(0);      Date end = new Date();      long timeTaken = (end.getTime() - start.getTime()) / 1000;      Object[] param = {new Long(timeTaken)};      String status = jEdit.getProperty("XSLTProcessor.transform.status", param);      Log.log(Log.MESSAGE, this, status);    } catch(Exception e) {      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));      if(stylesheetFileName == null) {        XSLTPlugin.processException(e, jEdit.getProperty("XSLTProcessor.error.preProcessProblem"), XSLTProcessor.this);      } else {        String msg = MessageFormat.format(jEdit.getProperty("XSLTProcessor.error.stylesheetProblem"),            new Object[]{stylesheetFileName});        XSLTPlugin.processException(e, msg, XSLTProcessor.this);      }    }  }}