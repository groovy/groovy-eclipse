/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.model;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainerInitializer;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.internal.core.ClasspathEntry;

/**
 * This class contains all the utility methods used in adding the Groovy Runtime
 * to a Java project.
 */
public class GroovyRuntime {

    // Breaking encapsulation here. I don't want to specify this classpath
    // container here because it is defined in a different plugin, but this
    // is the least complicated way pf doing it.
    public static final IPath DSLD_CONTAINER_ID = new Path("GROOVY_DSL_SUPPORT");

    public static void removeGroovyNature(IProject project) throws CoreException {
        IProjectDescription description = project.getDescription();
        String[] ids = description.getNatureIds();
        for (int i = 0; i < ids.length; i += 1) {
            if (ids[i].equals(GroovyNature.GROOVY_NATURE)) {
                String[] newIds = (String[]) ArrayUtils.remove(ids, i);
                description.setNatureIds(newIds);
                project.setDescription(description, null);
                return;
            }
        }
    }

    public static void removeLibraryFromClasspath(IJavaProject javaProject, IPath libraryPath) throws JavaModelException {
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        for (int i = 0; i < oldEntries.length; i += 1) {
            IClasspathEntry entry = oldEntries[i];
            if (entry.getPath().equals(libraryPath)) {
                IClasspathEntry[] newEntries = (IClasspathEntry[]) ArrayUtils.remove(oldEntries, i);
                javaProject.setRawClasspath(newEntries, null);
                return;
            }
        }
    }

    public static void addGroovyRuntime(IProject project) {
        GroovyCore.trace("GroovyRuntime.addGroovyRuntime()");
        try {
            if (project == null || !project.hasNature(JavaCore.NATURE_ID))
                return;
            if (project.hasNature(GroovyNature.GROOVY_NATURE))
                return;

            addGroovyNature(project);
            IJavaProject javaProject = JavaCore.create(project);
            addGroovyClasspathContainer(javaProject);

            // this breaks encapsulation, but it is the most logical place to put it:

            // add the DSLD classpath container
            addLibraryToClasspath(javaProject, DSLD_CONTAINER_ID, false);
        } catch (Exception e) {
            GroovyCore.logException("Failed to add groovy runtime support", e);
        }
    }

    public static boolean hasGroovyClasspathContainer(IJavaProject javaProject) throws CoreException {
        return hasClasspathContainer(javaProject, GroovyClasspathContainer.CONTAINER_ID);
    }

    public static IClasspathEntry getGroovyClasspathEntry(IJavaProject javaProject) throws JavaModelException {
        if (javaProject != null && javaProject.getProject().isAccessible()) {
            IClasspathEntry[] entries = javaProject.readRawClasspath();
            for (IClasspathEntry entry : entries) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                    if (GroovyClasspathContainer.CONTAINER_ID.equals(entry.getPath()) ||
                            GroovyClasspathContainer.CONTAINER_ID.isPrefixOf(entry.getPath())) {
                        return entry;
                    }
                }
            }
        }
        return null;
    }

    public static boolean hasClasspathContainer(IJavaProject javaProject, IPath libraryPath) throws CoreException {
        if (javaProject != null && javaProject.getProject().isAccessible()) {
            IClasspathEntry[] entries = javaProject.getRawClasspath();
            for (IClasspathEntry entry : entries) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                    if (libraryPath.equals(entry.getPath()) || libraryPath.isPrefixOf(entry.getPath())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Not used, but could be used to exclude all groovy files from compilation
     */
    /*public static void excludeGroovyFilesFromOutput(IJavaProject javaProject) {
        // make sure .groovy files are not copied to the output dir
        String excludedResources = javaProject.getOption(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, true);
        if (excludedResources.indexOf("*.groovy") == -1) {
            excludedResources = excludedResources.length() == 0 ? "*.groovy" : excludedResources + ",*.groovy";
            javaProject.setOption(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, excludedResources);
        }
    }*/

    /**
     * Not used, but could be used to include all groovy files for compilation
     */
    /*public static void includeGroovyFilesInOutput(IJavaProject javaProject) {
        // make sure .groovy files are not copied to the output dir
        String[] excludedResourcesArray = StringUtils.split(javaProject.getOption(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, true), ",");
        List<String> excludedResources = newEmptyList();
        for (int i = 0; i < excludedResourcesArray.length; i++) {
            String excluded = excludedResourcesArray[i].trim();
            if (excluded.endsWith("*.groovy"))
                continue;
            excludedResources.add(excluded);
        }
        javaProject.setOption(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, StringUtils.join(excludedResources, ","));
    }*/

    private static void internalAddGroovyClasspathContainer(IJavaProject javaProject, boolean isMinimal) {
        try {
            if (javaProject == null) {
                return;
            }
            if (hasGroovyClasspathContainer(javaProject)) {
                removeGroovyClasspathContainer(javaProject);
            }
            IClasspathEntry containerEntry = createContainerEntry(isMinimal);
            addClassPathEntry(javaProject, containerEntry);
        } catch (CoreException e) {
            GroovyCore.logException("Failed to add groovy classpath container:" + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static IClasspathEntry createContainerEntry(boolean isMinimal) {
        return JavaCore.newContainerEntry(GroovyClasspathContainer.CONTAINER_ID, ClasspathEntry.NO_ACCESS_RULES,
            (isMinimal ? new IClasspathAttribute[] {GroovyClasspathContainer.MINIMAL_ATTRIBUTE} : ClasspathEntry.NO_EXTRA_ATTRIBUTES), false);
    }

    public static void addMinimalGroovyClasspathContainer(IJavaProject javaProject) {
        internalAddGroovyClasspathContainer(javaProject, true);
    }

    public static void addGroovyClasspathContainer(IJavaProject javaProject) {
        internalAddGroovyClasspathContainer(javaProject, false);
    }

    public static void ensureGroovyClasspathContainer(IJavaProject javaProject, boolean isMinimal) {
        try {
            if (javaProject == null) {
                return;
            }
            IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
            boolean found = false;
            boolean workDone = false;
            for (int i = 0, n = rawClasspath.length; i < n; i += 1) {
                if (rawClasspath[i].getPath().equals(GroovyClasspathContainer.CONTAINER_ID)) {
                    found = true;
                    if (isMinimal) {
                        if (GroovyClasspathContainer.hasMinimalAttribute(rawClasspath[i])) {
                            // do nothing
                        } else {
                            rawClasspath[i] = createContainerEntry(true);
                            workDone = true;
                        }
                    } else {
                        if (GroovyClasspathContainer.hasMinimalAttribute(rawClasspath[i])) {
                            rawClasspath[i] = createContainerEntry(false);
                            workDone = true;
                        } else {
                            // do nothing
                        }
                    }
                    break;
                }
            }
            if (!found) {
                IClasspathEntry[] newClasspath = new IClasspathEntry[rawClasspath.length + 1];
                System.arraycopy(rawClasspath, 0, newClasspath, 0, rawClasspath.length);
                newClasspath[rawClasspath.length] = createContainerEntry(isMinimal);
                rawClasspath = newClasspath;
                workDone = true;
            }
            if (workDone) {
                javaProject.setRawClasspath(rawClasspath, null);
                // must be called after classpath is set so minimal attribute is available
                GroovyClasspathContainerInitializer.updateGroovyClasspathContainer(javaProject);
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Problem setting groovy classpath container", e);
        }
    }

    public static void removeGroovyClasspathContainer(IJavaProject javaProject) {
        removeClasspathContainer(GroovyClasspathContainer.CONTAINER_ID, javaProject);
    }

    public static void removeClasspathContainer(IPath containerPath, IJavaProject javaProject) {
        try {
            if (!hasGroovyClasspathContainer(javaProject)) {
                return;
            }

            IClasspathEntry[] entries = javaProject.getRawClasspath();
            int removeIndex = -1;
            for (int i = 0, n = entries.length; i < n; i += 1) {
                if (entries[i].getPath().equals(containerPath)) {
                    removeIndex = i;
                    break;
                }
            }
            IClasspathEntry[] newEntries = (IClasspathEntry[]) ArrayUtils.remove(entries, removeIndex);
            javaProject.setRawClasspath(newEntries, null);
        } catch (CoreException e) {
            GroovyCore.logException("Failed to add groovy classpath container:" + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a library/folder that already exists in the project to the
     * classpath. Only added if it is not already on the classpath.
     */
    public static void addLibraryToClasspath(IJavaProject javaProject, IPath libraryPath, boolean isExported) throws JavaModelException {
        boolean alreadyExists = includesClasspathEntry(javaProject, libraryPath.lastSegment());
        if (alreadyExists) {
            return;
        }
        addClassPathEntry(javaProject, new ClasspathEntry(IPackageFragmentRoot.K_BINARY, IClasspathEntry.CPE_CONTAINER,
                libraryPath,
                ClasspathEntry.INCLUDE_ALL, // inclusion patterns
                ClasspathEntry.EXCLUDE_NONE, // exclusion patterns
                null, null, null, // specific output folder
                true, // exported
                ClasspathEntry.NO_ACCESS_RULES,
                false, // no access rules to combine
                ClasspathEntry.NO_EXTRA_ATTRIBUTES));
    }

    public static void addGroovyNature(IProject project) throws CoreException {
        GroovyCore.trace("GroovyRuntime.addGroovyNature()");
        IProjectDescription description = project.getDescription();
        String[] ids = description.getNatureIds();

        // add groovy nature at the start so that its image will be shown
        String[] newIds = new String[ids == null ? 1 : ids.length + 1];
        newIds[0] = GroovyNature.GROOVY_NATURE;
        if (ids != null) {
            for (int i = 1, n = newIds.length; i < n; i += 1) {
                newIds[i] = ids[i - 1];
            }
        }

        description.setNatureIds(newIds);
        project.setDescription(description, null);
    }

    /**
     * Adds a classpath entry to the end of a project's the classpath.
     */
    public static void addClassPathEntry(IJavaProject project, IClasspathEntry newEntry) throws JavaModelException {
        IClasspathEntry[] newEntries = (IClasspathEntry[]) ArrayUtils.add(project.getRawClasspath(), newEntry);
        project.setRawClasspath(newEntries, null);
    }

    /**
     * Adds a classpath entry to the front of a project's the classpath.
     */
    public static void addClassPathEntryToFront(IJavaProject project, IClasspathEntry newEntry) throws JavaModelException {
        IClasspathEntry[] newEntries = (IClasspathEntry[]) ArrayUtils.add(project.getRawClasspath(), 0, newEntry);
        project.setRawClasspath(newEntries, null);
    }

    /**
     * Removes a classpath entry to a project.
     */
    public static void removeClassPathEntry(IJavaProject project, IClasspathEntry newEntry) throws JavaModelException {
        IClasspathEntry[] newEntries = (IClasspathEntry[]) ArrayUtils.removeElement(project.getRawClasspath(), newEntry);
        project.setRawClasspath(newEntries, null);
    }

    /**
     * Looks through a set of classpath entries and checks to see if the path is
     * in them.
     *
     * @return If possiblePath is included in entries returns {@code true}, otherwise returns {@code false}.
     */
    private static boolean includesClasspathEntry(IJavaProject project, String entryName) throws JavaModelException {
        IClasspathEntry[] entries = project.getRawClasspath();
        for (IClasspathEntry entry : entries) {
            if (entry.getPath().lastSegment().equals(entryName)) {
                return true;
            }
        }
        return false;
    }
}
