/*
 * TaskType.java - TaskList plugin
 * Copyright (C) 2001 Oliver Rutherfurd
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


import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.gjt.sp.jedit.GUIUtilities;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.util.Log;

import gnu.regexp.*;


public class TaskType
{
	public static final RESyntax RE_SYNTAX = new RESyntax(
		RESyntax.RE_SYNTAX_PERL5).set(
			RESyntax.RE_CHAR_CLASSES);

	private static final String DEFAULT_PATTERN = 
		"(?:^|\\s+)(MARKER):(?:\\s+)(.+)$";
	private static final boolean DEFAULT_MATCH_CASE = false;
	private static final String DEFAULT_SAMPLE = "MARKER: blah, blah...";

	public TaskType()
	{
		// TODO: remove these values
		this.pattern = DEFAULT_PATTERN;
		this.ignoreCase = DEFAULT_MATCH_CASE;
		this.sample = DEFAULT_SAMPLE;
		setIconPath("Exclamation.gif");
	}

	public TaskType(String pattern, String sample, 
		boolean ignoreCase, String iconPath)
	{
		this.pattern = pattern;
		this.sample = sample;
		this.ignoreCase = ignoreCase;
		setIconPath(iconPath);	// will attempt to load icon too

		compileRE();
	}

	public Task extractTask(Buffer buffer, String tokenText, 
		int line, int tokenOffset)
	{
		REMatch match = this.re.getMatch(tokenText);

		if(match == null)
			return null;

		/* NOTE: removed because gnu.regexp.REMatch doesn't have this anymore...
		if(match.getSubCount() != 3)
		{
			Log.log(Log.WARNING, TaskType.class,
				"Expected 3 sub-matches for '" + 
				this.pattern + "', got " + match.getSubCount());//##

			return null;
		}
		*/

		int start = (displayIdentifier == true ? match.getStartIndex(1) : 
			match.getStartIndex(2));
		int end = match.getEndIndex(2);

		return new Task(buffer, 
			icon, 
			line, 
			tokenText.substring(start, end), 
			tokenOffset + start, 
			tokenOffset + end);
	}

	public String getPattern(){ return this.pattern; }
	public void setPattern(String pattern)
	{
		if(!this.pattern.equals(pattern))
		{
			this.pattern = pattern;
			compileRE();
		}
	}

	public String getSample(){ return this.sample; }
	public void setSample(String sample)
	{
		this.sample = sample;
	}

	public boolean getIgnoreCase(){ return this.ignoreCase; }
	public void setIgnoreCase(boolean ignoreCase)
	{
		if(this.ignoreCase != ignoreCase)
		{
			this.ignoreCase = ignoreCase;
			this.reFlags = (ignoreCase ? RE.REG_ICASE : 0);
			compileRE();
		}
	}

	public Icon getIcon(){ return this.icon; }

	public String getIconPath(){ return this.iconPath; }
	public void setIconPath(String iconPath)
	{
		if(this.iconPath != iconPath || this.icon == null)
		{
			this.iconPath = iconPath;
			Icon _icon = TaskType.loadIcon(iconPath);
			// QUESTION: do this?
			if(_icon != null)
				this.icon = _icon;
		}
	}

	public int getREFlags()
	{
		return reFlags;
	}


	private void compileRE()
	{
		this.re = null;

		try
		{
			this.re = new RE(this.pattern, this.getREFlags(),
				TaskType.RE_SYNTAX);
		}
		catch(REException e)
		{
			Log.log(Log.ERROR, TaskType.class,
				"Failed to compile task pattern: " + pattern +
					e.toString());
		}
	}


	public void save(int i)
	{
		jEdit.setProperty("tasklist.tasktype." + i + ".pattern", pattern);
		jEdit.setProperty("tasklist.tasktype." + i + ".sample", sample);
		jEdit.setBooleanProperty("tasklist.tasktype." + i + ".ignorecase", ignoreCase);
		jEdit.setProperty("tasklist.tasktype." + i + ".iconpath", iconPath);
	}

	public String toString()
	{
		return this.pattern;
	}

	/*
	public static void load(int i)
	{

	}
	*/

	private RE re;
	private int reFlags;
	private String pattern;
	private String sample;
	private boolean ignoreCase;
	private String iconPath;
	private Icon icon;

	private boolean displayIdentifier = true;

	private static Hashtable icons;


	/**
	*
	*/
	public static Icon loadIcon(String iconName)
	{
		//Log.log(Log.DEBUG, TaskType.class, 
		//	"TaskType.loadIcon(" + iconName + ")");//##

		// check if there is a cached version first
		Icon icon = (Icon)icons.get(iconName);
		if(icon != null)
			return icon;

		// load the icon
		if(iconName.startsWith("file:"))
		{
			icon = new ImageIcon(iconName.substring(5));
		}
		else
		{
			URL url = TaskListPlugin.class.getResource("/icons/" + iconName);
			if(url == null)
			{
				Log.log(Log.ERROR, TaskType.class,
					"TaskType.loadIcon() - icon not found: " + iconName);
				return null;
			}

			icon = new ImageIcon(url);
		}

		icons.put(iconName, icon);
		return icon;
	}

	static
	{
		icons = new Hashtable();
		StringTokenizer st = new StringTokenizer(jEdit.getProperty("tasklist.icons"));
		while(st.hasMoreElements())
		{
			String icon = st.nextToken();
			loadIcon(icon);
		}
	}

}


