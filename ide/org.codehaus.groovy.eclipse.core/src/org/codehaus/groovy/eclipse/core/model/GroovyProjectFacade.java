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
package org.codehaus.groovy.eclipse.core.model;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Provides some useful methods for accessing Groovy state.
 */
@Deprecated
public class GroovyProjectFacade extends org.codehaus.jdt.groovy.model.GroovyProjectFacade {

    @Deprecated // use GroovyNature.hasGroovyNature(IProject)
    public static boolean isGroovyProject(IProject proj) {
        try {
            return proj.hasNature(GroovyNature.GROOVY_NATURE);
        } catch (CoreException e) {
            GroovyCore.logException("Error getting project nature: " + proj.getName(), e);
            return false;
        }
    }

    public GroovyProjectFacade(IJavaElement element) {
        super(element);
    }

    public GroovyProjectFacade(IJavaProject project) {
        super(project);
    }
}
