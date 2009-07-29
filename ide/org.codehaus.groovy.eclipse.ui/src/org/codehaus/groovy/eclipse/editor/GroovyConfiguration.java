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
package org.codehaus.groovy.eclipse.editor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class GroovyConfiguration extends JavaSourceViewerConfiguration {

    // FIXADE M2 will need these again for groovy colors in the editor
//	private ITextDoubleClickStrategy doubleClickStrategy;
	private GroovyTagScanner tagScanner = new GroovyTagScanner(new GroovyColorManager());
	private GroovyStringScanner stringScanner = new GroovyStringScanner();
	
	
	/**
	 * Single token scanner.
	 */
	static class SingleTokenScanner extends BufferedRuleBasedScanner {
		public SingleTokenScanner(TextAttribute attribute) {
			setDefaultReturnToken(new Token(attribute));
		}
	}

	public GroovyConfiguration(IColorManager colorManager, IPreferenceStore preferenceSource, ITextEditor editor) {
	    super(colorManager, preferenceSource, editor, IJavaPartitions.JAVA_PARTITIONING);
	}

    @Override
    protected RuleBasedScanner getCodeScanner() {
//        return super.getCodeScanner();
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

}