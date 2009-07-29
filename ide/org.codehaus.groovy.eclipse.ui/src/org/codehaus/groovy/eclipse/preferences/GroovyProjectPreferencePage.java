/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.preferences;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class GroovyProjectPreferencePage extends FieldEditorOverlayPage
        implements IWorkbenchPreferencePage {
    public GroovyProjectPreferencePage() {
        super(GRID);
        setPreferenceStore(createPreferenceStore());
    }
    
    private IPreferenceStore createPreferenceStore() {
        return new ScopedPreferenceStore(new InstanceScope(), GroovyCoreActivator.PLUGIN_ID);
    }


    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    public void createFieldEditors() {
        // Groovy compiler project output preference
        addField(new StringFieldEditor(
                PreferenceConstants.GROOVY_COMPILER_OUTPUT_PATH,
                "&Groovy compiler output location", getFieldEditorParent()) {
            // This is a hack to allow the Field Editor to be disabled, but the
            // project's
            // groovy output location to be updated.
            public void setEnabled(final boolean enabled, final Composite parent) {
                if (!enabled) {
                    super.setEnabled(true, parent);
                    super
                            .setStringValue(GroovyCore
                                    .getPreferenceStore()
                                    .getString(
                                            PreferenceConstants.GROOVY_COMPILER_OUTPUT_PATH));
                }
                super.setEnabled(enabled, parent);
            }
        });

        // Generate Class File Pref
        final BooleanFieldEditor classFilePrefEditor = new BooleanFieldEditor(
                PreferenceConstants.GROOVY_DONT_GENERATE_CLASS_FILES,
                "&Disable Groovy Compiler Generating Class Files",
                getFieldEditorParent());
        classFilePrefEditor.setPreferenceStore(getPreferenceStore());
        addField(classFilePrefEditor);

        // Check package path versus source path in project preference
        final BooleanFieldEditor checkPackagePrefEditor = new BooleanFieldEditor(
                PreferenceConstants.GROOVY_DONT_CHECK_PACKAGE_VS_SRC_PATH,
                "&Disable Check Package Matches Source Directory",
                getFieldEditorParent());
        checkPackagePrefEditor.setPreferenceStore(getPreferenceStore());
        addField(checkPackagePrefEditor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {}

    protected String getPageId() {
        return this.getClass().getPackage().getName();
    }

}