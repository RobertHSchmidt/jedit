/* Generated By:JJTree: Do not edit this line. ASTBlock.java */

package sidekick.ecmascript.parser;

import sidekick.ecmascript.parser.EcmaScript;
import sidekick.ecmascript.parser.EcmaScriptVisitor;

public class ASTBlock extends SimpleNode implements Scope {
    public ASTBlock(int id) {
        super(id);
    }

    public ASTBlock(EcmaScript p, int id) {
        super(p, id);
    }

    /** Accept the visitor. * */
    @Override
    public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
