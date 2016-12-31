/*
 * Copyright 2009-2016 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.eclipse.jdt.internal.ui.fix.ImportsCleanUp;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.CleanUpRequirements;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

public class GroovyImportsCleanUp extends AbstractGroovyCleanUp {

    private ImportsCleanUp javaCleanUp = new ImportsCleanUp(Collections.singletonMap(CleanUpConstants.ORGANIZE_IMPORTS, CleanUpOptions.TRUE));

    public CleanUpRequirements getRequirements() {
        return javaCleanUp.getRequirements();
    }

    public String[] getStepDescriptions() {
        return javaCleanUp.getStepDescriptions();
    }

    public RefactoringStatus checkPreConditions(IJavaProject project, ICompilationUnit[] compilationUnits, IProgressMonitor monitor) throws CoreException {
        List<ICompilationUnit> groovyUnits = new ArrayList<ICompilationUnit>(compilationUnits.length);
        //List<ICompilationUnit> otherUnits = new ArrayList<ICompilationUnit>(compilationUnits.length);
        for (ICompilationUnit unit : compilationUnits) {
            if (unit instanceof GroovyCompilationUnit) {
                groovyUnits.add(unit);
            }/* else {
                otherUnits.add(unit);
            }*/
        }
        RefactoringStatus groovyStatus = super.checkPreConditions(project, groovyUnits.toArray(new ICompilationUnit[groovyUnits.size()]), monitor);
        //RefactoringStatus otherStatus = javaCleanUp.checkPreConditions(project, otherUnits.toArray(new ICompilationUnit[otherUnits.size()]), monitor);
                javaCleanUp.checkPreConditions(project, new ICompilationUnit[0], monitor);
        return groovyStatus;
    }

    public RefactoringStatus checkPostConditions(IProgressMonitor monitor) throws CoreException {
        return javaCleanUp.checkPostConditions(monitor);
    }

    public ICleanUpFix createFix(CleanUpContext context) throws CoreException {
        ICompilationUnit unit = context.getCompilationUnit();
        if (!(unit instanceof GroovyCompilationUnit)) {
            return javaCleanUp.createFix(context);
        }

        final boolean hasAmbiguity[] = new boolean[] {false};
        IChooseImportQuery query = new IChooseImportQuery() {
            public TypeNameMatch[] chooseImports(TypeNameMatch[][] openChoices, ISourceRange[] ranges) {
                hasAmbiguity[0] = true;
                return new TypeNameMatch[0];
            }
        };

        OrganizeGroovyImports op = new OrganizeGroovyImports((GroovyCompilationUnit) unit, query);
        final TextEdit edit = op.calculateMissingImports();
        if (status == null) {
            status = new RefactoringStatus();
        }
        if (hasAmbiguity[0]) {
            status.addInfo(Messages.format(ActionMessages.OrganizeImportsAction_multi_error_unresolvable, getLocationString(unit)));
        } else if (edit == null) {
            status.addInfo(Messages.format(ActionMessages.OrganizeImportsAction_multi_error_parse, getLocationString(unit)));
        }

        if (edit == null || (edit instanceof MultiTextEdit && edit.getChildrenSize() == 0)) {
            return null;
        }
        return new ImportsFix(edit, unit, FixMessages.ImportsFix_OrganizeImports_Description);
    }

    private static String getLocationString(ICompilationUnit unit) {
        return BasicElementLabels.getPathLabel(unit.getPath(), false);
    }
}
