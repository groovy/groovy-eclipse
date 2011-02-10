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
package org.codehaus.groovy.eclipse.dsl.contributions;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup.TypeAndDeclaration;

/**
 * Represents a single contributed element
 * @author andrew
 * @created Nov 17, 2010
 */
public interface IContributionElement {
    String GROOVY_DSL_PROVIDER = "Groovy DSL";
    String NO_DOC = "Provided by ";

    
    IGroovyProposal toProposal(ClassNode declaringType, ResolverCache resolver);
    TypeAndDeclaration lookupType(String name, ClassNode declaringType, ResolverCache resolver);
    String contributionName();
}
