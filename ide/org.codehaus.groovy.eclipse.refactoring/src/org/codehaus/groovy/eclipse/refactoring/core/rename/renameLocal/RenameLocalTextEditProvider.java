/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Michael Klenk and others        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal;

import java.util.List;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameTextEditProvider;
import org.eclipse.text.edits.MultiTextEdit;

public class RenameLocalTextEditProvider extends RenameTextEditProvider {
	
	private final MethodNode method;
	private final VariableProxy selectedVariable;

	public RenameLocalTextEditProvider(IGroovyDocumentProvider docProvider,
			VariableProxy selectedVariable, MethodNode method) {
		super(selectedVariable.getName(), docProvider);
		this.selectedVariable = selectedVariable;
		this.method = method;
		
	}
	
	@Override
    public MultiTextEdit getMultiTextEdit() {
		RenameLocalTextEdit scanner = new RenameLocalTextEdit(docProvider,
				selectedVariable, method, newName);
		scanner.scanAST();
		return scanner.getEdits();
	}
	
	@Override
    public List<String> getAlreadyUsedNames() {
		LocalVariableCollector collector = new LocalVariableCollector(docProvider.getRootNode(),method);
		collector.scanAST();
		return collector.getUsedNames();
	}
}
