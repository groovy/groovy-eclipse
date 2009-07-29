/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.rename.renameField;

import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.refactoring.core.RefactoringProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.IAmbiguousRenameInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameInfo;

public class RenameFieldInfo extends RenameInfo implements IAmbiguousRenameInfo{

	RenameFieldProvider renFieldprovider;

	public RenameFieldInfo(RefactoringProvider provider) {
		super(provider);
		this.renFieldprovider = (RenameFieldProvider) provider;
	}
	
	public boolean refactoringIsAmbiguous(){
		return renFieldprovider.refactoringIsAmbiguous();
	}
	
	public Map<IGroovyDocumentProvider, List<ASTNode>> getAmbiguousCandidates(){
		return renFieldprovider.getAmbiguousCandidates();
	}
	
	public Map<IGroovyDocumentProvider, List<ASTNode>> getDefinitiveCandidates(){
		return renFieldprovider.getDefinitiveCandidates();
	}
	
	public void addDefinitiveEntry(IGroovyDocumentProvider docProvider, ASTNode node){
		renFieldprovider.addDefinitiveEntry(docProvider, node);
	}
	
	public void removeDefinitiveEntry(IGroovyDocumentProvider docProvider, ASTNode node){
		renFieldprovider.removeDefinitveEntry(docProvider, node);
	}

}
