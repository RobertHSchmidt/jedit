/* Generated By:JJTree: Do not edit this line. ASTContinueStatement.java */

package sidekick.ecmascript.parser;

public class ASTContinueStatement extends SimpleNode {
  public ASTContinueStatement(int id) {
    super(id);
  }

  public ASTContinueStatement(EcmaScript p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
