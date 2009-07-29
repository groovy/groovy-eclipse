/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename;

import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;

public interface IAmbiguousRenameInfo {

	public abstract boolean refactoringIsAmbiguous();

	public abstract Map<IGroovyDocumentProvider, List<ASTNode>> getAmbiguousCandidates();

	public abstract Map<IGroovyDocumentProvider, List<ASTNode>> getDefinitiveCandidates();

	public abstract void addDefinitiveEntry(IGroovyDocumentProvider docProvider, ASTNode node);

	public abstract void removeDefinitiveEntry(IGroovyDocumentProvider docProvider, ASTNode node);

}