/*
* Copyright (c) 2008, Dale Anson
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


package jdiff.component;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jdiff.DualDiff;
import jdiff.component.ui.*;
import jdiff.util.Diff;

import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.jedit.textarea.JEditTextArea;

/**
 * Component to show the merge controls.
 * Implements EBComponent to know about when the View splits and unsplits so
 * the buttons can be enabled or disabled accordingly.
 */
public class LineRendererPane extends JComponent implements EBComponent, CaretListener {

    private static final String uiClassID = "LineRendererPaneUI";

    private View view = null;
    private DiffLineModel diffLineModel = null;
    private LineProcessorRunner runner = new LineProcessorRunner();

    private Set<ChangeListener> changeListeners = new HashSet<ChangeListener>();

    /**
     * @param view the parent frame
     */
    public LineRendererPane( View view ) {
        if ( view == null ) {
            throw new IllegalArgumentException( "view cannot be null" );
        }
        this.view = view;
        this.updateUI();
        EditBus.addToBus(this);
    }

    public void setUI( LineRendererPaneUI ui ) {
        super.setUI( ui );
    }

    public void updateUI() {
        if ( UIManager.get( getUIClassID() ) != null ) {
            setUI( ( LineRendererPaneUI ) UIManager.getUI( this ) );
        }
        else {
            setUI( new BasicLineRendererPaneUI() );
        }
        fireStateChanged();
    }

    public LineRendererPaneUI getUI() {
        return ( LineRendererPaneUI ) ui;
    }

    public String getUIClassID() {
        return uiClassID;
    }

    public void addChangeListener( ChangeListener cl ) {
        if ( cl != null ) {
            changeListeners.add( cl );
        }
    }

    public void removeChangeListener( ChangeListener cl ) {
        if ( cl != null ) {
            changeListeners.remove( cl );
        }
    }

    public void fireStateChanged() {
        if ( changeListeners.size() > 0 ) {
            ChangeEvent event = new ChangeEvent( this );
            for ( ChangeListener cl : changeListeners ) {
                cl.stateChanged( event );
            }
        }
    }

    /**
     * @return parent frame
     */
    public View getView() {
        return view;
    }

    public void setModel( DiffLineModel model ) {
        diffLineModel = model;
        fireStateChanged();
    }

    public DiffLineModel getModel() {
        return diffLineModel;
    }

    @Override
    public Color getBackground() {
        return view.getEditPane().getTextArea().getPainter().getBackground();
    }

    @Override
    public Font getFont() {
        return view.getEditPane().getTextArea().getPainter().getFont();
    }

    public void clear() {
        setModel( null );
    }

    public void reset() {
        fireStateChanged();
    }

    public void handleMessage( EBMessage message ) {
        if ( message instanceof EditPaneUpdate ) {
            EditPaneUpdate epu = ( EditPaneUpdate ) message;
            if ( epu.getWhat() == EditPaneUpdate.DESTROYED ) {
                fireStateChanged();
            }
        }
        else if ( message instanceof ViewUpdate ) {
            fireStateChanged();
        }
        maybeAddCaretListeners();
    }

    /**
     * Adds or removes caret listeners as appropriate.
     */
    private void maybeAddCaretListeners() {
        if ( view == null ) {
            return ;
        }
        for ( EditPane ep : view.getEditPanes() ) {
            ep.getTextArea().removeCaretListener( this );
        }
        if ( view.getEditPanes().length == 2 ) {
            view.getEditPanes() [ 0 ].getTextArea().addCaretListener( this );
            view.getEditPanes() [ 1 ].getTextArea().addCaretListener( this );
        }
    }

    public void caretUpdate( final CaretEvent e ) {
        if ( e.getSource() instanceof JEditTextArea ) {
            JEditTextArea source = ( JEditTextArea ) e.getSource();
            runner.setSource( source );
            SwingUtilities.invokeLater( runner );
        }
    }

    /**
     * Builds the line diff model.
     */
    class LineProcessorRunner implements Runnable {
        JEditTextArea source = null;
        public void setSource( JEditTextArea source ) {
            this.source = source;
        }
        public void run() {
            if ( source == null ) {
                LineRendererPane.this.setModel(null);
                return ;
            }
            DualDiff dualDiff = DualDiff.getDualDiffFor( LineRendererPane.this.view );
            if ( dualDiff == null ) {
                LineRendererPane.this.setModel(null);
                return ;
            }
            Diff.Change hunk = dualDiff.getEdits();
            if ( hunk == null ) {
                LineRendererPane.this.setModel(null);
                return ;
            }

            JEditTextArea textArea0 = LineRendererPane.this.view.getEditPanes() [ 0 ].getTextArea();
            JEditTextArea textArea1 = LineRendererPane.this.view.getEditPanes() [ 1 ].getTextArea();
            String leftLine = "";
            String rightLine = "";
            if ( source == textArea0 ) {
                int caretLine = textArea0.getCaretLine();
                for ( ; hunk != null; hunk = hunk.link ) {
                    if ( caretLine >= hunk.line0 && caretLine < hunk.line0 + hunk.deleted ) {
                        System.out.println("+++++ 1 caretLine: " + caretLine + ", hunk.line0: " + hunk.line0 + ", hunk.deleted: " + hunk.deleted);
                        // in a hunk
                        if ( hunk.deleted == 0 && hunk.line0 > 0 ) {
                            leftLine = "";
                        }
                        else {
                            leftLine = textArea0.getLineText( caretLine );
                        }
                        int offset = caretLine - hunk.line0;
                        if ( offset < hunk.inserted ) {
                            rightLine = textArea1.getLineText( hunk.line1 + offset );
                        }
                        else {
                            rightLine = "";
                        }
                        break ;
                    }
                }
            }
            else {
                int caretLine = textArea1.getCaretLine();
                for ( ; hunk != null; hunk = hunk.link ) {
                    if ( caretLine >= hunk.line1 && caretLine < hunk.line1 + hunk.inserted ) {
                        System.out.println("+++++ 2 caretLine: " + caretLine + ", hunk.line1: " + hunk.line1 + ", hunk.inserted: " + hunk.inserted);
                        // in a hunk
                        if ( hunk.inserted == 0 && hunk.line1 > 0 ) {
                            rightLine = "";
                        }
                        else {
                            rightLine = textArea1.getLineText( caretLine );
                        }
                        int offset = caretLine - hunk.line1;
                        if ( offset < hunk.deleted ) {
                            leftLine = textArea0.getLineText( hunk.line0 + offset );
                        }
                        else {
                            leftLine = "";
                        }
                        break ;
                    }
                }
            }
            if ((leftLine.equals("") && rightLine.equals("")) || leftLine.equals(rightLine)) {
                LineRendererPane.this.setModel(null);
            }
            else {
                LineRendererPane.this.setModel( new DiffLineModel( leftLine, rightLine ) );
            }
        }
    }

}
