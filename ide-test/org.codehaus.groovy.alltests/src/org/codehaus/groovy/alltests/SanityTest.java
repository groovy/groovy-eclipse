/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
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
package org.codehaus.groovy.alltests;

import junit.framework.TestCase;

import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.frameworkadapter.util.CompilerLevelUtils;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * Ensure the proper compiler level is being run
 * @author Andrew Eisenberg
 * @created Jun 28, 2012
 */
public class SanityTest extends TestCase {

    private Version getEclipseVersion() {
        Bundle jdtcore = Platform.getBundle("org.eclipse.jdt.core");
        assertNotNull("Can't find jdt core", jdtcore);
        return jdtcore.getVersion();
    }
    
    private Version getGroovyCompilerVersion() {
        return CompilerUtils.getActiveGroovyBundle().getVersion();
    }
    
    public void testCompilerVersion() throws Exception {
        Version jdtVersion = getEclipseVersion();
        Version groovyVersion = getGroovyCompilerVersion();
        
        if (jdtVersion.getMinor() == 8) {
            assertEquals(2, groovyVersion.getMajor());
            assertEquals(0, groovyVersion.getMinor());
        } else if (jdtVersion.getMinor() == 7) {
            assertEquals(1, groovyVersion.getMajor());
            assertEquals(8, groovyVersion.getMinor());
        }
    }
}
