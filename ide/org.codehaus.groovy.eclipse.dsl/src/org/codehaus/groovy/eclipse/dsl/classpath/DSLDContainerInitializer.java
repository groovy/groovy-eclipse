package org.codehaus.groovy.eclipse.dsl.classpath;

import static org.eclipse.jdt.core.JavaCore.newLibraryEntry;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Classpath container initializer that grabs all of the DSLDs that live outside of the workspace.
 * 
 * @author andrew
 * @created May 21, 2011
 */
public class DSLDContainerInitializer extends ClasspathContainerInitializer {

    private final class DSLDClasspathContainer implements IClasspathContainer {
        private IClasspathEntry[] entries;

        public IPath getPath() {
            return CONTAINER_ID;
        }

        public int getKind() {
            return K_APPLICATION;
        }

        public String getDescription() {
            return "Groovy DSL Support";
        }
        
        public IClasspathEntry[] getClasspathEntries() {
            if (entries == null) {
                entries = calculateEntries();
            }
            return entries;
        }
        
        void reset() {
            entries = null;
        }

        /**
         * Two entries: the /dsld folder in the groovy bundle and the ~/.groovy/greclipse/dsld folder
         * @return
         */
        protected IClasspathEntry[] calculateEntries() {
            String dotGroovyLocation = CompilerUtils.getDotGroovyLocation();
            List<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>();
            if (dotGroovyLocation != null) {
                dotGroovyLocation += "/greclipse/global_dsld_support";
                File globalDsldLocation = new File(dotGroovyLocation);
                if (!globalDsldLocation.exists()) {
                    try {
                        globalDsldLocation.mkdirs();
                    } catch (SecurityException e) {
                        GroovyDSLCoreActivator.logException("Cannot create DSL support location at " + dotGroovyLocation + " since SecurityManager doesn't allow it.", e);
                    }
                }
                
                if (globalDsldLocation.exists()) {
                    IPath dsldPath = new Path(globalDsldLocation.getAbsolutePath());
                    newEntries.add(newLibraryEntry(dsldPath, null, null, false));
                }
            }
            
            URL folder = CompilerUtils.findDSLDFolder();
            if (folder != null) {
                String file = folder.getFile();
                if (new File(file).exists()) {
                    IPath path = new Path(folder.getPath());
                    newEntries.add(newLibraryEntry(path, null, null));
                }
            }
            return newEntries.toArray(new IClasspathEntry[0]);
        }
    }

    public static IPath CONTAINER_ID = new Path("GROOVY_DSL_SUPPORT");
    
    private IJavaProject javaProject;
    
    @Override
    public void initialize(final IPath containerPath, final IJavaProject javaProject) throws CoreException {
        this.javaProject = javaProject;
        IClasspathContainer container = new DSLDClasspathContainer();
        JavaCore.setClasspathContainer(containerPath, 
                new IJavaProject[] { javaProject }, 
                new IClasspathContainer[] {container}, null);
    }

    @Override
    public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) {
        return true;
    }
    
    @Override
    public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject project, IClasspathContainer containerSuggestion)
            throws CoreException {
        if (containerSuggestion instanceof DSLDClasspathContainer) {
            ((DSLDClasspathContainer) containerSuggestion).reset();
        }
        
        if (javaProject == null) {
            IClasspathContainer dsld = JavaCore.getClasspathContainer(GroovyClasspathContainer.CONTAINER_ID, javaProject);
            if (dsld instanceof DSLDClasspathContainer) {
                ((DSLDClasspathContainer) dsld).reset();
            }
        }
    }
}
