/*
 * SessionListChanged.java - EditBus message
 * Copyright (c) 2001 Dirk Moebius
 *
 * :tabSize=4:indentSize=4:noTabs=false:maxLineLen=0:
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


package sessions;


import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;


public final class SessionListChanged extends EBMessage
{

	SessionListChanged(EBComponent source)
	{
		super(source);
	}


	public final SessionManager getSessionManager()
	{
		return (SessionManager) getSource();
	}

}

