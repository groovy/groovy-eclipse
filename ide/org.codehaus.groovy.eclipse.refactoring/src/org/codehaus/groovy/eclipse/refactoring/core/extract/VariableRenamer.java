/*
 * Copyright 2003-2010 the original author or authors.
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

import java.util.Map;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 *
 * @author andrew
 * @created May 18, 2010
 */
public class VariableRenamer extends ClassCodeVisitorSupport {

    private MultiTextEdit edits;

    private Map<String, String> variablesToRename;

    public VariableRenamer() {
        edits = new MultiTextEdit();
    }

    /**
     * @param method
     * @param variablesToRename
     * @return
     */
    public MultiTextEdit rename(MethodNode method, Map<String, String> variablesToRename) {
        this.variablesToRename = variablesToRename;
        // no need to visit parameters since they have already been changed
        method.getCode().visit(this);
        return edits;
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        Variable accessedVar = expression.getAccessedVariable();
        // look for dynamic variables since the parameters already have the
        // new names, the actual references to the parameters are using the
        // old names
        if (accessedVar instanceof DynamicVariable) {
            String newName = variablesToRename.get(accessedVar.getName());
            if (newName != null) {
                edits.addChild(new ReplaceEdit(expression.getStart(), expression.getLength(), newName));
            }
        }
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }
}
