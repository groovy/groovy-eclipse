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

package org.codehaus.groovy.eclipse.test.ui;

import java.util.List;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.editor.highlighting.HighlightingExtenderRegistry;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.eclipse.jface.text.rules.IRule;

/**
 * @author Andrew Eisenberg
 * @created Oct 23, 2009
 *
 */
public class HighlightingExtenderTests extends EclipseTestCase {
    private HighlightingExtenderRegistry registry;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        registry = GroovyPlugin.getDefault().getTextTools().getHighlightingExtenderRegistry();
        registry.initialize();
    }
    public void testHighlightingExtender1() throws Exception {
        testProject.addNature(Extender1.NATURE1);
        testProject.addNature(Extender2.NATURE2);
        
        List<IRule> extraRules = registry.getAdditionalRulesForProject(testProject.getProject());
        assertEquals(Extender2.RULE, extraRules.get(0));
    }
    public void testHighlightingExtender2() throws Exception {
        testProject.addNature(Extender1.NATURE1);
        testProject.addNature(Extender2.NATURE2);
        
        List<String> extraKeywords = registry.getExtraGJDKKeywordsForProject(testProject.getProject());
        assertEquals(Extender1.GJDK_KEYWORD, extraKeywords.get(0));
    }
    public void testHighlightingExtender3() throws Exception {
        testProject.addNature(Extender1.NATURE1);
        testProject.addNature(Extender2.NATURE2);
        
        List<String> extraKeywords = registry.getExtraGroovyKeywordsForProject(testProject.getProject());
        assertEquals(Extender1.GROOVY_KEYWORD, extraKeywords.get(0));
    }
}
