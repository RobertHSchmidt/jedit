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
package projectviewer.vpt;

//{{{ Imports
import java.io.File;
import java.awt.Color;
import javax.swing.Icon;

import org.gjt.sp.jedit.GUIUtilities;
//}}}

/**
 *	Models a directory that is part of a project.
 *
 *	@author		Marcelo Vanzin
 *	@version	$Id$
 */
public class VPTDirectory extends VPTNode {

	//{{{ Constants

	private final static Icon dirClosedIcon 	= GUIUtilities.loadIcon("Folder.png");
	private final static Icon dirOpenedIcon 	= GUIUtilities.loadIcon("OpenFolder.png");

	//}}}
	
	//{{{ Attributes
	
	protected File		file;
	
	//}}}
	
	//{{{ Constructors 
	
	public VPTDirectory(String path) {
		this(new File(path));
	}
	
	public VPTDirectory(File file) {
		super(VPTNode.DIRECTORY, file.getName());
		this.file = file;
	}
	
	//}}}
	
	//{{{ Public methods
	
	//{{{ canWrite() method
	/** Returns is the underlying file is writable. */
	public boolean canWrite() {
		return file.canWrite();
	} //}}}

	//{{{ delete() method	
	/**
	 *	Deletes the file from disk and removes it from the current container.
	 */
	public boolean delete() {
		// deletes directory recursively?
		return false;
	} //}}}
	
	//{{{ getFile() method
	/** Return the file associated with this node. */
	public File getFile() {
		return file;
	} //}}}
	
	//{{{ getIcon(boolean) method
	/**
	 *	Returns the icon to be shown on the tree next to the node name.
	 *
	 *	@param	expanded	If the node is currently expanded or not.
	 */
	public Icon getIcon(boolean expanded) {
		return (expanded ? dirOpenedIcon : dirClosedIcon);
	} //}}}
	
	//{{{ toString() method
	/** Returns a string representation of the current node. */
	public String toString() {
		return"Directory [" + getFile().getAbsolutePath() + "]";
	} //}}}
	
	//{{{ getNodePath()
	/**	Returns the path to the file represented by this node. */
	public String getNodePath() {
		return getFile().getAbsolutePath();
	} //}}}

	//{{{ setFile(File) method
	/** Sets the file associated with this node. */
	public void setFile(File f) {
		this.file = f;
		setName(f.getName());
	} //}}}

	//}}}
	
}
