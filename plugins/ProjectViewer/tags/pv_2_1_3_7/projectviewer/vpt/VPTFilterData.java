/*
* :tabSize=4:indentSize=4:noTabs=false:
* :folding=explicit:collapseFolds=1:
*
*  This program is free software; you can redistribute it and/or
*  modify it under the terms of the GNU General Public License
*  as published by the Free Software Foundation; either version 2
*  of the License, or any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more detaProjectTreeSelectionListenerils.
*
*  You should have received a copy of the GNU General Public License
*  along with this program; if not, write to the Free Software
*  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package projectviewer.vpt;

import java.io.Serializable;
import java.util.regex.Pattern;
import org.gjt.sp.jedit.MiscUtilities;

/*
 *	@author		Rudolf Widmann
 *	@version	$Id$
 *	@since		PV 2.2.0.0
 */
public class VPTFilterData implements Serializable {

	private String name;
	private String glob;
	private Pattern pattern;

	public VPTFilterData(String name, String glob)
	{
		this.name = name;
		this.glob = glob;
		this.pattern = Pattern.compile(MiscUtilities.globToRE(glob));
	}

	public Pattern getPattern()
	{
		return pattern;
	}

	public String getGlob()
	{
		return glob;
	}

	public String getName()
	{
		return name;
	}

}

