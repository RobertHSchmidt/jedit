/*
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
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
package projectviewer.config;

//{{{ Imports
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JOptionPane;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.OptionPane;
import org.gjt.sp.jedit.OptionGroup;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.gui.OptionsDialog;
import org.gjt.sp.util.Log;

import projectviewer.vpt.VPTProject;
//}}}

/**
 *  A dialog for configuring the properties of a project. It works like jEdit's
 *	OptionsDialog (from which this class extends) to provide ways for other
 *	plugins to add option panes to it.
 *
 *  @author		Marcelo Vanzin
 *	@version	$Id$
 */
public class ProjectOptions extends OptionsDialog {

	//{{{ Static Members
	private static String			lookupPath;
	private static VPTProject		p;
	private static boolean			isNew;

	/**
	 *	Shows the project options dialog for the given project.
	 *
	 *	@param	project	The project to edit or null to create a new one.
	 *	@return	The new or modified project, or null if p was null and
	 *			dialog was cancelled.
	 */
	public static VPTProject run(VPTProject project) {
		return run(project, null);
	}

	/**
	 *	Shows the project options dialog for the given project, with an
	 *	optional default start folder where to open the file chooser
	 *	dialog.
	 *
	 *	<p>Method is sychronized so that the use of the static variables
	 *	is safe.</p>
	 *
	 *	@param	project	The project to edit or null to create a new one.
	 *	@param	startPath	Where to open the "choose root" file dialog.
	 *	@return	The new or modified project, or null if p was null and
	 *			dialog was cancelled.
	 */
	public static synchronized VPTProject run(VPTProject project, String startPath) {
		String title;
		if (project == null) {
			title = "projectviewer.create_project";
		} else {
			title = "projectviewer.edit_project";
		}

		if (project == null) {
			p = new VPTProject("");
			p.setRootPath("");
			isNew = true;
		} else {
			p = project;
			isNew = false;
		}

		lookupPath = startPath;
		new ProjectOptions(jEdit.getActiveView(), title);
		return p;
	}

	//}}}

	//{{{ Instance Variables

	private OptionGroup				rootGroup;
	private ProjectPropertiesPane	pOptPane;

	//}}}

	//{{{ Constructor

	private ProjectOptions(View view, String name) {
		super(JOptionPane.getFrameForComponent(view), name, null);
		setModal(true);
	}

	//}}}

	//{{{ cancel() method
	/**
	 *	Called when the cancel button is pressed. Sets the project to null
	 *	if "isNew" is true.
	 */
	public void cancel() {
		p = null;
		dispose();
	} //}}}

	//{{{ ok() method
	/**
	 *	Called when ok is pressed. Verifies if the project's properties are OK
	 *	before closing the dialog.
	 */
	public void ok() {
		save(rootGroup);
		if (pOptPane.isOK()) {
			dispose();
		}
	} //}}}

	//{{{ getDefaultGroup() method
	protected OptionGroup getDefaultGroup() {
		return rootGroup;
	} //}}}

	//{{{ createOptionTreeModel() method
	protected OptionTreeModel createOptionTreeModel() {
		OptionTreeModel paneTreeModel = new OptionTreeModel();
		rootGroup = (OptionGroup) paneTreeModel.getRoot();

		pOptPane = new ProjectPropertiesPane(p, isNew, lookupPath);
		addOptionPane(pOptPane);

		EditPlugin[] eplugins = jEdit.getPlugins();
		boolean added;
		for (int i = 0; i < eplugins.length; i++) {
			createOptions(eplugins[i]);
		}

		return paneTreeModel;
	} //}}}

	//{{{ createOptions(EditPlugin)
	/**
	 *	For jEdit 4.2: creates options panes based on properties set by the
	 *	plugin, so manual registration of the plugin is not necessary. More
	 *	details in the package description documentation.
	 *
	 *	@return	true if an option pane or an option group was added, false
	 *			otherwise.
	 */
	protected boolean createOptions(EditPlugin plugin) {
		// Look for a single option pane
		String property = "plugin.projectviewer." + plugin.getClassName() + ".option-pane";
		if ((property = jEdit.getProperty(property)) != null) {
			rootGroup.addOptionPane(property);
			return true;
		}

		// Look for an option group
		property = "plugin.projectviewer." +
					plugin.getClassName() + ".option-group";
		if ((property = jEdit.getProperty(property)) != null) {
			rootGroup.addOptionGroup(
				new OptionGroup("plugin." + plugin.getClassName(),
					jEdit.getProperty("plugin."
										+ plugin.getClassName() + ".name"),
					property)
				);
			return true;
		}

		// nothing found
		return false;
	} //}}}

	//{{{ save(Object)
	/** Saves the information from the option panes. */
	private void save(Object o) {
		if (o instanceof OptionGroup) {
			Enumeration en = ((OptionGroup)o).getMembers();
			while (en.hasMoreElements()) {
				Object m = en.nextElement();
				if (m instanceof OptionGroup) {
					save(m);
				} else if (m instanceof OptionPane) {
					try {
						((OptionPane)m).save();
					} catch (Exception e) {
						Log.log(Log.ERROR, m, e);
					}
				}
			}
		} else if (o instanceof OptionPane) {
			try {
				((OptionPane)o).save();
			} catch (Exception e) {
				Log.log(Log.ERROR, o, e);
			}
		}
	} //}}}

}

