 /*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.test.ui;

import java.util.Stack;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.editor.outline.GroovyOutlinePage;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IMember;


/*******************************************************
 * @author Maxime Hamm
 * @created April 11, 2011
 */
public class OutlineExtender2 extends OutlineExtender1 {

  public static final String NATURE = "org.codehaus.groovy.eclipse.tests.testNature2";
    
  public boolean appliesTo(GroovyCompilationUnit unit) {
    return new String(unit.getFileName()).contains("Y");
  }
  
  public GroovyOutlinePage getGroovyOutlinePageForEditor(String contextMenuID, GroovyEditor editor) {
    TCompilationUnit2 ounit = new TCompilationUnit2(this, editor.getGroovyCompilationUnit());       
    return new TGroovyOutlinePage(null, editor, ounit);
  }
  
  /*******************************************************
   * @author Maxime Hamm
   * @created April 11, 2011
   */
  public static class TCompilationUnit2 extends TCompilationUnit {
    
    public TCompilationUnit2(OutlineExtender2 outlineExtender, GroovyCompilationUnit unit) {
      super(outlineExtender, unit);
    }
    
    @Override
    public IMember[] refreshChildren() {
      type = new TType(this, getElementName().substring(0, getElementName().indexOf('.')));

      ModuleNode moduleNode = (ModuleNode) getNode();
      if (moduleNode != null) {
        new Finder(moduleNode, type).execute();
      }
      return new IMember[] {type};
    }
    
    @Override
    public void refresh() {
      super.refresh();
    }

  }
  
  /*********************************************************
   * @author Maxime Hamm
   * @created April 12, 2011
   */
  public static class Finder extends ASTNodeFinder {

    private ModuleNode moduleNode;
    private Stack<TType> methodStack = new Stack<TType>();
    
    public Finder(ModuleNode moduleNode, TType rootType) {
      super(new Region(moduleNode));
      this.moduleNode = moduleNode;
      methodStack.push(rootType);
    }
    
    public void execute() {
      doVisit(moduleNode);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression methodCall) {
      if (methodCall.getLineNumber()<0) {
        super.visitMethodCallExpression(methodCall);
        return;
      }
      
      TType parentType = methodStack.peek();      
      TType t = parentType.addTestType(methodCall.getMethodAsString());
      
      methodStack.push(t);      
      super.visitMethodCallExpression(methodCall);
      methodStack.pop();
    }
    
    @Override
    public void visitMethod(MethodNode method) {
      if (method.getLineNumber()<=1) {
        super.visitMethod(method);
        return;
      }
      
      TType parentType = methodStack.peek();  
      parentType.addTestMethod(method.getName(), method.getReturnType().getNameWithoutPackage());      
      super.visitMethod(method);
    }
    
    @Override
    public void visitVariableExpression(VariableExpression variable) {
      if (variable.getLineNumber()<0) {
        super.visitVariableExpression(variable);
        return;
      }
      
      TType parentType = methodStack.peek();  
      parentType.addTestField(variable.getName(), variable.getType().getNameWithoutPackage());      
      super.visitVariableExpression(variable);    
    }
    
    
  }
}