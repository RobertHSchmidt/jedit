/*
 * JCompilerPlugin.java - JCompiler plugin
 * Copyright (c) 1999, 2000 Kevin A. Burton and Aziz Sharif
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

import java.util.Vector;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.msg.*;
import jcompiler.*;
import jcompiler.actions.*;


public class JCompilerPlugin extends EBPlugin {
    public static final String NAME = "JCompiler";
    
    public void start() {
        shell = new JCompilerShell();
        EditBus.addToNamedList(Shell.SHELLS_LIST, NAME);
        jEdit.addAction(new jcompile_compile());
        jEdit.addAction(new jcompile_compilepkg());
        jEdit.addAction(new jcompile_rebuildpkg());
    }
    
    public void createMenuItems(Vector menuItems) {
        menuItems.addElement(GUIUtilities.loadMenu("jcompiler-menu"));
    }

    public void createOptionPanes(OptionsDialog od) {
        od.addOptionPane(new JCompilerPane());
    }
  
    public void handleMessage(EBMessage message) {
        if (message instanceof CreateShell) {
            CreateShell cmsg = (CreateShell) message;
            if (NAME.equals(cmsg.getShellName())) {
                cmsg.setShell(shell);
            }
        }
    }
    
    public static JCompilerShell getShell() {
        return shell;
    }
    
    // private members
    private static JCompilerShell shell;  
}
