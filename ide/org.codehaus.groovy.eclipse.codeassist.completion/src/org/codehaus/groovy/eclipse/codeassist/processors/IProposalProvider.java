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

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.codeassist.proposals.IGroovyProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;

/**
 * Use this class along with the completionProposalProvider extension point
 * to plug into Groovy-Eclipse's completion proposal support
 * @author Andrew Eisenberg
 * @created Nov 23, 2009
 */
public interface IProposalProvider {
	List<IGroovyProposal> getStatementAndExpressionProposals(ContentAssistContext context);
	List<MethodNode> getNewMethodProposals(ContentAssistContext context);
	List<String> getNewFieldProposals(ContentAssistContext context);
}
