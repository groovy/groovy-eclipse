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
package org.codehaus.groovy.eclipse.test.ui;

import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.eclipse.editor.highlighting.IHighlightingExtender;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.rules.IRule;

public class Extender1 implements IHighlightingExtender, IProjectNature {

    public static final String NATURE1 = "org.codehaus.groovy.eclipse.tests.testNature1";

    public static final String GJDK_KEYWORD = "extender1GJDKkeyword";
    public static final String GROOVY_KEYWORD = "extender1Groovykeyword";

    @Override
    public List<String> getAdditionalGJDKKeywords() {
        return Arrays.asList(GJDK_KEYWORD);
    }

    @Override
    public List<String> getAdditionalGroovyKeywords() {
        return Arrays.asList(GROOVY_KEYWORD);
    }

    @Override
    public List<IRule> getAdditionalRules() {
        return null;
    }

    @Override
    public void configure() throws CoreException {
    }

    @Override
    public void deconfigure() throws CoreException {
    }

    IProject p;

    @Override
    public IProject getProject() {
        return p;
    }

    @Override
    public void setProject(IProject project) {
        this.p = project;
    }
}
