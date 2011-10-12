/*
 * Copyright 2009-2011 the original author or authors.
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

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery;
import org.eclipse.jdt.internal.corext.fix.FixMessages;
import org.eclipse.jdt.internal.corext.fix.ImportsFix;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.fix.MultiFixMessages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.cleanup.CleanUpContext;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Andrew Eisenberg
 * @created Aug 17, 2009
 */
public class GroovyImportsCleanUp extends AbstractGroovyCleanUp {

    @Override
    public ICleanUpFix createFix(CleanUpContext context) throws CoreException {
        ICompilationUnit unit = context.getCompilationUnit();
        if (!(unit instanceof GroovyCompilationUnit)) {
            return null;
        }

        final boolean hasAmbiguity[] = new boolean[] { false };
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

    @Override
    public String[] getStepDescriptions() {
        return new String[] { MultiFixMessages.ImportsCleanUp_OrganizeImports_Description };
    }

    private static String getLocationString(final ICompilationUnit unit) {
        return BasicElementLabels.getPathLabel(unit.getPath(), false);
    }
}