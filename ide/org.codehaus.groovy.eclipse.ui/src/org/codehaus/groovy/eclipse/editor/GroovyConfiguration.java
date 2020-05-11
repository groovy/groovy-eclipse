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
package org.codehaus.groovy.eclipse.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.editor.highlighting.HighlightingExtenderRegistry;
import org.codehaus.groovy.eclipse.preferences.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.text.AbstractJavaScanner;
import org.eclipse.jdt.internal.ui.text.SingleTokenJavaScanner;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalCategory;
import org.eclipse.jdt.internal.ui.text.java.ContentAssistProcessor;
import org.eclipse.jdt.internal.ui.text.java.JavaAutoIndentStrategy;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProcessor;
import org.eclipse.jdt.internal.ui.text.java.SmartSemicolonAutoEditStrategy;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
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
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public class GroovyConfiguration extends JavaSourceViewerConfiguration {

    public GroovyConfiguration(final GroovyColorManager colorManager, final IPreferenceStore preferenceStore, final ITextEditor editor) {
        super(colorManager, preferenceStore, editor, IJavaPartitions.JAVA_PARTITIONING);

        // replace Java's string scanner to enable Groovy's color choices
        AbstractJavaScanner stringScanner = new SingleTokenJavaScanner(colorManager,
            PreferenceConstants.getPreferenceStore(), PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR);
        ReflectionUtils.setPrivateField(JavaSourceViewerConfiguration.class, "fStringScanner", this, stringScanner);

        // replace Java's code scanner to enable Groovy's token rules
        try {
            IProject project = null;
            if (editor != null && editor instanceof GroovyEditor) {
                if (editor.getEditorInput() instanceof FileEditorInput) {
                    project = ((FileEditorInput) editor.getEditorInput()).getFile().getProject();
                }
            }

            HighlightingExtenderRegistry registry = GroovyPlugin.getDefault().getTextTools().getHighlightingExtenderRegistry();

            AbstractJavaScanner codeScanner = new GroovyTagScanner(colorManager,
                registry.getInitialAdditionalRulesForProject(project),
                registry.getAdditionalRulesForProject(project),
                registry.getExtraGroovyKeywordsForProject(project),
                registry.getExtraGJDKKeywordsForProject(project));
            ReflectionUtils.setPrivateField(JavaSourceViewerConfiguration.class, "fCodeScanner", this, codeScanner);
        } catch (Exception e) {
            GroovyCore.logException("Error creating and registering GroovyTagScanner", e);
        }
    }

    @Override
    public IAutoEditStrategy[] getAutoEditStrategies(final ISourceViewer sourceViewer, final String contentType) {
        switch (contentType) {
        case GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS:
        case IJavaPartitions.JAVA_STRING:
            // TODO: Should GroovyStringAutoEditStrategy delegate to JavaStringAutoIndentStrategy instead of DefaultIndentLineAutoEditStrategy?
            // TODO: GroovyStringAutoEditStrategy does nothing; was there some intended behavior that is incomplete?
            return new IAutoEditStrategy[] {new GroovyStringAutoEditStrategy(contentType)};
        default:
            IAutoEditStrategy[] strategies = super.getAutoEditStrategies(sourceViewer, contentType);
            for (int i = 0, n = strategies.length; i < n; i += 1) {
                if (strategies[i] instanceof JavaAutoIndentStrategy) {
                    strategies[i] = new GroovyAutoIndentStrategy((JavaAutoIndentStrategy) strategies[i]);
                } else if (strategies[i] instanceof SmartSemicolonAutoEditStrategy) {
                    String partitioning = getConfiguredDocumentPartitioning(sourceViewer);
                    // split up semicolon and opening brace handling
                    strategies[i] = (document, command) -> {
                        if (";".equals(command.text)) {
                            IAutoEditStrategy delegate = new SmartSemicolonAutoEditStrategy(partitioning);
                            delegate.customizeDocumentCommand(document, command);
                        }
                    };
                    strategies = (IAutoEditStrategy[]) ArrayUtils.add(strategies, (IAutoEditStrategy) (document, command) -> {
                        if ("{".equals(command.text)) {
                            //int offset = SmartSemicolonAutoEditStrategy.firstNonWhitespaceBackward(document, command.offset-1, partitioning, -1);
                            Integer prev = ReflectionUtils.executePrivateMethod(SmartSemicolonAutoEditStrategy.class, "firstNonWhitespaceBackward",
                                new Class[] {IDocument.class, int.class, String.class, int.class}, SmartSemicolonAutoEditStrategy.class,
                                new Object[] {document, command.offset - 1, partitioning, -1});
                            try {
                                if (prev != null && prev != -1) {
                                    switch (document.getChar(prev)) {
                                    case ',':
                                    case '.':
                                    case '?':
                                    case ':':
                                        return;
                                    }
                                }
                            } catch (BadLocationException ignore) {
                            }

                            IAutoEditStrategy delegate = new SmartSemicolonAutoEditStrategy(partitioning);
                            delegate.customizeDocumentCommand(document, command);
                        }
                    });
                }
            }
            return strategies;
        }
    }

    @Override
    public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
        // TODO: Copy from GroovyPartitionScanner.LEGAL_CONTENT_TYPES?
        return new String[] {
            IDocument.DEFAULT_CONTENT_TYPE,
            IJavaPartitions.JAVA_DOC,
            IJavaPartitions.JAVA_MULTI_LINE_COMMENT,
            IJavaPartitions.JAVA_SINGLE_LINE_COMMENT,
            IJavaPartitions.JAVA_STRING,
            IJavaPartitions.JAVA_CHARACTER,
            GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS,
        };
    }

    @Override
    public IContentAssistant getContentAssistant(final ISourceViewer sourceViewer) {
        ContentAssistant assistant = (ContentAssistant) super.getContentAssistant(sourceViewer);

        String contentType = GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS;
        assistant.setContentAssistProcessor(new JavaCompletionProcessor(getEditor(), assistant, contentType), contentType);

        // retain only Groovy-approved completion proposal categories
        IContentAssistProcessor processor = assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
        List<CompletionProposalCategory> categories = ReflectionUtils.getPrivateField(ContentAssistProcessor.class, "fCategories", processor);
        List<CompletionProposalCategory> newCategories = new ArrayList<>();
        for (CompletionProposalCategory category : categories) {
            if (GROOVY_CONTENT_ASSIST.matcher(category.getId()).matches()) {
                newCategories.add(category);
            }
        }
        ReflectionUtils.setPrivateField(ContentAssistProcessor.class, "fCategories", processor, newCategories);

        return assistant;
    }

    private static final Pattern GROOVY_CONTENT_ASSIST = Pattern.compile("org.codehaus.groovy.+|org.eclipse.jdt.ui.(default|text)ProposalCategory");

    @Override
    protected Map<String, IAdaptable> getHyperlinkDetectorTargets(final ISourceViewer sourceViewer) {
        Map<String, IAdaptable> targets = super.getHyperlinkDetectorTargets(sourceViewer);
        targets.put("org.codehaus.groovy.eclipse.groovyCode", getEditor()); //$NON-NLS-1$
        return targets;
    }

    @Override
    public IInformationPresenter getOutlinePresenter(final ISourceViewer sourceViewer, final boolean doCodeResolve) {
        IInformationPresenter presenter = super.getOutlinePresenter(sourceViewer, doCodeResolve);
        if (presenter instanceof InformationPresenter) {
            IInformationProvider provider = presenter.getInformationProvider(IDocument.DEFAULT_CONTENT_TYPE);
            ((InformationPresenter) presenter).setInformationProvider(provider, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
        }
        return presenter;
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(final ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = (PresentationReconciler) super.getPresentationReconciler(sourceViewer);
        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getStringScanner());
        reconciler.setDamager(dr, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
        reconciler.setRepairer(dr, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
        return reconciler;
    }
}
