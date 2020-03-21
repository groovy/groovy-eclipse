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
package org.codehaus.groovy.eclipse.actions;

import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class AddGroovyClasspathContainerAction extends AbstractAddClasspathContainerAction {

    @Override
    protected IPath getClasspathContainerPath() {
        return new Path(GroovyClasspathContainer.ID);
    }

    @Override
    protected String addText() {
        return "Add " + GroovyClasspathContainer.NAME + " to classpath";
    }

    @Override
    protected String removeText() {
        return "Remove " + GroovyClasspathContainer.NAME + " from classpath";
    }

    @Override
    protected String disabledText() {
        return "Cannot add Groovy libraries";
    }

    @Override
    protected String errorMessage() {
        return "Failed to add Groovy libraries";
    }
}
