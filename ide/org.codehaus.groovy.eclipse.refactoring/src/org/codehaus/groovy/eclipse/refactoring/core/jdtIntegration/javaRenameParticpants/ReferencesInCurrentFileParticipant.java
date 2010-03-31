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
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRenameParticpants;

import java.util.List;

import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.eclipse.refactoring.Activator;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * Finds all Groovy references to the current rename in the current file
 * @author andrew
 *
 */
public class ReferencesInCurrentFileParticipant extends RenameParticipant {
    
    private IMember target;

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm,
            CheckConditionsContext context) throws OperationCanceledException {
        return new RefactoringStatus();
    }
    
    private boolean isEnabled() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        return store.getBoolean(PreferenceConstants.GROOVY_REFACTORING_ENABLED);
    }


    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {
        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor((GroovyCompilationUnit) target.getCompilationUnit());
        IRefactoringChangeRequestor requestor;
        switch (target.getElementType()) {
            case IJavaElement.FIELD:
                requestor = new RefactoringFieldReferenceSearchRequestor((IField) target);
                break;
            case IJavaElement.METHOD:
//                requestor = new RefactoringMethodReferenceSearchRequestor((IMethod) target);
//                break;
            case IJavaElement.TYPE:
//                requestor = new RefactoringTypeReferenceSearchRequestor((IType) target);
//                break;

            default:
                requestor = null;
                break;
        }
        if (requestor != null) {
            visitor.visitCompilationUnit(requestor);
            List<ITextSelection> matchLocations = requestor.getMatchLocations();
            return createChange(matchLocations);
        }
        return new CompositeChange("No extra Groovy changes");
    }
    
    private TextFileChange createChange(List<ITextSelection> matchLocations) {
        TextFileChange change = new TextFileChange("Extra Groovy changes", (IFile) target.getResource());
        MultiTextEdit multiEdit = new MultiTextEdit();
        change.setEdit(multiEdit);
        for (ITextSelection matchLocation : matchLocations) {
            multiEdit.addChild(new ReplaceEdit(matchLocation.getOffset(), 
                    matchLocation.getLength(), getArguments().getNewName()));
        }
        return change;
    }


    @Override
    public String getName() {
        return "Find Groovy references in current file participant";
    }

    /**
     * Only initialized if the element is an IJavaElement in 
     * a GroovyCompilationUnit.
     */
    @Override
    protected boolean initialize(Object element) {
        if (isEnabled() && 
                getArguments().getUpdateReferences() && 
                element instanceof IMember) {
            this.target = (IMember) element;
            if (target.getAncestor(IJavaElement.COMPILATION_UNIT) instanceof GroovyCompilationUnit) {
                return true;
            }
        }
        return false;
    }

}
