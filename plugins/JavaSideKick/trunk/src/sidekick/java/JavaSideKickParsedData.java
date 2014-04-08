/*
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
package sidekick.java;

import javax.swing.text.Segment;
import javax.swing.tree.*;
import java.util.*;
import org.gjt.sp.util.Log;

import sidekick.*;
import sidekick.util.Range;
import sidekick.java.node.CUNode;
import sidekick.java.node.TigerNode;

import org.gjt.sp.jedit.buffer.FoldHandler;
import org.gjt.sp.jedit.buffer.JEditBuffer;

/**
 * Stores a buffer structure tree.
 * Plugins can extend this class to persist plugin-specific information.
 * For example, the XML plugin stores code completion-related structures using
 * a subclass.
 *
 * @author Dale Anson
 */
public class JavaSideKickParsedData extends SideKickParsedData {

    private FoldHandler foldHandler = null;

    /**
     * @param fileName The file name being parsed, used as the root of the
     * tree.
     */
    public JavaSideKickParsedData( String fileName ) {
        super( fileName );
    }

    /**
     * Overridden to search TigerNodes rather than TreeNodes.  Not all tree nodes
     * may be showing, need the deepest asset at the cursor position for code
     * completion.
     * @param pos the caret position within the code
     */
    public IAsset getAssetAtOffset( int pos ) {
        Object userObject = ( ( DefaultMutableTreeNode ) root ).getUserObject();
        if ( ! ( userObject instanceof TigerNode ) ) {
            return null;
        }

        TigerNode tn = getTigerNodeAtOffset( ( TigerNode ) userObject, pos );
        return tn;
    }

    /**
     * Drill down to the node containing the given position.
     * @param tn The node to check the children of.
     * @param pos The caret position within the code.
     */
    private TigerNode getTigerNodeAtOffset( TigerNode tn, int pos ) {
        int closestPosition = tn.getStart().getOffset();
        TigerNode closestChild = tn;
        for ( int i = 0; i < tn.getChildCount(); i++ ) {
            TigerNode child = tn.getChildAt( i );
            int start = child.getStart().getOffset();
            int end = child.getEnd().getOffset();
            // do two different checks here since
            // during code completion, the parser may not be able to completely
            // parse the buffer and won't be able to calculate the end position
            // of nodes.
            if ( end == 0 && start >= closestPosition && start <= pos ) {
                closestPosition = start;
                closestChild = child;
            } else if ( start >= closestPosition && start <= pos && end >= pos ) {
                closestPosition = start;
                closestChild = child;
            }
        }
        if ( closestChild == tn ) {
            return tn;
        }
        return getTigerNodeAtOffset( closestChild, pos );
    }

    // overridden to handle Extends and Implements nodes
    protected boolean canAddToPath( TreeNode node ) {
        try {
            TigerNode tn = ( TigerNode ) getAsset( node );
            return ( tn.getOrdinal() == TigerNode.EXTENDS || tn.getOrdinal() == TigerNode.IMPLEMENTS ) ? false : true;
        } catch ( Exception e ) {
            return super.canAddToPath( node );
        }
    }
    
    /**
     * @return A special fold handler for java.
     */
    public FoldHandler getFoldHandler() {
        if ( foldHandler == null ) {
            foldHandler = new JavaFoldHandler();
        }
        return foldHandler;
    }
    
    /**
     * Java fold handler.    
     */
    private class JavaFoldHandler extends FoldHandler {

        public JavaFoldHandler() {
            super( "javasidekick" );
        }

        public int getFoldLevel( JEditBuffer buffer, int lineIndex, Segment seg ) {
            if ( lineIndex == 0 ) {
                return 0;
            }
            int lineStartOffset = buffer.getLineStartOffset( lineIndex );
            TreePath path = getTreePathForPosition( lineStartOffset );
            if ( path == null ) {
                return 0;
            } else {
                // check if in imports range
                Object userObject = ( ( DefaultMutableTreeNode ) root ).getUserObject();
                if ( ! ( userObject instanceof CUNode ) ) {
                    return 0;
                }
                Range importsRange = ( ( CUNode ) userObject ).getImportsRange();
                if ( importsRange != null ) {
                    if ( lineIndex >= importsRange.getStartLocation().line && lineIndex <= importsRange.getEndLocation().line ) {
                        return 1;
                    }
                }

                // otherwise, proceed as usual
                TreeNode treeNode = ( TreeNode ) path.getLastPathComponent();
                IAsset asset = getAsset( treeNode );
                if (userObject.equals(asset)) {
                    return 0;   
                }
                if ( asset.getStart().getOffset() == lineStartOffset ) {
                    return path.getPathCount() - 2;
                }

                return path.getPathCount() - 1;
            }
        }
    }
}
