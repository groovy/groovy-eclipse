/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.preferences;

import java.util.Arrays;

import groovy.lang.Tuple2;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.editor.GroovyColorManager;
import org.eclipse.debug.internal.ui.preferences.BooleanFieldEditor2;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.osgi.framework.Version;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */
public class GroovyEditorPreferencesPage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage {

    public GroovyEditorPreferencesPage() {
        super(GRID);
        setPreferenceStore(GroovyPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected String getPageId() {
        return "org.codehaus.groovy.eclipse.preferences.editor";
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    @Override
    public void createFieldEditors() {
        Composite parent = getFieldEditorParent();

        // Category Methods
        /*Tuple2<ColorFieldEditor, BooleanFieldEditor2> categoryMethodEditor =*/ createColorEditor(parent,
            PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR, "GroovyEditorPreferencesPage.GJDK_method_color");

        // Primitive Types (includes def, var, and void)
        Tuple2<ColorFieldEditor, BooleanFieldEditor2> primitivesEditor = createColorEditor(parent,
            PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_PRIMITIVES_COLOR, "GroovyEditorPreferencesPage.Primitives_color");

        // Other Keywords (excludes assert and return)
        Tuple2<ColorFieldEditor, BooleanFieldEditor2> keywordEditor = createColorEditor(parent,
            PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_KEYWORDS_COLOR, "GroovyEditorPreferencesPage.Keywords_color");

        // Assert Keyword
        Tuple2<ColorFieldEditor, BooleanFieldEditor2> assertEditor = createColorEditor(parent,
            PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ASSERT_COLOR, "GroovyEditorPreferencesPage.Assert_color");

        // Return Keyword
        Tuple2<ColorFieldEditor, BooleanFieldEditor2> returnEditor = createColorEditor(parent,
            PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR, "GroovyEditorPreferencesPage.Return_color");

        // Copy Java Preferences
        Button javaColorButton = new Button(parent, SWT.BUTTON1);
        javaColorButton.setText(Messages.getString("GroovyEditorPreferencesPage.Copy_Java_Color_Preferences"));
        javaColorButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
            IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();
            Arrays.asList(primitivesEditor, keywordEditor, assertEditor).forEach(
                tuple -> copyColorAndStyle(tuple, store, IJavaColorConstants.JAVA_KEYWORD));
            copyColorAndStyle(returnEditor, store, IJavaColorConstants.JAVA_KEYWORD_RETURN);
        }));
        GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).indent(0, IDialogConstants.VERTICAL_MARGIN).span(2, 1).applyTo(javaColorButton);

        // Semantic Highlighting
        createSemanticHighlightingEditors(parent);

        // Code Minings -- available since JDT UI 3.16
        if (JavaPlugin.getDefault().getBundle().getVersion().compareTo(new Version(3, 16, 0)) >= 0) {
            createPreferencePageLink(parent,
                "org.eclipse.jdt.ui.preferences.JavaEditorCodeMiningPreferencePage",
                Messages.getString("GroovyEditorPreferencesPage.InheritedJavaMiningsDescription"));
        }
    }

    private void createSemanticHighlightingEditors(Composite parent) {
        Group group = new Group(parent, SWT.SHADOW_NONE);
        group.setFont(group.getParent().getFont());
        group.setLayout(new GridLayout());
        group.setText(Messages.getString("GroovyEditorPreferencesPage.SemanticHighlightingPrefs"));
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).indent(0, IDialogConstants.VERTICAL_MARGIN).span(2, 1).applyTo(group);

        Composite panel = new Composite(group, SWT.NONE);

        addField(new BooleanFieldEditor(
            PreferenceConstants.GROOVY_SEMANTIC_HIGHLIGHTING,
            Messages.getString("GroovyEditorPreferencesPage.SemanticHighlightingToggle"), panel));

        addField(new BooleanFieldEditor(
            PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_SLASHY_STRINGS,
            Messages.getString("GroovyEditorPreferencesPage.DollarSlashyHighlightingToggle"), panel));

        createPreferencePageLink(panel,
            "org.eclipse.jdt.ui.preferences.JavaEditorColoringPreferencePage",
            Messages.getString("GroovyEditorPreferencesPage.InheritedJavaColorsDescription"));
    }

    private void createPreferencePageLink(Composite parent, String pageId, String linkText) {
        PreferenceLinkArea area = new PreferenceLinkArea(parent, SWT.WRAP, pageId, linkText, (IWorkbenchPreferenceContainer) getContainer(), null);
        GridDataFactory.swtDefaults().indent(0, IDialogConstants.VERTICAL_MARGIN).span(2, 1).applyTo(area.getControl());
    }

    private Tuple2<ColorFieldEditor, BooleanFieldEditor2> createColorEditor(Composite parent, String preference, String nls) {
        Tuple2<ColorFieldEditor, BooleanFieldEditor2> editors = new Tuple2<>(
            new ColorFieldEditor(preference, Messages.getString(nls), parent),
            new BooleanFieldEditor2(
                preference + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX,
                "  " + Messages.getString("GroovyEditorPreferencesPage.BoldToggle"),
                BooleanFieldEditor.SEPARATE_LABEL, parent)
        );

        addField(editors.getFirst());
        addField(editors.getSecond());

        return editors;
    }

    private void copyColorAndStyle(Tuple2<ColorFieldEditor, BooleanFieldEditor2> tuple, IPreferenceStore store, String pref) {
        tuple.getFirst().getColorSelector().setColorValue(PreferenceConverter.getColor(store, pref));
        tuple.getSecond().getChangeControl(null).setSelection(store.getBoolean(pref + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX));
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public boolean performOk() {
        boolean success = super.performOk();
        if (success) {
            GroovyColorManager colorManager = GroovyPlugin.getDefault().getTextTools().getColorManager();
            colorManager.uninitialize();
            colorManager.initialize();
        }
        return success;
    }
}
