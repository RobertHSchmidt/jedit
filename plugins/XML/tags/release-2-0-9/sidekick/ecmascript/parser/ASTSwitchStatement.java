/* Generated By:JJTree: Do not edit this line. ASTSwitchStatement.java */

package sidekick.ecmascript.parser;

public class ASTSwitchStatement extends SimpleNode {
  public ASTSwitchStatement(int id) {
    super(id);
  }

  public ASTSwitchStatement(EcmaScript p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
