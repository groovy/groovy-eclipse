/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.groovy.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import groovy.lang.Closure;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;

/**
 * A simplified type lookup that targets the general case where a provider wants to add
 * initialization to a class and add new methods or fields to certain types of objects.
 */
public abstract class AbstractSimplifiedTypeLookup implements ITypeLookupExtension {

    private Boolean isStatic;
    private Expression currentExpression;

    /**
     * Gives an option for descendants to set confidence by their own
     */
    protected TypeConfidence checkConfidence(Expression node, TypeConfidence originalConfidence, ASTNode declaration, String extraDoc) {
        return (originalConfidence == null ? confidence() : originalConfidence);
    }

    /**
     * @return the confidence level of lookup results for this type lookup. Defaults to {@link TypeConfidence#LOOSELY_INFERRED}
     */
    protected TypeConfidence confidence() {
        return TypeConfidence.LOOSELY_INFERRED;
    }

    /**
     * @return the expression AST node that is currently being inferred.
     */
    protected Expression getCurrentExpression() {
        return currentExpression;
    }

    /**
     * @return the variable AST node if declared within current or enclosing scope
     */
    protected Variable getDeclaredVariable(String name, VariableScope scope) {
        Variable var = null;
        VariableScope.VariableInfo info = scope.lookupName(name);
        if (info != null) {
            org.codehaus.groovy.ast.VariableScope groovyScope = null;
            if (info.scopeNode instanceof MethodNode) {
                groovyScope = ((MethodNode) info.scopeNode).getVariableScope();
            } else if (info.scopeNode instanceof ForStatement) {
                groovyScope = ((ForStatement) info.scopeNode).getVariableScope();
            } else if (info.scopeNode instanceof BlockStatement) {
                groovyScope = ((BlockStatement) info.scopeNode).getVariableScope();
            } else if (info.scopeNode instanceof ClosureExpression) {
                groovyScope = ((ClosureExpression) info.scopeNode).getVariableScope();
            } else if (info.scopeNode instanceof ClosureListExpression) {
                groovyScope = ((ClosureListExpression) info.scopeNode).getVariableScope();
            }
            while (groovyScope != null && (var = groovyScope.getDeclaredVariable(name)) == null) {
                groovyScope = groovyScope.getParent();
            }
        }
        return var;
    }

    /**
     * @return true iff the current expression being inferred is a quoted string
     */
    protected boolean isQuotedString() {
        return (currentExpression instanceof GStringExpression || (currentExpression instanceof ConstantExpression &&
            (currentExpression.getEnd() < 1 || currentExpression.getLength() != currentExpression.getText().length())));
    }

    /**
     * @return true iff the current lookup is in a static scope
     */
    protected boolean isStatic() {
        return isStatic.booleanValue();
    }

    @Override
    public final TypeLookupResult lookupType(Expression expression, VariableScope scope, ClassNode objectExpressionType, boolean isStaticObjectExpression) {
        boolean isCtorCall = (expression instanceof ConstructorCallExpression);
        if (isCtorCall || (expression instanceof VariableExpression) || (expression instanceof ConstantExpression &&
                                                                        (expression.getEnd() < 1 || expression.getLength() == expression.getText().length()))) {
            String name = (isCtorCall ? "<init>" : expression.getText());

            Variable variable = getDeclaredVariable(name, scope);
            if (variable != null && !variable.isDynamicTyped()) {
                return null; // var type is explicitly declared
            }

            List<TypeAndScope> declaringTypes;
            if (isCtorCall) {
                declaringTypes = Collections.singletonList(new TypeAndScope(expression.getType(), scope));
            } else if (objectExpressionType != null) {
                declaringTypes = Collections.singletonList(new TypeAndScope(objectExpressionType, scope));
            } else {
                declaringTypes = new ArrayList<>(); // implicit "this" candidates
                TypeAndScope.populate(declaringTypes, scope, scope.getEnclosingClosureResolveStrategy());
            }

            try {
                // I would have liked to pass these values into lookupTypeAndDeclaration, but I can't break API here...
                currentExpression = expression;
                for (TypeAndScope pair : declaringTypes) {
                    ClassNode declaringType = pair.declaringType;
                    if (declaringType.isUsingGenerics() && declaringType.equals(VariableScope.CLASS_CLASS_NODE)) {
                        declaringType = declaringType.getGenericsTypes()[0].getType(); isStatic = Boolean.TRUE;
                    } else {
                        isStatic = Boolean.valueOf(isStaticObjectExpression);
                    }

                    TypeAndDeclaration result = lookupTypeAndDeclaration(declaringType, name, pair.variableScope);
                    if (result != null) {
                        TypeConfidence confidence = checkConfidence(expression, result.confidence, result.declaration, result.extraDoc);
                        return new TypeLookupResult(result.type, result.declaringType != null ? result.declaringType : declaringType, result.declaration, confidence, pair.variableScope, result.extraDoc);
                    }
                }
            } finally {
                currentExpression = null; isStatic = null;
            }
        }
        return null;
    }

    /**
     * Clients should return a {@link TypeAndDeclaration} corresponding to an additional
     *
     * @return the type and declaration corresponding to the name in the given declaring type. The declaration may be null, but this
     *         should be avoided in that it prevents the use of navigation and of javadoc hovers
     */
    protected abstract TypeAndDeclaration lookupTypeAndDeclaration(ClassNode declaringType, String name, VariableScope scope);

    public static class TypeAndDeclaration {
        protected final ClassNode type;
        protected final ASTNode declaration;
        protected final ClassNode declaringType;
        protected final String extraDoc;
        protected final TypeConfidence confidence;

        public TypeAndDeclaration(ClassNode type, ASTNode declaration, ClassNode declaringType, String extraDoc, TypeConfidence confidence) {
            this.type = type;
            this.declaration = declaration;
            this.declaringType = declaringType;
            this.extraDoc = extraDoc;
            this.confidence = confidence;
        }

        public TypeAndDeclaration(ClassNode type, ASTNode declaration, ClassNode declaringType, String extraDoc) {
            this(type, declaration, declaringType, extraDoc, null);
        }

        public TypeAndDeclaration(ClassNode type, ASTNode declaration, ClassNode declaringType) {
            this(type, declaration, declaringType, null);
        }

        public TypeAndDeclaration(ClassNode type, ASTNode declaration) {
            this(type, declaration, null);
        }
    }

    private static class TypeAndScope {
        final ClassNode declaringType;
        final VariableScope variableScope;

        private TypeAndScope(ClassNode declaringType, VariableScope variableScope) {
            this.declaringType = Objects.requireNonNull(declaringType);
            this.variableScope = Objects.requireNonNull(variableScope);
        }

        private static void populate(List<TypeAndScope> types, VariableScope scope, int resolveStrategy) {
            if (resolveStrategy == Closure.DELEGATE_FIRST || resolveStrategy == Closure.DELEGATE_ONLY) {
                ClassNode delegate = scope.getDelegate();
                types.add(new TypeAndScope(delegate, scope));
            }
            if (resolveStrategy < Closure.DELEGATE_ONLY) {
                ClassNode owner = scope.getOwner();
                if (owner != null) {
                    VariableScope outer = owner.getNodeMetaData("outer.scope");
                    if (outer != null) { // owner is an enclosing closure
                        VariableScope.CallAndType cat = outer.getEnclosingMethodCallExpression();
                        populate(types, outer, (cat == null ? 0 : cat.getResolveStrategy(outer.getEnclosingClosure())));
                    } else {
                        types.add(new TypeAndScope(owner, scope));
                    }
                } else if ((owner = scope.getThis()) != null) {
                    types.add(new TypeAndScope(owner, scope));
                }
                if (resolveStrategy < Closure.DELEGATE_FIRST && scope.getEnclosingClosure() != null) {
                    ClassNode delegate = scope.getDelegate();
                    if (delegate != null && !delegate.equals(owner))
                        types.add(new TypeAndScope(delegate, scope));
                }
            }
            if (resolveStrategy <= Closure.TO_SELF && (resolveStrategy > 0 || scope.getEnclosingClosure() != null)) {
                types.add(new TypeAndScope(VariableScope.CLOSURE_CLASS_NODE, scope));
            }
        }
    }
}
