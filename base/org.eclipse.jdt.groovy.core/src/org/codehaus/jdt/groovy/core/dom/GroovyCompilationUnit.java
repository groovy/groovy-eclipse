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
package org.codehaus.jdt.groovy.core.dom;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class GroovyCompilationUnit extends CompilationUnit {

    public GroovyCompilationUnit(AST ast) {
        super(ast);
    }

    /**
     * sneaky... required because we cannot get in the jdt.debug code and change it. Here the plan is that if the debug
     * MethodSearchVisitor is being used we don't find the method. If it was found the body would be empty and the debug code would
     * think the method has not changed across builds - when it may have. It is empty because we don't parse below the method level
     * for groovy.
     *
     */
    @Override
    protected void accept0(ASTVisitor visitor) {
        String visitorName = visitor.getClass().getName();
        if (visitorName.equals("org.eclipse.jdt.internal.debug.core.hcr.MethodSearchVisitor")) {
            // String message =
            // "Cannot correctly answer the findMethod() call for Groovy code whilst debugging, the method AST will be empty";
            // throw new CoreException(new Status(IStatus.CANCEL, Activator.PLUGIN_ID, IStatus.OK, message, null));
            return;
        }
        super.accept0(visitor);
    }

    public List getCommentList() {
        // prevent NullPointerExceptions down-stream
        return super.getCommentList() != null ? super.getCommentList() : Collections.emptyList();
    }
}
