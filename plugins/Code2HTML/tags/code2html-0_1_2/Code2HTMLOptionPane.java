/*
 * Code2HTMLOptionPane.java
 * Copyright (c) 2000 Andre Kaplan
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

import javax.swing.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;

public class Code2HTMLOptionPane 
	extends AbstractOptionPane
{
	private JCheckBox ckUseCSS;
	private JCheckBox ckShowGutter;
	
	public Code2HTMLOptionPane() {
		super(jEdit.getProperty("code2html.label", "Code2HTML"));
	}

	public void _init() {
		this.ckUseCSS = new JCheckBox(
			jEdit.getProperty("options.code2html.use-css"),
			jEdit.getBooleanProperty("code2html.use-css", false)
		);
		addComponent(this.ckUseCSS);

		this.ckShowGutter = new JCheckBox(
			jEdit.getProperty("options.code2html.show-gutter"),
			jEdit.getBooleanProperty("code2html.show-gutter", false)
		);
		addComponent(this.ckShowGutter);
	}
	
	public void _save() {
		jEdit.setBooleanProperty("code2html.use-css", 
			this.ckUseCSS.isSelected());
		
		jEdit.setBooleanProperty("code2html.show-gutter", 
			this.ckShowGutter.isSelected());		
	}
}
