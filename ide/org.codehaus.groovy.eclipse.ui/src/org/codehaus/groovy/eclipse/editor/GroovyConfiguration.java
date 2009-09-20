package org.codehaus.groovy.eclipse.editor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class GroovyConfiguration extends JavaSourceViewerConfiguration {

//	private ITextDoubleClickStrategy doubleClickStrategy;
	private GroovyTagScanner tagScanner;
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
	    this.tagScanner = new GroovyTagScanner(colorManager);
	    this.stringScanner = new GroovyStringScanner(colorManager);
	}

    @Override
    protected RuleBasedScanner getCodeScanner() {
        return tagScanner;
    }

    @Override
    public IAutoEditStrategy[] getAutoEditStrategies(
            ISourceViewer sourceViewer, String contentType) {
        IAutoEditStrategy indentStrategy = new GroovyAutoIndentStrategy(getConfiguredDocumentPartitioning(sourceViewer), getProject());
        IAutoEditStrategy pairStrategy = new AutoEnclosingPairStrategy();
        IAutoEditStrategy[] defaultStrategies = super.getAutoEditStrategies(sourceViewer, contentType);
        if (defaultStrategies == null || defaultStrategies.length == 0) {
            return new IAutoEditStrategy[] { indentStrategy, pairStrategy };
        }
        IAutoEditStrategy[] newStrategies = new IAutoEditStrategy[defaultStrategies.length+2];
        System.arraycopy(defaultStrategies, 0, newStrategies, 0, defaultStrategies.length);
        newStrategies[defaultStrategies.length+1] = indentStrategy;
        newStrategies[defaultStrategies.length] = pairStrategy;
        return newStrategies;
    }
    
    private IJavaProject getProject() {
        ITextEditor editor= getEditor();
        if (editor == null)
            return null;

        IJavaElement element= null;
        IEditorInput input= editor.getEditorInput();
        IDocumentProvider provider= editor.getDocumentProvider();
        if (provider instanceof ICompilationUnitDocumentProvider) {
            ICompilationUnitDocumentProvider cudp= (ICompilationUnitDocumentProvider) provider;
            element= cudp.getWorkingCopy(input);
        } else if (input instanceof IClassFileEditorInput) {
            IClassFileEditorInput cfei= (IClassFileEditorInput) input;
            element= cfei.getClassFile();
        }

        if (element == null)
            return null;

        return element.getJavaProject();
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
    
    @Override
    public IQuickAssistAssistant getQuickAssistAssistant(
            ISourceViewer sourceViewer) {
        // disable quick assist
        return null;
    }
    
    
}