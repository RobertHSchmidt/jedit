/* Generated By:JJTree: Do not edit this line. ASTProgram.java */

package sidekick.ecmascript.parser;

//import org.dojo.jsl.top.SourceFile;

import sidekick.ecmascript.parser.EcmaScript;
import sidekick.ecmascript.parser.EcmaScriptVisitor;

public class ASTProgram extends SimpleNode implements Scope {

    //protected SourceFile sourceFile;

    public ASTProgram(int id) {
        super(id);
    }

    public ASTProgram(EcmaScript p, int id) {
        super(p, id);
    }

    /** Accept the visitor. * */
    @Override
    public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    /*
    public void setSourceFile(SourceFile sourceFile) {
        this.sourceFile = sourceFile;
    }

    public SourceFile getSourceFile() {
        return sourceFile;
    }
    */
}
