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

package org.codehaus.groovy.eclipse.editor.highlighting;

import java.util.List;

import org.eclipse.jface.text.rules.IRule;

/**
 * Extends the Groovy Editor with new Groovy keywords and
 * syntax highlighting rules.  See the extension point 
 * org.codehaus.groovy.eclipse.ui.syntaxHighlightingExtension
 * @author Andrew Eisenberg
 * @created Oct 23, 2009
 */
public interface IHighlightingExtender {
    
    /**
     * Provides a list of extra keywords to highlight in the same
     * color as all other groovy keywords
     */
    public List<String> getAdditionalGroovyKeywords();
    
    /**
     * Provides a list of extra keywords to highlight in the same
     * color as all other gjdk keywords
     */
    public List<String> getAdditionalGJDKKeywords();
    
    /**
     * Provides a list of additional highlighting rules
     */
    public List<IRule> getAdditionalRules();
}
