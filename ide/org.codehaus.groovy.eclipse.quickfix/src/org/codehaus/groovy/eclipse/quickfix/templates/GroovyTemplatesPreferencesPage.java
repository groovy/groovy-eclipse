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

import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.preferences.JavaSourcePreviewerUpdater;
import org.eclipse.jdt.internal.ui.text.SimpleJavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

/**
 * Allows editing of the preferences for groovy files.
 * @author Andrew Eisenberg
 * @created 2013-04-01
 */
public class GroovyTemplatesPreferencesPage extends TemplatePreferencePage {

    public GroovyTemplatesPreferencesPage() {
        GroovyQuickFixPlugin quickFixPlugin = GroovyQuickFixPlugin.getDefault();

        setPreferenceStore(quickFixPlugin.getPreferenceStore());
        setTemplateStore(quickFixPlugin.getTemplateStore());
        setContextTypeRegistry(quickFixPlugin.getTemplateContextRegistry());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {
        boolean ok = super.performOk();
        GroovyQuickFixPlugin.getDefault().savePreferences();
        return ok;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#isShowFormatterSetting()
     */
    protected boolean isShowFormatterSetting() {
        // template formatting has not been implemented
        return false;
    }

    /*
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#createTemplateEditDialog2(org.eclipse.jface.text.templates.Template, boolean, boolean)
     */
    @Override
    protected Template editTemplate(Template template, boolean edit, boolean isNameModifiable) {
        org.eclipse.jdt.internal.ui.preferences.EditTemplateDialog dialog= new org.eclipse.jdt.internal.ui.preferences.EditTemplateDialog(getShell(), template, edit, isNameModifiable, getContextTypeRegistry());
        if (dialog.open() == Window.OK) {
            return dialog.getTemplate();
        }
        return null;
    }

    /*
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#createViewer(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected SourceViewer createViewer(Composite parent) {
        IDocument document= new Document();
        JavaTextTools tools= JavaPlugin.getDefault().getJavaTextTools();
        tools.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
        IPreferenceStore store= JavaPlugin.getDefault().getCombinedPreferenceStore();
        SourceViewer viewer= new JavaSourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL, store);
        SimpleJavaSourceViewerConfiguration configuration= new SimpleJavaSourceViewerConfiguration(tools.getColorManager(), store, null, IJavaPartitions.JAVA_PARTITIONING, false);
        viewer.configure(configuration);
        viewer.setEditable(false);
        viewer.setDocument(document);

        Font font= JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
        viewer.getTextWidget().setFont(font);
        new JavaSourcePreviewerUpdater(viewer, configuration, store);

        Control control= viewer.getControl();
        GridData data= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
        control.setLayoutData(data);

        return viewer;
    }


}
