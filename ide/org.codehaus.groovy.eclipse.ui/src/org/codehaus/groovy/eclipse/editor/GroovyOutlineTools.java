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
package org.codehaus.groovy.eclipse.editor;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.editor.outline.OutlineExtenderRegistry;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Maxime Hamm
 * @created april 8, 2011
 *          Tools to manager outline content
 */
public class GroovyOutlineTools {

    private OutlineExtenderRegistry outlineExtenderRegistry;

    public void dispose() {
        outlineExtenderRegistry = null;
    }

    public OutlineExtenderRegistry getOutlineExtenderRegistry() {
        if (outlineExtenderRegistry == null) {
            outlineExtenderRegistry = new OutlineExtenderRegistry();
            try {
                outlineExtenderRegistry.initialize();
            } catch (CoreException e) {
                GroovyCore.logException("Error creating outline page registry", e);
            }
        }
        return outlineExtenderRegistry;
    }
}
