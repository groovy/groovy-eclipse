package org.codehaus.groovy.eclipse.refactoring.core.rename;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

public class ForcePreviewParticpant extends RenameParticipant {
    
    // give tests ability to disable this participant
    private static boolean muted = false;

    private static final String FIRST_MSG = "This is a rename refactoring involving Groovy.\n" +
            		"Due to Groovy's dynamicism, it is recommended that\n" +
            		"you preview the changes before applying them.";
    
    private static final String SECOND_MSG = 
            "If you do not want to rename the associated file, UNCHECK\n" +
    		"the 'Move Compilation Unit' option in the preview pane."; 
    private IType type;
    
    public ForcePreviewParticpant() { }

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm,
            CheckConditionsContext context) throws OperationCanceledException {
        RefactoringStatus status = RefactoringStatus.createWarningStatus(FIRST_MSG);
        if (shouldWarnAboutFileRename()) {
            status.addWarning(SECOND_MSG);
        }
        return status;
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {
        return null;
    }

    @Override
    public String getName() {
        return "Force previeew when performing Groovy changes";
    }

    @Override
    protected boolean initialize(Object element) {
        if (muted) {
            return false;
        }
        if (element instanceof IMember || element instanceof ILocalVariable) {
            IJavaElement member = (IJavaElement) element;
            try {
                if (element instanceof IType) {
                    type = (IType) element;
                }
                return member.getJavaProject().getProject().hasNature(GroovyNature.GROOVY_NATURE);
            } catch (CoreException e) {
                GroovyCore.logException("Exception initializing Groovy Rename Refactoring participant", e);
            }
        }
        return false;
    }
    
    boolean shouldWarnAboutFileRename() {
        if (type != null) {
            return type.getCompilationUnit().findPrimaryType().equals(type);
        }
        return false;
    }
    
    public static void mute() {
        muted = true;
    }
    public static void unmute() {
        muted = false;
    }

}
