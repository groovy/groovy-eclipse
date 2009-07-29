/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.test.core.types;

import junit.framework.*;

import org.codehaus.groovy.eclipse.core.types.*;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator.EvalResult;

/**
 * @author Heiko Boettger
 */
public class TypeEvaluatorTestCase extends TestCase {
  
  public void test_EvalutateStringExpression() {
    ITypeEvaluationContext context = new ITypeEvaluationContext(){
      public ClassLoader getClassLoader() {
        return null;
      }

      public String[] getImports() {
        return new String[0];
      }

      public Field lookupField(String type, String name, boolean accessible, boolean staticAccess) {
        throw new UnsupportedOperationException();
      }

      public Method lookupMethod(String type, String name, String[] paramTypes, boolean accessible, boolean staticAccess) {
        throw new UnsupportedOperationException();
      }

      public Property lookupProperty(String type, String name, boolean accessible, boolean staticAccess) {
        throw new UnsupportedOperationException();
      }

      public Type lookupSymbol(String name) {
        throw new UnsupportedOperationException();
      }
      
    };
    TypeEvaluator typeEvaluator = new TypeEvaluator(context);
    EvalResult result = typeEvaluator.evaluate("\"string\"");
    Assert.assertEquals("java.lang.String", result.getName());
  }
  
  public void test_EvalutateGStringExpression() {
    ITypeEvaluationContext context = new ITypeEvaluationContext(){
      public ClassLoader getClassLoader() {
        return null;
      }

      public String[] getImports() {
        return new String[0];
      }

      public Field lookupField(String type, String name, boolean accessible, boolean staticAccess) {
        throw new UnsupportedOperationException();
      }

      public Method lookupMethod(String type, String name, String[] paramTypes, boolean accessible, boolean staticAccess) {
        throw new UnsupportedOperationException();
      }

      public Property lookupProperty(String type, String name, boolean accessible, boolean staticAccess) {
        throw new UnsupportedOperationException();
      }

      public Type lookupSymbol(String name) {
        return new LocalVariable("AnyType", name);
      }
      
    };
    TypeEvaluator typeEvaluator = new TypeEvaluator(context);
    EvalResult result = typeEvaluator.evaluate("\"string${any}\"");
    Assert.assertEquals("groovy.lang.GString", result.getName());
  }
  
  public void test_EvalutateFieldExpression() {
    ITypeEvaluationContext context = new ITypeEvaluationContext(){
      public ClassLoader getClassLoader() {
        return null;
      }

      public String[] getImports() {
        return new String[0];
      }

      public Field lookupField(String type, String name, boolean accessible, boolean staticAccess) {
        return new Field("int", Modifiers.ACC_PUBLIC, name, new ClassType(type, Modifiers.ACC_PUBLIC, "String[]"));
      }

      public Method lookupMethod(String type, String name, String[] paramTypes, boolean accessible, boolean staticAccess) {
        throw new UnsupportedOperationException();
      }

      public Property lookupProperty(String type, String name, boolean accessible, boolean staticAccess) {
        return null;
      }

      public Type lookupSymbol(String name) {
        return new Field("java.lang.String[]", Modifiers.ACC_PUBLIC, name, new ClassType("mypackage.MyClass", Modifiers.ACC_PUBLIC, "MyClass"));
      }
      
    };
    TypeEvaluator typeEvaluator = new TypeEvaluator(context);
    EvalResult result = typeEvaluator.evaluate("array.length");
    Assert.assertEquals("int", result.getName());
  }
}
