package org.codehaus.groovy.eclipse.refactoring.core.rename;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.search.ISearchRequestor;
import org.codehaus.groovy.eclipse.core.search.SyntheticAccessorSearchRequestor;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.SearchResultGroup;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.base.ReferencesInBinaryContext;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameFieldProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameMethodProcessor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextEditChangeGroup;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

/**
 * A rename refactoring participant for renaming synthetic groovy properties and
 * accessors.
 *
 * Renames calls to synthetic getters, setters and issers in groovy and java
 * files for
 * groovy properties
 *
 * Renames accesses to synthetic groovy properties that are backed by a getter,
 * setter, and/or isser.
 *
 * @author andrew
 * @created Oct 31, 2012
 */
public class SyntheticAccessorsRenameParticipant extends RenameParticipant {

    private IMember renameTarget;

    private List<SearchMatch> matches;

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
        RefactoringStatus status = new RefactoringStatus();

        try {
            if (shouldUpdateReferences()) {
                matches = findExtraReferences(SubMonitor.convert(pm, "Finding synthetic Groovy references", 10));
            } else {
                matches = Collections.emptyList();
            }
            checkForBinaryRefs(matches, status);
            SearchResultGroup[] grouped = convert(matches);
            Checks.excludeCompilationUnits(grouped, status);
            status.merge(Checks.checkCompileErrorsInAffectedFiles(grouped));
            checkForPotentialRefs(matches, status);
        } catch (CoreException e) {
            GroovyCore.logException(e.getLocalizedMessage(), e);
            return RefactoringStatus.createFatalErrorStatus(e.getLocalizedMessage());
        }


        return status;
    }

    private boolean shouldUpdateReferences() {
        RefactoringProcessor processor = getProcessor();
        if (processor instanceof RenameFieldProcessor) {
            return ((RenameFieldProcessor) processor).getUpdateReferences();
        } else if (processor instanceof RenameMethodProcessor) {
            return ((RenameMethodProcessor) processor).getUpdateReferences();
        }
        // shouldn't get here
        return true;
    }

    private void checkForPotentialRefs(List<SearchMatch> toCheck, RefactoringStatus status) {
        for (SearchMatch match : toCheck) {
            if (match.getAccuracy() == SearchMatch.A_INACCURATE) {
                final RefactoringStatusEntry entry = new RefactoringStatusEntry(RefactoringStatus.WARNING,
                        RefactoringCoreMessages.RefactoringSearchEngine_potential_matches,
                        JavaStatusContext.create(JavaCore.createCompilationUnitFrom((IFile) match.getResource()), new SourceRange(match.getOffset(), match.getLength())));
                status.addEntry(entry);
            }
        }
    }

    private void checkForBinaryRefs(List<SearchMatch> toCheck, RefactoringStatus status) throws JavaModelException {
        ReferencesInBinaryContext binaryRefs = new ReferencesInBinaryContext(
                "Elements containing binary references to refactored element ''" + renameTarget.getElementName() + "''");
        for (Iterator<SearchMatch> iter = toCheck.iterator(); iter.hasNext();) {
            SearchMatch match = iter.next();
            if (isBinaryElement(match.getElement())) {
                if (match.getAccuracy() == SearchMatch.A_ACCURATE) {
                    // binary classpaths are often incomplete -> avoiding false
                    // positives from inaccurate matches
                    binaryRefs.add(match);
                }
                iter.remove();
            }
        }
        binaryRefs.addErrorIfNecessary(status);
    }

    private boolean isBinaryElement(Object element) throws JavaModelException {
        if (element instanceof IMember) {
            return ((IMember) element).isBinary();

        } else if (element instanceof ICompilationUnit) {
            return true;

        } else if (element instanceof IClassFile) {
            return false;

        } else if (element instanceof IPackageFragment) {
            return isBinaryElement(((IPackageFragment) element).getParent());

        } else if (element instanceof IPackageFragmentRoot) {
            return ((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_BINARY;

        }
        return false;

    }

    private SearchResultGroup[] convert(List<SearchMatch> toGroup) {
        Map<IResource, List<SearchMatch>> groups = new HashMap<IResource, List<SearchMatch>>(toGroup.size());
        for (SearchMatch searchMatch : toGroup) {
            if (searchMatch.getResource() == null) {
                // likely a binary match. These are handled elsewhere
                continue;
            }
            List<SearchMatch> group = groups.get(searchMatch.getResource());
            if (group == null) {
                group = new ArrayList<SearchMatch>();
                groups.put(searchMatch.getResource(), group);
            }
            group.add(searchMatch);
        }

        SearchResultGroup[] results = new SearchResultGroup[groups.size()];
        int i = 0;
        for (Entry<IResource, List<SearchMatch>> group : groups.entrySet()) {
            results[i++] = new SearchResultGroup(group.getKey(), group.getValue().toArray(new SearchMatch[0]));
        }
        return results;
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {

        CompositeChange change = new CompositeChange(getName());
        createMatchedChanges(matches, change, getNameMap());
        if (change.getChildren().length > 0) {
            return change;
        }
        return null;
    }

    @Override
    public String getName() {
        return "Rename Groovy synthetic getters and setters.";
    }

    /**
     * Only activate participant if this is a method or field rename in a Groovy
     * project. Must be source.
     */
    @Override
    protected boolean initialize(Object element) {
        if (element instanceof IMethod || element instanceof IField) {
            renameTarget = (IMember) element;
            if (!renameTarget.isReadOnly()
                    && GroovyNature.hasGroovyNature(renameTarget.getJavaProject().getProject())) {
                return true;
            }
        }
        return false;
    }

    private String accessorName(String prefix, String name) {
        return prefix + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private void addChange(CompositeChange finalChange, IMember enclosingElement, int offset, int length, String newName) {
        CompilationUnitChange existingChange = findOrCreateChange(enclosingElement, finalChange);
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
        existingChange.addChangeGroup(new TextEditChangeGroup(existingChange, new TextEditGroup(
"Update synthetic Groovy accessor",
                occurrenceEdit)));
    }

    private String basename(String fullName) {
        int baseStart;
        if (fullName.startsWith("is") && fullName.length() > 2 && Character.isUpperCase(fullName.charAt(2))) {
            baseStart = 2;
        } else if ((fullName.startsWith("get") || fullName.startsWith("set")) && fullName.length() > 3
                && Character.isUpperCase(fullName.charAt(3))) {
            baseStart = 3;
        } else {
            baseStart = -1;
        }

        if (baseStart > 0) {
            return Character.toLowerCase(fullName.charAt(baseStart)) + fullName.substring(baseStart + 1);
        } else {
            return fullName;
        }

    }

    private void createMatchedChanges(List<SearchMatch> references, CompositeChange finalChange, Map<String, String> nameMap)
            throws JavaModelException {
        for (SearchMatch searchMatch : references) {
            Object elt = searchMatch.getElement();
            if (elt instanceof IMember) {
                String oldName = findMatchName(searchMatch, nameMap.keySet());
                if (oldName != null) {
                    String newName = nameMap.get(oldName);
                    addChange(finalChange, (IMember) elt, searchMatch.getOffset(), oldName.length(), newName);
                }
            }
        }
    }

    private List<SearchMatch> findExtraReferences(IProgressMonitor pm) throws CoreException {
        SyntheticAccessorSearchRequestor synthRequestor = new SyntheticAccessorSearchRequestor();

        final List<SearchMatch> matches = new ArrayList<SearchMatch>();
        synthRequestor.findSyntheticMatches(renameTarget, new ISearchRequestor() {
            public void acceptMatch(SearchMatch match) {
                matches.add(match);
            }
        }, SubMonitor.convert(pm, "Find synthetic accessors", 10));
        return matches;
    }

    private String findMatchName(SearchMatch searchMatch, Set<String> keySet) throws JavaModelException {
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

    private CompilationUnitChange findOrCreateChange(IMember accessor, CompositeChange finalChange) {
        TextChange textChange = getTextChange(accessor.getCompilationUnit());
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
                    if (((CompilationUnitChange) change).getCompilationUnit().equals(accessor.getCompilationUnit())) {
                        existingChange = (CompilationUnitChange) change;
                        break;
                    }
                }
            }
        }

        if (existingChange == null) {
            // nope...must create a new change
            existingChange = new CompilationUnitChange("Synthetic Groovy Accessor changes for "
                    + accessor.getCompilationUnit().getElementName(), accessor.getCompilationUnit());
            existingChange.setEdit(new MultiTextEdit());
            finalChange.add(existingChange);
        }
        return existingChange;
    }

    private Map<String, String> getNameMap() {
        Map<String, String> nameMap = new HashMap<String, String>();
        String newBaseName = basename(getArguments().getNewName());
        String oldBaseName = basename(renameTarget.getElementName());

        nameMap.put(oldBaseName, newBaseName);
        nameMap.put(accessorName("is", oldBaseName), accessorName("is", newBaseName));
        nameMap.put(accessorName("get", oldBaseName), accessorName("get", newBaseName));
        nameMap.put(accessorName("set", oldBaseName), accessorName("set", newBaseName));

        return nameMap;
    }

}
