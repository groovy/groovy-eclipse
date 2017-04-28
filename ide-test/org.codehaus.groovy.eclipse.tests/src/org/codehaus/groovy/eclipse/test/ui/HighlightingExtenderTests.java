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
package org.codehaus.groovy.eclipse.test.ui;

import java.util.List;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.editor.highlighting.HighlightingExtenderRegistry;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.eclipse.jface.text.rules.IRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public final class HighlightingExtenderTests extends EclipseTestCase {

    private HighlightingExtenderRegistry registry;

    @Before
    public void setUp() throws Exception {
        registry = GroovyPlugin.getDefault().getTextTools().getHighlightingExtenderRegistry();
        registry.initialize();
    }

    @Test
    public void testHighlightingExtender1() throws Exception {
        testProject.addNature(Extender1.NATURE1);
        testProject.addNature(Extender2.NATURE2);

        List<IRule> extraRules = registry.getAdditionalRulesForProject(testProject.getProject());
        Assert.assertEquals(Extender2.RULE, extraRules.get(0));
    }

    @Test
    public void testHighlightingExtender2() throws Exception {
        testProject.addNature(Extender1.NATURE1);
        testProject.addNature(Extender2.NATURE2);

        List<String> extraKeywords = registry.getExtraGJDKKeywordsForProject(testProject.getProject());
        Assert.assertEquals(Extender1.GJDK_KEYWORD, extraKeywords.get(0));
    }

    @Test
    public void testHighlightingExtender3() throws Exception {
        testProject.addNature(Extender1.NATURE1);
        testProject.addNature(Extender2.NATURE2);

        List<String> extraKeywords = registry.getExtraGroovyKeywordsForProject(testProject.getProject());
        Assert.assertEquals(Extender1.GROOVY_KEYWORD, extraKeywords.get(0));
    }

    @Test
    public void testHighlightingExtender4() throws Exception {
        testProject.addNature(Extender1.NATURE1);
        testProject.addNature(Extender2.NATURE2);

        List<IRule> extraInitialRules = registry.getInitialAdditionalRulesForProject(testProject.getProject());
        Assert.assertEquals(Extender2.INITIAL_RULE, extraInitialRules.get(0));
    }
}
