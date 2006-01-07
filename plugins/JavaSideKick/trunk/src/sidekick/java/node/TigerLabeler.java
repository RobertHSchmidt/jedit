
package sidekick.java.node;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import sidekick.java.options.*;
import org.gjt.sp.jedit.GUIUtilities;


/**
 * Most of the display settings (how to show) are handled here.
 */
public class TigerLabeler {

    // current option settings
    private static DisplayOptions options = null;

    // inverse options for tool tips
    private static DisplayOptions inverseOptions = null;

    // various icons for display
    protected static Icon CU_ICON = GUIUtilities.loadIcon( "OpenFile.png" );
    protected static ImageIcon CLASS_ICON = new ImageIcon( TigerLabeler.class.getClassLoader().getResource( "sidekick/java/icons/Class.gif" ) );
    protected static ImageIcon EXTENDS_ICON = new ImageIcon( TigerLabeler.class.getClassLoader().getResource( "sidekick/java/icons/Extends.gif" ) );
    protected static ImageIcon IMPLEMENTS_ICON = new ImageIcon( TigerLabeler.class.getClassLoader().getResource( "sidekick/java/icons/Implements.gif" ) );
    protected static ImageIcon INTERFACE_ICON = new ImageIcon( TigerLabeler.class.getClassLoader().getResource( "sidekick/java/icons/Interface.gif" ) );
    protected static ImageIcon CONSTRUCTOR_ICON = new ImageIcon( TigerLabeler.class.getClassLoader().getResource( "sidekick/java/icons/Constructor.gif" ) );
    protected static ImageIcon METHOD_ICON = new ImageIcon( TigerLabeler.class.getClassLoader().getResource( "sidekick/java/icons/Method.gif" ) );
    protected static ImageIcon THROWS_ICON = new ImageIcon( TigerLabeler.class.getClassLoader().getResource( "sidekick/java/icons/Throws.gif" ) );
    protected static ImageIcon FIELD_ICON = new ImageIcon( TigerLabeler.class.getClassLoader().getResource( "sidekick/java/icons/Field.gif" ) );
    protected static ImageIcon ENUM_ICON = new ImageIcon( TigerLabeler.class.getClassLoader().getResource( "sidekick/java/icons/Constructor.gif" ) );
    protected static ImageIcon DEFAULT_ICON = new ImageIcon( TigerLabeler.class.getClassLoader().getResource( "sidekick/java/icons/Operation.gif" ) );

    
    public static void setDisplayOptions(DisplayOptions opts) {
        options = opts;   
    }

    public static Icon getIcon( TigerNode tn ) {
        if (options == null) {
            return null;
        }

        Icon icon = null;
        
        if ( options.getShowIcons() ) {
            switch ( tn.getOrdinal() ) {
                case TigerNode.COMPILATION_UNIT:
                    icon = CU_ICON;
                    break;
                case TigerNode.CLASS:
                    icon = CLASS_ICON;
                    break;
                case TigerNode.EXTENDS:
                    icon = EXTENDS_ICON;
                    break;
                case TigerNode.IMPLEMENTS:
                    icon = IMPLEMENTS_ICON;
                    break;
                case TigerNode.INTERFACE:
                    icon = INTERFACE_ICON;
                    break;
                case TigerNode.CONSTRUCTOR:
                    icon = CONSTRUCTOR_ICON;
                    break;
                case TigerNode.METHOD:
                    icon = METHOD_ICON;
                    break;
                case TigerNode.THROWS:
                    icon = THROWS_ICON;
                    break;
                case TigerNode.FIELD:
                    icon = FIELD_ICON;
                    break;
                case TigerNode.ENUM:
                    icon = ENUM_ICON;
                    break;
                default:
                    icon = DEFAULT_ICON;
                    break;
            }
        }
        // may be null to not show an icon
        return icon;
    }


    public static String getText( TigerNode tn ) {

        if ( tn != null ) {

            if (options == null) {
                return tn.toString();
            }

            // build the string for the label
            StringBuffer sb = new StringBuffer();

            // maybe add the line number
            if ( options.getShowLineNum() )
                sb.append( tn.getStartLocation().line ).append( ": " );

            // add visibility modifiers, use either +, #, -, or public,
            // protected, private
            int modifiers = tn.getModifiers();
            if ( options.getVisSymbols() ) {
                if ( ModifierSet.isPublic( modifiers ) )
                    sb.append( "+" );
                else if ( ModifierSet.isProtected( modifiers ) )
                    sb.append( "#" );
                else if ( ModifierSet.isPrivate( modifiers ) )
                    sb.append( "-" );
                else
                    sb.append( " " );
            }
            else {
                if ( ModifierSet.isPublic( modifiers ) )
                    sb.append( "public " );
                else if ( ModifierSet.isProtected( modifiers ) )
                    sb.append( "protected " );
                else if ( ModifierSet.isPrivate( modifiers ) )
                    sb.append( "private " );
                else
                    sb.append( " " );
            }

            // maybe show keywords, this is the "Keywords specified by icons" setting,
            // which seems like an odd choice of words to me.  I was expecting icons,
            // but I think it means more like "show keywords beside icons"
            if ( options.getShowIconKeywords() ) {
                switch ( tn.getOrdinal() ) {
                    case TigerNode.CLASS:
                        sb.append( "class " );
                        break;
                    case TigerNode.EXTENDS:
                        sb.append( "extends " );
                        break;
                    case TigerNode.IMPLEMENTS:
                        sb.append( "implements " );
                        break;
                }
            }

            // maybe add misc. modifiers, e.g. synchronized, native, transient, etc
            if ( options.getShowMiscMod() ) {
                String mods = ModifierSet.modifiersAsString( modifiers );
                if ( mods != null && mods.length() > 0 )
                    sb.append( mods ).append( " " );
            }

            // for methods and fields, maybe add return type before node name
            if ( !options.getTypeIsSuffixed() ) {
                switch ( tn.getOrdinal() ) {
                    case TigerNode.CONSTRUCTOR:
                        sb.append( "/*constructor*/" );
                        break;
                    case TigerNode.METHOD:
                        sb.append( ( ( MethodNode ) tn ).getReturnType() ).append( " " );
                        break;
                    case TigerNode.FIELD:
                        sb.append( ( ( FieldNode ) tn ).getType() ).append( " " );
                        break;
                    case TigerNode.ENUM:
                        sb.append( "enum " );
                        break;
                }
            }

            // maybe qualify inner class/interface name
            if ( options.getShowNestedName() && tn.getParent() != null &&
                    ( tn.getOrdinal() == TigerNode.CLASS || tn.getOrdinal() == TigerNode.INTERFACE ) &&
                    ( tn.getParent().getOrdinal() == TigerNode.CLASS || tn.getParent().getOrdinal() == TigerNode.INTERFACE ) ) {
                sb.append( tn.getParent().getName() ).append( "." );
            }


            // add the node name
            switch ( tn.getOrdinal() ) {
                case TigerNode.EXTENDS:
                    sb.append( "class " );
                    break;
                case TigerNode.IMPLEMENTS:
                    sb.append( "interface " );
                    break;
            }
            sb.append( tn.getName() );

            // maybe show generics type arguments
            if ( options.getShowTypeArgs() ) {
                String typeParams = null;
                if ( tn.getOrdinal() == TigerNode.CLASS ) {
                    typeParams = ( ( ClassNode ) tn ).getTypeParams();
                }
                else if ( tn.getOrdinal() == TigerNode.EXTENDS ) {
                    typeParams = ( ( ExtendsNode ) tn ).getTypeParams();
                }
                else if ( tn.getOrdinal() == TigerNode.IMPLEMENTS ) {
                    typeParams = ( ( ImplementsNode ) tn ).getTypeParams();
                }
                else if ( tn.getOrdinal() == TigerNode.FIELD ) {
                    typeParams = ( ( FieldNode ) tn ).getTypeParams();
                }
                if ( typeParams != null )
                    sb.append( typeParams );
            }

            // for constructors and methods, maybe add the arguments
            if ( options.getShowArguments() ) {
                if ( tn.getOrdinal() == TigerNode.CONSTRUCTOR ) {
                    sb.append( "(" ).append( ( ( ConstructorNode ) tn ).getFormalParams( options.getShowArgumentNames(), options.getTypeIsSuffixed(), options.getShowMiscMod(), options.getShowTypeArgs() ) ).append( ")" );
                }
                else if ( tn.getOrdinal() == TigerNode.METHOD ) {
                    sb.append( "(" ).append( ( ( MethodNode ) tn ).getFormalParams( options.getShowArgumentNames(), options.getTypeIsSuffixed(), options.getShowMiscMod(), options.getShowTypeArgs() ) ).append( ")" );
                }
            }

            // for methods and fields, maybe add return type after node name
            if ( options.getTypeIsSuffixed() ) {
                switch ( tn.getOrdinal() ) {
                    case TigerNode.CONSTRUCTOR:
                        sb.append( ": &lt;init&gt;" );
                        break;
                    case TigerNode.METHOD:
                        sb.append( " : " ).append( ( ( MethodNode ) tn ).getReturnType() );
                        break;
                    case TigerNode.FIELD:
                        sb.append( " : " ).append( ( ( FieldNode ) tn ).getType() );
                        break;
                    case TigerNode.ENUM:
                        sb.append( " : enum" );
                        break;
                }
            }


            String labelText = toHtml( sb.toString() );
            sb = new StringBuffer();
            sb.append( "<html>" );

            // maybe underline static items
            if ( options.getStaticUlined() && ModifierSet.isStatic( tn.getModifiers() ) ) {
                sb.append( "<u>" );
            }

            // maybe set abstract items in italics
            if ( options.getAbstractItalic() && ModifierSet.isAbstract( tn.getModifiers() ) ) {
                sb.append( "<i>" );
            }
            sb.append( labelText );

            return sb.toString();
        }
        else
            return tn.toString();
    }

    public static String getToolTipText( TigerNode tn ) {
        return tn.toString();
    }

    private static String toHtml( String s ) {
        s = s.replaceAll( "<", "&lt;" );
        s = s.replaceAll( ">", "&gt;" );
        return s;
    }

}

