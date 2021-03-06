/*
 * ErrorsOptionPane.java - Error pattern option pane
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2003, 2005 Slava Pestov, Alan Ezust
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

package console.options;

//{{{ Imports
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.OptionPane;
import org.gjt.sp.jedit.jEdit;

import console.ConsolePlugin;
import console.ErrorMatcher;
import console.gui.PanelStack;
//}}}

//{{{ ErrorsOptionPane class
/**
 * Shows a list of the current ErrorMatchers which can be used, and permits the easy
 * editing of them.
 */
public class ErrorsOptionPane extends AbstractOptionPane
{
	//{{{ Instance variables
	
	// Model for storing all of the ErrorMatchers
	private DefaultListModel errorListModel;

	// View for the list of errors
	private JList errorList;

	PanelStack panelStack;
	
	private JButton add;
	private JButton remove;
	//}}}

	
	// {{{ Public members
	
	//{{{ ErrorsOptionPane constructor
	public ErrorsOptionPane()
	{
		super("console.errors");
	} //}}}
	
	// }}}
	
	//{{{ Protected members

	//{{{ _init() method
	protected void _init()
	{

		setLayout(new BorderLayout());
		addComponent(Box.createVerticalStrut(6));

		errorListModel = createMatcherListModel();
		errorList = new JList(errorListModel);
		JScrollPane jsp =new JScrollPane(errorList); 
		jsp.setMinimumSize(new Dimension(125, 300));
		errorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		errorList.addListSelectionListener(new ListHandler());
		errorList.addMouseListener(new MouseHandler());
		errorList.setVisibleRowCount(5);

		// JSplitPane errors = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		// errors.add(jsp);
		String title = jEdit.getProperty("options.console.errors.caption");
		jsp.setBorder(new TitledBorder(title));
		
		Box westBox = new Box(BoxLayout.Y_AXIS);
		westBox.add(jsp);
		// add(jsp, BorderLayout.WEST);

		
		panelStack = new PanelStack();
//		errors.add(panelStack);
		//add(errors, BorderLayout.CENTER);
		add(panelStack, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel();
		buttons.setBorder(new EmptyBorder(6,0,0,0));
		buttons.setLayout(new BoxLayout(buttons,BoxLayout.X_AXIS));

		buttons.add(Box.createGlue());

		buttons.add(add = new JButton(jEdit.getProperty(
			"options.console.errors.add")));
		add.addActionListener(new ActionHandler());
		buttons.add(Box.createHorizontalStrut(6));

		buttons.add(Box.createHorizontalStrut(6));

		buttons.add(remove = new JButton(jEdit.getProperty(
			"options.console.errors.remove")));
		remove.addActionListener(new ActionHandler());
		buttons.add(Box.createHorizontalStrut(6));

		buttons.add(Box.createGlue());
		westBox.add(buttons);
//		add(buttons, BorderLayout.SOUTH);
		add(westBox, BorderLayout.WEST);
		errorList.setSelectedIndex(1);
		updateButtons();


	} //}}}

	//{{{ _save() method
	protected void _save()
	{
		
		StringBuffer list = new StringBuffer();
		for(int i = 0; i < errorListModel.getSize(); i++)
		{
			ErrorMatcher matcher = (ErrorMatcher)errorListModel.getElementAt(i);
			JPanel panel = panelStack.get(matcher.internalName());
			if (panel != null) {
				OptionPane pane = (OptionPane) panel;
				pane.save();
			}
			

			if(matcher.user)
			{
				if(i != 0)
					list.append(' ');
				list.append(matcher.internalName());
			}
		}

		jEdit.setProperty("console.error.user",list.toString());
	} //}}}

	//}}}
	
	//{{{ Private members

	//{{{ createMatcherListModel() method
	private DefaultListModel createMatcherListModel()
	{
		
		DefaultListModel listModel = new DefaultListModel();

		ErrorMatcher[] matchers = ConsolePlugin.loadErrorMatchers();
		for(int i = 0; i < matchers.length; i++)
		{
		//	listModel.addElement(matchers[i].clone());
			String internalName = matchers[i].internalName();
			listModel.addElement(matchers[i]);
		}

		return listModel;
	} //}}}
	
	//{{{ updateButtons() method
	
	private void updateButtons()
	{
		int index = errorList.getSelectedIndex();
		
		if(index == -1)
		{
			index = 1;
		}
		ErrorMatcher matcher = (ErrorMatcher)errorList.getSelectedValue();
		String internalName = matcher.internalName();
		if (!panelStack.raise(internalName)) {
			ErrorMatcherPanel panel = new ErrorMatcherPanel(internalName, matcher);
			
			panelStack.add(internalName, panel);
			panelStack.raise(internalName);
			validateTree();
		}
		remove.setEnabled(matcher.user);
	} //}}}

	//}}}

	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		

		
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			
			if(source == add)
			{
				/* Open a dialog and ask for the name: */
				String matcherNamePrompt = jEdit.getProperty("options.console.errors.name");
				String matcherName = JOptionPane.showInputDialog(matcherNamePrompt);
				ErrorMatcher matcher = new ErrorMatcher();
				matcher.name = matcherName;
				matcher.user = true;
				int index = errorList.getSelectedIndex() + 1;
				errorListModel.insertElementAt(matcher,index);
				errorList.setSelectedIndex(index);
			}
			else if(source == remove)
			{
				errorListModel.removeElementAt(errorList.getSelectedIndex());
				updateButtons();
			}
			/* else if(source == up)
			{
				int index = errorList.getSelectedIndex();
				Object selected = errorList.getSelectedValue();
				errorListModel.removeElementAt(index);
				errorListModel.insertElementAt(selected,index-1);
				errorList.setSelectedIndex(index-1);
			}
			else if(source == down)
			{
				int index = errorList.getSelectedIndex();
				Object selected = errorList.getSelectedValue();
				errorListModel.removeElementAt(index);
				errorListModel.insertElementAt(selected,index+1);
				errorList.setSelectedIndex(index+1);
			} */
		}
	} //}}}

	//{{{ ListHandler class
	class ListHandler implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent evt)
		{
			updateButtons();
		}
	} //}}}

	//{{{ MouseHandler class
	class MouseHandler extends MouseAdapter
	{
		public void mouseClicked(MouseEvent evt)
		{
			if(evt.getClickCount() == 2)
			{
				updateButtons();
			}
		}
	} //}}}
} //}}}

