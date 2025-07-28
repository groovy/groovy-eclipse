/*
 * Copyright 2009-2025 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.ui

import static org.codehaus.groovy.eclipse.GroovyPlugin.getDefault as getGroovyPlugin

import org.codehaus.groovy.eclipse.editor.highlighting.HighlightingExtenderRegistry
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.junit.Before
import org.junit.Test

final class HighlightingExtenderTests extends GroovyEclipseTestSuite {

    private final HighlightingExtenderRegistry registry = groovyPlugin.textTools.highlightingExtenderRegistry

    @Before
    void setUp() {
        registry.initialize()
        removeNature(Extender1.NATURE1, Extender2.NATURE2)
        addNature   (Extender1.NATURE1, Extender2.NATURE2)
    }

    @Test
    void testHighlightingExtender1() {
        withProject {
            def extraRules = registry.getAdditionalRulesForProject(it)
            assert extraRules[0] == Extender2.RULE
        }
    }

    @Test
    void testHighlightingExtender2() {
        withProject {
            def extraKeywords = registry.getExtraGJDKKeywordsForProject(it)
            assert extraKeywords[0] == Extender1.GJDK_KEYWORD
        }
    }

    @Test
    void testHighlightingExtender3() {
        withProject {
            def extraKeywords = registry.getExtraGroovyKeywordsForProject(it)
            assert extraKeywords[0] == Extender1.GROOVY_KEYWORD
        }
    }

    @Test
    void testHighlightingExtender4() {
        withProject {
            def extraInitialRules = registry.getInitialAdditionalRulesForProject(it)
            assert extraInitialRules[0] == Extender2.INITIAL_RULE
        }
    }
}
