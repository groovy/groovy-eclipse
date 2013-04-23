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

import org.codehaus.groovy.activator.GroovyActivator;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
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
        Version version = CompilerUtils.getActiveGroovyBundle().getVersion();
        assertEquals(CompilerUtils.getWorkspaceCompilerLevel().majorVersion, version.getMajor());
        assertEquals(CompilerUtils.getWorkspaceCompilerLevel().minorVersion, version.getMinor());
        return version;
    }
    
    public void testCompilerVersion() throws Exception {
        Version jdtVersion = getEclipseVersion();
        Version groovyVersion = getGroovyCompilerVersion();
        
        System.out.println("---------------------------------------");
        System.out.println("SanityTest.testCompilerVersion()");
        System.out.println("Using JDT version " + jdtVersion);
        System.out.println("Using Groovy version " + groovyVersion);
        System.out.println("Classloader location" + GroovyActivator.class.getClassLoader().getResource("."));
        System.out.println("Groovy bundle status "+ (Platform.getBundle("org.codehaus.groovy").getState() == Bundle.ACTIVE ? "ACTIVE" : "NOT ACTIVE"));
        System.out.println("Groovy bundle version "+ Platform.getBundle("org.codehaus.groovy").getVersion());
        System.out.println("---------------------------------------");
        
        // JDT 3.7 and 3.8 test against Groovy 2.0
        // JDT 3.9 test against Groovy 2.1
        if (jdtVersion.getMinor() == 8 || jdtVersion.getMinor() == 7) {
            assertEquals(2, groovyVersion.getMajor());
            assertEquals(0, groovyVersion.getMinor());
        } else if (jdtVersion.getMinor() == 9) {
            assertEquals(2, groovyVersion.getMajor());
            assertEquals(1, groovyVersion.getMinor());
        }
    }
    
    public void testCompilerJars() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("SanityTest.testCompilerJars()");
        System.out.println("Classloader location" + GroovyActivator.class.getClassLoader().getResource("."));
        assertNotNull("Couldn't find groovy-all jar", GroovyActivator.GROOVY_ALL_JAR_URL);
        assertNotNull("Couldn't find groovy jar", GroovyActivator.GROOVY_JAR_URL);
              }
    
    public static void main(String[] args) {
    }
}
