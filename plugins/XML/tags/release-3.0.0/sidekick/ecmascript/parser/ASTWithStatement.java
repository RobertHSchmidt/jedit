/* Generated By:JJTree: Do not edit this line. ASTWithStatement.java */

package sidekick.ecmascript.parser;

public class ASTWithStatement extends SimpleNode {
  public ASTWithStatement(int id) {
    super(id);
  }

  public ASTWithStatement(EcmaScript p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
