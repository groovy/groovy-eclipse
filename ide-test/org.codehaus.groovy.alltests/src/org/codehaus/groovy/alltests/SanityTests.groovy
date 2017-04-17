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
package org.codehaus.groovy.alltests

import org.codehaus.groovy.activator.GroovyActivator
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils
import org.eclipse.core.runtime.Platform
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.osgi.framework.Version

/**
 * Ensures the proper compiler level is being used.
 */
final class SanityTests {

    @Rule
    public TestName test = new TestName()

    @Before
    void setUp() {
        println '----------------------------------------'
        println 'Starting: ' + test.getMethodName()

        GroovyActivator.initialize()
        println 'ClassLoader location: ' + GroovyActivator.getClassLoader().getResource('.')
    }

    @Test
    void testCompilerVersion() {
        Version groovyVersion = CompilerUtils.getActiveGroovyBundle().getVersion()
        assert groovyVersion.major == CompilerUtils.getWorkspaceCompilerLevel().majorVersion
        assert groovyVersion.minor == CompilerUtils.getWorkspaceCompilerLevel().minorVersion

        println "Eclipse Platform version: ${Platform.getBundle('org.eclipse.platform')?.getVersion()}"
        println "JDT Core version: ${Platform.getBundle('org.eclipse.jdt.core').getVersion()}"
        println "Groovy version: $groovyVersion"

        // Ideally:
        // JDT 3.7 test against Groovy 2.1
        // JDT 3.8 test against Groovy 2.1
        // JDT 3.9 test against Groovy 2.2
        // JDT 3.10 test against Groovy 2.3

        assert "${groovyVersion.major}.${groovyVersion.minor}" == '2.5'
    }

    @Test
    void testGroovyJar() {
        assert GroovyActivator.GROOVY_ALL_JAR_URL != null
    }
}
