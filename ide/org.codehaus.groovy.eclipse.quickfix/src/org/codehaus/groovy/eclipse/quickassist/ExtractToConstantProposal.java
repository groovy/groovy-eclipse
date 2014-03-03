/*
 * Copyright 2014 SpringSource, a division of Pivotal Software, Inc
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

import org.codehaus.groovy.eclipse.refactoring.core.extract.ExtractGroovyConstantRefactoring;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;

/**
 * Quick Assist for extracting expression to a constant. Delegates the logic to {@link ExtractGroovyConstantRefactoring}
 *   
 * @author Alex Boyko
 *
 */
@SuppressWarnings("restriction")
public class ExtractToConstantProposal extends TextRefactoringProposal {
	
	public ExtractToConstantProposal(IInvocationContext context) {
		super(context, new ExtractGroovyConstantRefactoring(
				(GroovyCompilationUnit) context.getCompilationUnit(),
				context.getSelectionOffset(), context.getSelectionLength()));
		ExtractGroovyConstantRefactoring extractToConstantRefactoring = (ExtractGroovyConstantRefactoring) refactoring;
		extractToConstantRefactoring.setConstantName(extractToConstantRefactoring.guessConstantName());
		extractToConstantRefactoring.setVisibility(JdtFlags.VISIBILITY_STRING_PACKAGE);
		extractToConstantRefactoring.setReplaceAllOccurrences(false);
	}

	public ExtractToConstantProposal(IInvocationContext context, boolean all) {
		this(context);
		ExtractGroovyConstantRefactoring extractToConstantRefactoring = (ExtractGroovyConstantRefactoring) refactoring;
		extractToConstantRefactoring.setReplaceAllOccurrences(all);
	}

	@Override
	public String getAdditionalProposalInfo() {
		try {
			return ((ExtractGroovyConstantRefactoring)refactoring).getConstantSignaturePreview();
		} catch (JavaModelException e) {
			return getDisplayString();
		}
	}
	
	@Override
	protected String getImageBundleLocation() {
		return JavaPluginImages.IMG_CORRECTION_LOCAL;
	}

}
