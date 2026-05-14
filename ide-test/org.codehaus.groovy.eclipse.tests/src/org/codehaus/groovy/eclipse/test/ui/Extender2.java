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

import org.codehaus.groovy.eclipse.editor.highlighting.IHighlightingExtender2;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordRule;

public class Extender2 implements IHighlightingExtender2, IProjectNature {

    public static final String NATURE2 = "org.codehaus.groovy.eclipse.tests.testNature2";

    public static final IRule INITIAL_RULE = new WordRule(new WordDetectorMock("mainword"));
    public static final IRule RULE = new WordRule(new WordDetectorMock("secondaryword"));

    @Override
    public List<String> getAdditionalGJDKKeywords() {
        return null;
    }

    @Override
    public List<String> getAdditionalGroovyKeywords() {
        return null;
    }

    @Override
    public List<IRule> getInitialAdditionalRules() {
        return Arrays.asList(INITIAL_RULE);
    }

    @Override
    public List<IRule> getAdditionalRules() {
        return Arrays.asList(RULE);
    }

    @Override
    public void configure() throws CoreException {
    }

    @Override
    public void deconfigure() throws CoreException {
    }

    @Override
    public IProject getProject() {
        return p;
    }

    @Override
    public void setProject(IProject project) {
        this.p = project;
    }

    private IProject p;

    private static final class WordDetectorMock implements IWordDetector {

        WordDetectorMock(String word) {
        }

        @Override
        public boolean isWordStart(char c) {
            return false;
        }

        @Override
        public boolean isWordPart(char c) {
            return false;
        }
    }
}
