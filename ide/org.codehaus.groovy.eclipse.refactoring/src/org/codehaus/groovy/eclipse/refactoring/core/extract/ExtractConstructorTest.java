/*
 * Copyright 2009-2017 the original author or authors.
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

package org.codehaus.groovy.eclipse.refactoring.core.extract;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;

/**
 * Scans a Method if it contains a constructor call (super or this).
 */
public class ExtractConstructorTest extends CodeVisitorSupport {

    private boolean constructorCall = false;

    public boolean containsConstructorCall(MethodNode method) {
        constructorCall = false;
        visitBlockStatement((BlockStatement) method.getCode());
        return constructorCall;
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        constructorCall = true;
        super.visitConstructorCallExpression(call);
    }
}
