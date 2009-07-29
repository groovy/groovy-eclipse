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
package org.codehaus.groovy.eclipse.core.types.impl;

import static org.codehaus.groovy.eclipse.core.types.TypeUtil.newLocalVariable;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContextAware;
import org.codehaus.groovy.eclipse.core.context.impl.ClosureScopeContext;
import org.codehaus.groovy.eclipse.core.context.impl.ConstructorScopeContext;
import org.codehaus.groovy.eclipse.core.context.impl.MethodScopeContext;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.ISymbolTable;
import org.codehaus.groovy.eclipse.core.types.JavaLocalVariable;
import org.codehaus.groovy.eclipse.core.types.LocalVariable;
import org.codehaus.groovy.eclipse.core.types.Type;

/**
 * Groovy local variables symbol table. It is only to be used in a
 * {@link ConstructorScopeContext}, {@link MethodScopeContext}, or
 * {@link ClosureScopeContext}.
 * 
 * @author empovazan
 */
public class GroovyLocalsTable implements ISymbolTable,
        ISourceCodeContextAware, IGroovyProjectAware {
    private GroovyProjectFacade project;

    private ISourceCodeContext context;

    private VariableScope scope;

    public Type lookup(String name) {
        if (context == null && project == null) {
            return null;
        }

        // FUTURE: emp - 'this' and 'super' are not really local variables.
        // There should probably be an 'InstanceType' to parallel 'ClassType'.
        // Wait until a real need for this.
        if (name.equals("this")) {
            return new LocalVariable(((ClassNode) context.getASTPath()[1])
                    .getName(), "this", true);
        }

        if (name.equals("super")) {
            return new LocalVariable(((ClassNode) context.getASTPath()[1])
                    .getSuperClass().getName(), "super", true);
        }

        // Always check declared variable and parents, up to the class scope.
        VariableScope currentScope = scope;
        while (currentScope != null && currentScope.getClassScope() == null) {
            final Variable var = currentScope.getDeclaredVariable(name);
            if (var == null) {
                currentScope = currentScope.getParent();
                continue;
            }
            final String type = var.getType().getName();
            if (!type.equals("java.lang.Object")) {
                // If an array type or a type not in the Groovy project, then
                // make it a Java type.
                if (type.charAt(0) == '['
                        || project.getClassNodeForName(type) == null)
                    return new JavaLocalVariable(type, var.getName());
                new LocalVariable(type, var.getName());
            }
            return newLocalVariable(var);
        }
        return null;
    }

    public void setGroovyProject(GroovyProjectFacade project) {
        this.project = project;
    }

    public void setSourceCodeContext(ISourceCodeContext context) {
        this.context = context;
        // setup access to symbol table here from context.
        ASTNode[] path = context.getASTPath();
        ASTNode astNode = path[path.length - 1];
        if (astNode instanceof MethodNode) {
            astNode = ((MethodNode) astNode).getCode();
        } else if (astNode instanceof ClosureExpression) {
            astNode = ((ClosureExpression) astNode).getCode();
        } else if (astNode instanceof MethodCallExpression) {
            ArgumentListExpression ale = (ArgumentListExpression) ((MethodCallExpression) astNode)
                    .getArguments();
            if (ale.getExpression(0) instanceof ClosureExpression) {
                ClosureExpression cl = (ClosureExpression) ale.getExpression(0);
                astNode = cl.getCode();
            }
        }
        scope = ((BlockStatement) astNode).getVariableScope();
    }
}
