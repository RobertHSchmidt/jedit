/* Generated By:JJTree: Do not edit this line. ASTCaseGroup.java */

package sidekick.ecmascript.parser;

public class ASTCaseGroup extends SimpleNode {
  public ASTCaseGroup(int id) {
    super(id);
  }

  public ASTCaseGroup(EcmaScript p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
