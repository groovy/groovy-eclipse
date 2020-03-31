/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.model;

import static org.codehaus.groovy.ast.ClassHelper.GROOVY_OBJECT_TYPE;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * This class contains all the utility methods used in adding the Groovy Runtime
 * to a Java project.
 */
public class GroovyRuntime {

    // Breaking encapsulation here. We don't want to specify this classpath
    // container here because it is defined in a different plugin, but this
    // is the least complicated way pf doing it.
    public static final String DSLD_CONTAINER_ID = "GROOVY_DSL_SUPPORT";

    //

    public static void addGroovyRuntime(final IProject project) {
        GroovyCore.trace("GroovyRuntime.addGroovyRuntime(IProject)");
        try {
            if (project != null && project.isAccessible() && project.hasNature(JavaCore.NATURE_ID) && !project.hasNature(GroovyNature.GROOVY_NATURE)) {
                IJavaProject javaProject = JavaCore.create(project);
                // this breaks encapsulation, but seems the most logical place to put it:
                if (!findClasspathEntry(javaProject, cpe -> DSLD_CONTAINER_ID.equals(cpe.getPath().segment(0))).isPresent()) {
                    appendClasspathEntry(javaProject, JavaCore.newContainerEntry(new Path(DSLD_CONTAINER_ID)));
                }
                addGroovyClasspathContainer(javaProject);
                addGroovyNature(project);
            }
        } catch (Exception e) {
            GroovyCore.logException("Failed to add groovy runtime support", e);
        }
    }

    public static void addGroovyNature(final IProject project) throws CoreException {
        GroovyCore.trace("GroovyRuntime.addGroovyNature(IProject)");
        IProjectDescription description = project.getDescription();
        // add nature first so that its image will be shown
        description.setNatureIds((String[]) ArrayUtils.add(description.getNatureIds(), 0, GroovyNature.GROOVY_NATURE));
        project.setDescription(description, null); // NOTE: throws ResourceException if .project file is read-only

        IJavaProject javaProject = JavaCore.create(project);
        IType type = javaProject.findType(GROOVY_OBJECT_TYPE.getName());
        if (type != null && type.exists()) RequireModuleOperation.requireModule(javaProject, type);
    }

    public static void removeGroovyNature(final IProject project) throws CoreException {
        GroovyCore.trace("GroovyRuntime.removeGroovyNature(IProject)");
        IProjectDescription description = project.getDescription();
        if (description.hasNature(GroovyNature.GROOVY_NATURE)) {
            description.setNatureIds((String[]) ArrayUtils.removeElement(description.getNatureIds(), GroovyNature.GROOVY_NATURE));
            project.setDescription(description, null); // NOTE: throws ResourceException if .project file is read-only
        }
    }

    //--------------------------------------------------------------------------

    public static void addGroovyClasspathContainer(final IJavaProject javaProject) {
        addGroovyClasspathContainer(javaProject, false);
    }

    public static void addGroovyClasspathContainer(final IJavaProject javaProject, final boolean isMinimal) {
        try {
            if (hasGroovyClasspathContainer(javaProject)) {
                removeGroovyClasspathContainer(javaProject);
            }
            appendClasspathEntry(javaProject, newGroovyClasspathContainerEntry(isMinimal, JavaRuntime.isModularProject(javaProject), null));
        } catch (CoreException e) {
            GroovyCore.logException("Failed to add groovy classpath container:" + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static boolean hasGroovyClasspathContainer(final IJavaProject javaProject) throws JavaModelException {
        return findClasspathEntry(javaProject, cpe -> GroovyClasspathContainer.ID.equals(cpe.getPath().segment(0))).isPresent();
    }

    public static void removeGroovyClasspathContainer(final IJavaProject javaProject) throws JavaModelException {
        findClasspathEntry(javaProject, cpe -> GroovyClasspathContainer.ID.equals(cpe.getPath().segment(0))).ifPresent(cpe -> removeClasspathEntry(javaProject, cpe));
    }

    public static IClasspathEntry newGroovyClasspathContainerEntry(final boolean isMinimal, final boolean isModular, final Boolean userLibs) {
        IPath containerPath = new Path(GroovyClasspathContainer.ID);
        if (isMinimal) {
            containerPath = containerPath.append("minimal");
        } else if (Boolean.TRUE.equals(userLibs)) {
            containerPath = containerPath.append("user-libs=true");
        } else if (Boolean.FALSE.equals(userLibs)) {
            containerPath = containerPath.append("user-libs=false");
        }

        IClasspathAttribute[] attributes = ClasspathEntry.NO_EXTRA_ATTRIBUTES;
        if (isModular) {
            attributes = new IClasspathAttribute[] {JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true")};
        }

        return JavaCore.newContainerEntry(containerPath, ClasspathEntry.NO_ACCESS_RULES, attributes, false);
    }

    //--------------------------------------------------------------------------

    public static void appendClasspathEntry(final IJavaProject javaProject, final IClasspathEntry entry) throws JavaModelException {
        javaProject.setRawClasspath((IClasspathEntry[]) ArrayUtils.add(javaProject.getRawClasspath(), entry), null);
    }

    public static void prependClasspathEntry(final IJavaProject javaProject, final IClasspathEntry entry) throws JavaModelException {
        javaProject.setRawClasspath((IClasspathEntry[]) ArrayUtils.add(javaProject.getRawClasspath(), 0, entry), null);
    }

    public static void removeClasspathEntry(final IJavaProject javaProject, final IClasspathEntry entry) {
        try {
            javaProject.setRawClasspath((IClasspathEntry[]) ArrayUtils.removeElement(javaProject.getRawClasspath(), entry), null);
        } catch (JavaModelException e) {
            GroovyCore.logException("Failed to remove classpath container: " + entry, e);
        }
    }

    public static Optional<IClasspathEntry> findClasspathEntry(final IJavaProject javaProject, final Predicate<IClasspathEntry> p) throws JavaModelException {
        return Arrays.stream(javaProject.getRawClasspath()).filter(p).findFirst();
    }

    //--------------------------------------------------------------------------

    /**
     * Could be used to exclude all groovy files from compilation.
     */
    /*public static void excludeGroovyFilesFromOutput(final IJavaProject javaProject) {
        // make sure .groovy files are not copied to the output dir
        String excludedResources = javaProject.getOption(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, true);
        if (excludedResources.indexOf("*.groovy") == -1) {
            excludedResources = excludedResources.length() == 0 ? "*.groovy" : excludedResources + ",*.groovy";
            javaProject.setOption(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, excludedResources);
        }
    }*/

    /**
     * Could be used to include all groovy files for compilation.
     */
    /*public static void includeGroovyFilesInOutput(final IJavaProject javaProject) {
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
}
