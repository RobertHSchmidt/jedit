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

import ise.plugin.svn.library.*;
import ise.plugin.svn.data.PropertyData;
import ise.java.awt.KappaLayout;
import ise.java.awt.LambdaLayout;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.gjt.sp.jedit.View;


/**
 * A panel to display SVN properties.
 *
 * @author    Dale Anson
 */
public class PropertyPanel extends JPanel {

    private View view = null;
    private JButton new_btn = null;

    public PropertyPanel( View view, String filename, Properties props ) {
        this.view = view;
        HashMap<String, Properties> results = new HashMap<String, Properties>();
        results.put( filename, props );
        init( results );
    }

    public PropertyPanel( View view, Map<String, Properties> results ) {
        this.view = view;
        init( results );
    }

    private void init( Map<String, Properties> results ) {
        if ( results == null || results.size() == 0 ) {
            results = new HashMap<String, Properties>();
            Properties p = new Properties();
            p.setProperty( "Error", "No properties available." );
            results.put( "Error", p );
        }

        JPanel properties_panel = new JPanel( new LambdaLayout() );
        Set < Map.Entry < String, Properties >> result_set = results.entrySet();
        int row = 0;
        for ( Map.Entry<String, Properties> result : result_set ) {
            // fetch the properties and load them into a table
            String filename = result.getKey();
            Properties props = result.getValue();
            final JTable props_table = new JTable( );
            final JButton add_btn = new JButton( "Add" );
            final JButton edit_btn = new JButton( "Edit" );
            final JButton delete_btn = new JButton( "Delete" );
            final DefaultTableModel model = new DefaultTableModel(
                        new String[] {
                            "Name", "Value"
                        }, props.size() );
            props_table.setModel( model );
            props_table.setRowSelectionAllowed( true );
            props_table.setColumnSelectionAllowed( false );
            DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
            selectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
            props_table.setSelectionModel( selectionModel );
            selectionModel.addListSelectionListener( new ListSelectionListener() {
                        public void valueChanged( ListSelectionEvent lse ) {
                            edit_btn.setEnabled( props_table.getSelectedRow() > -1 );
                            delete_btn.setEnabled( props_table.getSelectedRow() > -1 );
                        }
                    }
                                                   );

            // crud. Properties should genericize to <String, String>, or at
            // least <String, Object>.
            Set < Map.Entry < Object, Object >> entrySet = props.entrySet();
            int i = 0;
            for ( Map.Entry<Object, Object> me : entrySet ) {
                Object key = me.getKey();
                Object value = me.getValue();
                if ( key == null || value == null ) {
                    continue;
                }
                model.setValueAt( key.toString(), i, 0 );
                model.setValueAt( value.toString(), i, 1 );
                ++i;
            }

            // add a mouse listener for the popup
            props_table.addMouseListener( new TableCellViewer( props_table ) );

            // create and add a panel with this result
            JPanel panel = new JPanel( new LambdaLayout() );
            panel.setBorder( new CompoundBorder( new EtchedBorder(), new EmptyBorder( 3, 3, 3, 3 ) ) );
            JLabel filename_label = new JLabel( "Properties for: " + filename, JLabel.LEFT );
            panel.add( filename_label, "0, 0, 1, 1, W, w, 3" );
            panel.add( GUIUtils.createTablePanel( props_table ), "0, 1, 1, 1, 0, wh, 3" );

            // set up the buttons
            KappaLayout kl = new KappaLayout();
            JPanel btn_panel = new JPanel( kl );
            add_btn.setEnabled( true );
            edit_btn.setEnabled( false );
            delete_btn.setEnabled( false );

            // button action listeners
            add_btn.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        PropertyEditor dialog = new PropertyEditor( view, null, null, true );
                        GUIUtils.center(view, dialog);
                        dialog.setVisible( true );
                        PropertyData data = dialog.getPropertyData();
                        if ( data == null ) {
                            return ;     // user cancelled
                        }
                        model.addRow( new String[] {data.getName(), data.getValue() } );

                    }
                }
            );
            edit_btn.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        int row = props_table.getSelectedRow();
                        if ( row > -1 ) {
                            String key = ( String ) model.getValueAt( row, 0 );
                            String value = ( String ) model.getValueAt( row, 1 );
                            PropertyEditor dialog = new PropertyEditor( view, key, value, true );
                            GUIUtils.center(view, dialog);
                            dialog.setVisible( true );
                            PropertyData data = dialog.getPropertyData();
                            if ( data == null ) {
                                return ;     // user cancelled
                            }
                            model.setValueAt( data.getName(), row, 0 );
                            model.setValueAt( data.getValue(), row, 1 );
                        }
                        return ;
                    }
                }
            );
            delete_btn.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        // TODO: add warning message and delete code
                    }
                }
            );

            btn_panel.add( add_btn, "0, 0, 1, 1, 0, w, 0" );
            btn_panel.add( edit_btn, "1, 0, 1, 1, 0, w, 0" );
            btn_panel.add( delete_btn, "2, 0, 1, 1, 0, w, 0" );
            kl.makeColumnsSameWidth( new int[] {0, 1, 2} );

            panel.add( btn_panel, "0, 2, 1, 1, E, 0, 3" );

            properties_panel.add( panel, "0, " + row + ", 1, 1, W, w, 0" );
            ++row;
        }


        // construct this panel
        setLayout( new BorderLayout() );
        setBorder( new javax.swing.border.EmptyBorder( 6, 6, 6, 6 ) );

        JScrollPane js = new JScrollPane( properties_panel );
        add( js, BorderLayout.CENTER );
    }

}
