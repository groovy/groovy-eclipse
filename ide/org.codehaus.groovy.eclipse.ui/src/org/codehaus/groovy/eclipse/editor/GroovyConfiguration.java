package org.codehaus.groovy.eclipse.editor;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.editor.highlighting.HighlightingExtenderRegistry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalCategory;
import org.eclipse.jdt.internal.ui.text.java.ContentAssistProcessor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProcessor;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public class GroovyConfiguration extends JavaSourceViewerConfiguration {

//	private ITextDoubleClickStrategy doubleClickStrategy;
	private GroovyStringScanner stringScanner;
	
	
	/**
	 * Single token scanner.
	 */
	static class SingleTokenScanner extends BufferedRuleBasedScanner {
		public SingleTokenScanner(TextAttribute attribute) {
			setDefaultReturnToken(new Token(attribute));
		}
	}

	public GroovyConfiguration(GroovyColorManager colorManager, IPreferenceStore preferenceSource, ITextEditor editor) {
	    super(colorManager, preferenceSource, editor, IJavaPartitions.JAVA_PARTITIONING);
	    this.stringScanner = new GroovyStringScanner(colorManager);
	    HighlightingExtenderRegistry registry = highlightingExtender();
	    IProject project = getProject();
	    GroovyTagScanner tagScanner = new GroovyTagScanner(colorManager, 
	            registry.getAdditionalRulesForProject(project),
	            registry.getExtraGroovyKeywordsForProject(project),
	            registry.getExtraGJDKKeywordsForProject(project));
	    ReflectionUtils.setPrivateField(JavaSourceViewerConfiguration.class, "fCodeScanner", this, tagScanner);
	}

    private HighlightingExtenderRegistry highlightingExtender() {
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
    protected RuleBasedScanner getStringScanner() {
        return stringScanner;
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(
            ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = (PresentationReconciler) super.getPresentationReconciler(sourceViewer);
        reconciler
            .setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

        
        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getStringScanner());
        reconciler.setDamager(dr,
                GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
        reconciler.setRepairer(dr,
                GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
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
            if (!category.getId().equals("org.eclipse.jdt.ui.javaTypeProposalCategory") &&
                    !category.getId().equals("org.eclipse.jdt.ui.javaNoTypeProposalCategory") &&
                    !category.getId().equals("org.eclipse.jdt.ui.javaAllProposalCategory")) {
                newCategories.add(category);
            }
        }
        
        
        ReflectionUtils.setPrivateField(ContentAssistProcessor.class, "fCategories", processor, newCategories);
        return assistant;
    }
    
    @Override
    public IQuickAssistAssistant getQuickAssistAssistant(
            ISourceViewer sourceViewer) {
        // disable quick assist
        return null;
    }
    
    
}