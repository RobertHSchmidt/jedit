/*
 * home.java - open homepage action for InfoViewer
 * Copyright (C) 1999 Dirk Moebius
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

package infoviewer.actions;

import java.awt.*;
import java.awt.event.*;
import infoviewer.*;
import org.gjt.sp.jedit.jEdit;


public class home extends InfoViewerAction {
    
    public home() {
        super("infoviewer.home");
    }
    
    public void actionPerformed(ActionEvent evt) {
        getViewer(evt).gotoURL(jEdit.getProperty("infoviewer.homepage"), true);
    }
}

