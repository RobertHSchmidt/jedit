/*
 * XSearchDialog.java - Search and replace dialog
 * :tabSize=2:indentSize=2:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2002 Rudolf Widmann
 * portions copyright (C) 1998, 1999, 2000, 2001 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

// package org.gjt.sp.jedit.search;

//{{{ Imports
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;
// import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.io.*;
import org.gjt.sp.jedit.msg.SearchSettingsChanged;
import org.gjt.sp.jedit.msg.ViewUpdate;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;

import org.gjt.sp.jedit.search.*;
//}}}

/**
 * Search and replace dialog.
 * @author Slava Pestov
 * @version $Id$
 * derived version $Id$
 */
public class XSearchDialog extends EnhancedDialog implements EBComponent
/**
 * Remark: it is not possible to inherit from SearchDialog, because the constructor is doing all the job
 */
{
	//{{{ Constants
	/**
	 * Default file set.
	 * @since jEdit 3.2pre2
	 */
	public static final int CURRENT_BUFFER = 0;
	public static final int ALL_BUFFERS = 1;
	public static final int DIRECTORY = 2;
	public static final int SEARCH_IN_OUT_NONE = 0;
	public static final int SEARCH_IN_OUT_INSIDE = 1;
	public static final int SEARCH_IN_OUT_OUTSIDE = 2;
	public static final int SEARCH_PART_NONE = 0;
	public static final int SEARCH_PART_PREFIX = 1;
	public static final int SEARCH_PART_SUFFIX = 2;
	public static final int SEARCH_PART_WHOLE_WORD = 3;

	//}}}

	//{{{ showSearchDialog() method
	/**
	 * Displays a search and replace dialog box, reusing an existing one
	 * if necessary.
	 * @param view The view
	 * @param searchString The search string
	 * @param searchIn One of CURRENT_BUFFER, ALL_BUFFERS, or DIRECTORY
	 * @since jEdit 4.0pre6
	 */
	public static void showSearchDialog(View view, String searchString,
		int searchIn)
	{
		if (XSearchAndReplace.debug) Log.log(Log.DEBUG, BeanShell.class,
			"XSearchDialog.82: invoke showSearchDialog with searchString = "+searchString);
		XSearchAndReplace.resetIgnoreFromTop(); // rwchg
		XSearchDialog dialog = (XSearchDialog)viewHash.get(view);
		if(dialog != null)
		{
			// ugly workaround
			if(OperatingSystem.isUnix() && !OperatingSystem.isMacOS())
			{
				dialog.setVisible(false);
				dialog.setVisible(true);
			}

			dialog.setSearchString(searchString,searchIn);
			GUIUtilities.requestFocus(dialog,dialog.find);
			dialog.toFront();
			dialog.requestFocus();
		}
		else
		{
			dialog = new XSearchDialog(view,searchString,searchIn);
			viewHash.put(view,dialog);
		}
	} //}}}

	//{{{ XSearchDialog constructor
	/**
	 * Creates a new search and replace dialog box.
	 * @param view The view
	 * @param searchString The search string
	 */
	public XSearchDialog(View view, String searchString)
	{
		this(view,searchString,CURRENT_BUFFER);
	} //}}}

	//{{{ XSearchDialog constructor
	/**
	 * Creates a new search and replace dialog box.
	 * @param view The view
	 * @param searchString The search string
	 * @param searchIn One of CURRENT_BUFFER, ALL_BUFFERS, or DIRECTORY
	 * @since jEdit 3.2pre2
	 */
	public XSearchDialog(View view, String searchString, int searchIn)
	{
		super(view,jEdit.getProperty("search.title"),false);

		this.view = view;

		content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(0,12,12,12));
		setContentPane(content);

		centerPanel = new JPanel(new BorderLayout());
		southPanel = new JPanel(new BorderLayout());

		// rwchg: switchable panel display
		// memorize add-panels
		globalFieldPanel = createFieldPanel();
		searchSettingsPanel = createSearchSettingsPanel();
		multiFilePanel = createMultiFilePanel();
		buttonsPanel = createButtonsPanel();
		extendedOptionsPanel = createExtendedOptionsPanel();

		centerPanel.add(BorderLayout.NORTH,globalFieldPanel);
		centerPanel.add(BorderLayout.CENTER,searchSettingsPanel);
		content.add(BorderLayout.CENTER,centerPanel);
		southPanel.add(BorderLayout.CENTER,extendedOptionsPanel);
		southPanel.add(BorderLayout.SOUTH,multiFilePanel);
		content.add(BorderLayout.SOUTH,southPanel);

		content.add(BorderLayout.EAST,buttonsPanel);
/**
 *  setup extended options
*/
		boolean resetRegex = true; // ico wordpart, regex was implicit set: reset it
		switch (XSearchAndReplace.getWordPartOption()) {
			case SEARCH_PART_PREFIX: wordPartPrefixRadioBtn.setSelected(true);
				break;
			case SEARCH_PART_SUFFIX: wordPartSuffixRadioBtn.setSelected(true);
				break;
			case SEARCH_PART_WHOLE_WORD: wordPartWholeRadioBtn.setSelected(true);
				break;
			default: resetRegex = false;	
		}
		if (resetRegex) XSearchAndReplace.setRegexp(false);
		
		switch (XSearchAndReplace.getCommentOption()) {
			case SEARCH_IN_OUT_INSIDE: searchCommentInsideRadioBtn.setSelected(true);
				break;
			case SEARCH_IN_OUT_OUTSIDE: searchCommentOutsideRadioBtn.setSelected(true);
				break;
		}
		switch (XSearchAndReplace.getFoldOption()) {
			case SEARCH_IN_OUT_INSIDE: searchFoldInsideRadioBtn.setSelected(true);
				break;
			case SEARCH_IN_OUT_OUTSIDE: searchFoldOutsideRadioBtn.setSelected(true);
				break;
		}
		if (XSearchAndReplace.getColumnOption()) {
			columnRadioBtn.setSelected(true);
			columnExpandTabsRadioBtn.setSelected(XSearchAndReplace.getColumnExpandTabsOption());
			columnLeftRowField.setText(Integer.toString(XSearchAndReplace.getColumnLeftCol()));
			columnRightRowField.setText(Integer.toString(XSearchAndReplace.getColumnRightCol()));
			enableColumnOptions(true);
		} else enableColumnOptions(false);

		ignoreCase.setSelected(XSearchAndReplace.getIgnoreCase());
		regexp.setSelected(XSearchAndReplace.getRegexp());
		wrap.setSelected(XSearchAndReplace.getAutoWrapAround());

		if(XSearchAndReplace.getReverseSearch())
			searchBack.setSelected(true);
		else if (XSearchAndReplace.getSearchFromTop())
			searchFromTop.setSelected(true);
		else
			searchForward.setSelected(true);

		if(XSearchAndReplace.getBeanShellReplace())
		{
			replace.setModel("replace.script");
			beanShellReplace.setSelected(true);
		}
		else
		{
			replace.setModel("replace");
			stringReplace.setSelected(true);
		}

		SearchFileSet fileset = XSearchAndReplace.getSearchFileSet();

		if(fileset instanceof DirectoryListSet)
		{
			filter.setText(((DirectoryListSet)fileset)
				.getFileFilter());
			directory.setText(((DirectoryListSet)fileset)
				.getDirectory());
			searchSubDirectories.setSelected(((DirectoryListSet)fileset)
				.isRecursive());
		}
		else
		{
			String path = MiscUtilities.getParentOfPath(
				view.getBuffer().getPath());

			if(path.endsWith("/") || path.endsWith(File.separator))
				path = path.substring(0,path.length() - 1);

			directory.setText(path);

			if(fileset instanceof AllBufferSet)
			{
				filter.setText(((AllBufferSet)fileset)
					.getFileFilter());
			}
			else
			{
				filter.setText("*" + MiscUtilities
					.getFileExtension(view.getBuffer()
					.getName()));
			}

			searchSubDirectories.setSelected(true);
		}

		// all pre-selections are done: show / hide panels
		if (!showSettings.isSelected()) showHideOptions(showSettings);
		if (!showReplace.isSelected()) showHideOptions(showReplace);
		if (!showExtendedOptions.isSelected()) showHideOptions(showExtendedOptions);
		if (!searchDirectory.isSelected() && !searchAllBuffers.isSelected()) showHideOptions(searchDirectory);
		setupCurrentSelectedOptionsLabel();

		directory.addCurrentToHistory();

		keepDialog.setSelected(jEdit.getBooleanProperty(
			"search.keepDialog.toggle"));
		setSearchString(searchString,searchIn);
		pack();
		jEdit.unsetProperty("search.width");
		jEdit.unsetProperty("search.d-width");
		jEdit.unsetProperty("search.height");
		jEdit.unsetProperty("search.d-height");
		GUIUtilities.loadGeometry(this,"search");
		show();

		EditBus.addToBus(this);

		GUIUtilities.requestFocus(this,find);
	} //}}}

	//{{{ setSearchString() method
	/**
	 * Sets the search string.
	 * @since jEdit 4.0pre5
	 */
	public void setSearchString(String searchString, int searchIn)
	{
		if(searchString == null)
			find.setText(null);
		else
		{
			if(searchString.indexOf('\n') == -1)
			{
				find.setText(searchString);
				find.selectAll();
			}
			else if(searchIn == CURRENT_BUFFER)
			{
				searchSelection.setSelected(true);
				hyperSearch.setSelected(true);
			}
		}

		if(searchIn == CURRENT_BUFFER)
		{
			if(!searchSelection.isSelected())
			{
				// might be already selected, see above.
				searchCurrentBuffer.setSelected(true);

				/* this property is only loaded and saved if
				 * the 'current buffer' file set is selected.
				 * otherwise, it defaults to on. */
				hyperSearch.setSelected(jEdit.getBooleanProperty(
					"search.hypersearch.toggle"));
			}
		}
		else if(searchIn == ALL_BUFFERS)
		{
			searchAllBuffers.setSelected(true);
			hyperSearch.setSelected(true);
		}
		else if(searchIn == DIRECTORY)
		{
			hyperSearch.setSelected(true);
			searchDirectory.setSelected(true);
		}

		updateEnabled();
	} //}}}

	//{{{ ok() method
	public void ok()
	{
		
if (XSearchAndReplace.debug) Log.log(Log.DEBUG, BeanShell.class,"XSearchDialog.331: ok invoked with find.getText() = "+find.getText()+", regexp.isSelected() = "+regexp.isSelected()+", wordPartPrefixRadioBtn.isSelected() = "+wordPartPrefixRadioBtn.isSelected()+", wordPartWholeRadioBtn.isSelected() = "+wordPartWholeRadioBtn.isSelected());
//		if (XSearchAndReplace.getSearchString() == null)
		try
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			if(!save(false))
				return;
			/* {{{ manipulate search string ico word part search
			this part has been moved to "XSearchAndreplace
			String visibleSearchString=null;
			// word part handling: manipulate search string, but memorize it
			if (!wordPartDefaultRadioBtn.isSelected()) {
				visibleSearchString = XSearchAndReplace.getSearchString();
				String regExpSearchString = visibleSearchString;
				// ap: change regExpSearchString
				XSearchAndReplace.setRegexp(true);
				if (wordPartWholeRadioBtn.isSelected()) {
					XSearchAndReplace.setSearchString("\\<"+regExpSearchString+"\\>");
				}
				else if (wordPartPrefixRadioBtn.isSelected()) {
					XSearchAndReplace.setSearchString("\\<"+regExpSearchString);
				}
				else if (wordPartSuffixRadioBtn.isSelected()) {
					XSearchAndReplace.setSearchString(regExpSearchString+"\\>");
				}
			}
			}}}*/
			if(hyperSearch.isSelected() || searchSelection.isSelected())
			{
				if(XSearchAndReplace.hyperSearch(view,
					searchSelection.isSelected()));
					closeOrKeepDialog();
			}
			else
			{
				boolean searchResult = XSearchAndReplace.find(view);
				if(searchResult)
					closeOrKeepDialog();
				else
				{
					toFront();
					requestFocus();
					find.requestFocus();
				}
			}
			/* word part handling: reset search string
			if (!wordPartDefaultRadioBtn.isSelected()) {
				XSearchAndReplace.setSearchString(visibleSearchString);
				XSearchAndReplace.setRegexp(false);
			}*/
		}
		finally
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	} //}}}

	//{{{ cancel() method
	public void cancel()
	{
		save(true);
		GUIUtilities.saveGeometry(this,"search");
		dispose();
	} //}}}

	//{{{ handleMessage() method
	public void handleMessage(EBMessage msg)
	{
		if(msg instanceof SearchSettingsChanged)
		{
			ignoreCase.setSelected(XSearchAndReplace.getIgnoreCase());
			regexp.setSelected(XSearchAndReplace.getRegexp());
		}
		else if(msg instanceof ViewUpdate)
		{
			ViewUpdate vmsg = (ViewUpdate)msg;
			if(vmsg.getView() == view
				&& vmsg.getWhat() == ViewUpdate.CLOSED)
			{
				viewHash.remove(view);
			}
		}
	} //}}}

	//{{{ dispose() method
	public void dispose()
	{
		EditBus.removeFromBus(this);
		viewHash.remove(view);
		super.dispose();
	} //}}}

	//{{{ Private members

	private static HashMap viewHash = new HashMap();

	//{{{ Instance variables
	private View view;

	// fields
	private HistoryTextField find, replace;

	private JRadioButton stringReplace, beanShellReplace;

	// rwchg: panels
	private JPanel content;
	private JPanel centerPanel;
	private JPanel southPanel;
	private JPanel globalFieldPanel;
	private JPanel searchSettingsPanel;
	private JPanel multiFilePanel;
	private JPanel extendedOptionsPanel;
	private JPanel grid;
	private Box buttonsPanel;
	private Box replaceModeBox;
//	private Box foldBox;

	// components inside of panels
	private JLabel fieldPanelReplaceLabel;
	private JLabel currentSelectedOptionsLabel = new JLabel(jEdit.getProperty("search.currOpt.label"));
	private int optionsLabelIndex;
	private StringBuffer currentSelectedSettingsOptions = new StringBuffer();
	private StringBuffer currentSelectedExtendedOptions = new StringBuffer();
	private JRadioButton showSettings;
	private JRadioButton showReplace;
	private JRadioButton showExtendedOptions;

	private JRadioButton wordPartWholeRadioBtn;
	private JRadioButton wordPartPrefixRadioBtn;
	private JRadioButton wordPartSuffixRadioBtn;
	private JRadioButton wordPartDefaultRadioBtn = new JRadioButton();
	//private JRadioButton wordPartActualRadioBtn = wordPartDefaultRadioBtn;

	private JRadioButton searchFoldInsideRadioBtn;
	private JRadioButton searchFoldOutsideRadioBtn;
	private JRadioButton searchFoldDefaultRadioBtn = new JRadioButton();
	//private JRadioButton searchFoldActualRadioBtn = searchFoldDefaultRadioBtn;

	private JRadioButton searchCommentInsideRadioBtn;
	private JRadioButton searchCommentOutsideRadioBtn;
	private JRadioButton searchCommentDefaultRadioBtn = new JRadioButton();
	//private JRadioButton searchCommentActualRadioBtn = searchCommentDefaultRadioBtn;

	private Component fieldPanelVerticalStrut = Box.createVerticalStrut(3);

	// search settings
	private JCheckBox keepDialog, ignoreCase, regexp, hyperSearch,
		wrap;
	private JRadioButton searchFromTop, searchBack, searchForward;
	private JRadioButton searchSelection, searchCurrentBuffer, searchAllBuffers,
		searchDirectory;

	// expanded options
	private JRadioButton columnRadioBtn;
	private JRadioButton columnExpandTabsRadioBtn;
	private JLabel leftRowLabel;
	private JTextField columnLeftRowField;
	private JLabel rightRowLabel;
	private JTextField columnRightRowField;

	// multifile settings
	private HistoryTextField filter, directory;
	private JCheckBox searchSubDirectories;
	private JButton choose;

	// buttons
	private JButton findBtn, /* replaceBtn, */ replaceAndFindBtn, replaceAllBtn,
		closeBtn;
	//}}}

	//{{{ createFieldPanel() method
	private JPanel createFieldPanel()
	{
		ButtonActionHandler actionHandler = new ButtonActionHandler();

		JPanel fieldPanel = new JPanel(new VariableGridLayout(
			VariableGridLayout.FIXED_NUM_COLUMNS,1));
		fieldPanel.setBorder(new EmptyBorder(0,0,12,12));

		JLabel label = new JLabel(jEdit.getProperty("search.find"));
		label.setDisplayedMnemonic(jEdit.getProperty("search.find.mnemonic")
			.charAt(0));
		find = new HistoryTextField("find");

		// don't want it to be too wide due to long strings
		Dimension size = find.getPreferredSize();
		size.width = find.getFontMetrics(find.getFont())
			.charWidth('a') * 25;
		find.setPreferredSize(size);

		find.addActionListener(actionHandler);
		label.setLabelFor(find);
		label.setBorder(new EmptyBorder(12,0,2,0));
		fieldPanel.add(label);
		// add: "Search for (press up arrow to recall previous)"
		fieldPanel.add(find);
		// add: <search input textField>

		// rwchg add panel display selection radio buttons
		SelectivShowActionHandler selectivShowActionHandler = new SelectivShowActionHandler();
		Box selectivShowBox = new Box(BoxLayout.X_AXIS);
		selectivShowBox.add(new JLabel(jEdit.getProperty("search.show-options")));

		showSettings = new MyJRadioButton(jEdit.getProperty("search.show-settings"));
		showSettings.setSelected(jEdit.getBooleanProperty("search.show-settings.toggle"));
		showSettings.addActionListener(selectivShowActionHandler);
		showReplace = new MyJRadioButton(jEdit.getProperty("search.show-replace"));
		showReplace.setSelected(jEdit.getBooleanProperty("search.show-replace.toggle"));
		showReplace.addActionListener(selectivShowActionHandler);
		showExtendedOptions = new MyJRadioButton(jEdit.getProperty("search.show-extended"));
		showExtendedOptions.setSelected(jEdit.getBooleanProperty("search.show-extended.toggle"));
		showExtendedOptions.addActionListener(selectivShowActionHandler);

		selectivShowBox.add(showSettings);
		selectivShowBox.add(showReplace);
		selectivShowBox.add(showExtendedOptions);
		fieldPanel.add(selectivShowBox);
		// add: <selectiv show options: search, replace, extended
		fieldPanel.add(currentSelectedOptionsLabel);
		// add: <display of current selected find options
		optionsLabelIndex = fieldPanel.getComponentCount() - 1;

		fieldPanelReplaceLabel = new JLabel(jEdit.getProperty("search.replace"));
		fieldPanelReplaceLabel.setDisplayedMnemonic(jEdit.getProperty("search.replace.mnemonic")
			.charAt(0));
		fieldPanelReplaceLabel.setBorder(new EmptyBorder(12,0,0,0));
		fieldPanel.add(fieldPanelReplaceLabel);
		// add: "Replace with"

		ButtonGroup grp = new ButtonGroup();
		ReplaceActionHandler replaceActionHandler = new ReplaceActionHandler();

		// we use a custom JRadioButton subclass that returns
		// false for isFocusTraversable() so that the user can
		// tab from the search field to the replace field with
		// one keystroke

		replaceModeBox = new Box(BoxLayout.X_AXIS);
		stringReplace = new MyJRadioButton(jEdit.getProperty(
			"search.string-replace-btn"));
		stringReplace.addActionListener(replaceActionHandler);
		grp.add(stringReplace);
		replaceModeBox.add(stringReplace);

		replaceModeBox.add(Box.createHorizontalStrut(12));

		beanShellReplace = new MyJRadioButton(jEdit.getProperty(
			"search.beanshell-replace-btn"));
		beanShellReplace.addActionListener(replaceActionHandler);
		grp.add(beanShellReplace);
		replaceModeBox.add(beanShellReplace);

		fieldPanel.add(replaceModeBox);
		// add: <replace mode: Text, Return value of BeanShell snippet
		fieldPanel.add(fieldPanelVerticalStrut);

		replace = new HistoryTextField("replace");
		replace.addActionListener(actionHandler);
		fieldPanelReplaceLabel.setLabelFor(replace);
		fieldPanel.add(replace);
		// add: <replace input textField>
		return fieldPanel;
	} //}}}

	//{{{ createExtendedOptionsPanel() method
	private JPanel createExtendedOptionsPanel()
	{
		JPanel extendedOptions = new JPanel(new VariableGridLayout(
			VariableGridLayout.FIXED_NUM_COLUMNS,1));
		extendedOptions.setBorder(new EmptyBorder(0,0,12,12));

		ExtendedOptionsActionHandler extendedOptionsActionHandler = new ExtendedOptionsActionHandler();

		/*******************************************************************
		 * word part handling: whole word / prefix / suffix
		 *******************************************************************/
		Box wordPartBox = new Box(BoxLayout.X_AXIS);

		wordPartWholeRadioBtn = new JRadioButton(jEdit.getProperty("search.ext.word-whole"));
		wordPartWholeRadioBtn.addActionListener(extendedOptionsActionHandler);
		wordPartPrefixRadioBtn = new JRadioButton(jEdit.getProperty("search.ext.word-prefix"));
		wordPartPrefixRadioBtn.addActionListener(extendedOptionsActionHandler);
		wordPartSuffixRadioBtn = new JRadioButton(jEdit.getProperty("search.ext.word-suffix"));
		wordPartSuffixRadioBtn.addActionListener(extendedOptionsActionHandler);

		wordPartBox.add(wordPartWholeRadioBtn);
		wordPartBox.add(wordPartPrefixRadioBtn);
		wordPartBox.add(wordPartSuffixRadioBtn);

		ButtonGroupHide wordPartGrp = new ButtonGroupHide();

		wordPartGrp.add(wordPartDefaultRadioBtn);
		wordPartGrp.add(wordPartWholeRadioBtn);
		wordPartGrp.add(wordPartPrefixRadioBtn);
		wordPartGrp.add(wordPartSuffixRadioBtn);
		wordPartDefaultRadioBtn.setSelected(true);
		extendedOptions.add(wordPartBox);

		/*******************************************************************
		 * column handling
		 *******************************************************************/
		Box columnBox = new Box(BoxLayout.X_AXIS);

		columnRadioBtn = new JRadioButton(jEdit.getProperty("search.ext.column"));
		columnRadioBtn.addActionListener(extendedOptionsActionHandler);

		columnExpandTabsRadioBtn = new JRadioButton(jEdit.getProperty("search.ext.column.expand-tabs"));
		columnExpandTabsRadioBtn.setSelected(true);
		// columnExpandTabsRadioBtn.addActionListener(extendedOptionsActionHandler);

		columnLeftRowField = new JTextField(3);
		leftRowLabel = new JLabel(jEdit.getProperty("search.ext.column.left"));
		columnRightRowField = new JTextField(3);
		rightRowLabel = new JLabel(jEdit.getProperty("search.ext.column.right"));

		columnExpandTabsRadioBtn.setEnabled(false);
		columnLeftRowField.setEnabled(false);
		leftRowLabel.setEnabled(false);
		columnRightRowField.setEnabled(false);
		rightRowLabel.setEnabled(false);

		columnBox.add(columnRadioBtn);
		columnBox.add(columnExpandTabsRadioBtn);
		columnBox.add(leftRowLabel);
		columnBox.add(Box.createHorizontalStrut(5));
		columnBox.add(columnLeftRowField);
		columnBox.add(Box.createHorizontalStrut(5));
		columnBox.add(rightRowLabel);
		columnBox.add(Box.createHorizontalStrut(5));
		columnBox.add(columnRightRowField);
//		selectivShowBox.add(Box.createHorizontalStrut(12));
		extendedOptions.add(columnBox);

		/*******************************************************************
		 * folding handling
		 * search only  o inside fold  o outside fold
		 *******************************************************************/
		Box foldBox = new Box(BoxLayout.X_AXIS);

		JLabel searchFoldLabel = new JLabel(jEdit.getProperty("search.ext.fold"));
		searchFoldInsideRadioBtn = new JRadioButton(jEdit.getProperty("search.ext.fold-inside"));
		searchFoldOutsideRadioBtn = new JRadioButton(jEdit.getProperty("search.ext.fold-outside"));
		searchFoldInsideRadioBtn.addActionListener(extendedOptionsActionHandler);
		searchFoldOutsideRadioBtn.addActionListener(extendedOptionsActionHandler);
		foldBox.add(searchFoldLabel);
		foldBox.add(searchFoldInsideRadioBtn);
		foldBox.add(searchFoldOutsideRadioBtn);

		ButtonGroupHide foldGrp = new ButtonGroupHide();
		foldGrp.add(searchFoldDefaultRadioBtn);
		foldGrp.add(searchFoldInsideRadioBtn);
		foldGrp.add(searchFoldOutsideRadioBtn);
		extendedOptions.add(foldBox);
		searchFoldDefaultRadioBtn.setSelected(true);

		/*******************************************************************
		 * comment handling
		 * search only  o inside comment  o outside comment
		 *******************************************************************/
		Box commentBox = new Box(BoxLayout.X_AXIS);

		JLabel searchCommentLabel = new JLabel(jEdit.getProperty("search.ext.comment"));
		searchCommentInsideRadioBtn = new JRadioButton(jEdit.getProperty("search.ext.comment-inside"));
		searchCommentOutsideRadioBtn = new JRadioButton(jEdit.getProperty("search.ext.comment-outside"));
		//searchCommentInsideRadioBtn.addActionListener(extendedOptionsActionHandler);
		//searchCommentOutsideRadioBtn.addActionListener(extendedOptionsActionHandler);
		commentBox.add(searchCommentLabel);
		commentBox.add(searchCommentInsideRadioBtn);
		commentBox.add(searchCommentOutsideRadioBtn);

		ButtonGroupHide commentGrp = new ButtonGroupHide();
		commentGrp.add(searchCommentDefaultRadioBtn);
		commentGrp.add(searchCommentInsideRadioBtn);
		commentGrp.add(searchCommentOutsideRadioBtn);
		searchCommentDefaultRadioBtn.setSelected(true);
		extendedOptions.add(commentBox);



		return extendedOptions;
	} //}}}

	//{{{ createSearchSettingsPanel() method
	private JPanel createSearchSettingsPanel()
	{
		JPanel searchSettings = new JPanel(new VariableGridLayout(
			VariableGridLayout.FIXED_NUM_COLUMNS,3));
		searchSettings.setBorder(new EmptyBorder(0,0,12,12));

		SettingsActionHandler actionHandler = new SettingsActionHandler();
		SelectivShowActionHandler selectivShowActionHandler = new SelectivShowActionHandler();

		ButtonGroup fileset = new ButtonGroup();
		ButtonGroup direction = new ButtonGroup();

		searchSettings.add(new JLabel(jEdit.getProperty("search.fileset")));

		searchSettings.add(new JLabel(jEdit.getProperty("search.settings")));

		searchSettings.add(new JLabel(jEdit.getProperty("search.direction")));

		searchSelection = new JRadioButton(jEdit.getProperty("search.selection"));
		searchSelection.setMnemonic(jEdit.getProperty("search.selection.mnemonic")
			.charAt(0));
		fileset.add(searchSelection);
		searchSettings.add(searchSelection);
		searchSelection.addActionListener(actionHandler);
		searchSelection.addActionListener(selectivShowActionHandler);

		keepDialog = new JCheckBox(jEdit.getProperty("search.keep"));
		keepDialog.setMnemonic(jEdit.getProperty("search.keep.mnemonic")
			.charAt(0));
		searchSettings.add(keepDialog);
		// rwchg: add search.fromTop
		// properties should be added in jedit_gui.props
		searchFromTop = new JRadioButton(jEdit.getProperty("search.fromTop")) ;
		searchFromTop.setMnemonic(jEdit.getProperty("search.fromTop.mnemonic")
			.charAt(0));
		direction.add(searchFromTop);
		searchSettings.add(searchFromTop);
		searchFromTop.addActionListener(actionHandler);

		searchCurrentBuffer = new JRadioButton(jEdit.getProperty("search.current"));
		searchCurrentBuffer.setMnemonic(jEdit.getProperty("search.current.mnemonic")
			.charAt(0));
		fileset.add(searchCurrentBuffer);
		searchSettings.add(searchCurrentBuffer);
		searchCurrentBuffer.addActionListener(actionHandler);
		searchCurrentBuffer.addActionListener(selectivShowActionHandler);

		ignoreCase = new JCheckBox(jEdit.getProperty("search.case"));
		ignoreCase.setMnemonic(jEdit.getProperty("search.case.mnemonic")
			.charAt(0));
		searchSettings.add(ignoreCase);
		ignoreCase.addActionListener(actionHandler);

		searchBack = new JRadioButton(jEdit.getProperty("search.back"));
		searchBack.setMnemonic(jEdit.getProperty("search.back.mnemonic")
			.charAt(0));
		direction.add(searchBack);
		searchSettings.add(searchBack);
		searchBack.addActionListener(actionHandler);

		searchAllBuffers = new JRadioButton(jEdit.getProperty("search.all"));
		searchAllBuffers.setMnemonic(jEdit.getProperty("search.all.mnemonic")
			.charAt(0));
		fileset.add(searchAllBuffers);
		searchSettings.add(searchAllBuffers);
		searchAllBuffers.addActionListener(actionHandler);
		searchAllBuffers.addActionListener(selectivShowActionHandler);

		regexp = new JCheckBox(jEdit.getProperty("search.regexp"));
		regexp.setMnemonic(jEdit.getProperty("search.regexp.mnemonic")
			.charAt(0));
		searchSettings.add(regexp);
		regexp.addActionListener(actionHandler);

		searchForward = new JRadioButton(jEdit.getProperty("search.forward"));
		searchForward.setMnemonic(jEdit.getProperty("search.forward.mnemonic")
			.charAt(0));
		direction.add(searchForward);
		searchSettings.add(searchForward);
		searchForward.addActionListener(actionHandler);

		searchDirectory = new JRadioButton(jEdit.getProperty("search.directory"));
		searchDirectory.setMnemonic(jEdit.getProperty("search.directory.mnemonic")
			.charAt(0));
		fileset.add(searchDirectory);
		searchSettings.add(searchDirectory);
		searchDirectory.addActionListener(actionHandler);
		searchDirectory.addActionListener(selectivShowActionHandler);

		hyperSearch = new JCheckBox(jEdit.getProperty("search.hypersearch"));
		hyperSearch.setMnemonic(jEdit.getProperty("search.hypersearch.mnemonic")
			.charAt(0));
		searchSettings.add(hyperSearch);
		hyperSearch.addActionListener(actionHandler);

		wrap = new JCheckBox(jEdit.getProperty("search.wrap"));
		wrap.setMnemonic(jEdit.getProperty("search.wrap.mnemonic")
			.charAt(0));
		searchSettings.add(wrap);
		wrap.addActionListener(actionHandler);

		return searchSettings;
	} //}}}

	//{{{ createMultiFilePanel() method
	private JPanel createMultiFilePanel()
	{
		JPanel multifile = new JPanel();

		GridBagLayout layout = new GridBagLayout();
		multifile.setLayout(layout);

		GridBagConstraints cons = new GridBagConstraints();
		cons.gridy = cons.gridwidth = cons.gridheight = 1;
		cons.anchor = GridBagConstraints.WEST;
		cons.fill = GridBagConstraints.HORIZONTAL;

		MultiFileActionHandler actionListener = new MultiFileActionHandler();
		filter = new HistoryTextField("search.filter");
		filter.addActionListener(actionListener);

		cons.insets = new Insets(0,0,3,0);

		JLabel label = new JLabel(jEdit.getProperty("search.filterField"),
			SwingConstants.RIGHT);
		label.setBorder(new EmptyBorder(0,0,0,12));
		label.setDisplayedMnemonic(jEdit.getProperty("search.filterField.mnemonic")
			.charAt(0));
		label.setLabelFor(filter);
		cons.weightx = 0.0f;
		layout.setConstraints(label,cons);
		multifile.add(label);

		cons.insets = new Insets(0,0,3,6);
		cons.weightx = 1.0f;
		layout.setConstraints(filter,cons);
		multifile.add(filter);

		cons.gridy++;

		directory = new HistoryTextField("search.directory");
		directory.addActionListener(actionListener);

		label = new JLabel(jEdit.getProperty("search.directoryField"),
			SwingConstants.RIGHT);
		label.setBorder(new EmptyBorder(0,0,0,12));

		label.setDisplayedMnemonic(jEdit.getProperty("search.directoryField.mnemonic")
			.charAt(0));
		label.setLabelFor(directory);
		cons.insets = new Insets(0,0,3,0);
		cons.weightx = 0.0f;
		layout.setConstraints(label,cons);
		multifile.add(label);

		cons.insets = new Insets(0,0,3,6);
		cons.weightx = 1.0f;
		cons.gridwidth = 2;
		layout.setConstraints(directory,cons);
		multifile.add(directory);

		choose = new JButton(jEdit.getProperty("search.choose"));
		choose.setMnemonic(jEdit.getProperty("search.choose.mnemonic")
			.charAt(0));
		cons.insets = new Insets(0,0,3,0);
		cons.weightx = 0.0f;
		cons.gridwidth = 1;
		layout.setConstraints(choose,cons);
		multifile.add(choose);
		choose.addActionListener(actionListener);

		cons.insets = new Insets(0,0,0,0);
		cons.gridy++;
		cons.gridwidth = 4;

		searchSubDirectories = new JCheckBox(jEdit.getProperty(
			"search.subdirs"));
		searchSubDirectories.setMnemonic(jEdit.getProperty("search.subdirs.mnemonic")
			.charAt(0));
		layout.setConstraints(searchSubDirectories,cons);
		multifile.add(searchSubDirectories);

		return multifile;
	} //}}}

	//{{{ createButtonsPanel() method
	private Box createButtonsPanel()
	{
		Box box = new Box(BoxLayout.Y_AXIS);

		ButtonActionHandler actionHandler = new ButtonActionHandler();

		box.add(Box.createVerticalStrut(12));

		grid = new JPanel(new GridLayout(0,1,0,12));

		findBtn = new JButton(jEdit.getProperty("search.findBtn"));
		getRootPane().setDefaultButton(findBtn);
		grid.add(findBtn);
		findBtn.addActionListener(actionHandler);

		/* replaceBtn = new JButton(jEdit.getProperty("search.replaceBtn"));
		replaceBtn.setMnemonic(jEdit.getProperty("search.replaceBtn.mnemonic")
			.charAt(0));
		grid.add(replaceBtn);
		replaceBtn.addActionListener(actionHandler); */

		replaceAndFindBtn = new JButton(jEdit.getProperty("search.replaceAndFindBtn"));
		replaceAndFindBtn.setMnemonic(jEdit.getProperty("search.replaceAndFindBtn.mnemonic")
			.charAt(0));
		grid.add(replaceAndFindBtn);
		replaceAndFindBtn.addActionListener(actionHandler);

		replaceAllBtn = new JButton(jEdit.getProperty("search.replaceAllBtn"));
		replaceAllBtn.setMnemonic(jEdit.getProperty("search.replaceAllBtn.mnemonic")
			.charAt(0));
		grid.add(replaceAllBtn);
		replaceAllBtn.addActionListener(actionHandler);

		closeBtn = new JButton(jEdit.getProperty("common.close"));
		grid.add(closeBtn);
		closeBtn.addActionListener(actionHandler);

		grid.setMaximumSize(grid.getPreferredSize());

		box.add(grid);
		box.add(Box.createGlue());

		return box;
	} //}}}

	//{{{ updateEnabled() method
	private void updateEnabled()
	{
		wrap.setEnabled(!hyperSearch.isSelected()
			&& !searchSelection.isSelected());

		boolean reverseEnabled = !hyperSearch.isSelected()
			&& searchCurrentBuffer.isSelected();
		boolean regexpSelected = regexp.isSelected();
		searchBack.setEnabled(reverseEnabled && !regexpSelected 
		&& wordPartDefaultRadioBtn.isSelected());
		// word part search not allowed in combination with regexp
		wordPartPrefixRadioBtn.setEnabled(!regexpSelected);
		wordPartSuffixRadioBtn.setEnabled(!regexpSelected);
		wordPartWholeRadioBtn.setEnabled(!regexpSelected);
		if (regexpSelected) wordPartDefaultRadioBtn.setSelected(true);
		
		searchForward.setEnabled(reverseEnabled);
		searchFromTop.setEnabled(reverseEnabled);
		if(!reverseEnabled || (!searchBack.isEnabled() && searchBack.isSelected()))
			searchFromTop.setSelected(true);

		filter.setEnabled(searchAllBuffers.isSelected()
			|| searchDirectory.isSelected());

		boolean directoryEnabled = searchDirectory.isSelected();

		directory.setEnabled(directoryEnabled);
		choose.setEnabled(directoryEnabled);
		searchSubDirectories.setEnabled(directoryEnabled);

		findBtn.setEnabled(!searchSelection.isSelected()
			|| hyperSearch.isSelected());
		replaceAndFindBtn.setEnabled(!hyperSearch.isSelected()
			&& !searchSelection.isSelected());
		if (hyperSearch.isSelected()) {
			// disable fold search
			searchFoldDefaultRadioBtn.setSelected(true);
			searchFoldInsideRadioBtn.setEnabled(false);
			searchFoldOutsideRadioBtn.setEnabled(false);
		} else {
			searchFoldInsideRadioBtn.setEnabled(true);
			searchFoldOutsideRadioBtn.setEnabled(true);
		}
			
	} //}}}

	//{{{ save() method
	/**
	 * @param cancel If true, we don't bother the user with warning messages
	 */
	private boolean save(boolean cancel)
	{
		String filter = this.filter.getText();
		this.filter.addCurrentToHistory();
		if(filter.length() == 0)
			filter = "*";

		SearchFileSet fileset = XSearchAndReplace.getSearchFileSet();

		if(searchSelection.isSelected())
			fileset = new CurrentBufferSet();
		else if(searchCurrentBuffer.isSelected())
		{
			fileset = new CurrentBufferSet();

			jEdit.setBooleanProperty("search.hypersearch.toggle",
				hyperSearch.isSelected());
		}
		else if(searchAllBuffers.isSelected())
			fileset = new AllBufferSet(filter);
		else if(searchDirectory.isSelected())
		{
			String directory = this.directory.getText();
			this.directory.addCurrentToHistory();
/*
			if((VFSManager.getVFSForPath(directory).getCapabilities()
				& VFS.LOW_LATENCY_CAP) == 0)
			{
				if(cancel)
					return false;

				int retVal = GUIUtilities.confirm(
					XSearchDialog.this,"remote-dir-search",
					null,JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
				if(retVal != JOptionPane.YES_OPTION)
					return false;
			}
*/
			boolean recurse = searchSubDirectories.isSelected();

			if(fileset instanceof DirectoryListSet)
			{
				DirectoryListSet dset = (DirectoryListSet)fileset;
				if(!dset.getDirectory().equals(directory)
					|| !dset.getFileFilter().equals(filter)
					|| !dset.isRecursive() == recurse)
					fileset = new DirectoryListSet(directory,filter,recurse);
			}
			else
				fileset = new DirectoryListSet(directory,filter,recurse);
		}
		else
		{
			// can't happen
			fileset = null;
		}

		jEdit.setBooleanProperty("search.keepDialog.toggle",
			keepDialog.isSelected());

		boolean ok = true;

		XSearchAndReplace.setSearchFileSet(fileset);

		if(find.getText().length() != 0)
		{
			find.addCurrentToHistory();
			if (XSearchAndReplace.getSearchString() != null) {
				if (!XSearchAndReplace.getSearchString().equals(find.getText())) {
					// search string has changed ==> reset refind
					XSearchAndReplace.resetIgnoreFromTop();
				}
			}
			// because of word part search, we have to set search string even if equal
			XSearchAndReplace.setSearchString(find.getText());
			replace.addCurrentToHistory();
			XSearchAndReplace.setReplaceString(replace.getText());
		}
		else
			ok = false;

			return ok;
	} //}}}

	//{{{ evalExtendedOptions() method
	/**
	 * checks extended options and assigns its values to XSearchAndReplace 
	 */
	private boolean evalExtendedOptions() {
		boolean ok = true;
		if (ok) {
		/*******************************************************************
		 * column handling
		 *******************************************************************/
			if (columnRadioBtn.isSelected()) {
				ok = evalColumnOptions();
			} else {
				XSearchAndReplace.resetColumnSearch();
			}
		}
		if (ok) {
		/*******************************************************************
		 * comment handling
		 *******************************************************************/
			if (searchCommentDefaultRadioBtn.isSelected())
				XSearchAndReplace.setCommentOption(SEARCH_IN_OUT_NONE);
			else if (searchCommentInsideRadioBtn.isSelected())
				XSearchAndReplace.setCommentOption(SEARCH_IN_OUT_INSIDE);
			else XSearchAndReplace.setCommentOption(SEARCH_IN_OUT_OUTSIDE);
		/*******************************************************************
		 * folding handling
		 *******************************************************************/
			if (searchFoldDefaultRadioBtn.isSelected())
				XSearchAndReplace.setFoldOption(SEARCH_IN_OUT_NONE);
			else if (searchFoldOutsideRadioBtn.isSelected())
				XSearchAndReplace.setFoldOption(SEARCH_IN_OUT_OUTSIDE);
			else XSearchAndReplace.setFoldOption(SEARCH_IN_OUT_INSIDE);
		/*******************************************************************
		 * word part handling
		 *******************************************************************/
			if (wordPartDefaultRadioBtn.isSelected())
				XSearchAndReplace.setWordPartOption(SEARCH_PART_NONE);
			else if (wordPartWholeRadioBtn.isSelected())
				XSearchAndReplace.setWordPartOption(SEARCH_PART_WHOLE_WORD);
			else if (wordPartPrefixRadioBtn.isSelected())
				XSearchAndReplace.setWordPartOption(SEARCH_PART_PREFIX);
			else XSearchAndReplace.setWordPartOption(SEARCH_PART_SUFFIX);
		}
		return ok;
	}
	//{{{ enableColumnOptions() method
	private void enableColumnOptions(boolean setEnabled) {
		columnExpandTabsRadioBtn.setEnabled(setEnabled); 
		columnRightRowField.setEnabled(setEnabled);
		rightRowLabel.setEnabled(setEnabled);
		columnLeftRowField.setEnabled(setEnabled);
		leftRowLabel.setEnabled(setEnabled);
	}//}}}
	

	//{{{ evalColumnOptions() method
	private boolean evalColumnOptions() {
		boolean extendTabs = columnExpandTabsRadioBtn.isSelected();
		int startRange;
		int endRange;
		JTextField errorField=null;
		JLabel     errorLabel=null;
		try {
	// eval startRange
		if (columnLeftRowField.getText().length() == 0) {
			startRange = 1;
		} else {
			errorField = columnLeftRowField;
			errorLabel = leftRowLabel;
			startRange = Integer.parseInt(errorField.getText());
			if (startRange < 1) throw new NumberFormatException();
		}
		// eval endRange
		if (columnRightRowField.getText().length() == 0) {
			if (find.getText().length() > 0) endRange = startRange + find.getText().length() - 1;
			else endRange = startRange;
			columnRightRowField.setText(Integer.toString(endRange));
		}
			else {
				errorField = columnRightRowField;
				errorLabel = rightRowLabel;
				endRange = Integer.parseInt(errorField.getText());
				if (endRange < startRange) throw new NumberFormatException();
			}
		}
		catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null,
			"Wrong input :"+errorField.getText(),
			errorLabel.getText(),
			JOptionPane.ERROR_MESSAGE);
			errorField.requestFocus();
			return false;
		}
		XSearchAndReplace.setColumnSearchOptions(extendTabs, startRange, endRange);
		if (XSearchAndReplace.debug) Log.log(Log.DEBUG, BeanShell.class,"extendTabs = "+
			extendTabs+", startRange = "+startRange+", endRange = "+endRange);
		return true;
	} //}}}

	//{{{ closeOrKeepDialog() method
	private void closeOrKeepDialog()
	{
		if(keepDialog.isSelected())
		{
			// Windows bug workaround in case a YES/NO confirm
			// was shown

			// ... but if HyperSearch results window is floating,
			// the workaround causes problems!
			if(!hyperSearch.isSelected())
			{
				toFront();
				requestFocus();
				find.requestFocus();
			}
			return;
		}
		else
		{
			GUIUtilities.saveGeometry(this,"search");
			dispose();
		}
	} //}}}

	//{{{ showHideOptions() method
	private void showHideOptions(Object source)	{
		if(source == showSettings) {
			jEdit.setBooleanProperty("search.show-settings.toggle",showSettings.isSelected());
			currentSelectedSettingsOptions.setLength(0); // init text
			if (showSettings.isSelected()) {
				// display "settings" and path
				centerPanel.add(BorderLayout.CENTER,searchSettingsPanel);
				if (searchDirectory.isSelected() || searchAllBuffers.isSelected())
					southPanel.add(BorderLayout.SOUTH,multiFilePanel);
			} else {
				// remove "settings" and path
				centerPanel.remove(searchSettingsPanel);
				southPanel.remove(multiFilePanel);
			}
		}
		else if(source == showReplace) {
			jEdit.setBooleanProperty("search.show-replace.toggle",showReplace.isSelected());
			if (showReplace.isSelected()) {
				globalFieldPanel.add(fieldPanelReplaceLabel);
				globalFieldPanel.add(replaceModeBox);
				globalFieldPanel.add(fieldPanelVerticalStrut);
				globalFieldPanel.add(replace);
				grid.remove(closeBtn);  // remove first to keep sorting
				grid.add(replaceAndFindBtn);
				grid.add(replaceAllBtn);
				grid.add(closeBtn);

			} else {
				globalFieldPanel.remove(fieldPanelReplaceLabel);
				globalFieldPanel.remove(replaceModeBox);
				globalFieldPanel.remove(fieldPanelVerticalStrut);
				globalFieldPanel.remove(replace);
				grid.remove(replaceAndFindBtn);
				grid.remove(replaceAllBtn);
			}
		}
		else if(source == showExtendedOptions) {
			jEdit.setBooleanProperty("search.show-extended.toggle",showExtendedOptions.isSelected());
			if (showExtendedOptions.isSelected()) {
				southPanel.add(BorderLayout.CENTER,extendedOptionsPanel);
			} else {
				southPanel.remove(extendedOptionsPanel);
			}
		}
		else if(source == searchDirectory || source == searchAllBuffers 
			|| source == searchSelection || source == searchCurrentBuffer)
			if (searchDirectory.isSelected() || searchAllBuffers.isSelected()) {
				southPanel.add(BorderLayout.SOUTH,multiFilePanel);
			} else {
				southPanel.remove(multiFilePanel);
			}
	} //}}}
	//{{{ setupCurrentSelectedOptionsLabel() method
	// setup currentSelectedOptionsLabel to display options in one line if panel part is hidden
	private void setupCurrentSelectedOptionsLabel() {
		StringBuffer currentSelectedOptions = new StringBuffer();
		if (!showSettings.isSelected() || !showExtendedOptions.isSelected()) {
			if (!showSettings.isSelected()) {
				if (ignoreCase.isSelected()) 
					currentSelectedOptions.append(jEdit.getProperty("search.currOpt.ignoreCase")+" ");
				if (regexp.isSelected()) 
					currentSelectedOptions.append(jEdit.getProperty("search.currOpt.regexp")+" ");
				if (searchFromTop.isSelected()) 
					currentSelectedOptions.append(jEdit.getProperty("search.currOpt.fromTop")+" ");
				if (searchBack.isSelected()) 
					currentSelectedOptions.append(jEdit.getProperty("search.currOpt.backward")+" ");
				if (wrap.isSelected()) 
					currentSelectedOptions.append(jEdit.getProperty("search.currOpt.wrap")+" ");
			}
			if (!showExtendedOptions.isSelected()) {
				if (wordPartWholeRadioBtn.isSelected()) 
					currentSelectedOptions.append(jEdit.getProperty("search.currOpt.word")+" ");
				if (wordPartPrefixRadioBtn.isSelected()) 
					currentSelectedOptions.append(jEdit.getProperty("search.currOpt.prefix")+" ");
				if (wordPartSuffixRadioBtn.isSelected()) 
					currentSelectedOptions.append(jEdit.getProperty("search.currOpt.suffix")+" ");
				if (columnRadioBtn.isSelected())
					currentSelectedOptions.append(jEdit.getProperty("search.currOpt.column")+" ");
				if (searchFoldInsideRadioBtn.isSelected()) 
					currentSelectedOptions.append(jEdit.getProperty("search.currOpt.insideFold")+" ");
				if (searchFoldOutsideRadioBtn.isSelected()) 
					currentSelectedOptions.append(jEdit.getProperty("search.currOpt.outsideFold")+" ");
				if (searchCommentInsideRadioBtn.isSelected()) 
					currentSelectedOptions.append(jEdit.getProperty("search.currOpt.insideComment")+" ");
				if (searchCommentOutsideRadioBtn.isSelected()) 
					currentSelectedOptions.append(jEdit.getProperty("search.currOpt.outsideComment")+" ");
			}
		}

		// debug: display components
/*		Component[] globComps = globalFieldPanel.getComponents();
		if (XSearchAndReplace.debug) for(int i = 0; i < globComps.length; i++) {
			Log.log(Log.DEBUG, BeanShell.class,"tp1212: globComps["+i+"] = "+globComps[i]);
		} */
		if (currentSelectedOptions.length() > 0) {
			currentSelectedOptionsLabel.setText(
				jEdit.getProperty("search.currOpt.label")+" "+
				currentSelectedOptions.toString());
			// check if there are still the "replace" components
			if (globalFieldPanel.getComponentCount() > optionsLabelIndex) {
				if (globalFieldPanel.getComponent(optionsLabelIndex) != currentSelectedOptionsLabel) {
					if (XSearchAndReplace.debug) Log.log(Log.DEBUG, BeanShell.class,"tp1217: add Label at  "+optionsLabelIndex);
					globalFieldPanel.add(currentSelectedOptionsLabel,optionsLabelIndex);
				} else 
					if (XSearchAndReplace.debug) Log.log(Log.DEBUG, BeanShell.class,"tp1219: Label already exists at "+optionsLabelIndex);
			} else {
				globalFieldPanel.add(currentSelectedOptionsLabel);
			}
		} else 
			if (globalFieldPanel.getComponentCount() > optionsLabelIndex) {
				if (globalFieldPanel.getComponent(optionsLabelIndex) == currentSelectedOptionsLabel) {
					if (XSearchAndReplace.debug) Log.log(Log.DEBUG, BeanShell.class,"tp1224: remove Label at  "+optionsLabelIndex);
					globalFieldPanel.remove(optionsLabelIndex);
				} else 
					if (XSearchAndReplace.debug) Log.log(Log.DEBUG, BeanShell.class,"tp1227: Label does not exist at "+optionsLabelIndex);
			}
	} //}}}

	//}}}

	//{{{ Inner classes

	//{{{ MyJRadioButton class

	// used for the stringReplace and beanShell replace radio buttons,
	// so that the user can press tab to go from the find field to the
	// replace field in one go
	class MyJRadioButton extends JRadioButton
	{
		MyJRadioButton(String label)
		{
			super(label);
		}

		public boolean isFocusTraversable()
		{
			return false;
		}
	} //}}}

	//{{{ ReplaceActionHandler class
	class ReplaceActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			replace.setModel(beanShellReplace.isSelected()
				? "replace.script"
				: "replace");
			XSearchAndReplace.setBeanShellReplace(
				beanShellReplace.isSelected());
		}
	} //}}}

	//{{{ SelectivShowActionHandler class
	class SelectivShowActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			showHideOptions(evt.getSource());
			setupCurrentSelectedOptionsLabel();

			pack();
	//		show();
		}
	} //}}}

	//{{{ ExtendedOptionsActionHandler class
	class ExtendedOptionsActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			XSearchAndReplace.resetIgnoreFromTop();
			Object source = evt.getSource();
			if (source == columnRadioBtn) {
				enableColumnOptions(columnRadioBtn.isSelected());
			}
			else if (source == wordPartPrefixRadioBtn || source == wordPartSuffixRadioBtn 
				|| source == wordPartWholeRadioBtn) {
					if (((JRadioButton)source).isSelected()) {
						regexp.setSelected(false);
						regexp.setEnabled(false);
						searchBack.setEnabled(false);
						regexp.setEnabled(true);
					}
					updateEnabled();
			}
/*
			else if (source == searchFoldInsideRadioBtn || source == searchFoldOutsideRadioBtn) {
				// don't allow hypersearch ico fold searching
				if (((JRadioButton)source).isSelected()) {
					hyperSearch.setSelected(false);
					hyperSearch.setEnabled(false);
				} else {
					hyperSearch.setEnabled(true);
				}
			}
			*/
			/*
			else if (source == searchFoldInsideRadioBtn || source == searchFoldOutsideRadioBtn) {
				if (source == searchFoldActualRadioBtn) {
					// the already selected button is selected once more ==> select default button
					searchFoldDefaultRadioBtn.setSelected(true);
					searchFoldActualRadioBtn = searchFoldDefaultRadioBtn;
				} else {
					((JRadioButton)source).setSelected(true);
					searchFoldActualRadioBtn = (JRadioButton)source;
				}
			} else if (source == searchCommentInsideRadioBtn || source == searchCommentOutsideRadioBtn) {
				if (source == searchCommentActualRadioBtn) {
					// the already selected button is selected once more ==> select default button
					searchCommentDefaultRadioBtn.setSelected(true);
					searchCommentActualRadioBtn = searchCommentDefaultRadioBtn;
				} else {
					((JRadioButton)source).setSelected(true);
					searchCommentActualRadioBtn = (JRadioButton)source;
				}
			}
			*/
			pack();
			show();
		}
	} //}}}

	//{{{ SettingsActionHandler class
	class SettingsActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			XSearchAndReplace.resetIgnoreFromTop(); // rwchg: when settings change, no refind
			Object source = evt.getSource();

			if(source == ignoreCase)
				XSearchAndReplace.setIgnoreCase(ignoreCase.isSelected());
			else if(source == regexp)
				XSearchAndReplace.setRegexp(regexp.isSelected());
			else if(source == searchBack || source == searchForward || source == searchFromTop) {
				XSearchAndReplace.setReverseSearch(searchBack.isSelected());
				XSearchAndReplace.setSearchFromTop(searchFromTop.isSelected());
			}
			else if(source == wrap)
				XSearchAndReplace.setAutoWrapAround(wrap.isSelected());
			else if(source == searchCurrentBuffer)
				hyperSearch.setSelected(false);
			else if(source == searchSelection
				|| source == searchAllBuffers
				|| source == searchDirectory)
				hyperSearch.setSelected(true);

			updateEnabled();
		}
	} //}}}

	//{{{ MultiFileActionHandler class
	class MultiFileActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			if(evt.getSource() == choose)
			{
				/*
				String[] dirs = GUIUtilities.showVFSFileDialog(
					view,directory.getText(),
					VFSBrowser.CHOOSE_DIRECTORY_DIALOG,
					false);
				if(dirs != null)
					directory.setText(dirs[0]);
					*/
				File dir = new File(directory.getText());
				JFileChooser chooser = new JFileChooser(dir.getParent());
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setSelectedFile(dir);

				if(chooser.showOpenDialog(XSearchDialog.this)
					== JFileChooser.APPROVE_OPTION)
					directory.setText(chooser.getSelectedFile().getPath());
					
			}
			else // source is directory or filter field
			{
				// just as if Enter was pressed in another
				// text field
				ok();
			}
		}
	} //}}}

	//{{{ ButtonActionHandler class
	class ButtonActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();

			if(source == closeBtn)
				cancel();
			else if (evalExtendedOptions()) {
				if(source == findBtn || source == find
				|| source == replace)
				{
					ok();
				}
				else if(source == replaceAndFindBtn)
				{
					save(false);
					if(XSearchAndReplace.replace(view))
						ok();
					else
						getToolkit().beep();
				}
				else if(source == replaceAllBtn)
				{
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					save(false);

					if(searchSelection.isSelected())
					{
						if(XSearchAndReplace.replace(view))
							closeOrKeepDialog();
						else
							getToolkit().beep();
					}
					else
					{
						if(XSearchAndReplace.replaceAll(view))
							closeOrKeepDialog();
						else
							getToolkit().beep();
					}

					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		}
	} //}}}

	//}}}
}
