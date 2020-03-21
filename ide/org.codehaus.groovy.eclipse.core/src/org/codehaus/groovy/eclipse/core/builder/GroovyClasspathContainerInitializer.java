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
package org.codehaus.groovy.eclipse.core.builder;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class GroovyClasspathContainerInitializer extends ClasspathContainerInitializer {

    @Override
    public String getDescription(final IPath containerPath, final IJavaProject javaProject) {
        return GroovyClasspathContainer.NAME + " for " + javaProject.getElementName();
    }

    @Override
    public void initialize(IPath containerPath, final IJavaProject javaProject) throws CoreException {
        if (containerPath.segmentCount() == 1 && !GroovyRuntime.findClasspathEntry(javaProject, cpe ->
                GroovyClasspathContainer.ID.equals(cpe.getPath().segment(0))).filter(GroovyClasspathContainer::hasLegacyMinimalAttribute).isPresent()) {
            String val = GroovyClasspathContainer.getLegacyUserLibsPreference(javaProject);
            if (!"default".equals(val)) {
                containerPath = containerPath.append("user-libs=" + Boolean.valueOf(val).toString());
            }
        }
        JavaCore.setClasspathContainer(containerPath, new IJavaProject[]{javaProject}, new IClasspathContainer[]{new GroovyClasspathContainer(containerPath)}, null);
    }

    @Override
    public boolean canUpdateClasspathContainer(final IPath containerPath, final IJavaProject javaProject) {
        return true;
    }

    @Override
    public void requestClasspathContainerUpdate(final IPath containerPath, final IJavaProject javaProject, final IClasspathContainer containerSuggestion) throws CoreException {
        IClasspathContainer container = JavaCore.getClasspathContainer(containerPath, javaProject);
        if (container instanceof GroovyClasspathContainer) {
            ((GroovyClasspathContainer) container).reset();
        }
    }
}
