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

import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_GROOVYDOC_KEYWORD_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_GROOVYDOC_LINK_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_GROOVYDOC_TAG_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.eclipse.editor.GroovyColorManager;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

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

	public GroovyEditorPreferencesPage() {
		super(GRID);
		setPreferenceStore(GroovyPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {

		// GJDK Color Prefs
		addField(new BooleanFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_ENABLED,
				Messages
						.getString("GroovyEditorPreferencesPage.Enable_GJDK_method_coloring"), getFieldEditorParent())); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR,
				Messages
						.getString("GroovyEditorPreferencesPage.GJDK_method_color"), getFieldEditorParent())); //$NON-NLS-1$

		// Groovy Keyword Color Prefs
		addField(new BooleanFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_ENABLED,
				Messages
						.getString("GroovyEditorPreferencesPage.Enable_Groovy_keyword_coloring"), getFieldEditorParent())); //$NON-NLS-1$
		addField(new ColorFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_COLOR,
				Messages
						.getString("GroovyEditorPreferencesPage.Groovy_keyword_color"), getFieldEditorParent())); //$NON-NLS-1$

		// Multiline Comment Color Prefs
		addField(new BooleanFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_ENABLED,
				Messages
						.getString("GroovyEditorPreferencesPage.Enable_multi_line_comment_coloring"), getFieldEditorParent())); //$NON-NLS-1$

		final ColorFieldEditor commentColor = new ColorFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_COLOR,
				Messages
						.getString("GroovyEditorPreferencesPage.Multi_line_comment_color"), getFieldEditorParent()); //$NON-NLS-1$

		addField(commentColor);

		// Java Types Comment Color Prefs
		addField(new BooleanFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_ENABLED,
				Messages
						.getString("GroovyEditorPreferencesPage.Enable_Java_types_coloring"), getFieldEditorParent())); //$NON-NLS-1$

		final ColorFieldEditor javaTypesColor = new ColorFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR,
				Messages
						.getString("GroovyEditorPreferencesPage.Java_types_color"), getFieldEditorParent()); //$NON-NLS-1$

		addField(javaTypesColor);

		// Java Keyword Color Prefs
		addField(new BooleanFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_ENABLED,
				Messages
						.getString("GroovyEditorPreferencesPage.Enable_Java_keyword_coloring"), getFieldEditorParent())); //$NON-NLS-1$

		final ColorFieldEditor javaKeywordColor = new ColorFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR,
				Messages
						.getString("GroovyEditorPreferencesPage.Java_keyword_color"), getFieldEditorParent()); //$NON-NLS-1$

		addField(javaKeywordColor);

		// String Coloring
		addField(new BooleanFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_ENABLED,
				Messages
						.getString("GroovyEditorPreferencesPage.Enable_String_coloring"), getFieldEditorParent())); //$NON-NLS-1$

		final ColorFieldEditor stringColor = new ColorFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR,
				Messages.getString("GroovyEditorPreferencesPage.String_color"), getFieldEditorParent()); //$NON-NLS-1$
		
		addField(stringColor);

		// Number Coloring
		addField(new BooleanFieldEditor(
		        PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_ENABLED,
		        Messages
		        .getString("GroovyEditorPreferencesPage.Enable_Number_coloring"), getFieldEditorParent())); //$NON-NLS-1$
		
		final ColorFieldEditor numberColor = new ColorFieldEditor(
		        PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR,
		        Messages.getString("GroovyEditorPreferencesPage.Number_color"), getFieldEditorParent()); //$NON-NLS-1$
		
		addField(numberColor);
		
		// GroovyDoc Keyword
		addField(new BooleanFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_GROOVYDOC_KEYWORD_ENABLED,
				Messages
						.getString("GroovyEditorPreferencesPage.Enable_GroovyDoc_keyword_coloring"), getFieldEditorParent())); //$NON-NLS-1$

		final ColorFieldEditor groovyDocKeywordColor = new ColorFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_GROOVYDOC_KEYWORD_COLOR,
				Messages
						.getString("GroovyEditorPreferencesPage.GroovyDoc_keyword_color"), getFieldEditorParent()); //$NON-NLS-1$

		addField(groovyDocKeywordColor);

		
		// GroovyDoc Tag
		addField(new BooleanFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_GROOVYDOC_TAG_ENABLED,
				Messages
						.getString("GroovyEditorPreferencesPage.Enable_GroovyDoc_tag_coloring"), getFieldEditorParent())); //$NON-NLS-1$

		final ColorFieldEditor groovyDocTagColor = new ColorFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_GROOVYDOC_TAG_COLOR,
				Messages
						.getString("GroovyEditorPreferencesPage.GroovyDoc_tag_color"), getFieldEditorParent()); //$NON-NLS-1$

		addField(groovyDocTagColor);

		
		// GroovyDoc Link
		addField(new BooleanFieldEditor(
				PreferenceConstants.GROOVY_EDITOR_GROOVYDOC_LINK_ENABLED,
				Messages
						.getString("GroovyEditorPreferencesPage.Enable_GroovyDoc_link_coloring"), getFieldEditorParent())); //$NON-NLS-1$

        final ColorFieldEditor groovyDocLinkColor = new ColorFieldEditor(
                PreferenceConstants.GROOVY_EDITOR_GROOVYDOC_LINK_COLOR,
                Messages
                        .getString("GroovyEditorPreferencesPage.GroovyDoc_link_color"), getFieldEditorParent()); //$NON-NLS-1$

        addField(groovyDocLinkColor);

        
        // Default color
        final ColorFieldEditor groovyDefaultColor = new ColorFieldEditor(
                PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR,
                Messages
                        .getString("GroovyEditorPreferencesPage.Groovy_Default_color"), getFieldEditorParent()); //$NON-NLS-1$

        addField(groovyDefaultColor);

        
        // Change to Java Defaults
        Button javaColorButton = new Button(super.getFieldEditorParent(),
                SWT.BUTTON1);
        
		javaColorButton
				.setText(Messages
						.getString("GroovyEditorPreferencesPage.Copy_Java_Color_Preferences")); //$NON-NLS-1$
		javaColorButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {

				IPreferenceStore store = JavaPlugin.getDefault()
						.getPreferenceStore();

				RGB rgb = PreferenceConverter.getColor(store,
						IJavaColorConstants.JAVA_STRING);
				stringColor.getColorSelector().setColorValue(rgb);

				rgb = PreferenceConverter.getColor(store,
                        IJavaColorConstants.JAVA_DEFAULT);
                numberColor.getColorSelector().setColorValue(rgb);
				
				rgb = PreferenceConverter.getColor(store,
						IJavaColorConstants.JAVA_KEYWORD);
				javaKeywordColor.getColorSelector().setColorValue(rgb);
				javaTypesColor.getColorSelector().setColorValue(rgb);

				rgb = PreferenceConverter.getColor(store,
						IJavaColorConstants.JAVA_MULTI_LINE_COMMENT);
				commentColor.getColorSelector().setColorValue(rgb);

				rgb = PreferenceConverter.getColor(store,
						IJavaColorConstants.JAVADOC_KEYWORD);
				groovyDocKeywordColor.getColorSelector().setColorValue(rgb);

				rgb = PreferenceConverter.getColor(store,
						IJavaColorConstants.JAVADOC_TAG);
				groovyDocTagColor.getColorSelector().setColorValue(rgb);

				rgb = PreferenceConverter.getColor(store,
						IJavaColorConstants.JAVADOC_LINK);
				groovyDocLinkColor.getColorSelector().setColorValue(rgb);

				rgb = PreferenceConverter.getColor(store,
				        IJavaColorConstants.JAVA_DEFAULT);
				groovyDefaultColor.getColorSelector().setColorValue(rgb);
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	protected String getPageId() {
		return this.getClass().getPackage().getName();
	}

	@Override
	public boolean performOk() {
	    boolean success = super.performOk();
	    if (success) {
    	    GroovyColorManager colorManager = GroovyPlugin.getDefault().getTextTools().getColorManager();
            colorManager.unbindColor(GROOVY_EDITOR_GROOVYDOC_KEYWORD_COLOR);
            colorManager.unbindColor(GROOVY_EDITOR_GROOVYDOC_TAG_COLOR);
            colorManager.unbindColor(GROOVY_EDITOR_GROOVYDOC_LINK_COLOR);
            colorManager.unbindColor(GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR);
            colorManager.unbindColor(GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_COLOR);
            colorManager.unbindColor(GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR);
            colorManager.unbindColor(GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR);
            colorManager.unbindColor(GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR);
            colorManager.unbindColor(GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR);
            colorManager.unbindColor(GROOVY_EDITOR_DEFAULT_COLOR);

            colorManager.bindColor(GROOVY_EDITOR_GROOVYDOC_KEYWORD_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_GROOVYDOC_KEYWORD_COLOR));
            colorManager.bindColor(GROOVY_EDITOR_GROOVYDOC_TAG_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_GROOVYDOC_TAG_COLOR));
            colorManager.bindColor(GROOVY_EDITOR_GROOVYDOC_LINK_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_GROOVYDOC_LINK_COLOR));
            colorManager.bindColor(GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR));
            colorManager.bindColor(GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_COLOR));
            colorManager.bindColor(GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR));
            colorManager.bindColor(GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR));
            colorManager.bindColor(GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR));
            colorManager.bindColor(GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR));
            colorManager.bindColor(GROOVY_EDITOR_DEFAULT_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_DEFAULT_COLOR));
	    }
	    return success;
	}
}