/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.tests;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.search.ITypeLookup;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * A simple type lookup that all expressions are of type {@link HTML}.
 */
public class TestTypeLookup implements ITypeLookup {

    @Override
    public TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType) {
        return new TypeLookupResult(ClassHelper.make(javax.swing.text.html.HTML.class), VariableScope.VOID_CLASS_NODE, VariableScope.STRING_CLASS_NODE.getMethod("toString", Parameter.EMPTY_ARRAY), TypeConfidence.LOOSELY_INFERRED, scope);
    }

    @Override
    public TypeLookupResult lookupType(FieldNode node, VariableScope scope) {
        return null;
    }

    @Override
    public TypeLookupResult lookupType(MethodNode node, VariableScope scope) {
        return null;
    }

    @Override
    public TypeLookupResult lookupType(AnnotationNode node, VariableScope scope) {
        return null;
    }

    @Override
    public TypeLookupResult lookupType(ImportNode node, VariableScope scope) {
        return null;
    }

    @Override
    public TypeLookupResult lookupType(ClassNode node, VariableScope scope) {
        return null;
    }

    @Override
    public TypeLookupResult lookupType(Parameter node, VariableScope scope) {
        return null;
    }

    @Override
    public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
        // do nothing
    }
}
