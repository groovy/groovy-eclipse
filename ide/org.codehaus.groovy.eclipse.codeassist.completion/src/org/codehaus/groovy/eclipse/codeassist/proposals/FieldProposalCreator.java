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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.objectweb.asm.Opcodes;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 * 
 * GRECLIPSE-512: most fields have properties created for them, so we want to 
 * avoid duplicate proposals.
 * Constants are an exception, so return them here.
 * I may have missed something, so be prepared to add more kinds of fields here.
 */
public class FieldProposalCreator extends AbstractProposalCreator implements IProposalCreator {
    
    private static final GroovyFieldProposal CLASS_PROPOSAL = createClassProposal();

    private static GroovyFieldProposal createClassProposal() {
        FieldNode field = new FieldNode("class", Opcodes.ACC_PUBLIC & Opcodes.ACC_STATIC & Opcodes.ACC_FINAL, VariableScope.CLASS_CLASS_NODE, VariableScope.OBJECT_CLASS_NODE, null);
        field.setDeclaringClass(VariableScope.OBJECT_CLASS_NODE);
        return new GroovyFieldProposal(field);
    }
    
    public List<IGroovyProposal> findAllProposals(ClassNode type,
            Set<ClassNode> categories, String prefix, boolean isStatic) {
        Collection<FieldNode> allFields = getAllConstants(type);
        List<IGroovyProposal> groovyProposals = new LinkedList<IGroovyProposal>();
        for (FieldNode field : allFields) {
            if ((!isStatic || field.isStatic()) &&
                    ProposalUtils.looselyMatches(prefix, field.getName())) {
                groovyProposals.add(new GroovyFieldProposal(field));
            }
        }
        
        if (isStatic && "class".startsWith(prefix)) {
            groovyProposals.add(CLASS_PROPOSAL);
        }
        
        return groovyProposals;
    }
    
    private Collection<FieldNode> getAllConstants(ClassNode thisType) {
        Set<ClassNode> types = new HashSet<ClassNode>();
        getAllSupers(thisType, types);
        List<FieldNode> allFields = new LinkedList<FieldNode>();
        for (ClassNode type : types) {
            type = type.redirect();
            if (type instanceof JDTClassNode) {
                for (FieldNode field : type.getFields()) {
                    if (checkName(field.getName()) && checkModifiers(field)) {
                        allFields.add(field);
                    }
                }
            }
        }
        return allFields;
    }

    /**
     * @param field
     * @return
     */
    private boolean checkModifiers(FieldNode field) {
        return (field.getModifiers() & (Opcodes.ACC_STATIC | Opcodes.ACC_FINAL)) != 0;
    }

}