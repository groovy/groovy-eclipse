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
package org.codehaus.groovy.eclipse.refactoring.actions;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.fix.AbstractCleanUp;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public abstract class AbstractGroovyCleanUp extends AbstractCleanUp {

    protected RefactoringStatus status;

    @Override
    public RefactoringStatus checkPreConditions(IJavaProject project,
            ICompilationUnit[] compilationUnits, IProgressMonitor monitor)
            throws CoreException {

        RefactoringStatus status = new RefactoringStatus();

        try {
            for (ICompilationUnit unit : compilationUnits) {
                if (!(unit instanceof GroovyCompilationUnit)) {
                    status.addError("Cannot use cleanup on a non-groovy compilation unit: " + unit.getElementName());
                } else if (((GroovyCompilationUnit) unit).getModuleNode() == null) {
                    status.addError("Cannot find module node for compilation unit: " + unit.getElementName());
                }
            }
        } catch (Exception e) {
            GroovyCore.logException("Cannot perform cleanup.", e);
            status.addFatalError("Cannot perform cleanup. See error log. " + e.getMessage());
        }

        return status;
    }

    @Override
    public RefactoringStatus checkPostConditions(IProgressMonitor monitor) throws CoreException {
        try {
            if (status == null || status.isOK()) {
                return super.checkPostConditions(monitor);
            } else {
                return status;
            }
        } finally {
            status = null;
        }
    }
}
