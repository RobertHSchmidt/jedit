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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import org.gjt.sp.jedit.OptionPane;
import org.gjt.sp.jedit.jEdit;
import ise.java.awt.KappaLayout;

/**
 * Plugin option pane.
 */
public class SubversionOptions implements OptionPane {
    private JPanel panel = null;
    private JCheckBox useTsvnTemplate = null;
    private JSpinner maxLogs = null;

    public SubversionOptions( ) {}

    public void init() {
        if ( panel != null )
            return ;
        panel = new JPanel( new KappaLayout() );
        panel.setName("SubversionOptions");


        useTsvnTemplate = new JCheckBox( "Use tsvn:logtemplate property for commit template" );
        useTsvnTemplate.setName("useTsvnTemplate");
        useTsvnTemplate.setSelected( jEdit.getBooleanProperty( "ise.plugin.svn.useTsvnTemplate", false ) );

        maxLogs = new JSpinner();
        maxLogs.setName("maxLogs");
        ( ( JSpinner.NumberEditor ) maxLogs.getEditor() ).getModel().setMinimum( Integer.valueOf( 1 ) );
        int logRows = jEdit.getIntegerProperty("ise.plugin.svn.logRows", 1000); 
        ( ( JSpinner.NumberEditor ) maxLogs.getEditor() ).getModel().setValue( logRows );
        ((JSpinner.NumberEditor)maxLogs.getEditor()).getTextField().setForeground( jEdit.getColorProperty( "view.fgColor", Color.BLACK ) );
        ((JSpinner.NumberEditor)maxLogs.getEditor()).getTextField().setBackground( jEdit.getColorProperty( "view.bgColor", Color.WHITE ) );

        panel.add( useTsvnTemplate, "0, 0, 2, 1, 0, wh, 5" );
        
        JPanel max_logs_panel = new JPanel( new FlowLayout() );
        max_logs_panel.add( new JLabel( jEdit.getProperty( "ips.Maximum_log_entries_to_show>", "Maximum log entries to show:" ) ) );
        max_logs_panel.add( maxLogs );
        panel.add( "0, 1, 2, 1, W,  , 5", max_logs_panel );

    }

    public void save() {
        if ( useTsvnTemplate != null ) {
            jEdit.setBooleanProperty( "ise.plugin.svn.useTsvnTemplate", useTsvnTemplate.isSelected() );
        }
        if ( maxLogs != null ) {
            jEdit.setIntegerProperty( "ise.plugin.svn.logRows", ( ( Integer ) maxLogs.getValue() ).intValue());
        }
    }

    public Component getComponent() {
        if ( panel == null )
            init();
        return panel;
    }

    public String getName() {
        return "subversion";
    }

}