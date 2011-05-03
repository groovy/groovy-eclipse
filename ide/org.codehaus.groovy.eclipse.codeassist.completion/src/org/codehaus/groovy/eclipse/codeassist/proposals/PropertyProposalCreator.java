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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;

/**
 * Consider deleting this class.  As of GRECLIPSE-611, properties are no
 * longer used for content assist proposals, but rather fields are.
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 */
public class PropertyProposalCreator extends AbstractProposalCreator implements IProposalCreator {


    public List<IGroovyProposal> findAllProposals(ClassNode type,
            Set<ClassNode> categories, String prefix, boolean isStatic) {
        List<IGroovyProposal> groovyProposals = new LinkedList<IGroovyProposal>();
        return groovyProposals;
    }
}
