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

package org.codehaus.groovy.eclipse.codeassist.proposals;

import groovyjarjarasm.asm.Opcodes;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.eclipse.jdt.core.ICompilationUnit;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 *
 */
public class GroovyCategoryMethodProposal extends GroovyMethodProposal {

    public GroovyCategoryMethodProposal(MethodNode method) {
        super(method, "Category: " + method.getDeclaringClass().getNameWithoutPackage());
    }

    public GroovyCategoryMethodProposal(MethodNode method, String contributor) {
        super(method, contributor);
    }

    public GroovyCategoryMethodProposal(MethodNode method, String contributor, ProposalFormattingOptions options) {
        super(method, contributor, options);
    }

    @Override
    protected int getModifiers() {
        return method.getModifiers()  & ~Opcodes.ACC_STATIC;  // category methods are defined as static, but should not appear as such when a proposal
    }

    @Override
    protected char[] createMethodSignature() {
        return ProposalUtils.createMethodSignature(method, 1);
    }

    @Override
    protected char[][] createAllParameterNames(ICompilationUnit unit) {
        return removeFirst(super.createAllParameterNames(unit));
    }

    @Override
    protected char[][] getParameterTypeNames(Parameter[] parameters) {
        return removeFirst(super.getParameterTypeNames(parameters));
    }

    private char[][] removeFirst(char[][] array) {
        if (array.length > 0) {
            char[][] newArray = new char[array.length-1][];
            System.arraycopy(array, 1, newArray, 0, array.length-1);
            return newArray;
        } else {
            // shouldn't happen
            return array;
        }
    }
}
