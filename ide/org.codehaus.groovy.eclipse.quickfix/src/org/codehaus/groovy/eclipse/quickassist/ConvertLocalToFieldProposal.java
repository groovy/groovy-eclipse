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

import org.codehaus.groovy.eclipse.refactoring.core.extract.ConvertGroovyLocalToFieldRefactoring;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;

/**
 * Quick Assist for extracting a local variable to a field. Delegates the logic to {@link ConvertGroovyLocalToFieldRefactoring}
 *
 * @author Alex Boyko
 */
public class ConvertLocalToFieldProposal extends TextRefactoringProposal {

	public ConvertLocalToFieldProposal(IInvocationContext context) {
		super(context, new ConvertGroovyLocalToFieldRefactoring(
				(GroovyCompilationUnit) context.getCompilationUnit(), context
				.getSelectionOffset(), context
				.getSelectionLength()));
	}

	@Override
	protected String getImageBundleLocation() {
		return JavaPluginImages.IMG_CORRECTION_CHANGE;
	}

}
