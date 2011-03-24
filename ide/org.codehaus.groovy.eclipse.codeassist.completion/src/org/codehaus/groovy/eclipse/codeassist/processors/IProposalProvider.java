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

package org.codehaus.groovy.eclipse.codeassist.processors;

import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;

/**
 * Use this class along with the completionProposalProvider extension point
 * to plug into Groovy-Eclipse's completion proposal support
 * @author Andrew Eisenberg
 * @created Nov 23, 2009
 */
public interface IProposalProvider {
    /**
     * Provider should add all of the extra proposals available at the particular context
     * @param context never null.
     * @param completionType  might be null if the type requestor turned up no answer
     * @param isStatic defaults to false if type requestor turned up no answer
     * @param categories null if type requestor turned up no answer, otherwise always contains DGM, and will also contain all other declared categories
     * @return
     */
	List<IGroovyProposal> getStatementAndExpressionProposals(ContentAssistContext context, ClassNode completionType, boolean isStatic, Set<ClassNode> categories);

	/**
	 * Respond with all new methods possible at this context.  Will only be called when the
	 * location is {@link ContentAssistLocation#SCRIPT} or {@link ContentAssistLocation#CLASS_BODY}
	 * @param context
	 * @return
	 */
	List<MethodNode> getNewMethodProposals(ContentAssistContext context);
    /**
     * Respond with all new fields possible at this context (only their names).  Will only be called when the
     * location is {@link ContentAssistLocation#SCRIPT} or {@link ContentAssistLocation#CLASS_BODY}
     * @param context
     * @return
     */
	List<String> getNewFieldProposals(ContentAssistContext context);

    /**
     * Append this to a field name when returning values in
     * {@link #getNewFieldProposals(ContentAssistContext)} so that the field declaration will be
     * invoked as non-static with a 'def' keyword.
     */
    String NONSTATIC_FIELD = "NONSTATIC ";
}
