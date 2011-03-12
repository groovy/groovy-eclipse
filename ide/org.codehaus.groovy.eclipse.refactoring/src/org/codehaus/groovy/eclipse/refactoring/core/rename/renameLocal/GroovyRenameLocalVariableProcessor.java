
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
package org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal;

import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.refactoring.Activator;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.LocalVariableReferenceRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameModifications;
import org.eclipse.jdt.internal.corext.refactoring.util.ResourceUtil;
import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;
/**
 *
 * @author Andrew Eisenberg
 * @created Apr 1, 2010
 */
public class GroovyRenameLocalVariableProcessor extends JavaRenameProcessor {

    GroovyCompilationUnit unit;
    Variable variable;  // initially null
    CompilationUnitChange change;  // initially null
    ILocalVariable localVariable;
    GroovyRenameLocalVariableProcessor(ILocalVariable localVariable, String newName, RefactoringStatus status) {
        initialize(localVariable, newName, status);
    }

    /**
     * @param localVariable
     * @param newName
     * @param status
     */
    private void initialize(ILocalVariable localVariable, String newName,
            RefactoringStatus status) {
        this.localVariable = localVariable;
        ICompilationUnit unit = (ICompilationUnit) localVariable.getAncestor(IJavaElement.COMPILATION_UNIT);
        if (unit instanceof GroovyCompilationUnit) {
            this.unit = (GroovyCompilationUnit) unit;
        } else {
            status.merge(RefactoringStatus.createErrorStatus("Expecting a Groovy compilation unit, but instead found " + unit.getElementName()));
        }
        if (newName != null && newName.length() > 0) {
            if (newName.equals(localVariable.getElementName())) {
                status.merge(RefactoringStatus.createErrorStatus("New name is the same as the old name"));
            }
            setNewElementName(newName);
        } else {
            status.merge(RefactoringStatus.createErrorStatus("Invalid new name"));
        }
    }

    @Override
    protected RenameModifications computeRenameModifications()
            throws CoreException {
        RenameModifications mods = new RenameModifications();
        mods.rename(localVariable, new RenameArguments(getNewElementName(), true));
        return mods;
    }

    @Override
    protected RefactoringStatus doCheckFinalConditions(IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException,
            OperationCanceledException {
        // ensure that we are working on a working copy so that
        // we can use == for testing nodes.
        boolean wasWorkingCopy = true;
        try {
            if (! unit.isWorkingCopy()) {
                unit.becomeWorkingCopy(new SubProgressMonitor(pm, 10));
                wasWorkingCopy = false;
            }
            List<IRegion> references = findReferences();
            change = createEdits(references);
            RefactoringStatus result = checkShadowing();
            return result;
        } catch (Exception e) {
            return RefactoringStatus.create(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Exception creating edits for rename refactoring", e));
        } finally {
            if (!wasWorkingCopy) {
                unit.discardWorkingCopy();
            }
        }
    }

    /**
     * Check to see if the new name shadows an existing name
     * @return
     */
    private RefactoringStatus checkShadowing() {
        IType type = (IType) localVariable.getAncestor(IJavaElement.TYPE);
        if (type != null) {
            IField maybeShadows = type.getField(getNewElementName());
            if (maybeShadows.exists()) {
                return RefactoringStatus.createWarningStatus("Warning: new variable name " +
                        getNewElementName() + " shadows a field in " + type.getElementName());
            }
        }

        LocalVariableNameCheckerRequestor requestor = new LocalVariableNameCheckerRequestor(variable, getNewElementName());
        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
        visitor.visitCompilationUnit(requestor);
        if (requestor.isShadowing()) {
            IJavaElement parent = localVariable.getParent();
            if (parent instanceof IMethod) {
                return RefactoringStatus.createWarningStatus("Warning: new variable name " +
                        getNewElementName() + " shadows a variable in " + localVariable.getParent().getElementName(),
                        JavaStatusContext.create((IMethod) parent));
            } else {
                return RefactoringStatus.createWarningStatus("Warning: new variable name " +
                        getNewElementName() + " shadows a variable in " + localVariable.getParent().getElementName());
            }
        }

        return new RefactoringStatus();
    }

    @Override
    protected String[] getAffectedProjectNatures() throws CoreException {
         return new String[] { JavaCore.NATURE_ID, GroovyNature.GROOVY_NATURE };
    }
    @Override
    protected IFile[] getChangedFiles() throws CoreException {
        return new IFile[] { ResourceUtil.getFile(unit) };
    }
    @Override
    public int getSaveMode() {
        return RefactoringSaveHelper.SAVE_NOTHING;
    }
    public RefactoringStatus checkNewElementName(String newName)
            throws CoreException {
        if (localVariable.getElementName().equals(newName)) {
            return RefactoringStatus.createErrorStatus("New name must be different from old name");
        } else {
            return RefactoringStatus.create(JavaConventions.validateFieldName(
                    newName, CompilerOptions.VERSION_1_6,
                    CompilerOptions.VERSION_1_6));
        }
    }

    public String getCurrentElementName() {
        return localVariable.getElementName();
    }
    public Object getNewElement() throws CoreException {

        // be compatible between 3.6 and 3.7+
        return ReflectionUtils.createLocalVariable(localVariable.getParent(), getNewElementName(), localVariable.getNameRange()
                .getOffset(), localVariable.getTypeSignature());

    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
            throws CoreException, OperationCanceledException {
        Variable variable = findVariable();
        if (variable == null) {
            return RefactoringStatus.createErrorStatus("Cannot find local variable " + localVariable.getElementName());
        }
        return new RefactoringStatus();
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {
        return change;
    }
    @Override
    public Object[] getElements() {
        return new Object[] { localVariable };
    }
    @Override
    public String getIdentifier() {
        return RenameLocalGroovyVariableContribution.ID;
    }
    @Override
    public String getProcessorName() {
        return "Rename Local Variable (Groovy)";
    }
    @Override
    public boolean isApplicable() throws CoreException {
        if (unit == null) {
            return false;
        }
        return true;
    }

    private List<IRegion> findReferences() {
        if (variable == null) {
            try {
                variable = findVariable();
            } catch (JavaModelException e) {
                throw new RuntimeException(e);
            }
        }
        LocalVariableReferenceRequestor requestor = new LocalVariableReferenceRequestor(variable, localVariable.getParent());
        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit);
        visitor.visitCompilationUnit(requestor);
        return requestor.getReferences();

    }

    /**
     * @return
     * @throws JavaModelException
     */
    private Variable findVariable() throws JavaModelException {
        ISourceRange sourceRange = localVariable.getSourceRange();
        ASTNodeFinder findLocalVar = new ASTNodeFinder(new Region(sourceRange.getOffset(), sourceRange.getLength()));
        ASTNode node = findLocalVar.doVisit(unit.getModuleNode());
        return node instanceof Variable ? (Variable) node : null;
    }

    private CompilationUnitChange createEdits(List<IRegion> references) {
        TextEdit[] allEdits= new TextEdit[references.size()];
        int index = 0;
        for (IRegion region : references) {
            allEdits[index] = new ReplaceEdit(region.getOffset(), region.getLength(), getNewElementName());
            index++;
        }

        CompilationUnitChange change = new CompilationUnitChange(RefactoringCoreMessages.RenameTempRefactoring_rename, unit);
        MultiTextEdit rootEdit= new MultiTextEdit();
        change.setEdit(rootEdit);
        change.setKeepPreviewEdits(true);

        for (int i = 0; i < allEdits.length; i++) {
            rootEdit.addChild(allEdits[i]);
            change.addTextEditGroup(new TextEditGroup(RefactoringCoreMessages.RenameTempRefactoring_changeName, allEdits[i]));
        }
        return change;
    }

}
