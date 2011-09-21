/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;

public class GroovyQuickAssist extends GroovyContentProcessor implements
		IQuickAssistProcessor {

	public boolean hasAssists(IInvocationContext context) throws CoreException {
		if (context != null
				&& isContentInGroovyProject(context.getCompilationUnit())) {
			return new AddSuggestionsQuickAssistProposal(context)
					.hasProposals();
		}
		return false;
	}

	public IJavaCompletionProposal[] getAssists(IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {
		if (!hasAssists(context)) {
			return null;
		}
		List<IJavaCompletionProposal> proposalList = new ArrayList<IJavaCompletionProposal>();

		IJavaCompletionProposal javaProposal = new AddSuggestionsQuickAssistProposal(
				context).convertToJavaCompletionProposal();
		proposalList.add(javaProposal);
		return proposalList.toArray(new IJavaCompletionProposal[] {});
	}

}
