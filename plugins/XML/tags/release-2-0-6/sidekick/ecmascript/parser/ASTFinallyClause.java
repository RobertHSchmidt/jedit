/* Generated By:JJTree: Do not edit this line. ASTFinallyClause.java */

package sidekick.ecmascript.parser;

public class ASTFinallyClause extends SimpleNode {
  public ASTFinallyClause(int id) {
    super(id);
  }

  public ASTFinallyClause(EcmaScript p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
