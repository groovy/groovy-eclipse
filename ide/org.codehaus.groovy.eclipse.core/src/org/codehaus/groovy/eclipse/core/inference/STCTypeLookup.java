/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 *
 * andrew - Initial API and implementation
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
package org.codehaus.groovy.eclipse.core.inference;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.search.ITypeLookup;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 *
 * @author Andrew Eisenberg
 * @created 2013-02-25
 */
public class STCTypeLookup implements ITypeLookup {

    // only enabled for Groovy 2.1 or greater
    private static final boolean isEnabled = CompilerUtils.getActiveGroovyBundle().getVersion().getMajor() >= 2
            && CompilerUtils.getActiveGroovyBundle().getVersion().getMinor() > -1;

    public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {}

    public TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType) {
        if (!isEnabled) {
            return null;
        }
        Object inferredType = node.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        if (inferredType instanceof ClassNode) {
            ASTNode decl = node;
            if (node instanceof VariableExpression) {
                Variable accessedVariable = ((VariableExpression) node).getAccessedVariable();
                if (accessedVariable instanceof ASTNode) {
                    decl = (ASTNode) accessedVariable;
                }
            } else if (node instanceof FieldExpression) {
                decl = ((FieldExpression) node).getField();
            } else if (node instanceof ClassExpression) {
                decl = node.getType();
            }
            return new TypeLookupResult((ClassNode) inferredType, objectExpressionType, decl, TypeConfidence.INFERRED, scope);
        }
        return null;
    }

    public TypeLookupResult lookupType(FieldNode node, VariableScope scope) {
        if (!isEnabled) {
            return null;
        }
        Object inferredType = node.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        if (inferredType instanceof ClassNode) {
            return new TypeLookupResult((ClassNode) inferredType, node.getDeclaringClass(), node, TypeConfidence.INFERRED, scope);
        }
        return null;
    }

    public TypeLookupResult lookupType(MethodNode node, VariableScope scope) {
        if (!isEnabled) {
            return null;
        }
        Object inferredType = node.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE);
        if (inferredType instanceof ClassNode) {
            return new TypeLookupResult((ClassNode) inferredType, node.getDeclaringClass(), node, TypeConfidence.INFERRED, scope);
        }
        return null;
    }

    public TypeLookupResult lookupType(AnnotationNode node, VariableScope scope) {
        return null;
    }

    public TypeLookupResult lookupType(ImportNode node, VariableScope scope) {
        return null;
    }

    public TypeLookupResult lookupType(ClassNode node, VariableScope scope) {
        return null;
    }

    public TypeLookupResult lookupType(Parameter node, VariableScope scope) {
        return null;
    }
}
