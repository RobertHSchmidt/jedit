/* Generated By:JJTree: Do not edit this line. SimpleNode.java */



package sidekick.ecmascript.parser;



import java.util.*;

import javax.swing.Icon;
import javax.swing.text.Position;

import sidekick.IAsset;
import sidekick.util.*;

import sidekick.ecmascript.parser.ASTDoStatement;
import sidekick.ecmascript.parser.ASTEmptyStatement;
import sidekick.ecmascript.parser.ASTExpressionStatement;
import sidekick.ecmascript.parser.ASTForInStatement;
import sidekick.ecmascript.parser.ASTForStatement;
import sidekick.ecmascript.parser.ASTForVarInStatement;
import sidekick.ecmascript.parser.ASTForVarStatement;
import sidekick.ecmascript.parser.ASTIfStatement;
import sidekick.ecmascript.parser.ASTVariableStatement;
import sidekick.ecmascript.parser.ASTWhileStatement;
import sidekick.ecmascript.parser.ASTWithStatement;
import sidekick.ecmascript.parser.EcmaScript;
import sidekick.ecmascript.parser.EcmaScriptConstants;
import sidekick.ecmascript.parser.EcmaScriptTreeConstants;
import sidekick.ecmascript.parser.EcmaScriptVisitor;
import sidekick.ecmascript.parser.Node;
import sidekick.ecmascript.parser.Token;


public class SimpleNode implements Node, SideKickElement, EcmaScriptConstants,
    EcmaScriptTreeConstants {

    protected Node parent;
    protected Node[] children;
    protected int id;
    protected EcmaScript parser;
    private Location start = null;
    private Location end = null;
    private Position startPosition = null;
    private Position endPosition = null;
    private boolean visible = false;
    protected Token beginToken;
    protected Token endToken;
    protected boolean inserted = false;
    protected LinkedList javadocComments;


    public SimpleNode( int i ) {
        id = i;
    }

    public SimpleNode( EcmaScript p, int i ) {
        this( i );
        parser = p;
    }

    public void jjtOpen() {}

    public void jjtClose() {}

    public void jjtSetParent( Node n ) {
        parent = n;
    }
    public Node jjtGetParent() {
        return parent;
    }

    public void jjtAddChild( Node n, int i ) {
        if ( children == null ) {
            children = new Node[ i + 1 ];
        }
        else if ( i >= children.length ) {
            Node c[] = new Node[ i + 1 ];
            System.arraycopy( children, 0, c, 0, children.length );
            children = c;
        }
        children[ i ] = n;
    }

    public Node jjtGetChild( int i ) {
        return children[ i ];
    }

    public int jjtGetNumChildren() {
        return ( children == null ) ? 0 : children.length;
    }

    public boolean hasChildren() {
        return jjtGetNumChildren() > 0;
    }

    public List getChildren() {
        if (children != null)
            return Arrays.asList( children );
        else
            return null;
    }

    /** Accept the visitor. **/
    public Object jjtAccept( EcmaScriptVisitor visitor, Object data ) {
        return visitor.visit( this, data );
    }

    /** Accept the visitor. **/
    public Object childrenAccept( EcmaScriptVisitor visitor, Object data ) {
        if ( children != null ) {
            for ( int i = 0; i < children.length; ++i ) {
                children[ i ].jjtAccept( visitor, data );
            }
        }
        return data;
    }

    // ----------------------------------------------------------------------------------------------
    // for SideKickElement

    public void setStartLocation( Location loc ) {
        start = loc;
    }

    public Location getStartLocation() {
        if ( start == null )
            start = new Location();
        return start;
    }

    public void setEndLocation( Location loc ) {
        end = loc;
    }

    public Location getEndLocation() {
        if ( end == null )
            end = new Location();
        return end;
    }

    public void setStartPosition( Position s ) {
        startPosition = s;
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public void setEndPosition( Position s ) {
        endPosition = s;
    }

    public Position getEndPosition() {
        return endPosition;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean b) {
        visible = b;
    }


    // ----------------------------------------------------------------------------------------------
    // additional API's uwedeportivo

    public void insertChild( Node n, int i ) {
        if ( children == null ) {
            throw new IllegalStateException(
                "no children insertion not possible" );
        }

        if ( ( i > children.length ) || ( i < 0 ) ) {
            throw new IndexOutOfBoundsException();
        }

        Node c[] = new Node[ children.length + 1 ];
        if ( i > 0 ) {
            System.arraycopy( children, 0, c, 0, i );
        }
        c[ i ] = n;
        if ( i < children.length ) {
            System.arraycopy( children, i, c, i + 1, children.length - i );
        }

        children = c;
    }

    public Node getPrevSibling() {
        if ( parent == null ) {
            return null;
        }

        SimpleNode parentSNode = ( SimpleNode ) parent;

        int i = 0;

        for ( i = 0; i < parentSNode.children.length; i++ ) {
            if ( parentSNode.children[ i ] == this ) {
                break;
            }
        }

        if ( i == 0 ) {
            return null;
        }
        else {
            return parentSNode.children[ i - 1 ];
        }
    }

    public Node getNextSibling() {
        if ( parent == null ) {
            return null;
        }

        SimpleNode parentSNode = ( SimpleNode ) parent;

        int i = 0;

        for ( i = 0; i < parentSNode.children.length; i++ ) {
            if ( parentSNode.children[ i ] == this ) {
                break;
            }
        }

        if ( i == parentSNode.children.length - 1 ) {
            return null;
        }
        else {
            return parentSNode.children[ i + 1 ];
        }

    }

    public void setChild( Node n, int i ) {
        children[ i ] = n;
        n.jjtSetParent( this );
    }

    public int getChildIndex( Node n ) {
        int i = 0;

        for ( i = 0; i < children.length; i++ ) {
            if ( children[ i ] == n ) {
                break;
            }
        }

        return ( i == children.length ) ? -1 : i;
    }

    public void remove() {
        if ( parent == null ) {
            return ;
        }

        SimpleNode parentSNode = ( SimpleNode ) parent;

        int i = 0;

        for ( i = 0; i < parentSNode.children.length; i++ ) {
            if ( parentSNode.children[ i ] == this ) {
                break;
            }
        }

        Node c[] = new Node[ parentSNode.children.length - 1 ];

        if ( i == 0 ) {
            System.arraycopy( parentSNode.children, 1, c, 0,
                    parentSNode.children.length - 1 );
        }
        else if ( i == parentSNode.children.length - 1 ) {
            System.arraycopy( parentSNode.children, 0, c, 0,
                    parentSNode.children.length - 1 );
        }
        else {
            System.arraycopy( parentSNode.children, 0, c, 0, i );
            System.arraycopy( parentSNode.children, i + 1, c, i,
                    parentSNode.children.length - i - 1 );
        }

        parentSNode.children = c;
    }

    public void removeSafely() {
        this.removeSafely( false );
    }

    public void removeSafely( boolean ignoreSpecialTokens ) {
        // we have to clear up the ast if we're the only child of an if
        // statement for example
        // we do it simple in that case (we just replace with empty statement)

        SimpleNode parentSNode = ( SimpleNode ) parent;
        boolean replace = false;

        if ( ( this instanceof ASTVariableStatement )
                || ( this instanceof ASTExpressionStatement ) ) {

            if ( ( parentSNode instanceof ASTIfStatement )
                    || ( parentSNode instanceof ASTWhileStatement )
                    || ( parentSNode instanceof ASTDoStatement )
                    || ( parentSNode instanceof ASTForStatement )
                    || ( parentSNode instanceof ASTForVarStatement )
                    || ( parentSNode instanceof ASTForInStatement )
                    || ( parentSNode instanceof ASTForVarInStatement )
                    || ( parentSNode instanceof ASTWithStatement ) ) {
                replace = true;
            }
        }

        if ( replace ) {
            int index = parentSNode.getChildIndex( this );

            ASTEmptyStatement replacement = new ASTEmptyStatement(
                        EcmaScriptTreeConstants.JJTEMPTYSTATEMENT );

            Token scToken = new Token();
            scToken.kind = EcmaScriptConstants.SEMICOLON;
            scToken.image = ";";
            // we have to transfer the special tokens of the replaced code to
            // the replacement
            scToken.specialToken = getBeginToken().specialToken;

            replacement.setBeginToken( scToken );
            replacement.setEndToken( scToken );
            replacement.setInserted( true );

            parentSNode.setChild( replacement, index );
        }
        else {
            if ( !ignoreSpecialTokens ) {
                // transfer the special tokens first
                Token specialTransfer = getBeginToken().specialToken;

                if ( specialTransfer != null ) {
                    Token nextBeginToken = getEndToken().next;

                    if ( nextBeginToken != null ) {
                        SimpleNode.transferSpecial( specialTransfer,
                                nextBeginToken );
                    }
                }
            }

            remove();
        }

    }

    static public void transferSpecial( Token specialTransfer,
            Token nextBeginToken ) {
        if ( nextBeginToken.specialToken == null ) {
            nextBeginToken.specialToken = specialTransfer;
        }
        else if ( nextBeginToken.specialToken != specialTransfer ) {
            Token endSpecial = nextBeginToken.specialToken;

            while ( endSpecial.specialToken != null ) {
                endSpecial = endSpecial.specialToken;
            }

            endSpecial.specialToken = specialTransfer;
        }
    }

    /*
     * You can override these two methods in subclasses of SimpleNode to
     * customize the way the node appears when the tree is dumped. If your
     * output uses more than one line you should override toString(String),
     * otherwise overriding toString() is probably all you need to do.
     */

    @Override
    public String toString() {
        return EcmaScriptTreeConstants.jjtNodeName[ id ];
    }

    public String toString( String prefix ) {
        return prefix + toString();
    }

    /*
     * Override this method if you want to customize how the node dumps out its
     * children.
     */
    public void dump( String prefix ) {
        System.out.println( toString( prefix ) );
        if ( children != null ) {
            for ( int i = 0; i < children.length; ++i ) {
                SimpleNode n = ( SimpleNode ) children[ i ];
                if ( n != null ) {
                    n.dump( prefix + " " );
                }
            }
        }
    }

    public void setBeginToken( Token token ) {
        beginToken = token;
    }

    public Token getBeginToken() {
        return beginToken;
    }

    public void setEndToken( Token token ) {
        endToken = token;
    }

    public Token getEndToken() {
        return endToken;
    }

    public boolean inserted() {
        return inserted;
    }

    public void setInserted( boolean aFlag ) {
        inserted = aFlag;
    }

    private void findComments() {
        javadocComments = new LinkedList();

        Token token = getBeginToken();
        Comment aComment = null;

        if ( token != null ) {
            Token specialToken = token.specialToken;

            while ( specialToken != null ) {
                aComment = null;

                if ( specialToken.kind == EcmaScriptConstants.MULTI_LINE_COMMENT ) {
                    try {
                        aComment = new Comment( specialToken.image );
                    }
                    catch ( Exception exc ) {
                        exc.printStackTrace();
                    }

                    if ( ( aComment != null ) && aComment.isJavadoc() ) {
                        javadocComments.add( aComment );
                    }
                }

                specialToken = specialToken.specialToken;
            }
        }
    }

    public Comment getComment() {
        if ( javadocComments == null ) {
            findComments();
        }

        return javadocComments.size() == 0 ? null : ( Comment ) javadocComments
               .getFirst();
    }

    public List getComments() {
        if ( javadocComments == null ) {
            findComments();
        }

        return javadocComments;
    }


}
