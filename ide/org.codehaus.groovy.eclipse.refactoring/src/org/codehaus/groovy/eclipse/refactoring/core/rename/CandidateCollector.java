/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.core.rename;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.ITextSelection;

/**
 * Collects Groovy and Java candidates.
 */
public class CandidateCollector {

    protected ITextSelection selection;
    protected GroovyCompilationUnit unit;

    protected ISourceReference refactoringTarget;
    private boolean isValid = true;

    public CandidateCollector(GroovyCompilationUnit unit, ITextSelection selection) {
        this.unit = unit;
        this.selection = selection;
    }

    /**
     * The selected node.  May be null if the selection is not a valid
     * refactoring target.
     */
    public ISourceReference getRefactoringTarget() {
        if (isValid && refactoringTarget == null) {
            try {
                IJavaElement[] element = unit.codeSelect(selection.getOffset(), selection.getLength());
                if (element != null && element.length > 0 && element[0] instanceof ISourceReference) {
                    refactoringTarget = (ISourceReference) element[0];
                } else {
                    isValid = false;
                }
            } catch (JavaModelException e) {
                GroovyCore.logException("Exception finding element at offset " + selection.getOffset() + " in compilation unit " + unit.getElementName(), e);
            }
        }
        return refactoringTarget;
    }
}
