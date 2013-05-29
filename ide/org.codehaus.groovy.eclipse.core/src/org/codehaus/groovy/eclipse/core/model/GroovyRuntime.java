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
package org.codehaus.groovy.eclipse.core.model;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.codehaus.groovy.eclipse.core.util.ArrayUtils;
import org.codehaus.groovy.eclipse.core.util.ObjectUtils;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathEntry;

/**
 * This class contains all the utility methods used in adding the Groovy Runtime
 * to a Java project.
 */
public class GroovyRuntime {

    public static void removeGroovyNature(final IProject project)
            throws CoreException {
        GroovyCore.trace("GroovyRuntime.removeGroovyNature()");
        final IProjectDescription description = project.getDescription();
        final String[] ids = description.getNatureIds();
        for (int i = 0; i < ids.length; ++i) {
            if (ids[i].equals(GroovyNature.GROOVY_NATURE)) {
                final String[] newIds = (String[]) ArrayUtils.remove(ids, i);
                description.setNatureIds(newIds);
                project.setDescription(description, null);
                return;
            }
        }
    }


    public static void removeLibraryFromClasspath(
            final IJavaProject javaProject, final IPath libraryPath)
            throws JavaModelException {
        final IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        for (int i = 0; i < oldEntries.length; i++) {
            final IClasspathEntry entry = oldEntries[i];
            if (entry.getPath().equals(libraryPath)) {
                final IClasspathEntry[] newEntries = (IClasspathEntry[]) ArrayUtils
                        .remove(oldEntries, i);
                javaProject.setRawClasspath(newEntries, null);
                return;
            }
        }
    }

    // Breaking encapsu;ation here. I don't want to specify this classpath
    // container here
    // because it is defined in a different plugin, but this is the least
    // complicated way pf doing it.
    public static IPath DSLD_CONTAINER_ID = new Path("GROOVY_DSL_SUPPORT");

    public static void addGroovyRuntime(final IProject project) {
        GroovyCore.trace("GroovyRuntime.addGroovyRuntime()");
        try {
            if (project == null || !project.hasNature(JavaCore.NATURE_ID))
                return;
            if (project.hasNature(GroovyNature.GROOVY_NATURE))
                return;

            addGroovyNature(project);
            final IJavaProject javaProject = JavaCore.create(project);
            addGroovyClasspathContainer(javaProject);

            // this breaks encapsulation, but it is the most logical place to
            // put it
            // add the DSLD classpath container
            addLibraryToClasspath(javaProject, DSLD_CONTAINER_ID, true);

        } catch (final Exception e) {
            GroovyCore.logException("Failed to add groovy runtime support", e);
        }
    }

    public static boolean hasGroovyClasspathContainer(
            final IJavaProject javaProject) throws CoreException {
        return hasClasspathContainer(javaProject, GroovyClasspathContainer.CONTAINER_ID);
    }

    public static IClasspathEntry getGroovyClasspathEntry(IJavaProject javaProject) throws JavaModelException {
        if (javaProject == null || !javaProject.getProject().isAccessible())
            return null;
        final IClasspathEntry[] entries = javaProject.getRawClasspath();
        for (int i = 0; i < entries.length; i++) {
            final IClasspathEntry entry = entries[i];
            if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                if (ObjectUtils.equals(entry.getPath(), GroovyClasspathContainer.CONTAINER_ID)
                        || GroovyClasspathContainer.CONTAINER_ID.isPrefixOf(entry.getPath())) {
                    return entry;
                }
            }
        }
        return null;
    }

    public static boolean hasClasspathContainer(final IJavaProject javaProject, final IPath libraryPath) throws CoreException {
        if (javaProject == null || !javaProject.getProject().isAccessible())
            return false;
        final IClasspathEntry[] entries = javaProject.getRawClasspath();
        for (int i = 0; i < entries.length; i++) {
            final IClasspathEntry entry = entries[i];
            if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                if (ObjectUtils.equals(entry.getPath(), libraryPath) || libraryPath.isPrefixOf(entry.getPath())) {
                    return true;
                }
            }
        }
        return false;
    }
//    /**
//     * Not used, but could be used to exclude all groovy files from compilation
//     * @param javaProject
//     */
//    public static void excludeGroovyFilesFromOutput(
//            final IJavaProject javaProject) {
//        // make sure .groovy files are not copied to the output dir
//        String excludedResources = javaProject.getOption(
//                JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, true);
//        if (excludedResources.indexOf("*.groovy") == -1) {
//            excludedResources = excludedResources.length() == 0 ? "*.groovy"
//                    : excludedResources + ",*.groovy";
//            javaProject.setOption(
//                    JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER,
//                    excludedResources);
//        }
//    }
//
//    /**
//     * Not used, but could be used to include all groovy files for compilation
//     * @param javaProject
//     */
//    public static void includeGroovyFilesInOutput(final IJavaProject javaProject) {
//        // make sure .groovy files are not copied to the output dir
//        final String[] excludedResourcesArray = StringUtils.split(
//                javaProject.getOption(
//                        JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, true),
//                ",");
//        final List<String> excludedResources = newEmptyList();
//        for (int i = 0; i < excludedResourcesArray.length; i++) {
//            final String excluded = excludedResourcesArray[i].trim();
//            if (excluded.endsWith("*.groovy"))
//                continue;
//            excludedResources.add(excluded);
//        }
//        javaProject.setOption(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER,
//                StringUtils.join(excludedResources, ","));
//    }

    private static void internalAddGroovyClasspathContainer(IJavaProject javaProject, boolean isMinimal) {
        try {
            if (javaProject == null) {
                return;
            }

            if (hasGroovyClasspathContainer(javaProject)) {
                removeGroovyClasspathContainer(javaProject);
            }
            final IClasspathEntry containerEntry = createContainerEntry(isMinimal);
            addClassPathEntry(javaProject, containerEntry);
        } catch (final CoreException ce) {
            GroovyCore.logException("Failed to add groovy classpath container:" + ce.getMessage(), ce);
            throw new RuntimeException(ce);
        }
    }

    private static IClasspathEntry createContainerEntry(boolean isMinimal) {
        return JavaCore.newContainerEntry(GroovyClasspathContainer.CONTAINER_ID, new IAccessRule[0],
                (isMinimal ? GroovyClasspathContainer.MINIMAL_ATTRIBUTE_ARR : new IClasspathAttribute[0]), true);
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
            for (int i = 0; i < rawClasspath.length; i++) {
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
                workDone = true;
            }

            if (workDone) {
                javaProject.setRawClasspath(rawClasspath, null);
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Problem setting groovy classpath container", e);
        }
    }

    public static void removeGroovyClasspathContainer(
            final IJavaProject javaProject) {
        removeClasspathContainer(GroovyClasspathContainer.CONTAINER_ID, javaProject);
    }

    public static void removeClasspathContainer(IPath containerPath, IJavaProject javaProject) {
        try {
            if (!hasGroovyClasspathContainer(javaProject)) {
                return;
            }

            IClasspathEntry[] entries = javaProject.getRawClasspath();
            int removeIndex = -1;
            for (int i = 0; i < entries.length; i++) {
                if (entries[i].getPath().equals(containerPath)) {
                    removeIndex = i;
                    break;
                }
            }
            IClasspathEntry[] newEntries = (IClasspathEntry[]) ArrayUtils.remove(entries, removeIndex);
            javaProject.setRawClasspath(newEntries, null);
        } catch (final CoreException ce) {
            GroovyCore.logException("Failed to add groovy classpath container:"
                    + ce.getMessage(), ce);
            throw new RuntimeException(ce);
        }
    }

    /**
     * Adds a library/folder that already exists in the project to the
     * classpath. Only added if it is not already on the classpath.
     *
     * @param javaProject
     *            The project to add add the classpath entry to.
     * @param libraryPath
     *            The path to add to the classpath.
     * @param isExported TODO
     * @throws JavaModelException
     */
    public static void addLibraryToClasspath(final IJavaProject javaProject,
            final IPath libraryPath, boolean isExported) throws JavaModelException {

        boolean alreadyExists = includesClasspathEntry(javaProject, libraryPath
                .lastSegment());
        if (alreadyExists) {
            return;
        }
        addClassPathEntry(javaProject, new ClasspathEntry(IPackageFragmentRoot.K_BINARY, IClasspathEntry.CPE_CONTAINER,
                libraryPath,
                ClasspathEntry.INCLUDE_ALL, // inclusion patterns
                ClasspathEntry.EXCLUDE_NONE, // exclusion patterns
                null, null, null, // specific output folder
                true, // exported
                ClasspathEntry.NO_ACCESS_RULES, false, // no access rules to
                                                       // combine
                ClasspathEntry.NO_EXTRA_ATTRIBUTES));
    }

    public static void addGroovyNature(final IProject project)
            throws CoreException {
        GroovyCore.trace("GroovyRuntime.addGroovyNature()");
        final IProjectDescription description = project.getDescription();
        final String[] ids = description.getNatureIds();

        // add groovy nature at the start so that its image will be shown
        final String[] newIds = new String[ids == null ? 1 : ids.length + 1];
        newIds[0] = GroovyNature.GROOVY_NATURE;
        if (ids != null) {
            for (int i = 1; i < newIds.length; i++) {
                newIds[i] = ids[i - 1];
            }
        }

        description.setNatureIds(newIds);
        project.setDescription(description, null);
    }

    /**
     * Adds a classpath entry to the end of a project's the classpath
     *
     * @param project
     *            The project to add the entry to.
     * @param newEntry
     *            The entry to add.
     * @throws JavaModelException
     */
    public static void addClassPathEntry(IJavaProject project,
            IClasspathEntry newEntry) throws JavaModelException {
        IClasspathEntry[] newEntries = (IClasspathEntry[]) ArrayUtils.add(
                project.getRawClasspath(), newEntry);
        project.setRawClasspath(newEntries, null);
    }

    /**
     * Adds a classpath entry to the front of a project's the classpath
     *
     * @param project
     *            The project to add the entry to.
     * @param newEntry
     *            The entry to add.
     * @throws JavaModelException
     */
    public static void addClassPathEntryToFront(IJavaProject project, IClasspathEntry newEntry) throws JavaModelException {
        IClasspathEntry[] newEntries = (IClasspathEntry[]) ArrayUtils.add(project.getRawClasspath(), 0, newEntry);
        project.setRawClasspath(newEntries, null);
    }

    /**
     * Removes a classpath entry to a project
     *
     * @param project
     *            The project to remove the entry.
     * @param newEntry
     *            The entry to remove.
     * @throws JavaModelException
     */
    public static void removeClassPathEntry(IJavaProject project,
            IClasspathEntry newEntry) throws JavaModelException {
        IClasspathEntry[] newEntries = (IClasspathEntry[]) ArrayUtils.removeElement(
                project.getRawClasspath(), newEntry);
        project.setRawClasspath(newEntries, null);
    }

    /**
     * Looks through a set of classpath entries and checks to see if the path is
     * in them.
     *
     * @param project
     *            The project to search.
     * @param possiblePath
     *            The path to check the entries for.
     * @return If possiblePath is included in entries returns true, otherwise
     *         returns false.
     * @throws JavaModelException
     */
    private static boolean includesClasspathEntry(IJavaProject project,
            String entryName) throws JavaModelException {
        IClasspathEntry[] entries = project.getRawClasspath();
        for (int i = 0; i < entries.length; i++) {
            IClasspathEntry entry = entries[i];
            if (entry.getPath().lastSegment().equals(entryName)) {
                return true;
            }
        }
        return false;
    }
}
