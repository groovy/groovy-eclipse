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
package org.codehaus.groovy.eclipse.test.ui

import org.codehaus.groovy.eclipse.GroovyPlugin
import org.codehaus.groovy.eclipse.editor.highlighting.HighlightingExtenderRegistry
import org.codehaus.groovy.eclipse.test.EclipseTestCase
import org.eclipse.jface.text.rules.IRule
import org.junit.Before
import org.junit.Test

final class HighlightingExtenderTests extends EclipseTestCase {

    private final HighlightingExtenderRegistry registry = GroovyPlugin.default.textTools.highlightingExtenderRegistry

    @Before
    void setUp() {
        registry.initialize()
    }

    @Test
    void testHighlightingExtender1() {
        testProject.addNature(Extender1.NATURE1)
        testProject.addNature(Extender2.NATURE2)
        List<IRule> extraRules = registry.getAdditionalRulesForProject(testProject.getProject())
        assert extraRules.get(0) == Extender2.RULE
    }

    @Test
    void testHighlightingExtender2() {
        testProject.addNature(Extender1.NATURE1)
        testProject.addNature(Extender2.NATURE2)
        List<String> extraKeywords = registry.getExtraGJDKKeywordsForProject(testProject.getProject())
        assert extraKeywords.get(0) == Extender1.GJDK_KEYWORD
    }

    @Test
    void testHighlightingExtender3() {
        testProject.addNature(Extender1.NATURE1)
        testProject.addNature(Extender2.NATURE2)
        List<String> extraKeywords = registry.getExtraGroovyKeywordsForProject(testProject.getProject())
        assert extraKeywords.get(0) == Extender1.GROOVY_KEYWORD
    }

    @Test
    void testHighlightingExtender4() {
        testProject.addNature(Extender1.NATURE1)
        testProject.addNature(Extender2.NATURE2)
        List<IRule> extraInitialRules = registry.getInitialAdditionalRulesForProject(testProject.getProject())
        assert extraInitialRules.get(0) == Extender2.INITIAL_RULE
    }
}
