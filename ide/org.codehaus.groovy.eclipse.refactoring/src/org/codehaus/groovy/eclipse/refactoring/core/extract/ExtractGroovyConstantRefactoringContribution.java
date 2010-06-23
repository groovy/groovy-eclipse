package org.codehaus.groovy.eclipse.refactoring.core.extract;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.scripting.JavaUIRefactoringContribution;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class ExtractGroovyConstantRefactoringContribution extends
        JavaUIRefactoringContribution {

    public RefactoringDescriptor createDescriptor() {
        return RefactoringSignatureDescriptorFactory
                .createExtractConstantDescriptor();
    }

    public RefactoringDescriptor createDescriptor(String id, String project,
            String description, String comment, Map arguments, int flags) {
        return RefactoringSignatureDescriptorFactory
                .createExtractConstantDescriptor(project, description, comment,
                        arguments, flags);
    }

    @Override
    public Refactoring createRefactoring(JavaRefactoringDescriptor descriptor,
            RefactoringStatus status) throws CoreException {
        JavaRefactoringArguments arguments = new JavaRefactoringArguments(
                descriptor.getProject(), retrieveArgumentMap(descriptor));
        return new ExtractGroovyConstantRefactoring(arguments, status);
    }
}
