/*
 * Copyright 2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.proposals;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.ui.utils.GroovyResourceUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;

/**
 * Converts a Java resource to a Groovy resource if certain problems are
 * encountered, like ';' at the end of a statement.
 * 
 * @author Nieraj Singh
 * 
 */
public class ConvertToGroovyFileResolver extends AbstractQuickFixResolver {

	public static final String DESCRIPTION = "Convert to Groovy file and open in Groovy editor";

	public ConvertToGroovyFileResolver(QuickFixProblemContext problem) {
		super(problem);
	}

	public static class ConvertToGroovyQuickFix extends
			AbstractGroovyQuickFixProposal {
		public ConvertToGroovyQuickFix(QuickFixProblemContext problem) {
			super(problem);
		}

		protected String getImageBundleLocation() {
			return JavaPluginImages.IMG_CORRECTION_CHANGE;
		}

		public void apply(IDocument document) {
			IResource resource = getQuickFixProblemContext().getResource();
			List<IResource> resources = new ArrayList<IResource>();
			resources.add(resource);
			GroovyResourceUtil.renameFile(GroovyResourceUtil.GROOVY, resources);
		}

		public String getDisplayString() {
			return DESCRIPTION;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.codehaus.groovy.eclipse.quickfix.proposals.AbstractQuickFixResolver
	 * #getDescriptors()
	 */
	protected ProblemType[] getTypes() {
		return new ProblemType[] { ProblemType.MISSING_SEMI_COLON_TYPE, ProblemType.MISSING_SEMI_COLON_TYPE_VARIANT };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixResolver#
	 * getQuickFixProposals()
	 */
	public List<IJavaCompletionProposal> getQuickFixProposals() {
		List<IJavaCompletionProposal> fixes = new ArrayList<IJavaCompletionProposal>();
		fixes.add(new ConvertToGroovyQuickFix(getQuickFixProblem()));
		return fixes;
	}

}
