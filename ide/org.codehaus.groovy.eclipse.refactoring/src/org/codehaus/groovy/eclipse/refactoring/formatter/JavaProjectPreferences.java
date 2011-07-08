/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.formatter;

import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * A lightweight adapter to get java options for a given IJavaProject in the
 * form of an IPreferencesStore.
 * <p>
 * The preferences store is read-only and it does not support listeners (i.e. it
 * sends no notification events. It is possible to add/remove listeners, but
 * this has no effect.
 * <p>
 * The preferences store is <b>not</b> updated when preferences in Eclipse are
 * changed. It only takes a snapshot of the current preferences by calling the
 * IJavaProject.getOptions method.
 * 
 * @author kdvolder
 * @created 2010-05-21
 */
public class JavaProjectPreferences implements IPreferenceStore {

    private Map<String, String> options;

    private IJavaProject javaProject;

    public void refresh() {
        if (javaProject != null) {
            this.options = javaProject.getOptions(true);
        } else {
            this.options = JavaCore.getOptions();
        }
    }

    public JavaProjectPreferences(IJavaProject javaProject) {
        this.javaProject = javaProject;
        refresh();
    }

    public void addPropertyChangeListener(IPropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(IPropertyChangeListener listener) {
    }

    public boolean contains(String name) {
        return options.containsKey(name);
    }

    public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
    }

    public boolean getBoolean(String name) {
        return Boolean.valueOf(getString(name));
    }

    public boolean getDefaultBoolean(String name) {
        return BOOLEAN_DEFAULT_DEFAULT;
    }

    public double getDefaultDouble(String name) {
        return DOUBLE_DEFAULT_DEFAULT;
    }

    public float getDefaultFloat(String name) {
        return FLOAT_DEFAULT_DEFAULT;
    }

    public int getDefaultInt(String name) {
        return INT_DEFAULT_DEFAULT;
    }

    public long getDefaultLong(String name) {
        return LONG_DEFAULT_DEFAULT;
    }

    public String getDefaultString(String name) {
        return STRING_DEFAULT_DEFAULT;
    }

    public double getDouble(String name) {
        return Double.valueOf(getString(name));
    }

    public float getFloat(String name) {
        return Float.valueOf(getString(name));
    }

    public int getInt(String name) {
        return Integer.valueOf(getString(name));
    }

    public long getLong(String name) {
        return Long.valueOf(getString(name));
    }

    public String getString(String name) {
        String result = options.get(name);
        if (result == null)
            result = getDefaultString(name);
        return result;
    }

    public boolean isDefault(String name) {
        return getDefaultString(name).equals(getString(name));
    }

    public boolean needsSaving() {
        return false;
    }

    public void putValue(String name, String value) {
        throw new UnsupportedOperationException("This is a read-only preferences store");
    }

    public void setDefault(String name, double value) {
        throw new UnsupportedOperationException("This is a read-only preferences store");
    }

    public void setDefault(String name, float value) {
        throw new UnsupportedOperationException("This is a read-only preferences store");
    }

    public void setDefault(String name, int value) {
        throw new UnsupportedOperationException("This is a read-only preferences store");
    }

    public void setDefault(String name, long value) {
        throw new UnsupportedOperationException("This is a read-only preferences store");
    }

    public void setDefault(String name, String defaultObject) {
        throw new UnsupportedOperationException("This is a read-only preferences store");
    }

    public void setDefault(String name, boolean value) {
        throw new UnsupportedOperationException("This is a read-only preferences store");
    }

    public void setToDefault(String name) {
        throw new UnsupportedOperationException("This is a read-only preferences store");
    }

    public void setValue(String name, double value) {
        throw new UnsupportedOperationException("This is a read-only preferences store");
    }

    public void setValue(String name, float value) {
        throw new UnsupportedOperationException("This is a read-only preferences store");
    }

    public void setValue(String name, int value) {
        throw new UnsupportedOperationException("This is a read-only preferences store");
    }

    public void setValue(String name, long value) {
        throw new UnsupportedOperationException("This is a read-only preferences store");
    }

    public void setValue(String name, String value) {
        throw new UnsupportedOperationException("This is a read-only preferences store");
    }

    public void setValue(String name, boolean value) {
        throw new UnsupportedOperationException("This is a read-only preferences store");
    }
}
