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
package org.codehaus.groovy.eclipse.launchers;

import java.util.List;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Methods for launch shortcuts to keep things dry.
 */
public final class LaunchShortcutHelper extends NLS {

    private LaunchShortcutHelper() {}

    public static String GroovyLaunchShortcut_title;
    public static String GroovyLaunchShortcut_message;
    public static String GroovyLaunchShortcut_notFound;
    public static String GroovyLaunchShortcut_notRunnable;
    public static String GroovyLaunchShortcut_noSelection;
    public static String GroovyLaunchShortcut_noSelection0;
    public static String GroovyLaunchShortcut_noSelection1;
    public static String GroovyLaunchShortcut_classpathError;
    public static String GroovyLaunchShortcut_failureToLaunch;

    public static String SelectMainTypeDialog_title;
    public static String SelectMainTypeDialog_message;

    static {
        initializeMessages("org.codehaus.groovy.eclipse.launchers.LaunchMessages", LaunchShortcutHelper.class);
    }

    /**
     * Prompts user to select from a list of items.
     *
     * @param options The options to pick from.
     * @param labelProvider The label provider for the objects
     * @param title The title for the dialog
     * @param message The message for the dialog
     * @return Returns the object the user selected.
     * @throws OperationCanceledException If the user selects cancel
     */
    public static <T> T chooseFromList(final List<T> options, final ILabelProvider labelProvider, final String title, final String message) {
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(GroovyPlugin.getActiveWorkbenchShell(), labelProvider);
        dialog.setElements(options.toArray());
        dialog.setMessage(message);
        dialog.setMultipleSelection(false);
        dialog.setTitle(title);

        int code = dialog.open();
        labelProvider.dispose();
        if (code == Window.OK) {
            @SuppressWarnings("unchecked")
            T result = (T) dialog.getFirstResult();

            return result;
        }

        // if the user hits cancel, stop the whole thing silently
        throw new OperationCanceledException();
    }
}
