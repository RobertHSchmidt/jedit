/* * XSLTProcessor.java - GUI for performing XSL Transformations * * Copyright (C) 2002 Greg Merrill *               2002, 2003 Robert McKinnon * * This program is free software; you can redistribute it and/or * modify it under the terms of the GNU General Public License * as published by the Free Software Foundation; either version 2 * of the License, or any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with this program; if not, write to the Free Software * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. */package xslt;import java.awt.BorderLayout;import java.awt.Component;import java.awt.Container;import java.awt.Cursor;import java.awt.Dimension;import java.awt.GridLayout;import java.awt.event.ActionEvent;import java.awt.event.KeyAdapter;import java.awt.event.KeyEvent;import java.awt.event.MouseAdapter;import java.awt.event.MouseEvent;import java.io.File;import java.io.FileReader;import java.io.IOException;import java.io.StringReader;import java.util.Date;import java.util.Map;import javax.swing.BorderFactory;import javax.swing.JButton;import javax.swing.JComponent;import javax.swing.JFileChooser;import javax.swing.JFrame;import javax.swing.JLabel;import javax.swing.JPanel;import javax.swing.JPopupMenu;import javax.swing.JSplitPane;import javax.swing.JTextField;import javax.swing.JToolBar;import org.gjt.sp.jedit.Buffer;import org.gjt.sp.jedit.GUIUtilities;import org.gjt.sp.jedit.View;import org.gjt.sp.jedit.jEdit;import org.gjt.sp.jedit.gui.DockableWindowManager;import org.gjt.sp.util.Log;import org.xml.sax.InputSource;/** * GUI for performing XSL Transformations. * * @author Greg Merrill * @author Robert McKinnon - robmckinnon@users.sourceforge.net */public class XSLTProcessor extends JPanel {  private static final String LAST_RESULT = "xslt.last-result";  private View view;  private JTextField resultFileTextField;  private OpenFileAction openFileAction = new OpenFileAction();  private XsltAction resultNameAction = new ResultNameAction();  private XsltAction saveSettingsAction = new SaveSettingsAction();  private XsltAction loadSettingsAction = new LoadSettingsAction();  private XsltAction transformAction = new TransformAction();  private JPopupMenu sourceMenu;  private JPopupMenu resultMenu;  private JPopupMenu stylesheetMenu;  private JPopupMenu stylesheetParameterMenu;  private InputSelectionPanel inputSelectionPanel;  private StylesheetPanel stylesheetPanel;  private StylesheetParameterPanel parameterPanel;  /**   * Constructor for the XSLTProcessor object.   *   *@param theView   */  public XSLTProcessor(View theView, String position) {    super(new BorderLayout());    this.view = theView;    boolean sideBySide = position.equals(DockableWindowManager.TOP) || position.equals(DockableWindowManager.BOTTOM);    OpenFileKeyAdaptor keyListener = new OpenFileKeyAdaptor();    XsltMouseAdaptor mouseListener = new XsltMouseAdaptor();    this.resultFileTextField = initFileTextField(LAST_RESULT, "xslt.result.prompt", keyListener, mouseListener);    this.inputSelectionPanel = new InputSelectionPanel(view, this, mouseListener);    this.stylesheetPanel = new StylesheetPanel(view, this, keyListener, mouseListener);    this.parameterPanel = new StylesheetParameterPanel(mouseListener);    this.sourceMenu = inputSelectionPanel.initSelectionPanelMenu(openFileAction);    this.resultMenu = XsltAction.initMenu(new XsltAction[]{openFileAction, null, resultNameAction});    this.stylesheetMenu = stylesheetPanel.initStylesheetMenu(openFileAction);    this.stylesheetParameterMenu = parameterPanel.initStylesheetMenu(openFileAction);    JPanel resultPanel = initResultPanel(jEdit.getProperty("xslt.result.label"), resultFileTextField, resultNameAction.getButton());    JPanel transformPanel = initTransformToolBar();    if(sideBySide) {      createHorizontalLayout(inputSelectionPanel, transformPanel, resultPanel);    } else {      createVerticalLayout(inputSelectionPanel, transformPanel, resultPanel);    }    XSLTPlugin.setProcessor(this);  }  private void createVerticalLayout(JPanel sourcePanel, JPanel transformPanel, JPanel resultPanel) {    add(sourcePanel, BorderLayout.NORTH);    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, stylesheetPanel, parameterPanel);    splitPane.setOneTouchExpandable(true);    //JSplitPane splitPane = splitPane1;    add(splitPane);    JPanel panel = new JPanel(new GridLayout(2,1));    panel.add(transformPanel);    panel.add(resultPanel);    add(panel, BorderLayout.SOUTH);  }  private void createHorizontalLayout(JPanel sourcePanel, JPanel transformPanel, JPanel resultPanel) {    JPanel topPanel = new JPanel(new GridLayout(3,1));    topPanel.add(sourcePanel);    topPanel.add(transformPanel);    topPanel.add(resultPanel);    JPanel panel = new JPanel(new BorderLayout());    panel.add(topPanel, BorderLayout.NORTH);    setLayout(new GridLayout(1,3,6,0));    add(stylesheetPanel);    add(parameterPanel);    add(panel);  }  /**   * Performs the transform action.   */  public void clickTransformButton() {    transformAction.actionPerformed(null);  }  /**   * Performs the load settings action.   */  public void clickLoadSettingsButton() {    loadSettingsAction.actionPerformed(null);  }  /**   * Performs the save settings action.   */  public void clickSaveSettingsButton() {    saveSettingsAction.actionPerformed(null);  }  public void setTransformEnabled(boolean isEnabled) {    this.transformAction.setEnabled(isEnabled);  }  private JTextField initFileTextField(String lastProperty, String descriptionProperty, OpenFileKeyAdaptor keyListener, XsltMouseAdaptor mouseListener) {    JTextField textField = new JTextField();    textField.setEditable(true);    String lastSource = jEdit.getProperty(lastProperty);    if(lastSource == null) {      textField.setText(jEdit.getProperty(descriptionProperty));    } else {      textField.setText(lastSource);    }    textField.addKeyListener(keyListener);    textField.addMouseListener(mouseListener);    return textField;  }  private JPanel initResultPanel(String labelText, JComponent centerComponent, JButton browseButton) {    JLabel label = new JLabel(labelText);    JPanel lPanel = new JPanel(new GridLayout(2,1));    lPanel.add(label);    lPanel.add(centerComponent);    JPanel rPanel = new JPanel(new BorderLayout());    rPanel.add(browseButton, BorderLayout.SOUTH);    JPanel panel = new JPanel(new BorderLayout(6, 4));    panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 2));    panel.add(lPanel);    panel.add(rPanel, BorderLayout.EAST);    return panel;  }  private JPanel initTransformToolBar() {    this.transformAction.setEnabled(stylesheetPanel.stylesheetsExist());    JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);    toolBar.setFloatable(false);    toolBar.add(loadSettingsAction.getButton());    toolBar.add(saveSettingsAction.getButton());    toolBar.addSeparator();    toolBar.add(transformAction.getButton());    JPanel panel = new JPanel();    panel.add(toolBar);    return panel;  }  public View getView() {    return view;  }  public InputSelectionPanel getInputSelectionPanel() {  	return inputSelectionPanel;  }    public StylesheetPanel getStylesheetPanel() {    return stylesheetPanel;  }  public StylesheetParameterPanel getParameterPanel() {    return parameterPanel;  }  public String getResultFile() {    return this.resultFileTextField.getText();  }    public void setResultFile(String resultFile) {    resultFileTextField.setText(resultFile);    jEdit.setProperty(LAST_RESULT, resultFile);  }   public void setStylesheets(String[] stylesheets) {    this.stylesheetPanel.setStylesheets(stylesheets);  }  public void setStylesheetParameters(String[] names, String[] values) {    this.parameterPanel.setParameters(names, values);  }  /**   * Attempts to load settings from a user specified file.   */  private void loadSettings() {    try {      XsltSettings settings = new XsltSettings(this);      settings.loadFromFile();    } catch(Exception e) {      Log.log(Log.ERROR, this, e.toString());    }  }  /**   * Attempts to save settings to a user specified file.   */  private void saveSettings() {    try {      XsltSettings settings = new XsltSettings(this);      settings.writeToFile();    } catch(IOException e) {      Log.log(Log.ERROR, this, e.toString());    }  }  private void chooseFile() {    String path = null;    String[] selections = GUIUtilities.showVFSFileDialog(view, path, JFileChooser.OPEN_DIALOG, false);    if(selections != null) {        setResultFile(selections[0]);    }    Container topLevelAncestor = XSLTProcessor.this.getTopLevelAncestor();    if(topLevelAncestor instanceof JFrame) {      ((JFrame)topLevelAncestor).toFront();    }  }  public boolean isResultFileDefined() {    return !getResultFile().equals(jEdit.getProperty("xslt.result.prompt"));  }  private void transform() {    if(!getStylesheetPanel().stylesheetsExist()) {      XSLTPlugin.showMessageDialog("xslt.transform.message.no-stylesheets", this);    } else if(!inputSelectionPanel.isSourceFileDefined()) {      XSLTPlugin.showMessageDialog("xslt.transform.message.no-source", this);    } else if(!isResultFileDefined()) {      XSLTPlugin.showMessageDialog("xslt.transform.message.no-result", this);    } else {      doTransform();    }  }  private void doTransform() {    try {      Date start = new Date();      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));      	  String path = new String();	  InputSource inputSource;		  if (inputSelectionPanel.isFileSelected()) {		  path = inputSelectionPanel.getSourceFile();		  FileReader textReader = new FileReader(new File(path));		  inputSource = new InputSource(textReader);	  } else {		  Buffer buffer = view.getBuffer();		  path = buffer.getPath();		  String text = buffer.getText(0, buffer.getLength());		  inputSource = new InputSource(new StringReader(text));	  }  		  inputSource.setSystemId(path);	              XSLTUtilities.transform(inputSource, getStylesheets(), getStylesheetParameters(), getResultFile());      Buffer buffer = jEdit.getBuffer(getResultFile());      if(buffer == null) {        jEdit.openFile(view, getResultFile());      } else {        buffer.reload(view);      }      logTimeTaken(start);    } catch(Exception e) {      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));      String message = jEdit.getProperty("xslt.transform.message.failure");      XSLTPlugin.processException(e, message, XSLTProcessor.this);    } finally {      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));    }  }  private Object[] getStylesheets() {    return getStylesheetPanel().getStylesheets();  }  private Map getStylesheetParameters() {    return getParameterPanel().getParametersMap();  }  private void logTimeTaken(Date start) {    Date end = new Date();    long timeTaken = end.getTime() - start.getTime();    long secondsTaken = timeTaken / 1000;    long partialSecondsTaken = timeTaken % 1000;    Object[] param = {secondsTaken + "." + partialSecondsTaken};    String status = jEdit.getProperty("xslt.transform.message.success", param);    Log.log(Log.MESSAGE, this, status);  }  private void openFile(Object eventSource) {    String file = null;    if(eventSource == inputSelectionPanel.getTextField() && inputSelectionPanel.isSourceFileDefined()) {      file = inputSelectionPanel.getSourceFile();    } else if(eventSource == resultFileTextField && isResultFileDefined()) {      file = getResultFile();    } else {      file = stylesheetPanel.getSelectedStylesheet();    }    if(file != null) {      jEdit.openFile(view, file);    }  }  private class ResultNameAction extends XsltAction {    public void actionPerformed(ActionEvent e) {      Log.log(Log.MESSAGE, this, "name action performed");      chooseFile();    }    protected String getActionName() {      return "result.name";    }  }  private class OpenFileAction extends XsltAction {    private Object source;    public void setSource(Object source) {      this.source = source;    }    public void actionPerformed(ActionEvent e) {      Log.log(Log.DEBUG, source, "open file action called");      openFile(source);    }    protected String getActionName() {      return "file.open";    }  }  private class TransformAction extends XsltAction {    public void actionPerformed(ActionEvent e) {      transform();    }    protected String getActionName() {      return "transform";    }    protected Dimension getButtonDimension() {      Dimension dimension = new Dimension(74, 30);      return dimension;    }  }  private class SaveSettingsAction extends XsltAction {    public void actionPerformed(ActionEvent e) {      saveSettings();    }    protected String getActionName() {      return "settings.save";    }  }  private class LoadSettingsAction extends XsltAction {    public void actionPerformed(ActionEvent e) {      loadSettings();    }    protected String getActionName() {      return "settings.load";    }  }  private class OpenFileKeyAdaptor extends KeyAdapter {    /**     * If enter key is pressed, opens relevant file depending on the source of the key press event.     * Implementation of {@link java.awt.event.KeyListener} interface method.     */    public void keyPressed(KeyEvent event) {      if(event.getKeyCode() == KeyEvent.VK_ENTER) {        openFile(event.getSource());        event.consume();      }    }  }  private class XsltMouseAdaptor extends MouseAdapter {    /**     * Implementation of {@link java.awt.event.MouseListener} interface method.     */    public void mouseClicked(MouseEvent event) {      parameterPanel.stopEditing();      Object eventSource = event.getSource();      if(event.getClickCount() >= 2) {        openFile(eventSource);        event.consume();      } else if(GUIUtilities.isPopupTrigger(event)) {        Log.log(Log.DEBUG, this, "the event source is " + eventSource);        openFileAction.setSource(eventSource);        JPopupMenu menu;        if(inputSelectionPanel.isSourceFieldEventSource(eventSource)           && inputSelectionPanel.isSourceFileDefined()           && inputSelectionPanel.isFileSelected()) {          XSLTProcessor.this.openFileAction.setEnabled(true);          menu = sourceMenu;        } else if(eventSource == resultFileTextField && isResultFileDefined()) {          XSLTProcessor.this.openFileAction.setEnabled(true);          menu = resultMenu;        } else if(stylesheetPanel.isStylesheetListEventSource(eventSource)) {          XSLTProcessor.this.openFileAction.setEnabled(stylesheetPanel.stylesheetsExist());          menu = stylesheetMenu;        } else {          menu = stylesheetParameterMenu;        }        GUIUtilities.showPopupMenu(menu, (Component)eventSource, event.getX(), event.getY());      }    }    /**     * Implementation of {@link java.awt.event.MouseListener} interface method.     */    public void mousePressed(MouseEvent event) {      if(GUIUtilities.isPopupTrigger(event)) {        stylesheetPanel.setSelected(event.getPoint());      }    }  }}