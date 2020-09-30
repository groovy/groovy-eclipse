/*
 * Copyright 2009-2020 the original author or authors.
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
package org.eclipse.jdt.groovy.core.tests.basic;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.junit.Test;

public final class ConfigurationTests {

    @Test
    public void testDefaults() {
        CompilerConfiguration config = CompilerConfiguration.DEFAULT;

        assertNull(config.getJointCompilationOptions());
        assertNull(config.getDisabledGlobalASTTransformations());
        assertEquals(Collections.emptyList(), config.getClasspath());
        assertEquals(Collections.emptyList(), config.getCompilationCustomizers());
        assertEquals(Collections.singleton("groovy"), config.getScriptExtensions());
        assertEquals("1.8", config.getTargetBytecode());
    }

    @Test
    public void testTargetVersion() {
        CompilerConfiguration config = new CompilerConfiguration();

        String x15x = isAtLeastGroovy(30) ? "15" : "14";
        String x16x = isAtLeastGroovy(30) ? "16" : "14";

        String[] inputs = {"1.3", "1.4", "1.5", "1.6", "1.7", "1.8", "1.9", "5",   "6",   "7",   "8",   "9", "9.0", "10", "11", "12", "13", "14", "15", "16"};
        String[] expect = {"1.4", "1.4", "1.5", "1.6", "1.7", "1.8", "9",   "1.5", "1.6", "1.7", "1.8", "9", "9",   "10", "11", "12", "13", "14", x15x, x16x};
        assertArrayEquals(expect, Arrays.stream(inputs).map(v -> { config.setTargetBytecode(v); return config.getTargetBytecode(); }).toArray(String[]::new));
    }
}
