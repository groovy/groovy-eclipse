/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.actions;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Things that want to be treated as rename targets that provide special renaming behavior, overriding
 * the default JDT / Greclipse renaming behaviour can do so by addapting to IRenameTarget.
 *
 * @author Kris De Volder
 */
public interface IRenameTarget {

    /**
     * Called when a rename refactoring is requested from the editor or project
     * explorer. By
     * implementing this method we can intercept what happens when the rename
     * action is triggered on a given rename target.
     * The typical implementation will open a refactoring wizard.
     * <p>
     *
     * @param groovyEditor editor in the context of which we were invoked or
     *            null if
     *            invoked from the project explorer.
     *
     * @param lightweight is true if the request is to do an 'inline' renaming.
     *            Implementation may
     *            ignore this if it isn't capable of doing inline renaming.
     *
     * @return true if action was intercepted, false otherwise. If false is
     *         returned, or an unexpected
     *         exception is thrown, then the caller should fallback to using its
     *         own handling of the action.
     */
    boolean performRenameAction(Shell shell, AbstractTextEditor editor, boolean lightweight);

}
