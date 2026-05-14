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
package org.codehaus.groovy.eclipse.dsl.classpath;

import static org.eclipse.jdt.core.JavaCore.newLibraryEntry;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

/**
 * Classpath container initializer that collates all of the DSLDs that live outside of the workspace.
 */
public class DSLDContainerInitializer extends ClasspathContainerInitializer {

    public static final String PLUGIN_DSLD_SUPPORT = "plugin_dsld_support";
    public static final String GLOBAL_DSLD_SUPPORT = "global_dsld_support";

    /**
     * The location for global dsld files.  Null if the locaiton does not exist and cannot be created.
     */
    private static final File GLOBAL_DSLD_LOCATION = resolveGlobalDsldLocation();

    private static File resolveGlobalDsldLocation() {
        String dotGroovyLocation;
        if (GroovyDSLCoreActivator.getDefault().isDSLDDisabled() || (dotGroovyLocation = CompilerUtils.getDotGroovyLocation()) == null) {
            return null;
        }

        File globalDsldDir = new File(dotGroovyLocation + "/greclipse/" + GLOBAL_DSLD_SUPPORT);
        if (!globalDsldDir.exists()) {
            try {
                globalDsldDir.mkdirs();
            } catch (SecurityException ignore) {
            }
        }

        if (globalDsldDir.exists()) {
            return globalDsldDir;
        } else {
            GroovyDSLCoreActivator.logWarning("Cannot create DSL support location at " + globalDsldDir.getPath() + ". " +
                "Location is read-only or a security manager is preventing it.");
            return null;
        }
    }

    //--------------------------------------------------------------------------

    @Override
    public void initialize(final IPath containerPath, final IJavaProject javaProject) throws CoreException {
        JavaCore.setClasspathContainer(containerPath, new IJavaProject[]{javaProject}, new IClasspathContainer[]{new DSLDClasspathContainer()}, null);
    }

    @Override
    public boolean canUpdateClasspathContainer(final IPath containerPath, final IJavaProject javaProject) {
        return true;
    }

    @Override
    public void requestClasspathContainerUpdate(final IPath containerPath, final IJavaProject javaProject, final IClasspathContainer containerSuggestion)
            throws CoreException {
        IClasspathContainer container = JavaCore.getClasspathContainer(containerPath, javaProject);
        if (container instanceof DSLDClasspathContainer) {
            ((DSLDClasspathContainer) container).reset();
        }
    }

    //--------------------------------------------------------------------------

    private static class DSLDClasspathContainer implements IClasspathContainer {
        private IClasspathEntry[] entries;

        @Override
        public IClasspathEntry[] getClasspathEntries() {
            if (entries == null) {
                entries = resolveEntries();
            }
            return entries;
        }

        @Override
        public String getDescription() {
            return "Groovy DSL Support";
        }

        @Override
        public int getKind() {
            return K_APPLICATION;
        }

        @Override
        public IPath getPath() {
            return GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID;
        }

        void reset() {
            entries = null;
        }

        protected static IClasspathEntry[] resolveEntries() {
            if (GroovyDSLCoreActivator.getDefault().isDSLDDisabled()) {
                return new IClasspathEntry[0];
            }
            List<IClasspathEntry> newEntries = new ArrayList<>();
            if (GLOBAL_DSLD_LOCATION != null && GLOBAL_DSLD_LOCATION.exists()) {
                IPath path = new Path(GLOBAL_DSLD_LOCATION.getAbsolutePath());
                newEntries.add(newLibraryEntry(path, null, null));
            }
            try {
                Bundle groovyBundle = CompilerUtils.getActiveGroovyBundle();
                URL dsldSupportPath = groovyBundle.getEntry(PLUGIN_DSLD_SUPPORT);
                Assert.isTrue(dsldSupportPath != null, "Plugin DSLD location not found");

                IPath path = new Path(FileLocator.toFileURL(dsldSupportPath).getPath());
                Assert.isTrue(path.toFile().exists(), "Plugin DSLD location does not exist: " + path);

                newEntries.add(newLibraryEntry(path, null, null));
            } catch (Exception e) {
                GroovyDSLCoreActivator.logException(e);
            }
            return newEntries.toArray(new IClasspathEntry[newEntries.size()]);
        }
    }
}
