/*
Copyright (c) 2007, Dale Anson
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

package ise.plugin.svn.gui;

// imports
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.browser.VFSBrowser;

import projectviewer.ProjectViewer;
import projectviewer.config.ProjectOptions;
import ise.java.awt.KappaLayout;
import ise.plugin.svn.data.SVNData;
import ise.plugin.svn.library.PasswordHandler;
import ise.plugin.svn.library.PasswordHandlerException;


/**
 * Dialog for undeleting files and/or directories
 */
public class UndeleteDialog extends JDialog {
    // instance fields
    private View view = null;
    private List<String> paths = null;

    private boolean cancelled = false;

    private SVNData undeleteData = null;

    public UndeleteDialog( View view, List<String> paths ) {
        super( ( JFrame ) view, "Undelete", true );
        if ( paths == null ) {
            throw new IllegalArgumentException( "paths may not be null" );
        }
        this.view = view;
        this.paths = paths;
        _init();
    }

    /** Initialises the option pane. */
    protected void _init() {

        undeleteData = new SVNData();

        JPanel panel = new JPanel( new KappaLayout() );
        panel.setBorder( new EmptyBorder( 6, 6, 6, 6 ) );

        JLabel file_label = new JLabel("Undelete these files:");
        final JPanel file_panel = new JPanel(new GridLayout(0, 1, 2, 3));
        file_panel.setBackground(Color.WHITE);
        file_panel.setBorder(new EmptyBorder(3, 3, 3, 3));
        for(String path : paths) {
            JCheckBox cb = new JCheckBox(path);
            cb.setSelected(true);
            cb.setBackground(Color.WHITE);
            file_panel.add(cb);
        }

        // buttons
        KappaLayout kl = new KappaLayout();
        JPanel btn_panel = new JPanel( kl );
        JButton ok_btn = new JButton( "Ok" );
        JButton cancel_btn = new JButton( "Cancel" );
        btn_panel.add( "0, 0, 1, 1, 0, w, 3", ok_btn );
        btn_panel.add( "1, 0, 1, 1, 0, w, 3", cancel_btn );
        kl.makeColumnsSameWidth( 0, 1 );

        ok_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        // get the paths
                        List<String> paths = new ArrayList<String>();
                        Component[] files = file_panel.getComponents();
                        for (Component file : files) {
                            JCheckBox cb = (JCheckBox)file;
                            if (cb.isSelected()) {
                                paths.add(cb.getText());
                            }
                        }
                        if (paths.size() == 0) {
                            // nothing to undelete, bail out
                            undeleteData = null;
                        }
                        else {
                            undeleteData.setPaths(paths);
                        }
                        UndeleteDialog.this.setVisible( false );
                        UndeleteDialog.this.dispose();
                    }
                }
                                );

        cancel_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        undeleteData = null;
                        UndeleteDialog.this.setVisible( false );
                        UndeleteDialog.this.dispose();
                    }
                }
                                    );

        // add the components to the option panel
        panel.add( "0, 0, 1, 1, W,  , 3", file_label );
        panel.add( "0, 1, 1, 1, W, wh, 3", new JScrollPane( file_panel ) );
        panel.add( "1, 1, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 120, true));
        panel.add( "0, 2, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 11, true ) );
        panel.add( "0, 3, 1, 1, E,  , 0", btn_panel );

        setContentPane( panel );
        pack();

    }

    public SVNData getData() {
        return undeleteData;
    }
}
