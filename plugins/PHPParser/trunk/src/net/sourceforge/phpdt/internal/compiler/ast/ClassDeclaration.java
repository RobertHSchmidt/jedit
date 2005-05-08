package net.sourceforge.phpdt.internal.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.phpdt.internal.compiler.parser.Outlineable;
import net.sourceforge.phpdt.internal.compiler.parser.OutlineableWithChildren;


/**
 * This class is my ClassDeclaration declaration for php. It is similar to org.eclipse.jdt.internal.compiler.ast.TypeDeclaration
 * It directly extends AstNode because a class cannot appear anywhere in php
 *
 * @author Matthieu Casanova
 */
public final class ClassDeclaration extends Statement implements OutlineableWithChildren {

  private ClassHeader classHeader;

  private int declarationSourceStart;
  private int declarationSourceEnd;
  private int bodyStart;
  private int bodyEnd;

  /** The constructor of the class. */
  private MethodDeclaration constructor;

  private List methods = new ArrayList();
  private final transient OutlineableWithChildren parent;
  /** The outlineable children (those will be in the node array too. */
  private final List children = new ArrayList();

  /**
   * Create a class giving starting and ending offset.
   *
   * @param sourceStart starting offset
   * @param sourceEnd   ending offset
   */
  public ClassDeclaration(OutlineableWithChildren parent,
                          ClassHeader classHeader,
                          int sourceStart,
                          int sourceEnd,
                          int beginLine,
                          int endLine,
                          int beginColumn,
                          int endColumn) {
    super(sourceStart, sourceEnd, beginLine, endLine, beginColumn, endColumn);
    this.parent = parent;
    this.classHeader = classHeader;
  }

  /**
   * Add a method to the class.
   *
   * @param method the method declaration
   */
  public void addMethod(MethodDeclaration method) {
    classHeader.addMethod(method.getMethodHeader());
    methods.add(method);
    add(method);
    if (method.getName().equals(classHeader.getName())) {
      constructor = method;
    }
  }

  /**
   * Add a method to the class.
   *
   * @param field the method declaration
   */
  public void addField(FieldDeclaration field) {
    VariableDeclaration c = field.getVariable();
    children.add(c);
    classHeader.addField(field);
  }

  public boolean add(Outlineable o) {
    return children.add(o);
  }

  /**
   * Tell if the class has a constructor.
   *
   * @return a boolean
   */
  public boolean hasConstructor() {
    return constructor != null;
  }

  /**
   * Return the class as String.
   *
   * @param tab how many tabs before the class
   *
   * @return the code of this class into String
   */
  public String toString(int tab) {
    return classHeader.toString(tab) + toStringBody(tab);
  }

  /**
   * Return the body of the class as String.
   *
   * @param tab how many tabs before the body of the class
   *
   * @return the body as String
   */
  private String toStringBody(int tab) {
    StringBuffer buff = new StringBuffer(" {");//$NON-NLS-1$
    List fields = classHeader.getFields();
    if (fields != null) {
      for (int i = 0; i < fields.size(); i++) {
        FieldDeclaration field = (FieldDeclaration) fields.get(i);
        buff.append('\n'); //$NON-NLS-1$
        buff.append(field.toString(tab + 1));
        buff.append(';');//$NON-NLS-1$
      }
    }
    for (int i = 0; i < methods.size(); i++) {
      MethodDeclaration o = (MethodDeclaration) methods.get(i);
      buff.append('\n');//$NON-NLS-1$
      buff.append(o.toString(tab + 1));
    }
    buff.append('\n').append(tabString(tab)).append('}'); //$NON-NLS-2$ //$NON-NLS-1$
    return buff.toString();
  }

  public OutlineableWithChildren getParent() {
    return parent;
  }

  public Outlineable get(int index) {
    return (Outlineable) children.get(index);
  }

  public int size() {
    return children.size();
  }

  public String toString() {
    return classHeader.toString();
  }

  /**
   * Get the variables from outside (parameters, globals ...)
   *
   * @param list the list where we will put variables
   */
  public void getOutsideVariable(List list) {
  }

  /**
   * get the modified variables.
   *
   * @param list the list where we will put variables
   */
  public void getModifiedVariable(List list) {
  }

  /**
   * Get the variables used.
   *
   * @param list the list where we will put variables
   */
  public void getUsedVariable(List list) {
  }

  public String getName() {
    return classHeader.getName();
  }

  public void setBodyStart(int bodyStart) {
    this.bodyStart = bodyStart;
  }

  public void setBodyEnd(int bodyEnd) {
    sourceEnd = bodyEnd;
    this.bodyEnd = bodyEnd;
  }

  public int getBodyStart() {
    return bodyStart;
  }

  public int getBodyEnd() {
    return bodyEnd;
  }

  public MethodDeclaration insideWichMethodIsThisOffset(int offset) {
    for (int i = 0; i < methods.size(); i++) {
      MethodDeclaration methodDeclaration = (MethodDeclaration) methods.get(i);
      if (offset < methodDeclaration.getBodyEnd() && offset> methodDeclaration.getBodyStart()) {
          return methodDeclaration;
      }
    }
    return null;
  }

  public ClassHeader getClassHeader() {
    return classHeader;
  }
}
