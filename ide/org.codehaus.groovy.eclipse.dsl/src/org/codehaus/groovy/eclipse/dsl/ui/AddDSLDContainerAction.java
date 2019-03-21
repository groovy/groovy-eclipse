/*
 * Copyright 2009-2018 the original author or authors.
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

import org.codehaus.groovy.eclipse.actions.AbstractAddClasspathContainerAction;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;

public class AddDSLDContainerAction extends AbstractAddClasspathContainerAction {

    @Override
    protected IPath getClasspathContainerPath() {
        return GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID;
    }

    @Override
    protected String disabledText() {
        return "Cannot add DSL support";
    }

    @Override
    protected String errorMessage() {
        return "Failed to add DSL support";
    }

    @Override
    protected String addText() {
        return "Add Groovy DSL Support to classpath";
    }

    @Override
    protected String removeText() {
        return "Remove Groovy DSL Support from classpath";
    }

    @Override
    public void run(IAction action) {
        super.run(action);
        GroovyDSLCoreActivator.getDefault().getContainerListener().ignoreProject(targetProject.getProject());
    }
}
