/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.refactoring.core.rename;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringSearchEngine;
import org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup;
import org.eclipse.jdt.internal.corext.refactoring.base.ReferencesInBinaryContext;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

public class MethodVariantRenameParticipant extends RenameParticipant {

    private IMethod target;
    private Map<ICompilationUnit, List<SearchMatch>> matches;

    @Override
    public String getName() {
        return "Rename Groovy method variant references.";
    }

    @Override
    protected boolean initialize(final Object element) {
        target = (IMethod) element;
        if (getArguments().getUpdateReferences()) {
            try {
                // must be method with default value(s)
                ISourceRange range = target.getNameRange();
                ICompilationUnit unit = target.getCompilationUnit();
                if (!target.isBinary() && !target.isReadOnly() && range.getOffset() > 0 && unit instanceof GroovyCompilationUnit) {
                    Region region = new Region(range.getOffset(), range.getLength()); // find associated Groovy AST node
                    ASTNode method = new ASTNodeFinder(region).doVisit(((GroovyCompilationUnit) unit).getModuleNode());
                    if (method instanceof MethodNode && ((MethodNode) method).getOriginal().hasDefaultValue()) {
                        return true;
                    }
                }
            } catch (JavaModelException e) {
                GroovyCore.logException(e.getLocalizedMessage(), e);
            }
        }
        return false;
    }

    @Override
    public RefactoringStatus checkConditions(final IProgressMonitor pm, final CheckConditionsContext context) {
        RefactoringStatus status = new RefactoringStatus();
        SubMonitor submon = SubMonitor.convert(pm, "Searching for Groovy method variant references", 13);
        try {
            SearchPattern pattern = null;
            for (IMethod method : target.getDeclaringType().getMethods()) {
                if (!method.equals(target) && method.getNameRange().equals(target.getNameRange())) {
                    if (pattern == null) {
                        pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
                    } else {
                        pattern = SearchPattern.createOrPattern(pattern, SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES));
                    }
                }
            }
            submon.worked(1);

            SearchResultGroup[] groups = RefactoringSearchEngine.search(pattern, SearchEngine.createWorkspaceScope(), submon.split(10), status);
            groups = Checks.excludeCompilationUnits(groups, status); // exclude non-parsable compilation units
            status.merge(Checks.checkCompileErrorsInAffectedFiles(groups));
            matches = new HashMap<>();
            submon.worked(1);

            ReferencesInBinaryContext binaryReferences = new ReferencesInBinaryContext("Binary references to method '" + target.getElementName() + "'");
            for (SearchResultGroup group : groups) {
                for (SearchMatch match : group.getSearchResults()) {
                    if (match.getAccuracy() == SearchMatch.A_ACCURATE) {
                        if (((IMethod) match.getElement()).isBinary()) {
                            binaryReferences.add(match);
                        } else {
                            matches.computeIfAbsent(group.getCompilationUnit(), x -> new ArrayList<>()).add(match);
                        }
                    }
                }
            }
            binaryReferences.addErrorIfNecessary(status);

            submon.worked(1);
        } catch (CoreException e) {
            status.merge(RefactoringStatus.createFatalErrorStatus(e.getLocalizedMessage()));
        }
        return status;
    }

    @Override
    public Change createChange(final IProgressMonitor pm) throws CoreException {
        CompositeChange change = new CompositeChange(getName());
        SubMonitor submon = SubMonitor.convert(pm, matches.size());
        for (Map.Entry<ICompilationUnit, List<SearchMatch>> entry : matches.entrySet()) {
            CompilationUnitChange edits = Optional.ofNullable((CompilationUnitChange) getTextChange(entry.getKey())).orElseGet(() -> {
                CompilationUnitChange cuc = new CompilationUnitChange(getName(), entry.getKey());
                cuc.setEdit(new MultiTextEdit());
                change.add(cuc);
                return cuc;
            });
            for (SearchMatch match : entry.getValue()) {
                edits.addEdit(new ReplaceEdit(match.getOffset(), target.getElementName().length(), getArguments().getNewName()));
            }
            submon.worked(1);
        }
        matches = null;
        target = null;
        return change;
    }
}
