/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.classpath;

import static org.eclipse.jdt.core.JavaCore.newLibraryEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.eclipse.core.runtime.Assert;
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
 */
public class DSLDContainerInitializer extends ClasspathContainerInitializer {

    private static final IClasspathEntry[] NO_ENTRIES = new IClasspathEntry[0];

    /**
     * The location for global dsld files.  Null if the locaiton does not exist and cannot be created
     */
    private static final File globalDsldLocation = getGlobalDsldLocation();

    private static final class DSLDClasspathContainer implements IClasspathContainer {
        private IClasspathEntry[] entries;

        public IPath getPath() {
            return GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID;
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
         */
        protected IClasspathEntry[] calculateEntries() {
            if (GroovyDSLCoreActivator.getDefault().isDSLDDisabled()) {
                return NO_ENTRIES;
            }
            List<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>();
            if (globalDsldLocation != null && globalDsldLocation.exists()) {
                IPath dsldPath = new Path(globalDsldLocation.getAbsolutePath());
                newEntries.add(newLibraryEntry(dsldPath, null, null, false));
            }
            try {
                IPath folder = CompilerUtils.findDSLDFolder();
                if (folder != null) {
                    Assert.isTrue(folder.toFile().exists(), "Plugin DSLD location does not exist: " + folder);
                    newEntries.add(newLibraryEntry(folder, null, null));
                }
            } catch (Exception e) {
                GroovyDSLCoreActivator.logException(e);
            }
            return newEntries.toArray(NO_ENTRIES);
        }
    }

    private static File getGlobalDsldLocation() {
        final String dotGroovyLocation;
        if (GroovyDSLCoreActivator.getDefault().isDSLDDisabled() ||
                (dotGroovyLocation = CompilerUtils.getDotGroovyLocation()) == null) {
            return null;
        }

        final File globalDsldDir = new File(dotGroovyLocation + "/greclipse/global_dsld_support");
        if (!globalDsldDir.exists()) {
            try {
                globalDsldDir.mkdirs();
            } catch (SecurityException e) {
            }
        }

        if (globalDsldDir.exists()) {
            return globalDsldDir;
        } else {
            GroovyDSLCoreActivator.logWarning("Cannot create DSL support location at " + globalDsldDir.getPath() + ". Location is read-only, or a security manager is preventing it.");
            return null;
        }
    }

    private IJavaProject javaProject;

    @Override
    public void initialize(final IPath containerPath, final IJavaProject javaProject) throws CoreException {
        this.javaProject = javaProject;
        IClasspathContainer container = new DSLDClasspathContainer();
        JavaCore.setClasspathContainer(containerPath, new IJavaProject[] {javaProject}, new IClasspathContainer[] {container}, null);
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
