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
package org.codehaus.jdt.groovy.integration.internal;

import org.codehaus.jdt.groovy.integration.EventHandler;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.jdt.internal.core.JavaProject;

class GroovyEventHandler implements EventHandler {

    // Recognized events:
    // "close" - called when a java project is closed
    // "cleanOutputFolders" - called when a clean occurs (either when forced or when part of a full build)
    @Override
    public void handle(final JavaProject javaProject, final String event) {
        if (javaProject != null && event.matches("close|cleanOutputFolders")) {
            GroovyParser.clearCache(javaProject.getElementName());
        }
    }
}
