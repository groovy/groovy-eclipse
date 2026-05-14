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
package org.codehaus.groovy.eclipse.dsl;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Listens for classpath changes and refreshes DSLDs accordingly.  Only looks
 * for raw classpath changes all other changes are handled by the {@link DSLDResourceListener}.
 */
public class DSLDElementListener implements IElementChangedListener {

    @Override
    public void elementChanged(ElementChangedEvent event) {
        // the root delta is always the JavaModel
        if (event.getType() == ElementChangedEvent.POST_CHANGE && event.getDelta() != null) {
            List<IProject> projectsToRefresh = new ArrayList<>();
            for (IJavaElementDelta delta : event.getDelta().getChangedChildren()) {
                // Look for resolved classpath changes for Groovy projects
                if (delta.getElement() instanceof IJavaProject &&
                        isResolvedClasspathChangeNotRawClasspath(delta) &&
                        GroovyNature.hasGroovyNature(((IJavaProject) delta.getElement()).getProject())) {
                    projectsToRefresh.add(((IJavaProject) delta.getElement()).getProject());
                }
            }
            if (!projectsToRefresh.isEmpty()) {
                GroovyDSLCoreActivator.getDefault().getContextStoreManager().initialize(projectsToRefresh, false);
            }
        }
    }

    // returns true if there is a change to a classpath container, or something else that does not
    // show up in the .classpath file.
    private boolean isResolvedClasspathChangeNotRawClasspath(IJavaElementDelta delta) {
        return (delta.getFlags() & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0 &&
                (delta.getFlags() & IJavaElementDelta.F_CLASSPATH_CHANGED) == 0;
    }
}
