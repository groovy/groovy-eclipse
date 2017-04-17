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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.codehaus.groovy.activator.GroovyActivator;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.eclipse.core.runtime.Platform;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * Ensures the proper compiler level is being used.
 */
public class SanityTest {

    private static final java.io.PrintStream out = System.out;

    @Rule
    public TestName test = new TestName();

    @Before
    public void setUp() throws Exception {
        out.println("----------------------------------------");
        out.println("Starting: " + test.getMethodName());

        GroovyActivator.initialize();
        out.println("ClassLoader location " + GroovyActivator.class.getClassLoader().getResource("."));
    }

    private Version getEclipsePlatformVersion() {
        Bundle eclipsePlatform = Platform.getBundle("org.eclipse.platform");
        return eclipsePlatform == null ? null : eclipsePlatform.getVersion();
    }

    private Version getEclipseVersion() {
        Bundle jdtCore = Platform.getBundle("org.eclipse.jdt.core");
        assertThat("Can't find JDT Core", jdtCore, notNullValue());
        return jdtCore.getVersion();
    }

    private Version getGroovyCompilerVersion() {
        Version version = CompilerUtils.getActiveGroovyBundle().getVersion();
        assertThat(version.getMajor(), equalTo(CompilerUtils.getWorkspaceCompilerLevel().majorVersion));
        assertThat(version.getMinor(), equalTo(CompilerUtils.getWorkspaceCompilerLevel().minorVersion));
        return version;
    }

    @Test
    public void testCompilerVersion() {
        out.println("Eclipse Platform version " + getEclipsePlatformVersion());
        out.println("JDT version " + getEclipseVersion());
        Version groovyVersion = getGroovyCompilerVersion();
        out.println("Groovy version " + groovyVersion);

        //Ideally:
        // JDT 3.7 test against Groovy 2.1
        // JDT 3.8 test against Groovy 2.1
        // JDT 3.9 test against Groovy 2.2
        // JDT 3.10 test against Groovy 2.3

        assertThat(groovyVersion.getMajor() + "." + groovyVersion.getMinor(), equalTo("2.5"));
    }

    @Test
    public void testCompilerJars() {
        assertThat("Couldn't find groovy-all jar", GroovyActivator.GROOVY_ALL_JAR_URL, notNullValue());
    }
}
