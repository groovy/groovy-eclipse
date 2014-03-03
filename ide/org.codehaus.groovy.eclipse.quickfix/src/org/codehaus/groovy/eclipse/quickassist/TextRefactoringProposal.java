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

import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.text.edits.MalformedTreeException;

/**
 * Quick Assist for a refactoring. Delegates the logic to {@link Refactoring} that generates a {@link TextChange}
 *   
 * @author Alex Boyko
 *
 */
public abstract class TextRefactoringProposal extends AbstractGroovyCompletionProposal {
	
	protected Refactoring refactoring;
	
	public TextRefactoringProposal(IInvocationContext context, Refactoring refactoring) {
		super(context);
		this.refactoring = refactoring;
	}

	public void apply(IDocument document) {
		try {
			TextChange change = (TextChange) refactoring.createChange(new NullProgressMonitor());
			change.getEdit().apply(document);
		} catch (CoreException e) {
			GroovyQuickFixPlugin.log(e);
		} catch (MalformedTreeException e) {
			GroovyQuickFixPlugin.log(e);
		} catch (BadLocationException e) {
			GroovyQuickFixPlugin.log(e);
		}
	}

	public String getDisplayString() {
		return refactoring.getName();
	}
	
	@Override
	public boolean hasProposals() {
		try {
			RefactoringStatus status = refactoring.checkAllConditions(new NullProgressMonitor());
			return status.isOK();
		} catch (OperationCanceledException e) {
			GroovyQuickFixPlugin.log(e);
		} catch (CoreException e) {
			GroovyQuickFixPlugin.log(e);
		}
		return false;
	}

}
