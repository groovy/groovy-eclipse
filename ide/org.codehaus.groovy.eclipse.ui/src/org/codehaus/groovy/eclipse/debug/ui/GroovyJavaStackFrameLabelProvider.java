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
package org.codehaus.groovy.eclipse.debug.ui;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.JavaDebugOptionsManager;
import org.eclipse.jdt.internal.debug.ui.variables.JavaStackFrameLabelProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;

class GroovyJavaStackFrameLabelProvider extends JavaStackFrameLabelProvider implements IPropertyChangeListener {

    private boolean enabled;
    private String[] filters;
    private final IPreferenceStore preferenceStore = GroovyPlugin.getDefault().getPreferenceStore();
    private static final Pattern LAMBDA_FILTER = Pattern.compile("\\${2}Lambda\\$\\d+(?:\\.|/0x)[0-9a-f]{8,16}$");

    GroovyJavaStackFrameLabelProvider() {
        computeEnabled();
        computeFilters();
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        switch (event.getProperty()) {
        case PreferenceConstants.GROOVY_DEBUG_FILTER_STACK:
            computeEnabled();
            break;
        case PreferenceConstants.GROOVY_DEBUG_FILTER_LIST:
            computeFilters();
            break;
        }
    }

    @Override
    protected void retrieveLabel(final ILabelUpdate update) throws CoreException {
        super.retrieveLabel(update);
        if (enabled && !update.isCanceled()) {
            Object element = update.getElement();
            if (element instanceof IJavaStackFrame) {
                if (isFiltered(((IJavaStackFrame) element).getReceivingTypeName())) {
                    try {
                        RGB mutedColor = JFaceResources.getColorRegistry().getRGB(JFacePreferences.DECORATIONS_COLOR);
                        update.setForeground(mutedColor, 0);
                    } catch (ArrayIndexOutOfBoundsException ignore) {
                        // there are no columns in this LabelUpdate
                    } catch (NullPointerException ignore) {
                        // the columns are null
                    }
                }
            }
        }
    }

    //--------------------------------------------------------------------------

    private void computeEnabled() {
        enabled = preferenceStore.getBoolean(PreferenceConstants.GROOVY_DEBUG_FILTER_STACK);
    }

    private void computeFilters() {
        String filterList = preferenceStore.getString(PreferenceConstants.GROOVY_DEBUG_FILTER_LIST);
        if (filterList != null) {
            filters = JavaDebugOptionsManager.parseList(filterList);
        } else {
            filters = CharOperation.NO_STRINGS;
        }
    }

    private boolean isFiltered(final String qualifiedName) {
        return Arrays.stream(filters).anyMatch(qualifiedName::startsWith) || LAMBDA_FILTER.matcher(qualifiedName).find();
    }

    void connect() {
        preferenceStore.addPropertyChangeListener(this);
    }

    void disconnect() {
        preferenceStore.removePropertyChangeListener(this);
    }
}
