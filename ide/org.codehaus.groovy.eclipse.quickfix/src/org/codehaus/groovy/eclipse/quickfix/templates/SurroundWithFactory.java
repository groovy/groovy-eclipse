/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
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
package org.codehaus.groovy.eclipse.quickfix.templates;

import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.editor.ISurroundWithFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.actions.ActionGroup;

/**
 * TODO convert this to using an adapter or a plugin
 * TODO don't use early startup
 * 
 * @author Andrew Eisenberg
 * @created 2013-04-01
 */
public class SurroundWithFactory implements ISurroundWithFactory {
    public ActionGroup createSurrundWithGroup(GroovyEditor editor, String group) {
        return new SurroundWithActionGroup(editor, group);
    }
}