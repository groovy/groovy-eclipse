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
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 * FIXADE M2 this class should no longer be necessary when JDTClassNodes properly create property node
 */
public class FieldProposalCreator extends AbstractProposalCreator implements IProposalCreator {

    public List<IGroovyProposal> findAllProposals(ClassNode type,
            Set<ClassNode> categories, String prefix, boolean isStatic) {
        Collection<FieldNode> allFields = getAllFields(type);
        List<IGroovyProposal> groovyProposals = new LinkedList<IGroovyProposal>();
        for (FieldNode field : allFields) {
            if ((!isStatic || field.isStatic()) &&
                    ProposalUtils.looselyMatches(prefix, field.getName())) {
                groovyProposals.add(new GroovyFieldProposal(field));
            }
        }
        
        return groovyProposals;
    }
    
    private Collection<FieldNode> getAllFields(ClassNode thisType) {
        Set<ClassNode> types = new HashSet<ClassNode>();
        getAllSupers(thisType, types);
        List<FieldNode> allFields = new LinkedList<FieldNode>();
        for (ClassNode type : types) {
            // GRECLIPSE-512, JDTClassNodes do not have properties yet
            if (type instanceof JDTClassNode) {
                for (FieldNode field : type.getFields()) {
                    if (checkName(field.getName())) {
                        allFields.add(field);
                    }
                }
            }
        }
        return allFields;
    }

}