/*
 * Copyright 2009-2017 the original author or authors.
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
 * Ensures the proper compiler level is being used.
 */
public class SanityTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        GroovyActivator.initialize();
        super.setUp();
    }

    private Version getEclipsePlatformVersion() {
        Bundle eclipseplatform = Platform.getBundle("org.eclipse.platform");
        System.out.println("org.eclipse.platform?" + eclipseplatform);
        return eclipseplatform == null ? null : eclipseplatform.getVersion();
    }

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
        Version eclipseplatformVersion = getEclipsePlatformVersion();
        Version groovyVersion = getGroovyCompilerVersion();

        System.err.println("---------------------------------------");
        System.err.println("SanityTest.testCompilerVersion()");
        System.err.println("Eclipse Platform " + eclipseplatformVersion);
        System.err.println("Using JDT version " + jdtVersion);
        System.err.println("Using Groovy version " + groovyVersion);
        System.err.println("Classloader location" + GroovyActivator.class.getClassLoader().getResource("."));
        System.err.println("Groovy bundle status "+ (Platform.getBundle("org.codehaus.groovy").getState() == Bundle.ACTIVE ? "ACTIVE" : "NOT ACTIVE"));
        System.err.println("Groovy bundle version "+ Platform.getBundle("org.codehaus.groovy").getVersion());
        System.err.println("---------------------------------------");

        //Ideally:
        // JDT 3.7 test against Groovy 2.1
        // JDT 3.8 test against Groovy 2.1
        // JDT 3.9 test against Groovy 2.2
        // JDT 3.10 test against Groovy 2.3

        assertEquals("2.5", groovyVersion.getMajor() + "." +groovyVersion.getMinor());
    }

    public void testCompilerJars() {
        System.out.println("---------------------------------------");
        System.out.println("SanityTest.testCompilerJars()");
        System.out.println("Classloader location " + GroovyActivator.class.getClassLoader().getResource("."));
        assertNotNull("Couldn't find groovy-all jar", GroovyActivator.GROOVY_ALL_JAR_URL);
    }
}
