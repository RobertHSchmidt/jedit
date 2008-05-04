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

import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import ise.plugin.svn.action.*;
import ise.plugin.svn.library.FileUtilities;
import ise.plugin.svn.library.GUIUtils;
import ise.java.awt.KappaLayout;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.gjt.sp.jedit.View;
import jdiff.DualDiff;
import jdiff.DiffMessage;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;

public class ResolveConflictDialog extends JDialog implements EBComponent {
    private ButtonGroup bg = null;
    private JRadioButton merge_rb = new JRadioButton( "Do manual merge with JDiff" );
    private JRadioButton keep_mine_rb = new JRadioButton( "Keep mine" );
    private JRadioButton keep_theirs_rb = new JRadioButton( "Use theirs" );

    private View view = null;
    private SVNStatus status = null;

    private JPanel mine_panel;
    private JPanel theirs_panel;

    /**
     * Presents a dialog to the user so they may resolve the conflicts in the
     * given file.  Based on the filename, this constructor attempts to find
     * files with the same name plus a ".mine" and a ".rXXX" extenstion and
     * uses those files to initiate JDiff to perform a manual merge or a file
     * copy to keep one or the other.
     * @param view the parent frame for this dialog
     * @param filename the name of the file to resolve conflicts for
     */
    public ResolveConflictDialog( View view, String filename ) {
        super( ( JFrame ) view, "Resolve Conflict for " + filename, true );
        File file = new File( filename );
        File workFile = new File( filename + ".mine" );
        File newFile = findNewFile( file );
        MySVNStatus status = new MySVNStatus( file, newFile, workFile );
        init( view, status );
    }

    /**
     * Presents a dialog to the user so they may resolve the conflicts in the
     * given file.  Based on the filename, this constructor attempts to find
     * files with the same name plus a ".mine" and a ".rXXX" extenstion and
     * uses those files to initiate JDiff to perform a manual merge or a file
     * copy to keep one or the other.
     * @param view the parent frame for this dialog
     * @param status an SVNStatus containing the info necessary to allow the
     * user to resolve conflicts.
     */
    public ResolveConflictDialog( View view, SVNStatus status ) {
        super( ( JFrame ) view, "Resolve Conflict for " + status.getFile().getName(), true );
        init( view, status );
    }

    /**
     * Finds a file named like the given file, but with ".rXXX" appended to the name,
     * where XXX represents a revision number.
     * Since it is possible that there are several such files, this method returns
     * the file with the largest number.
     */
    private File findNewFile( File file ) {
        File dir = file.getParentFile();
        final String toMatch = file.getName() + ".r";
        File[] files = dir.listFiles( new FileFilter() {
                    public boolean accept( File f ) {
                        return f.getName().startsWith( toMatch );
                    }
                }
                                    );
        if ( files.length == 0 ) {
            return null;
        }
        Arrays.sort( files, new Comparator<File>() {
                    public int compare( File a, File b ) {
                        Integer first = new Integer( a.getName().substring( toMatch.length() ) );
                        Integer second = new Integer( b.getName().substring( toMatch.length() ) );
                        return first.compareTo( second );
                    }
                }
                   );
        return files[ files.length - 1 ];
    }

    /**
     * Build the GUI for the dialog.
     */
    private void init( View view, SVNStatus status ) {
        if ( status == null ) {
            throw new IllegalArgumentException( "status cannnot be null" );
        }
        this.view = view;
        this.status = status;

        EditBus.addToBus( this );

        JPanel contents = new JPanel( new KappaLayout() );
        contents.setBorder( new EmptyBorder( 6, 6, 6, 6 ) );

        // conflict choices
        bg = new ButtonGroup();
        bg.add( merge_rb );
        bg.add( keep_mine_rb );
        bg.add( keep_theirs_rb );
        merge_rb.setSelected( true );

        // ok and cancel buttons
        KappaLayout kl = new KappaLayout();
        JPanel btn_panel = new JPanel( kl );
        JButton ok_btn = new JButton( "Ok" );
        ok_btn.addActionListener( getOkActionListener() );
        JButton cancel_btn = new JButton( "Cancel" );
        cancel_btn.addActionListener( getCancelActionListener() );
        btn_panel.add( "0, 0, 1, 1, 0, w, 3", ok_btn );
        btn_panel.add( "1, 0, 1, 1, 0, w, 3", cancel_btn );
        kl.makeColumnsSameWidth( 0, 1 );

        // layout the parts
        contents.add( "0, 0, , , W, , 3", new JLabel( "Select resolution method:" ) );
        contents.add( "0, 1, , , W, , 3", KappaLayout.createVerticalStrut( 6, true ) );
        contents.add( "0, 2, , , W, , 3", merge_rb );
        contents.add( "0, 3, , , W, , 3", keep_mine_rb );
        contents.add( "0, 4, , , W, , 3", keep_theirs_rb );
        contents.add( "0, 5, , , W, , 0", KappaLayout.createVerticalStrut( 10, true ) );
        contents.add( "0, 6, , , E, , 0", btn_panel );
        setContentPane( contents );
        pack();
        GUIUtils.center( view, this );
    }

    /**
     * Action for the Ok button.
     */
    private ActionListener getOkActionListener() {
        return new ActionListener() {
                   public void actionPerformed( ActionEvent ae ) {
                       ResolveConflictDialog.this.setVisible( false );
                       ResolveConflictDialog.this.dispose();
                       Runnable runner = new Runnable() {
                                   public void run() {
                                       if ( keep_mine_rb.isSelected() ) {
                                           doKeepMine();
                                       }
                                       else if ( keep_theirs_rb.isSelected() ) {
                                           doKeepTheirs();
                                       }
                                       else {
                                           doManualMerge();
                                       }
                                   }
                               };
                       SwingUtilities.invokeLater( runner );
                   }
               };
    }

    /**
     * Action for the Cancel button.
     */
    private ActionListener getCancelActionListener() {
        return new ActionListener() {
                   public void actionPerformed( ActionEvent ae ) {
                       ResolveConflictDialog.this.setVisible( false );
                       ResolveConflictDialog.this.dispose();
                   }
               };
    }

    /**
     * Keep mine means copy filename.mine to filename and mark the file resolved.
     */
    private void doKeepMine() {
        try {
            FileUtilities.copy( status.getConflictWrkFile(), status.getFile() );
            resolve();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Keep theirs means copy filename.rXXX to filename and mark the file resolved.
     */
    private void doKeepTheirs() {
        try {
            FileUtilities.copy( status.getConflictNewFile(), status.getFile() );
            resolve();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Mark filename as resolved.
     */
    private void resolve() {
        List<String> paths = new ArrayList<String>();
        paths.add( status.getFile().getAbsolutePath() );
        ResolvedAction action = new ResolvedAction( view, paths, null, null, true );
        action.actionPerformed( null );
    }

    /**
     * Do a manual merge via JDiff.
     * TODO: if the user clicks the unsplit button in the middle of merging, there
     * needs to be a way to remove the "Keep this file" buttons from the top of the
     * text areas.
     */
    private void doManualMerge() {
        try {
            final File mine = status.getConflictWrkFile();
            final File theirs = status.getConflictNewFile();

            if ( mine == null && theirs == null ) {
                JOptionPane.showMessageDialog( view, "Unable to fetch contents for comparison.", "Error", JOptionPane.ERROR_MESSAGE );
                return ;
            }

            // show JDiff
            view.unsplit();
            DualDiff.toggleFor( view );

            Runnable runner = new Runnable() {
                        public void run() {
                            // set the edit panes in the view
                            final EditPane[] editPanes = view.getEditPanes();
                            mine_panel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
                            theirs_panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );

                            // show mine in the left edit pane
                            editPanes[ 0 ].setBuffer( jEdit.openFile( view, mine.getAbsolutePath() ) );
                            JButton mine_btn = new JButton( "Keep this file" );
                            mine_btn.setToolTipText( "When done merging, click this button to keep this file as the merged file." );
                            mine_btn.addActionListener(
                                new ActionListener() {
                                    public void actionPerformed( ActionEvent ae ) {
                                        try {
                                            // copy the contents of the selected buffer to the file with the conflicts
                                            String buffer_text = editPanes[ 0 ].getTextArea().getText();
                                            StringReader reader = new StringReader( buffer_text );
                                            FileWriter writer = new FileWriter( status.getFile(), false );
                                            FileUtilities.copy( reader, writer );

                                            // close the working files
                                            jEdit._closeBuffer( view, jEdit.getBuffer( mine.getAbsolutePath() ) );
                                            jEdit._closeBuffer( view, jEdit.getBuffer( theirs.getAbsolutePath() ) );
                                            jEdit._closeBuffer( view, jEdit.getBuffer( status.getFile().getAbsolutePath() ) );

                                            // remove buttons from text area
                                            editPanes[ 0 ].getTextArea().removeTopComponent( mine_panel );
                                            editPanes[ 1 ].getTextArea().removeTopComponent( theirs_panel );

                                            // close JDiff and unsplit the view
                                            DualDiff.toggleFor( view );
                                            Runnable r2d2 = new Runnable() {
                                                public void run() {
                                                    view.unsplit();
                                                }
                                            };
                                            SwingUtilities.invokeLater(r2d2);

                                            // open the cleaned up file
                                            jEdit.openFile( view, status.getFile().getAbsolutePath() );

                                            // mark file as resolved
                                            if ( hasConflictMarkers( editPanes[ 0 ].getTextArea().getText() ) ) {
                                                int rtn = JOptionPane.showConfirmDialog( view, "This file appears to contain SVN conflict markers.\nAre you sure you want to use this file as is?", "Possible Conflict Markers", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
                                                if ( rtn == JOptionPane.NO_OPTION ) {
                                                    return ;
                                                }
                                            }
                                            ResolveConflictDialog.this.resolve();
                                        }
                                        catch ( Exception e ) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            );
                            mine_panel.add( mine_btn );
                            editPanes[ 0 ].getTextArea().addTopComponent( mine_panel );

                            // show theirs in the right edit pane
                            editPanes[ 1 ].setBuffer( jEdit.openFile( view, theirs.getAbsolutePath() ) );
                            JButton theirs_btn = new JButton( "Keep this file" );
                            theirs_btn.setToolTipText( "When done merging, click this button to keep this file as the merged file." );
                            theirs_btn.addActionListener(
                                new ActionListener() {
                                    public void actionPerformed( ActionEvent ae ) {
                                        try {
                                            // copy the contents of the selected buffer to the file with the conflicts
                                            String buffer_text = editPanes[ 1 ].getTextArea().getText();
                                            StringReader reader = new StringReader( buffer_text );
                                            FileWriter writer = new FileWriter( status.getFile(), false );
                                            FileUtilities.copy( reader, writer );

                                            // close the working files
                                            jEdit._closeBuffer( view, jEdit.getBuffer( mine.getAbsolutePath() ) );
                                            jEdit._closeBuffer( view, jEdit.getBuffer( theirs.getAbsolutePath() ) );
                                            jEdit._closeBuffer( view, jEdit.getBuffer( status.getFile().getAbsolutePath() ) );

                                            // remove buttons from text area
                                            editPanes[ 0 ].getTextArea().removeTopComponent( mine_panel );
                                            editPanes[ 1 ].getTextArea().removeTopComponent( theirs_panel );

                                            // close JDiff and unsplit the view
                                            DualDiff.toggleFor( view );
                                            Runnable r2d2 = new Runnable() {
                                                public void run() {
                                                    view.unsplit();
                                                }
                                            };
                                            SwingUtilities.invokeLater(r2d2);

                                            // open the cleaned up file
                                            jEdit.openFile( view, status.getFile().getAbsolutePath() );

                                            // mark file as resolved
                                            if ( hasConflictMarkers( editPanes[ 1 ].getTextArea().getText() ) ) {
                                                int rtn = JOptionPane.showConfirmDialog( view, "This file appears to contain SVN conflict markers.\nAre you sure you want to use this file as is?", "Possible Conflict Markers", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
                                                if ( rtn == JOptionPane.NO_OPTION ) {
                                                    return ;
                                                }
                                            }
                                            ResolveConflictDialog.this.resolve();
                                        }
                                        catch ( Exception e ) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            );
                            theirs_panel.add( theirs_btn );
                            editPanes[ 1 ].getTextArea().addTopComponent( theirs_panel );

                            // show the jdiff dockable
                            view.getDockableWindowManager().showDockableWindow( "jdiff-lines" );

                            // do an explicit repaint of the view to clean up the display
                            view.repaint();
                        }
                    };
            SwingUtilities.invokeLater( runner );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * @return true if the given string contains svn conflict markers
     */
    private boolean hasConflictMarkers( String s ) {
        Pattern conflict_start = Pattern.compile( "^<<<<<<<" );
        Pattern conflict_middle = Pattern.compile( "^=======" );
        Pattern conflict_end = Pattern.compile( "^>>>>>>>" );
        Matcher start_matcher = conflict_start.matcher( s );
        Matcher middle_matcher = conflict_middle.matcher( s );
        Matcher end_matcher = conflict_end.matcher( s );
        return start_matcher.matches() || middle_matcher.matches() || end_matcher.matches() ;
    }

    public void handleMessage( EBMessage msg ) {
        if ( msg instanceof DiffMessage ) {
            DiffMessage dmsg = ( DiffMessage ) msg;
            if ( DiffMessage.OFF.equals( dmsg.getWhat() ) ) {
                EditPane[] editPanes = view.getEditPanes();
                for ( EditPane pane : editPanes ) {
                    pane.getTextArea().removeTopComponent( mine_panel );
                    pane.getTextArea().removeTopComponent( theirs_panel );
                }
            }
        }
    }

    class MySVNStatus extends SVNStatus {
        public MySVNStatus( File file, File conflictNewFile, File conflictWrkFile ) {
            super( null, file, null, null, null, null, null, null, null, null, null, false, false, false, conflictNewFile, null, conflictWrkFile, null, null, null, null, null, null );
        }
    }
}