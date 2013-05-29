package org.codehaus.groovy.eclipse.core.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;

public class GroovyClasspathContainerInitializer extends
        ClasspathContainerInitializer {

    @Override
    public void initialize(IPath containerPath, IJavaProject project)
            throws CoreException {
        IClasspathContainer container = new GroovyClasspathContainer(project.getProject());
        JavaCore.setClasspathContainer(containerPath, 
        		new IJavaProject[] {project}, 
        		new IClasspathContainer[] {container}, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) {
        // always ok to return classpath container
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject javaProject,
            IClasspathContainer containerSuggestion) throws CoreException {
        if (containerSuggestion instanceof GroovyClasspathContainer) {
            ((GroovyClasspathContainer) containerSuggestion).reset();
        }
        IClasspathContainer gcc = JavaCore.getClasspathContainer(GroovyClasspathContainer.CONTAINER_ID, javaProject);
        if (gcc instanceof GroovyClasspathContainer) {
            ((GroovyClasspathContainer) gcc).reset();
        }
    }
    
    /**
     * Refresh all classpath containers.  Should do this if the ~/.groovy/lib directory has changed.
     * @throws JavaModelException
     */
    public static void updateAllGroovyClasspathContainers() throws JavaModelException {
        IJavaProject[] projects = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects();
        updateSomeGroovyClasspathContainers(projects);
    }
    
    public static void updateGroovyClasspathContainer(IJavaProject project) throws JavaModelException {
        updateSomeGroovyClasspathContainers(new IJavaProject[] { project });
    }
    

    /**
     * @param projects
     * @throws JavaModelException
     */
    private static void updateSomeGroovyClasspathContainers(
    		IJavaProject[] projects) throws JavaModelException {
        List<IJavaProject> affectedProjects = new ArrayList<IJavaProject>(projects.length);
        List<IClasspathContainer> affectedContainers = new ArrayList<IClasspathContainer>(projects.length);
        for (IJavaProject elt : projects) {
            IJavaProject project = (IJavaProject) elt;
            IClasspathContainer gcc = JavaCore.getClasspathContainer(GroovyClasspathContainer.CONTAINER_ID, project);
            if (gcc instanceof GroovyClasspathContainer) {
                ((GroovyClasspathContainer) gcc).reset();
                affectedProjects.add(project);
                affectedContainers.add(null);
            }
        }
        JavaCore.setClasspathContainer(GroovyClasspathContainer.CONTAINER_ID, affectedProjects.toArray(new IJavaProject[0]), 
                affectedContainers.toArray(new IClasspathContainer[0]), new NullProgressMonitor());
    }
}
