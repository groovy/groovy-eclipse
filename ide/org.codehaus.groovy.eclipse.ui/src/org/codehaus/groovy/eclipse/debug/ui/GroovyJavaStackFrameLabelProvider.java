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
package org.codehaus.groovy.eclipse.debug.ui;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.variables.JavaStackFrameLabelProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;

/**
 * 
 * @author Andrew Eisenberg
 * @created Jan 27, 2010
 */
class GroovyJavaStackFrameLabelProvider extends JavaStackFrameLabelProvider implements IPropertyChangeListener {
    
    private boolean isEnabled;
    private String[] filteredList;
    private IPreferenceStore preferenceStore;
    public GroovyJavaStackFrameLabelProvider() {
        preferenceStore = GroovyPlugin.getDefault().getPreferenceStore();
        isEnabled = preferenceStore.getBoolean(PreferenceConstants.GROOVY_DEBUG_FILTER_STACK);
        filteredList = computeFilteredList();
    }
	
    private String[] computeFilteredList() {
        String filter = preferenceStore.getString(PreferenceConstants.GROOVY_DEBUG_FILTER_LIST);
        if (filter != null) {
            return filter.split(",");
        } else {
            return new String[0];
        }
    }

    @Override
    protected void retrieveLabel(ILabelUpdate update) throws CoreException {
	    super.retrieveLabel(update);
		if (isEnabled && !update.isCanceled()) {
		    Object element = update.getElement();
			if (element instanceof IJavaStackFrame) {
			    IJavaStackFrame frame = (IJavaStackFrame) element;
			    if (isFiltered(frame.getDeclaringTypeName())) {
			        try {
			            update.setForeground(new RGB(200, 200, 200), 0);
			        } catch (ArrayIndexOutOfBoundsException e) {
			            // ignore, there are no columns in this LabelUpdate
			        } catch (NullPointerException e) {
			            // ignore, the columns are null
			        }
			    }
			}
		}
	}
    
    private boolean isFiltered(String qualifiedName) {
        for (String filter : filteredList) {
            if (qualifiedName.startsWith(filter)) {
                return true;
            }
        }
        return false;
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (PreferenceConstants.GROOVY_DEBUG_FILTER_STACK.equals(event.getProperty())) {
            isEnabled = preferenceStore.getBoolean(PreferenceConstants.GROOVY_DEBUG_FILTER_STACK);
        } else if (PreferenceConstants.GROOVY_DEBUG_FILTER_LIST.equals(event.getProperty())) {
            filteredList = computeFilteredList();
        }
    }
	
    
    void connect() {
        GroovyPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    }
    
    void disconnect() {
        GroovyPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    }
}
