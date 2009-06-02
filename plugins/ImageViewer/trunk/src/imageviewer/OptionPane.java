/*
Copyright (c) 2009, Dale Anson
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.
* Neither the name of the author nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package imageviewer;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;


public class OptionPane extends AbstractOptionPane {

    private JCheckBox vfsMouseOver = null;
    private JCheckBox pvMouseOver = null;

    public OptionPane() {
        this("");
    }

    public OptionPane( String name ) {
        super( name );
    }

    public void _init() {
        setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );
        addComponent( new JLabel( "<html><h3>Image Viewer</h3>" ) );
        vfsMouseOver = new JCheckBox( jEdit.getProperty( "imageviewer.allowVFSMouseOver.label", "Show images on mouse over in File System Browser" ) );
        vfsMouseOver.setSelected( jEdit.getBooleanProperty( "imageviewer.allowVFSMouseOver", true ) );
        pvMouseOver = new JCheckBox( jEdit.getProperty( "imageviewer.allowPVMouseOver.label", "Show images on mouse over in ProjectViewer" ) );
        pvMouseOver.setSelected( jEdit.getBooleanProperty( "imageviewer.allowPVMouseOver", true ) );
        addComponent( vfsMouseOver );
        addComponent( pvMouseOver );
    }

    public void _save() {
        jEdit.setBooleanProperty( "imageviewer.allowVFSMouseOver", vfsMouseOver.isSelected() );
        jEdit.setBooleanProperty( "imageviewer.allowPVMouseOver", pvMouseOver.isSelected() );
    }
}