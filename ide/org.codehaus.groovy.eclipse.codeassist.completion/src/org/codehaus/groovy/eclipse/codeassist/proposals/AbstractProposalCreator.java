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

import java.util.Set;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 *
 */
public abstract class AbstractProposalCreator implements IProposalCreator {

    /**
     * The type of the LHS of the assignment statement associated with this
     * invocation or null if there is none
     */
    protected ClassNode lhsType;

    protected VariableScope currentScope;

    protected boolean checkName(String name) {
        return name.charAt(0) != '<' && !name.contains("$");
    }

    protected void getAllSupers(ClassNode type, Set<ClassNode> set) {
        if (type == null) {
            return;
        }
        set.add(type);
        getAllSupers(type.getSuperClass(), set);
        for (ClassNode inter : (Iterable<ClassNode>) type.getAllInterfaces()) {
            if (! inter.getName().equals(type.getName())) {
                getAllSupers(inter, set);
            }
        }
    }

    protected boolean isInterestingType(ClassNode type) {
        return lhsType != null
                && ClassHelper.getUnwrapper(type).equals(lhsType);
    }

    protected void getAllSupersAsStrings(ClassNode type, Set<String> set) {
        if (type == null) {
            return;
        }
        set.add(type.getName());
        getAllSupersAsStrings(type.getSuperClass(), set);
        for (ClassNode inter : (Iterable<ClassNode>) type.getAllInterfaces()) {
            if (! inter.getName().equals(type.getName())) {
                getAllSupersAsStrings(inter, set);
            }
        }
    }

    public void setLhsType(ClassNode lhsType) {
        this.lhsType = lhsType;
    }

    public void setCurrentScope(VariableScope currentScope) {
        this.currentScope = currentScope;
    }
}