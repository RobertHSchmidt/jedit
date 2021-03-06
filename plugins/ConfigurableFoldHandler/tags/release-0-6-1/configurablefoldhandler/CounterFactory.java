package configurablefoldhandler;

/*
 * CounterFactory.java
 * 
 * Copyright (c) 2002 C.J.Kent
 *
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=custom:collapseFolds=0:
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

import org.gjt.sp.util.Log;

import java.lang.reflect.InvocationTargetException;

/**
 * This class creates and returns a new implementation of {@link RegexCounter}.
 * The implementing class depends on the version of Java that is being used.
 * Under Java 1.3 a {@link GNURegexCounter} is returned that uses the
 * <code>gnu.regexp</code> package for matching. Under Java 1.4 a
 * {@link JavaRegexCounter} is returned that uses the
 * <code>java.util.regex</code> classes for matching (and therefore supports a
 * wider range of regex syntax).
 */
public class CounterFactory
{
	private Class counterClass;
	
	public CounterFactory()
	{
		String counterClassName;
		
		try
		{
			Class.forName("java.util.regex.Pattern");
			counterClassName = "configurablefoldhandler.JavaRegexCounter";
		}
		catch(ClassNotFoundException ex)
		{
			counterClassName = "configurablefoldhandler.GNURegexCounter";
		}
		
		try
		{
			counterClass = Class.forName(counterClassName);
		}
		catch(ClassNotFoundException ex)
		{
			// should never happen
			Log.log(Log.ERROR, this, ex.getMessage());
		}
	}
	
	/**
	 * Returns a new {@link FoldCounter} instance.
	 */
	public FoldCounter getFoldCounter(String startStr, String endStr,
		boolean useRegex) throws FoldStringsException
	{
		if (useRegex)
		{
			try
			{
				return (FoldCounter) counterClass.getConstructors()[0]
					.newInstance(new Object[] { startStr, endStr });
			}
			catch(InvocationTargetException e)
			{
				throw new FoldStringsException(e.getTargetException());
			}
			catch(Exception e)
			{
				throw new FoldStringsException(e);
			}
		}
		else
		{
			return new PlainCounter(startStr, endStr);
		}
	}
}
