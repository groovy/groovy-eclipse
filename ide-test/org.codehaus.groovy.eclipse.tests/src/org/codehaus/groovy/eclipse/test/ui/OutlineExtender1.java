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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.editor.outline.GroovyOutlinePage;
import org.codehaus.groovy.eclipse.editor.outline.IOJavaElement;
import org.codehaus.groovy.eclipse.editor.outline.IOutlineExtender;
import org.codehaus.groovy.eclipse.editor.outline.OCompilationUnit;
import org.codehaus.groovy.eclipse.editor.outline.OField;
import org.codehaus.groovy.eclipse.editor.outline.OMethod;
import org.codehaus.groovy.eclipse.editor.outline.OType;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;

/*******************************************************
 * @author Maxime Hamm
 * @created April 11, 2011
 */
public class OutlineExtender1 implements IOutlineExtender, IProjectNature {

  public static final String NATURE = "org.codehaus.groovy.eclipse.tests.testNature1";
  
  public void configure() throws CoreException {
  }
  public void deconfigure() throws CoreException {
  }
  
  IProject p;
  public IProject getProject() {
      return p;
  }
  public void setProject(IProject project) {
      this.p = project;
  }

  public GroovyOutlinePage getGroovyOutlinePageForEditor(String contextMenuID, GroovyEditor editor) {
    TCompilationUnit ounit = new TCompilationUnit(this, editor.getGroovyCompilationUnit());       
    return new TGroovyOutlinePage(null, editor, ounit);
  }
  
  public boolean appliesTo(GroovyCompilationUnit unit) {
    return new String(unit.getFileName()).contains("X");
  }
  
  /*******************************************************
   * @author Maxime Hamm
   * @created April 12, 2011
   */
  public static class TGroovyOutlinePage extends GroovyOutlinePage {
    public TGroovyOutlinePage(String contextMenuID, GroovyEditor editor, OCompilationUnit unit) {
      super(contextMenuID, editor, unit);
    }
    
    public JavaOutlineViewer getViewer() {
      return getOutlineViewer();
    }
  }
  
  /*******************************************************
   * @author Maxime Hamm
   * @created April 11, 2011
   */
  public static class TCompilationUnit extends OCompilationUnit {
    
    public OutlineExtender1 outlineExtender;
    public TType type; 
    
    public TCompilationUnit(OutlineExtender1 outlineExtender, GroovyCompilationUnit unit) {
      super(unit);
      this.outlineExtender = outlineExtender;
    }
    
    @Override
    public IMember[] refreshChildren() {
      type = new TType(this, getElementName());
      return new IMember[] {type};
    }
    
    @Override
    public IMember getOutlineElementAt(int caretOffset) {
      return type;
    }  
  }
  
  /*******************************************************
   * @author Maxime Hamm
   * @created April 11, 2011
   */
  public static class TType extends OType {
    
    public TType(IOJavaElement parent, String name) {
      super(parent, new ConstantExpression(name), name);
      this.name = name;
    }

    @Override
    public ASTNode getElementNameNode() {
      return getNode();
    }      

    public TType addTestType(String name) {
      TType t = new TType(this, name);
      addChild(t);
      return t;
    }
    
    public TMethod addTestMethod(String name, String returnType) {
      TMethod m = new TMethod(this, name, returnType);
      addChild(m);
      return m;
    }
    
    public TField addTestField(String name, String typeSignature) {
      TField f = new TField(this, name, typeSignature);
      addChild(f);
      return f;
    }
  }
  
  /*******************************************************
   * @author Maxime Hamm
   * @created April 12, 2011
   */
  public static class TMethod extends OMethod {
    
    private String returnType;

    public TMethod(OType parent, String name, String returnType) {
      super(parent, new ConstantExpression(name), name);      
      this.name = name;
      this.returnType = returnType;
    }

    @Override
    public ASTNode getElementNameNode() {
      return getNode();
    }

    @Override
    public String getReturnTypeName() {
      return returnType; 
    }      
    
  }
  
  /*******************************************************
   * @author Maxime Hamm
   * @created April 12, 2011
   */
  public static class TField extends OField {
    
    private String typeSignature;

    public TField(OType parent, String name, String typeSignature) {
      super(parent, new ConstantExpression(name), name);      
      this.name = name;
      this.typeSignature = typeSignature;
    }

    @Override
    public ASTNode getElementNameNode() {
      return getNode();
    }

    @Override
    public String getTypeSignature() {     
      return typeSignature;
    }   
    
  }
}