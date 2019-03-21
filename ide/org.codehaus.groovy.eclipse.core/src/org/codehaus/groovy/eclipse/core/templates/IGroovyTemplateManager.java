/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.templates;

import java.io.IOException;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Implementation of this interface can execute templates to generate new Groovy
 * classes or tests.
 */
@FunctionalInterface
public interface IGroovyTemplateManager {

    /**
     * Processes the underlying template
     *
     * @param bindings
     *            The bindings to use in the template
     * @param progressMonitor
     *            A ProgressMonitor to visualize the progress
     * @return The result of the processed template
     */
    String processTemplate(Map<String, Object> bindings, IProgressMonitor progressMonitor) throws CompilationFailedException, IOException;
}
