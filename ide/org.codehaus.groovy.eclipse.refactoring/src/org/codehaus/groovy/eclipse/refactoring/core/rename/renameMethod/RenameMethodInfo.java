/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename.renameMethod;

import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.IAmbiguousRenameInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameInfo;

/**
 * Info Class for the refactoring rename method
 * @author reto kleeb
 *
 */
public class RenameMethodInfo extends RenameInfo implements IAmbiguousRenameInfo {
	
	RenameMethodProvider renMethprovider;
	
	public RenameMethodInfo(RefactoringProvider provider) {
		super(provider);
		renMethprovider = (RenameMethodProvider) provider;
	}
	
	public boolean refactoringIsAmbiguous(){
		return renMethprovider.refactoringIsAmbiguous();
	}

	public Map<IGroovyDocumentProvider, List<ASTNode>> getAmbiguousCandidates(){
		return renMethprovider.getAmbiguousCandidates();
	}
	
	public Map<IGroovyDocumentProvider, List<ASTNode>> getDefinitiveCandidates(){
		return renMethprovider.getDefinitiveCandidates();
	}

	public void addDefinitiveEntry(IGroovyDocumentProvider docProvider, ASTNode node){
		renMethprovider.addDefinitiveEntry(docProvider, node);
	}

	public void removeDefinitiveEntry(IGroovyDocumentProvider docProvider, ASTNode node){
		renMethprovider.removeDefinitveEntry(docProvider, node);
	}

}
