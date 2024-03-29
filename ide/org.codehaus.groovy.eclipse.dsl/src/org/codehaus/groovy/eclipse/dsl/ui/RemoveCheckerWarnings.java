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
package org.codehaus.groovy.eclipse.dsl.ui;

import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IObjectActionDelegate;

public class RemoveCheckerWarnings extends AbstractCheckerAction implements IObjectActionDelegate {

    @Override
    public void run(IAction action) {
        List<IResource> sel = findSelection();
        if (sel != null) {
            for (IResource resource : sel) {
                try {
                    resource.deleteMarkers(GroovyDSLCoreActivator.MARKER_ID, true, IResource.DEPTH_INFINITE);
                } catch (CoreException e) {
                    GroovyCore.logException(e.getMessage(), e);
                }
            }
        }
    }
}
