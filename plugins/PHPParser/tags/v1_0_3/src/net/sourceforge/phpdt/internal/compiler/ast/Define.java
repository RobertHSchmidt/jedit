package net.sourceforge.phpdt.internal.compiler.ast;

import net.sourceforge.phpdt.internal.compiler.ast.declarations.VariableUsage;
import net.sourceforge.phpdt.internal.compiler.parser.Outlineable;

import java.util.List;

/**
 * a Define.
 * define(expression,expression)
 * 
 * @author Matthieu Casanova
 */
public final class Define extends Statement implements Outlineable {

  private final Expression defineName;
  private final Expression defineValue;

  private final Object parent;

  public Define(final Object parent,
                final Expression defineName,
                final Expression defineValue,
                final int sourceStart,
                final int sourceEnd) {
    super(sourceStart, sourceEnd);
    this.parent = parent;
    this.defineName = defineName;
    this.defineValue = defineValue;
  }

  public String toString(final int tab) {
    final String nameString = defineName.toStringExpression();
    final String valueString = defineValue.toStringExpression();
    final StringBuffer buff = new StringBuffer(tab + 10 + nameString.length() + valueString.length());
    buff.append(tabString(tab));
    buff.append("define(");
    buff.append(nameString);
    buff.append(", ");
    buff.append(valueString);
    buff.append(")");
    return buff.toString();
  }

  public String toString() {
    final String nameString = defineName.toStringExpression();
    final String valueString = defineValue.toStringExpression();
    final StringBuffer buff = new StringBuffer(nameString.length() + valueString.length() + 3);
    buff.append(nameString);
    buff.append(" = ");
    buff.append(valueString);
    return buff.toString();
  }

  public Object getParent() {
    return parent;
  }

  /**
   * Get the variables from outside (parameters, globals ...)
   *
   * @param list the list where we will put variables
   */
  public void getOutsideVariable(final List list) {
    list.add(new VariableUsage(defineName.toStringExpression(), sourceStart));//todo: someday : evaluate the defineName
  }

  /**
   * get the modified variables.
   *
   * @param list the list where we will put variables
   */
  public void getModifiedVariable(final List list) {}

  /**
   * Get the variables used.
   *
   * @param list the list where we will put variables
   */
  public void getUsedVariable(final List list) {}
}
