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
package org.codehaus.groovy.eclipse.preferences;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.eclipse.editor.GroovyColorManager;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

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
public class GroovyEditorPreferencesPage extends FieldEditorOverlayPage
implements IWorkbenchPreferencePage {

    class SpacerFieldEditor extends FieldEditor {

        private Label spacer;

        public SpacerFieldEditor(Composite parent) {
            spacer = new Label(parent, SWT.NONE);
            GridData gd = new GridData();
            spacer.setLayoutData(gd);
        }

        @Override
        protected void adjustForNumColumns(int numColumns) {
            ((GridData) spacer.getLayoutData()).horizontalSpan = numColumns;
        }

        @Override
        protected void doFillIntoGrid(Composite parent, int numColumns) {
            GridData gd = new GridData();
            gd.horizontalSpan = numColumns;
            spacer.setLayoutData(gd);
        }

        @Override
        protected void doLoad() {}

        @Override
        public void store() {}

        @Override
        protected void doLoadDefault() {}

        @Override
        protected void doStore() {}

        @Override
        public int getNumberOfControls() {
            return 0;
        }

    }

    public GroovyEditorPreferencesPage() {
        super(GRID);
        setPreferenceStore(GroovyPlugin.getDefault().getPreferenceStore());
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    @Override
    public void createFieldEditors() {

        // GJDK Color Prefs
        final ColorFieldEditor gjdkEditor = createColorEditor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR,
                "GroovyEditorPreferencesPage.GJDK_method_color");

        // Groovy Keyword Color Prefs
        final ColorFieldEditor gKeywordEditor = createColorEditor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_COLOR,
                "GroovyEditorPreferencesPage.Groovy_keyword_color");

        // Java Types Comment Color Prefs
        final ColorFieldEditor javaTypesEditor = createColorEditor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR,
                "GroovyEditorPreferencesPage.Java_types_color");

        // Java Keyword Color Prefs
        final ColorFieldEditor javaKeywordEditor = createColorEditor(
                PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR, "GroovyEditorPreferencesPage.Java_keyword_color");

        // String Coloring
        final ColorFieldEditor stringEditor = createColorEditor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR,
                "GroovyEditorPreferencesPage.String_color");


        // Bracket Coloring
        final ColorFieldEditor bracketEditor = createColorEditor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_BRACKET_COLOR,
                "GroovyEditorPreferencesPage.Bracket_color");

        // Operator Coloring
        final ColorFieldEditor operatorEditor = createColorEditor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR,
                "GroovyEditorPreferencesPage.Operator_color");

        // Annotation Coloring
        final ColorFieldEditor annotationEditor = createColorEditor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR,
                "GroovyEditorPreferencesPage.Annotation_color");

        // Return Coloring
        final ColorFieldEditor returnEditor = createColorEditor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR,
                "GroovyEditorPreferencesPage.Return_color");

        // Number Coloring
        final ColorFieldEditor numberEditor = createColorEditor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR,
                "GroovyEditorPreferencesPage.Number_color");

        // Default color
        final ColorFieldEditor defaultEditor = createColorEditor(PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR,
                "GroovyEditorPreferencesPage.Groovy_Default_color");

        // Semantic highlighting
        Label l = new Label(getFieldEditorParent(), SWT.NONE);
        l.setText("\n\nSemantic Highlighting preferences:");
        Composite c = new Composite(getFieldEditorParent(), SWT.NONE | SWT.BORDER);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        c.setLayoutData(gd);
        c.setLayout(new FillLayout(SWT.VERTICAL));
        addField(new BooleanFieldEditor(PreferenceConstants.GROOVY_SEMANTIC_HIGHLIGHTING,
                "Enable semantic highlighting (underline statically unknown references,\n"
                        + "highlight fields, methods, statics, and deprecated elements", c));
        addField(new BooleanFieldEditor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_SLASHY_STRINGS,
                "Highlight dollar slashy Strings: e.g., $/ ... /$", c));

        PreferenceLinkArea area = new PreferenceLinkArea(
                c,
                SWT.WRAP,
                "org.eclipse.jdt.ui.preferences.JavaEditorColoringPreferencePage",
                " \n\n"
                        + "Semantic highlighting colors for fields and methods are inherited from Java and can be edited here: "
                        + "\n<a>Java -> Editor -> Syntax Coloring</a> page. And go to the Java section.",
                        (IWorkbenchPreferenceContainer) getContainer(), null);
        area.getControl().setLayoutData(gd);

        // Change to Java Defaults
        Composite parent = getFieldEditorParent();
        Button javaColorButton = new Button(parent, SWT.BUTTON1);

        javaColorButton.setText(Messages.getString("GroovyEditorPreferencesPage.Copy_Java_Color_Preferences"));
        javaColorButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent arg0) {

                IPreferenceStore store = JavaPlugin.getDefault()
                        .getPreferenceStore();
                RGB rgb = PreferenceConverter.getColor(store,
                        IJavaColorConstants.JAVA_KEYWORD);
                gjdkEditor.getColorSelector().setColorValue(rgb);
                gKeywordEditor.getColorSelector().setColorValue(rgb);
                javaTypesEditor.getColorSelector().setColorValue(rgb);
                javaKeywordEditor.getColorSelector().setColorValue(rgb);

                rgb = PreferenceConverter.getColor(store,
                        IJavaColorConstants.JAVA_STRING);
                stringEditor.getColorSelector().setColorValue(rgb);

                rgb = PreferenceConverter.getColor(store,
                        IJavaColorConstants.JAVA_BRACKET);
                bracketEditor.getColorSelector().setColorValue(rgb);

                rgb = PreferenceConverter.getColor(store,
                        IJavaColorConstants.JAVA_OPERATOR);
                operatorEditor.getColorSelector().setColorValue(rgb);

                rgb = PreferenceConverter.getColor(store,
                        IJavaColorConstants.JAVA_ANNOTATION);
                annotationEditor.getColorSelector().setColorValue(rgb);

                rgb = PreferenceConverter.getColor(store,
                        IJavaColorConstants.JAVA_KEYWORD_RETURN);
                returnEditor.getColorSelector().setColorValue(rgb);

                rgb = PreferenceConverter.getColor(store, IJavaColorConstants.JAVA_DEFAULT);
                numberEditor.getColorSelector().setColorValue(rgb);
                defaultEditor.getColorSelector().setColorValue(rgb);

            }

            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

    }

    private ColorFieldEditor createColorEditor(String preference, String nls) {
        Composite parent = getFieldEditorParent();
        addField(new SpacerFieldEditor(parent));
        ColorFieldEditor colorFieldEditor = new ColorFieldEditor(preference, Messages.getString(nls), parent);
        addField(colorFieldEditor);
        addField(new BooleanFieldEditor(preference + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, "make bold",
                BooleanFieldEditor.SEPARATE_LABEL, getFieldEditorParent()));
        return colorFieldEditor;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }

    @Override
    protected String getPageId() {
        return "org.codehaus.groovy.eclipse.preferences.editor";
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