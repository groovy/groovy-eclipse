/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.alltests

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
        println 'Starting: ' + test.methodName
    }

    @Test
    void testCompilerVersion() {
        Version groovyVersion = CompilerUtils.activeGroovyBundle.version
        assert groovyVersion.major == CompilerUtils.workspaceCompilerLevel.majorVersion
        assert groovyVersion.minor == CompilerUtils.workspaceCompilerLevel.minorVersion

        println "Eclipse Platform version: ${Platform.getBundle('org.eclipse.platform')?.version}"
        println "JDT Core version: ${Platform.getBundle('org.eclipse.jdt.core').version}"
        println "Groovy version: $groovyVersion"

        int major = groovyVersion.major,
            minor = groovyVersion.minor
        assert "${major}.${minor}" == '4.0'
    }
}
