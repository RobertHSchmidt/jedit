/* Generated By:JJTree: Do not edit this line. ASTVariableDeclarationList.java */

package sidekick.ecmascript.parser;

public class ASTVariableDeclarationList extends SimpleNode {
  public ASTVariableDeclarationList(int id) {
    super(id);
  }

  public ASTVariableDeclarationList(EcmaScript p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
