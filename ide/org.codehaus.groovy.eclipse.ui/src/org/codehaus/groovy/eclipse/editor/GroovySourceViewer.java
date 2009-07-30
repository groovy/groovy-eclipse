/*******************************************************************************
 * Copyright (c) 2000, 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrew Eisenberg - adapted for groovy
 *******************************************************************************/
package org.codehaus.groovy.eclipse.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.java.JavaFormattingContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * This is copied from CompilationUnitEditor#AdaptedSourceViewer
 * so that the GroovySourceViewer can subclass and make changes
 * 
 * XXX This class is not used yet, but it may in the future.
 * However, watch out for a ClassCastException at CompilationUnitEditor.handlePreferenceStoreChanged
 * line 1535
 * 
 * @author Andrew Eisenberg
 * @created Jul 22, 2009
 *
 */
class GroovySourceViewer extends JavaSourceViewer  {
    private static final boolean CODE_ASSIST_DEBUG= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jdt.ui/debug/ResultCollector"));  //$NON-NLS-1$//$NON-NLS-2$

    /**
     * Text operation code for requesting common prefix completion.
     */
    public static final int CONTENTASSIST_COMPLETE_PREFIX= 60;

    private GroovyEditor editor;
    
	public GroovySourceViewer(GroovyEditor editor, Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean showAnnotationsOverview, int styles, IPreferenceStore store) {
		super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles, store);
		this.editor = editor;
	}

	public IContentAssistant getContentAssistant() {
		return fContentAssistant;
	}

	/*
	 * @see ITextOperationTarget#doOperation(int)
	 */
	public void doOperation(int operation) {

		if (getTextWidget() == null)
			return;

		switch (operation) {
			case CONTENTASSIST_PROPOSALS:
				long time= CODE_ASSIST_DEBUG ? System.currentTimeMillis() : 0;
				String msg= fContentAssistant.showPossibleCompletions();
				if (CODE_ASSIST_DEBUG) {
					long delta= System.currentTimeMillis() - time;
					System.err.println("Code Assist (total): " + delta); //$NON-NLS-1$
				}
				editor.setStatusLineErrorMessage(msg);
				return;
			case QUICK_ASSIST:
				/*
				 * XXX: We can get rid of this once the SourceViewer has a way to update the status line
				 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=133787
				 */
				msg= fQuickAssistAssistant.showPossibleQuickAssists();
				editor.setStatusLineErrorMessage(msg);
				return;
		}

		super.doOperation(operation);
	}

	/*
	 * @see IWidgetTokenOwner#requestWidgetToken(IWidgetTokenKeeper)
	 */
	public boolean requestWidgetToken(IWidgetTokenKeeper requester) {
		if (PlatformUI.getWorkbench().getHelpSystem().isContextHelpDisplayed())
			return false;
		return super.requestWidgetToken(requester);
	}

	/*
	 * @see IWidgetTokenOwnerExtension#requestWidgetToken(IWidgetTokenKeeper, int)
	 * @since 3.0
	 */
	public boolean requestWidgetToken(IWidgetTokenKeeper requester, int priority) {
		if (PlatformUI.getWorkbench().getHelpSystem().isContextHelpDisplayed())
			return false;
		return super.requestWidgetToken(requester, priority);
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewer#createFormattingContext()
	 * @since 3.0
	 */
	@SuppressWarnings("unchecked")
    public IFormattingContext createFormattingContext() {
		IFormattingContext context= new JavaFormattingContext();

		Map preferences;
		IJavaElement inputJavaElement= editor.getInputJavaElement();
		IJavaProject javaProject= inputJavaElement != null ? inputJavaElement.getJavaProject() : null;
		if (javaProject == null)
			preferences= new HashMap(JavaCore.getOptions());
		else
			preferences= new HashMap(javaProject.getOptions(true));

		context.setProperty(FormattingContextProperties.CONTEXT_PREFERENCES, preferences);

		return context;
	}
	
}
