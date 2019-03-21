/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor.highlighting;

import java.util.List;

import org.eclipse.jface.text.rules.IRule;

/**
 * Extends the Groovy Editor with new Groovy keywords and
 * syntax highlighting rules.  See the extension point
 * org.codehaus.groovy.eclipse.ui.syntaxHighlightingExtension
 */
public interface IHighlightingExtender {

    /**
     * Provides a list of extra keywords to highlight in the same
     * color as all other groovy keywords
     */
    List<String> getAdditionalGroovyKeywords();

    /**
     * Provides a list of extra keywords to highlight in the same
     * color as all other gjdk keywords
     */
    List<String> getAdditionalGJDKKeywords();

    /**
     * Provides a list of additional highlighting rules
     */
    List<IRule> getAdditionalRules();
}
