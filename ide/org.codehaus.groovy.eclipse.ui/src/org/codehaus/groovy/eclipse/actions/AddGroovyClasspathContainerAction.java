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
package org.codehaus.groovy.eclipse.actions;

import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IObjectActionDelegate;

public class AddGroovyClasspathContainerAction extends AbstractAddClasspathContainerAction implements IObjectActionDelegate {
    @Override
    protected IPath getClasspathContainerPath() {
        return GroovyClasspathContainer.CONTAINER_ID;
    }

    @Override
    protected String errorMessage() {
        return "Failed to add Groovy libraries to classpath";
    }

    @Override
    protected String disabledText() {
        return "Cannot add Groovy libraries";
    }

    @Override
    protected String addText() {
        return "Add Groovy libraries to classpath";
    }

    @Override
    protected String removeText() {
        return "Remove Groovy libraries from classpath";
    }

    @Override
    protected boolean exportClasspath() {
        return false;
    }
}
