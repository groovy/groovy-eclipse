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

import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;

/**
 * Tests that the compiler can successfully be switched back and
 * forth between versions
 * @author Andrew Eisenberg
 * @created Oct 11, 2009
 *
 */
public class CompilerSwitchTests extends EclipseTestCase {
    public void testClassPathContainerContents() throws Exception {
        // we no longer have any ui for compiler switching,
        // so this test is useless
        System.out.println("This test is disabled.");
        if (true) {
            return;
        }

        String current = CompilerUtils.getGroovyVersion();
        assertTrue("Compiler version should default to 1.7, but is instead" + current,
                current.contains("1.7"));
        String other = CompilerUtils.getOtherVersion();
        assertTrue("Other compiler version should be 1.6, but is instead" + other,
                other.contains("1.6"));

        // switch to 1.6
        CompilerUtils.switchVersions(false);

        current = CompilerUtils.getGroovyVersion();

        assertTrue("Compiler version should be 1.6, but is instead" + current,
                current.contains("1.6"));

        other = CompilerUtils.getOtherVersion();
        assertTrue("Other compiler version should be 1.7, but is instead" + other,
                other.contains("1.7"));

        // switch back to 1.7
        CompilerUtils.switchVersions(true);

        current = CompilerUtils.getGroovyVersion();
        assertTrue("Compiler version should be 1.7, but is instead" + current,
                current.contains("1.7"));

        other = CompilerUtils.getOtherVersion();
        assertTrue("Other compiler version should be 1.6, but is instead" + other,
                other.contains("1.6"));



    }
}
