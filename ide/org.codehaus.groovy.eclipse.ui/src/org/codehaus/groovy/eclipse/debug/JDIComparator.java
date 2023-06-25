/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.grails.ide.eclipse.groovy.debug.core.evaluation;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;

/**
 * Static methods for overriding comparisons in evaluated scripts
 * This class exists because Groovy's mop does not let you take control over
 * operations like <. >, ==, etc.
 * Instead, we walk the Script ast and look for these operations.  Replace them with
 * calls to methods in this class.
 * The problem is...how do we get this class loaded on the classpath???
 * @author Andrew Eisenberg
 * @since 2.5.2
 */
public class JDIComparator {
    
    public static class ComparatorVisitor extends ClassCodeVisitorSupport {
        
        /**
         * here, we capture comparisons
         * replace the LHS with a call to the appropriate method in
         * the JDIComparator.  The RHS is replaced with 'false'
         * and the operator is replaced with '||' so that the 
         * binary expression always returns the result of the LHS.
         */
        @Override
        public void visitBinaryExpression(BinaryExpression expression) {
            super.visitBinaryExpression(expression);

            String compareName = JDIComparator.methodNameForToken(expression.getOperation());
            if (compareName != null) {
                MethodCallExpression call = new MethodCallExpression(
                        new VariableExpression("__comparator"), 
                        compareName, 
                        new ArgumentListExpression(expression.getLeftExpression(), expression.getRightExpression()));
                expression.setLeftExpression(call);
                expression.setRightExpression(ConstantExpression.FALSE);
                // arrgh...must use reflection to set the type
                Token t = expression.getOperation();
                t.setText("||");
                t.setMeaning(Types.LOGICAL_OR);
                ReflectionUtils.setPrivateField(Token.class, "type", t, Types.LOGICAL_OR);
            }
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

    }
    
    private final JDITargetDelegate delegate;
    private final IJavaClassType scriptByteCodeAdapter;
    public JDIComparator(JDITargetDelegate delegate) throws DebugException {
        this.delegate = delegate;
        this.scriptByteCodeAdapter = (IJavaClassType) delegate.getType("org.codehaus.groovy.runtime.ScriptBytecodeAdapter");
    }
    
    public boolean isLessThan(Object o1, Object o2) throws DebugException {
        return invoke("compareLessThan", o1, o2);
    }

    public boolean isGreaterThan(Object o1, Object o2) throws DebugException {
        return invoke("compareGreaterThan", o1, o2);
    }
    public boolean isLessThanOrEqual(Object o1, Object o2) throws DebugException {
        return invoke("compareLessThanEqual", o1, o2);
    }
    public boolean isGreaterThanOrEqual(Object o1, Object o2) throws DebugException {
        return invoke("compareGreaterThanEqual", o1, o2);
    }
    public boolean isEqual(Object o1, Object o2) throws DebugException {
        return invoke("compareEqual", o1, o2);
    }
    
    public boolean isNotEqual(Object o1, Object o2) throws DebugException {
        return invoke("compareNotEqual", o1, o2);
    }
    
    public boolean isIdentical(Object o1, Object o2) throws DebugException {
        return invoke("compareIdentical", o1, o2);
    }
    
    public boolean isNotIdentical(Object o1, Object o2) throws DebugException {
        return invoke("compareNotIdentical", o1, o2);
    }
    
    public Integer compareTo(Object o1, Object o2) throws DebugException {
        IJavaValue v1 = delegate.toJDIObject(o1);
        IJavaValue v2 = delegate.toJDIObject(o2);
        IJavaValue result = scriptByteCodeAdapter.sendMessage("compareTo", 
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Integer;", 
                new IJavaValue[] { v1, v2 }, delegate.getThread());
        return delegate.convertToInteger(result);
    }

    private boolean invoke(String methodName, Object o1, Object o2)
    throws DebugException {
        IJavaValue v1 = delegate.toJDIObject(o1);
        IJavaValue v2 = delegate.toJDIObject(o2);
        IJavaPrimitiveValue result = (IJavaPrimitiveValue) scriptByteCodeAdapter.sendMessage(methodName, "(Ljava/lang/Object;Ljava/lang/Object;)Z", new IJavaValue[] { v1, v2 }, delegate.getThread());
        return result.getBooleanValue();
    }

    public static String methodNameForToken(Token operation) {
        switch (operation.getType()) {
            case Types.COMPARE_LESS_THAN:
                return "isLessThan";
            
            case Types.COMPARE_GREATER_THAN:
                return "isGreaterThan";
                
            case Types.COMPARE_LESS_THAN_EQUAL:
                return "isLessThanOrEqual";
                
            case Types.COMPARE_GREATER_THAN_EQUAL:
                return "isGreaterThanOrEqual";
                
            case Types.COMPARE_EQUAL:
                return "isEqual";
                
            case Types.COMPARE_NOT_EQUAL:
                return "isNotEqual";
                
            case Types.COMPARE_IDENTICAL:
                return "isIdentical";
                
            case Types.COMPARE_NOT_IDENTICAL:
                return "isNotIdentical";
                
            case Types.COMPARE_TO:
                return "compareTo";
                
            default:
                // don't convert this token type.
                return null;
        }
    }
}
