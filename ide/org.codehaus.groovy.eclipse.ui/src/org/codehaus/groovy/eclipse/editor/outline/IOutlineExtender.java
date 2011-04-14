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
package org.codehaus.groovy.eclipse.editor.outline;

import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;

/**
 * Extends the Groovy Editor to allow update the outline content.
 * See the extension point
 * org.codehaus.groovy.eclipse.ui.outlineExtension
 *
 * @author Maxime Hamm
 * @created April 4, 2011
 */
public interface IOutlineExtender {

    /**
     * Return the outline page to use for the editor's unit
     *
     * @param contextMenuID
     * @param editor
     * @return the outline page
     */
    GroovyOutlinePage getGroovyOutlinePageForEditor(String contextMenuID, GroovyEditor editor);

    /**
     * @return true if this extender want's to setup the outline view
     *         for the unit file
     */
    boolean appliesTo(GroovyCompilationUnit unit);

}
