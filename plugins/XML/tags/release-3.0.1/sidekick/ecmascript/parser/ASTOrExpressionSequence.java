/* Generated By:JJTree: Do not edit this line. ASTOrExpressionSequence.java */

package sidekick.ecmascript.parser;

public class ASTOrExpressionSequence extends SimpleNode {
  public ASTOrExpressionSequence(int id) {
    super(id);
  }

  public ASTOrExpressionSequence(EcmaScript p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
