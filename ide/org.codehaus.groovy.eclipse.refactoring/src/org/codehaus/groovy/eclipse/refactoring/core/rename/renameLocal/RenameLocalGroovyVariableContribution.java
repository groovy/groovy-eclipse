package org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.corext.refactoring.scripting.JavaUIRefactoringContribution;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

public class RenameLocalGroovyVariableContribution extends JavaUIRefactoringContribution {

    public static final String ID = "org.codehaus.groovy.eclipse.refactoring.renameLocal";
    
    @Override
    public Refactoring createRefactoring(JavaRefactoringDescriptor descriptor,
            RefactoringStatus status) throws CoreException {
        if (descriptor instanceof RenameJavaElementDescriptor) {
            IJavaElement elt = (IJavaElement) ReflectionUtils.getPrivateField(RenameJavaElementDescriptor.class, "fJavaElement", descriptor);
            String newName = (String) ReflectionUtils.getPrivateField(RenameJavaElementDescriptor.class, "fName", descriptor);
            if (elt instanceof ILocalVariable && newName != null) {
                ILocalVariable var = (ILocalVariable) elt;
                return new RenameRefactoring(new GroovyRenameLocalVariableProcessor(var, newName, status));
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RefactoringDescriptor createDescriptor(String id, String project,
            String description, String comment, Map arguments, int flags)
            throws IllegalArgumentException {
        return new RenameJavaElementDescriptor(id, project, description, comment, arguments, flags);
    }
}
