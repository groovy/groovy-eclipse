/*
 * Copyright 2009-2024 the original author or authors.
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
        var config = CompilerConfiguration.DEFAULT;

        assertNull(config.getJointCompilationOptions());
        assertNull(config.getDisabledGlobalASTTransformations());
        assertEquals(Collections.emptyList(), config.getClasspath());
        assertEquals(Collections.emptyList(), config.getCompilationCustomizers());
        assertEquals(Collections.singleton("groovy"), config.getScriptExtensions());
        assertEquals(isAtLeastGroovy(40) ? System.getProperty("java.specification.version") : "1.8", config.getTargetBytecode());
    }

    @Test
    public void testTargetVersion() {
        var config = new CompilerConfiguration();

        var x18x = isAtLeastGroovy(40) ? "18" : "17";
        var x19x = isAtLeastGroovy(40) ? "19" : "17";
        var x20x = isAtLeastGroovy(40) ? "20" : "17";
        var x21x = isAtLeastGroovy(40) ? "21" : "17";
        var x22x = isAtLeastGroovy(40) ? "22" : "17";
        var x23x = isAtLeastGroovy(40) ? "23" : "17";

        String[] inputs = {"1.3", "1.4", "1.5", "1.6", "1.7", "1.8", "1.9", "5",   "6",   "7",   "8",   "9", "9.0", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24"};
        String[] expect = {"1.4", "1.4", "1.5", "1.6", "1.7", "1.8", "9",   "1.5", "1.6", "1.7", "1.8", "9", "9",   "10", "11", "12", "13", "14", "15", "16", "17", x18x, x19x, x20x, x21x, x22x, x23x, x23x};

        if (isAtLeastGroovy(50)) Arrays.fill(expect, 0, 14, "11");
        assertArrayEquals(expect, Arrays.stream(inputs).map(v -> { config.setTargetBytecode(v); return config.getTargetBytecode(); }).toArray(String[]::new));
    }
}
