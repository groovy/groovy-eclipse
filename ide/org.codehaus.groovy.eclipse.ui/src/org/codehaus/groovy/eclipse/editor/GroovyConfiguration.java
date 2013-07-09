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
package org.codehaus.groovy.eclipse.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.eclipse.editor.highlighting.HighlightingExtenderRegistry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.text.SingleTokenJavaScanner;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalCategory;
import org.eclipse.jdt.internal.ui.text.java.ContentAssistProcessor;
import org.eclipse.jdt.internal.ui.text.java.JavaAutoIndentStrategy;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProcessor;
import org.eclipse.jdt.internal.ui.text.java.hover.JavaInformationProvider;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public class GroovyConfiguration extends JavaSourceViewerConfiguration {

    public GroovyConfiguration(GroovyColorManager colorManager, IPreferenceStore preferenceSource, ITextEditor editor) {
        super(colorManager, preferenceSource, editor, IJavaPartitions.JAVA_PARTITIONING);
        ReflectionUtils.setPrivateField(JavaSourceViewerConfiguration.class, "fStringScanner", this,
                createStringScanner(colorManager, preferenceSource));
        try {
            ReflectionUtils.setPrivateField(JavaSourceViewerConfiguration.class, "fCodeScanner", this,
                    createTagScanner(getProject(), colorManager, getHighlightingExtenderRegistry()));
        } catch (CoreException e) {
            GroovyCore.logException("Error creating syntax highlighter", e);
        }
    }

    /**
     * @return a regular string scanner that uses Groovy's string color
     */
    private RuleBasedScanner createStringScanner(IColorManager colorManager, IPreferenceStore store) {
        return new SingleTokenJavaScanner(colorManager, store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR);
    }

    private GroovyTagScanner createTagScanner(IProject project, IColorManager colorManager, HighlightingExtenderRegistry registry) throws CoreException {
        return new GroovyTagScanner(colorManager,
                registry.getInitialAdditionalRulesForProject(project),
                registry.getAdditionalRulesForProject(project),
                registry.getExtraGroovyKeywordsForProject(project),
                registry.getExtraGJDKKeywordsForProject(project));
    }

    public HighlightingExtenderRegistry getHighlightingExtenderRegistry() {
        return GroovyPlugin.getDefault().getTextTools().getHighlightingExtenderRegistry();
    }

    private IProject getProject() {
        ITextEditor editor = getEditor();
        if (editor != null && editor instanceof GroovyEditor) {
            IEditorInput input = ((GroovyEditor) editor).internalInput;
            if (input instanceof FileEditorInput) {
                IFile file = ((FileEditorInput) input).getFile();
                return file.getProject();
            }
        }
        return null;
    }


    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {

        PresentationReconciler reconciler = (PresentationReconciler) super.getPresentationReconciler(sourceViewer);

        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getStringScanner());
        reconciler.setDamager(dr, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
        reconciler.setRepairer(dr, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
        return reconciler;
    }

    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[] {
                IDocument.DEFAULT_CONTENT_TYPE,
                IJavaPartitions.JAVA_DOC,
                IJavaPartitions.JAVA_MULTI_LINE_COMMENT,
                IJavaPartitions.JAVA_SINGLE_LINE_COMMENT,
                IJavaPartitions.JAVA_STRING,
                IJavaPartitions.JAVA_CHARACTER,
                GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        ContentAssistant assistant = (ContentAssistant) super.getContentAssistant(sourceViewer);

        ContentAssistProcessor stringProcessor= new JavaCompletionProcessor(getEditor(), assistant, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
        assistant.setContentAssistProcessor(stringProcessor, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);

        // remove Java content assist processor category
        // do a list copy so as not to disturb globally shared list.
        IContentAssistProcessor processor = assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
        List<CompletionProposalCategory> categories = (List<CompletionProposalCategory>) ReflectionUtils.getPrivateField(ContentAssistProcessor.class, "fCategories", processor);
        List<CompletionProposalCategory> newCategories = new ArrayList<CompletionProposalCategory>(categories.size()-1);
        for (CompletionProposalCategory category : categories) {
            if (!category.getId().equals("org.eclipse.jdt.ui.javaTypeProposalCategory")
                    && !category.getId().equals("org.eclipse.jdt.ui.templateProposalCategory")
                    && !category.getId().equals("org.eclipse.ajdt.ui.templateCategory")
                    && !category.getId().equals("org.eclipse.jdt.ui.swtProposalCategory")
                    && !category.getId().equals("org.eclipse.jdt.ui.javaNoTypeProposalCategory")
                    && !category.getId().equals("org.eclipse.jdt.ui.javaAllProposalCategory")
                    && !category.getId().equals("org.eclipse.mylyn.java.ui.javaAllProposalCategory")) {
                newCategories.add(category);
            }
        }

        ReflectionUtils.setPrivateField(ContentAssistProcessor.class, "fCategories", processor, newCategories);
        return assistant;
    }

    @Override
    public IInformationPresenter getOutlinePresenter(ISourceViewer sourceViewer, boolean doCodeResolve) {
        IInformationPresenter presenter = super.getOutlinePresenter(sourceViewer, doCodeResolve);
        if (presenter instanceof InformationPresenter) {
            IInformationProvider provider = presenter.getInformationProvider(IDocument.DEFAULT_CONTENT_TYPE);
            ((InformationPresenter) presenter).setInformationProvider(provider, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
        }
        return presenter;
    }

    @Override
    public IQuickAssistAssistant getQuickAssistAssistant(
            ISourceViewer sourceViewer) {
        // Enable quick fix assistant for Groovy quick fixes
        return super.getQuickAssistAssistant(sourceViewer);
    }

    @Override
    public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
        if (GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS.equals(contentType) || IJavaPartitions.JAVA_STRING.equals(contentType)) {
            return new IAutoEditStrategy[] { new GroovyMultilineStringAutoEditStrategy(contentType) };
        }
        IAutoEditStrategy[] strats = super.getAutoEditStrategies(sourceViewer, contentType);
        for (int i = 0; i < strats.length; i++) {
            if (strats[i] instanceof JavaAutoIndentStrategy) {
                strats[i] = new GroovyAutoIndentStrategy(contentType, (JavaAutoIndentStrategy) strats[i]);
            }
        }
        return strats;
    }

    /**
     * Use our {@link GroovyExtraInformationHover} instead of a
     * {@link JavadocHover}. Shows extra information provided
     * by DSLs
     */
    @Override
    public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
        IInformationPresenter informationPresenter = super.getInformationPresenter(sourceViewer);

        // the org.eclipse.jdt.internal.ui.text.java.hover.JavaTypeHover was removed in 4.2.M7
        // if this class doesn't exist then we don't need to do anything with it.
        try {
            Class<?> clazz = Class.forName("org.eclipse.jdt.internal.ui.text.java.hover.JavaTypeHover");
            JavaInformationProvider provider = (JavaInformationProvider) informationPresenter
                    .getInformationProvider(IDocument.DEFAULT_CONTENT_TYPE);
            IJavaEditorTextHover implementation = (IJavaEditorTextHover) ReflectionUtils.getPrivateField(JavaInformationProvider.class,
                    "fImplementation", provider);
            // when the extra information is invoked from this way, always return
            // some information since there is no BestMatchHover to fall back on
            // This hover is typically invoked when pressing F2.
            // Hovers that are invoked through a mouse, use a BestMatchHover.
            GroovyExtraInformationHover hover = new GroovyExtraInformationHover(true);
            hover.setEditor(this.getEditor());
            ReflectionUtils.setPrivateField(clazz, "fJavadocHover", implementation, hover);
        } catch (ClassNotFoundException e) {
            // can ignore.  Will happen if on 4.2 or later
        }
        return informationPresenter;
    }

    /*
     * Type parameters have changed between 3.7 and 4.2.  So just remove them
     * Otherwise compile errors
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected Map getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
        Map targets = super.getHyperlinkDetectorTargets(sourceViewer);
        targets.put("org.codehaus.groovy.eclipse.groovyCode", getEditor()); //$NON-NLS-1$
        return targets;
    }
}