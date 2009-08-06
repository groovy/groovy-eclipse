package org.codehaus.groovy.eclipse.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class GroovyClasspathContainerInitializer extends
        ClasspathContainerInitializer {

    public void initialize(IPath containerPath, IJavaProject project)
            throws CoreException {
        IClasspathContainer container = new GroovyClasspathContainer();
        JavaCore.setClasspathContainer(containerPath, 
        		new IJavaProject[] {project}, 
        		new IClasspathContainer[] {container}, null);
    }

}
