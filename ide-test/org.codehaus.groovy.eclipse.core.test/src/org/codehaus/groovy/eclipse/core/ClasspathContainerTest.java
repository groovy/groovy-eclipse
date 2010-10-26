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
package org.codehaus.groovy.eclipse.core;

import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.core.JavaModelManager;

public class ClasspathContainerTest extends EclipseTestCase {
    public void testClassPathContainerContents() throws Exception {
        GroovyClasspathContainer container = (GroovyClasspathContainer)
            JavaModelManager.getJavaModelManager().getClasspathContainer(new Path("GROOVY_SUPPORT"), testProject.getJavaProject());
        IClasspathEntry[] entries = container.getClasspathEntries();

        boolean groovyAllFound = false;
        for (IClasspathEntry entry : entries) {
            String pathStr = entry.getPath().toPortableString();
            if (pathStr.indexOf("groovy-all") != -1) {
                if (groovyAllFound) {
                    fail("Groovy-all found twice in Groovy Classpath container: " + entry);
                }
                groovyAllFound = true;
            } else if (pathStr.indexOf("/org.codehaus.groovy") == -1) {
                // fail if there is a path that is not inside of the groovy
                // plugin
                fail("Unexpected entry in Groovy Classpath container: " + entry);
            }
        }

    }
}
