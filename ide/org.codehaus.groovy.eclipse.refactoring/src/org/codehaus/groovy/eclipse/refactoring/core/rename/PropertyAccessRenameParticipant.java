/*
 * Copyright 2009-2023 the original author or authors.
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

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.search.SyntheticAccessorSearchRequestor;
import org.codehaus.groovy.eclipse.refactoring.core.utils.StatusHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup;
import org.eclipse.jdt.internal.corext.refactoring.base.ReferencesInBinaryContext;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextEditChangeGroup;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

/**
 * A rename refactoring participant for renaming synthetic Groovy properties and
 * accessors.  Renames calls to synthetic getters, setters and issers in groovy
 * and java files for groovy properties.  Renames accesses to synthetic groovy
 * properties that are backed by a getter, setter, and/or isser.
 */
public class PropertyAccessRenameParticipant extends RenameParticipant {

    private IMember renameTarget;
    private List<SearchMatch> matches;

    @Override
    public String getName() {
        return "Rename Groovy property getters and setters.";
    }

    @Override
    protected boolean initialize(final Object element) {
        if (element instanceof IMethod || element instanceof IField) {
            renameTarget = (IMember) element;
            if (!renameTarget.isReadOnly()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RefactoringStatus checkConditions(final IProgressMonitor pm, final CheckConditionsContext context) {
        RefactoringStatus status = new RefactoringStatus();
        matches = new ArrayList<>();
        try {
            if (getArguments().getUpdateReferences()) {
                findExtraReferences(SubMonitor.convert(pm, "Finding Groovy property references", 20));
            }
            checkForBinaryRefs(status);
            checkForPotentialMatches(status);
            SearchResultGroup[] groups = convertMatches();
            Checks.excludeCompilationUnits(groups, status);
            status.merge(Checks.checkCompileErrorsInAffectedFiles(groups));
        } catch (CoreException e) {
            GroovyCore.logException(e.getLocalizedMessage(), e);
            return RefactoringStatus.createFatalErrorStatus(e.getLocalizedMessage());
        }
        return status;
    }

    private void findExtraReferences(final IProgressMonitor pm) throws CoreException {
        SyntheticAccessorSearchRequestor requestor = new SyntheticAccessorSearchRequestor();
        requestor.findSyntheticMatches(renameTarget, matches::add, SubMonitor.convert(pm, "Find synthetic property accessors", 10));

        // when searching for an accessor method, also search for pseudo-property uses of the method; "foo.bar" for "getBar()", "isBar()" or "setBar(...)"
        if (renameTarget.getElementType() == IJavaElement.METHOD && renameTarget.getElementName().matches("(?:[gs]et|is)\\p{javaJavaIdentifierPart}+") &&
                ((IMethod) renameTarget).getParameters().length == (renameTarget.getElementName().startsWith("set") ? 1 : 0)) {
            String name = propertyName(renameTarget.getElementName());
            IMethod method = ((IType) renameTarget.getParent()).getMethod(name, ((IMethod) renameTarget).getParameterTypes());
            if (!method.exists()) {
                method = SyntheticAccessorSearchRequestor.syntheticMemberProxy(
                    IMethod.class, method, ((IMethod) renameTarget).getReturnType());

                SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
                SearchParticipant[] searchParticipants = new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
                new SearchEngine().search(pattern, searchParticipants, SearchEngine.createWorkspaceScope(), new SearchRequestor() {
                    @Override
                    public void acceptSearchMatch(final SearchMatch match) throws CoreException {
                        matches.add(match);
                    }
                }, SubMonitor.convert(pm, "Find property-style uses of non-synthetic methods", 10));

                return;
            }
        }
        pm.worked(10);
    }

    private void checkForBinaryRefs(final RefactoringStatus status) throws JavaModelException {
        ReferencesInBinaryContext binaryRefs = new ReferencesInBinaryContext(
            "Elements containing binary references to refactored element ''" + renameTarget.getElementName() + "''");
        for (Iterator<SearchMatch> it = matches.iterator(); it.hasNext();) {
            SearchMatch match = it.next();
            if (isBinaryElement(match.getElement())) {
                if (match.getAccuracy() == SearchMatch.A_ACCURATE) {
                    // binary classpaths are often incomplete -> avoiding false
                    // positives from inaccurate matches
                    binaryRefs.add(match);
                }
                it.remove();
            }
        }
        binaryRefs.addErrorIfNecessary(status);
    }

    private static boolean isBinaryElement(final Object element) throws JavaModelException {
        if (element instanceof IMember) {
            return ((IMember) element).isBinary();
        } else if (element instanceof IClassFile) {
            return false;
        } else if (element instanceof ICompilationUnit) {
            return true;
        } else if (element instanceof IPackageFragment) {
            return isBinaryElement(((IPackageFragment) element).getParent());
        } else if (element instanceof IPackageFragmentRoot) {
            return ((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_BINARY;
        }
        return false;
    }

    private void checkForPotentialMatches(final RefactoringStatus status) {
        for (SearchMatch match : matches) {
            if (match.getAccuracy() == SearchMatch.A_INACCURATE) {
                final RefactoringStatusEntry entry = new RefactoringStatusEntry(RefactoringStatus.WARNING,
                    RefactoringCoreMessages.RefactoringSearchEngine_potential_matches,
                    StatusHelper.createContext(JavaCore.createCompilationUnitFrom((IFile) match.getResource()),
                        new SourceRange(match.getOffset(), match.getLength())));
                status.addEntry(entry);
            }
        }
    }

    private SearchResultGroup[] convertMatches() {
        Map<IResource, SearchResultGroup> groups = new HashMap<>(matches.size());

        for (SearchMatch match : matches) {
            if (match.getResource() != null) {
                groups.computeIfAbsent(match.getResource(), mr -> new SearchResultGroup(mr, new SearchMatch[0])).add(match);
            }
        }

        return groups.values().toArray(new SearchResultGroup[groups.size()]);
    }

    //--------------------------------------------------------------------------

    @Override
    public Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
        CompositeChange change = new CompositeChange(getName());
        Map<String, String> names = getNameMap();
        for (SearchMatch searchMatch : matches) {
            Object el = searchMatch.getElement();
            if (el instanceof IMember || el instanceof IImportDeclaration) {
                String oldName = findMatchName(searchMatch, names.keySet());
                if (oldName != null) {
                    String newName = names.get(oldName);
                    addChange(change, (ICompilationUnit) ((IJavaElement) el).getAncestor(IJavaElement.COMPILATION_UNIT), searchMatch.getOffset(), oldName.length(), newName);
                }
            }
        }
        if (change.getChildren().length > 0) {
            return change;
        }
        return null;
    }

    private void addChange(final CompositeChange finalChange, final ICompilationUnit compilationUnit, final int offset, final int length, final String newName) {
        CompilationUnitChange existingChange = findOrCreateChange(compilationUnit, finalChange);
        TextEditChangeGroup[] groups = existingChange.getTextEditChangeGroups();
        TextEdit occurrenceEdit = new ReplaceEdit(offset, length, newName);

        boolean isOverlapping = false;
        for (TextEditChangeGroup group : groups) {
            if (group.getTextEdits()[0].covers(occurrenceEdit)) {
                isOverlapping = true;
                break;
            }
        }
        if (isOverlapping) {
            // don't step on someone else's feet
            return;
        }
        existingChange.addEdit(occurrenceEdit);
        existingChange.addChangeGroup(new TextEditChangeGroup(existingChange,
            new TextEditGroup("Update synthetic Groovy accessor", occurrenceEdit)));
    }

    private String findMatchName(final SearchMatch searchMatch, final Set<String> keySet) throws JavaModelException {
        IJavaElement element = JavaCore.create(searchMatch.getResource());
        if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
            ICompilationUnit unit = (ICompilationUnit) element;
            String matchedText = unit.getBuffer().getText(searchMatch.getOffset(), searchMatch.getLength());
            for (String oldName : keySet) {
                if (matchedText.startsWith(oldName)) {
                    return oldName;
                }
            }
        }
        return null;
    }

    private CompilationUnitChange findOrCreateChange(final ICompilationUnit compilationUnit, final CompositeChange finalChange) {
        TextChange textChange = getTextChange(compilationUnit);
        CompilationUnitChange existingChange = null;
        if (textChange instanceof CompilationUnitChange) {
            // check to see if change exists from some other part of the
            // refactoring
            existingChange = (CompilationUnitChange) textChange;
        } else {
            // check to see if we have already touched this file
            Change[] children = finalChange.getChildren();
            for (Change change : children) {
                if (change instanceof CompilationUnitChange) {
                    if (((CompilationUnitChange) change).getCompilationUnit().equals(compilationUnit)) {
                        existingChange = (CompilationUnitChange) change;
                        break;
                    }
                }
            }
        }

        if (existingChange == null) {
            // nope...must create a new change
            existingChange = new CompilationUnitChange("Synthetic Groovy Accessor changes for " + compilationUnit.getElementName(), compilationUnit);
            existingChange.setEdit(new MultiTextEdit());
            finalChange.add(existingChange);
        }
        return existingChange;
    }

    private Map<String, String> getNameMap() {
        Map<String, String> nameMap = new HashMap<>(4);
        String newBaseName = propertyName(getArguments().getNewName());
        String oldBaseName = propertyName(renameTarget.getElementName());

        nameMap.put(oldBaseName, newBaseName);
        nameMap.put(accessorName("is", oldBaseName), accessorName("is", newBaseName));
        nameMap.put(accessorName("get", oldBaseName), accessorName("get", newBaseName));
        nameMap.put(accessorName("set", oldBaseName), accessorName("set", newBaseName));

        return nameMap;
    }

    private static String accessorName(final String prefix, final String name) {
        return prefix + org.apache.groovy.util.BeanUtils.capitalize(name);
    }

    private static String propertyName(final String fullName) {
        int prefixLength = 0;
        if (fullName.startsWith("is")) {
            prefixLength = 2;
        } else if (fullName.startsWith("get") || fullName.startsWith("set")) {
            prefixLength = 3;
        }

        if (fullName.length() == prefixLength) {
            return fullName;
        }

        return Introspector.decapitalize(fullName.substring(prefixLength));
    }
}
