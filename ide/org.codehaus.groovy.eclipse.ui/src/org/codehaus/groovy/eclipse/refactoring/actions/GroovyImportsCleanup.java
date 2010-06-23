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

package org.codehaus.groovy.eclipse.refactoring.actions;

import java.util.Map;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.FixMessages;
import org.eclipse.jdt.internal.corext.fix.ImportsFix;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.fix.AbstractCleanUp;
import org.eclipse.jdt.internal.ui.fix.MultiFixMessages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Andrew Eisenberg
 * @created Aug 17, 2009
 *
 */
public class GroovyImportsCleanup extends AbstractCleanUp {

    private RefactoringStatus fStatus;

    public GroovyImportsCleanup(Map options) {
        super(options);
    }

    public GroovyImportsCleanup() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CleanUpRequirements getRequirements() {
        return new CleanUpRequirements(false, false, false, null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ICleanUpFix createFix(CleanUpContext context) throws CoreException {

        ICompilationUnit unit = context.getCompilationUnit();
        if (! (unit instanceof GroovyCompilationUnit)) {
            return null;
        }

        final boolean hasAmbiguity[]= new boolean[] { false };
        IChooseImportQuery query= new IChooseImportQuery() {
            public TypeNameMatch[] chooseImports(TypeNameMatch[][] openChoices, ISourceRange[] ranges) {
                hasAmbiguity[0]= true;
                return new TypeNameMatch[0];
            }
        };

        OrganizeGroovyImports op= new OrganizeGroovyImports((GroovyCompilationUnit) unit, query);
        final TextEdit edit= op.calculateMissingImports();
        if (hasAmbiguity[0]) {
            fStatus.addInfo(Messages.format(
                    ActionMessages.OrganizeImportsAction_multi_error_unresolvable,
                    getLocationString(unit)));
        } else if (edit == null) {
            fStatus.addInfo(Messages.format(ActionMessages.OrganizeImportsAction_multi_error_parse, getLocationString(unit)));
        }

        if (edit == null || (edit instanceof MultiTextEdit && edit.getChildrenSize() == 0)) {
            return null;
        }
        return new ImportsFix(edit, unit, FixMessages.ImportsFix_OrganizeImports_Description);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefactoringStatus checkPreConditions(IJavaProject project, ICompilationUnit[] compilationUnits, IProgressMonitor monitor) throws CoreException {

        if (isEnabled(CleanUpConstants.ORGANIZE_IMPORTS)) {
            fStatus= new RefactoringStatus();
        }

        return super.checkPreConditions(project, compilationUnits, monitor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefactoringStatus checkPostConditions(IProgressMonitor monitor) throws CoreException {
        try {
            if (fStatus == null || fStatus.isOK()) {
                return super.checkPostConditions(monitor);
            } else {
                return fStatus;
            }
        } finally {
            fStatus= null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getStepDescriptions() {
        if (isEnabled(CleanUpConstants.ORGANIZE_IMPORTS))
            return new String[] {MultiFixMessages.ImportsCleanUp_OrganizeImports_Description};

        return null;
    }

    private static String getLocationString(final ICompilationUnit cu) {
        return BasicElementLabels.getPathLabel(cu.getPath(), false);
    }
}
