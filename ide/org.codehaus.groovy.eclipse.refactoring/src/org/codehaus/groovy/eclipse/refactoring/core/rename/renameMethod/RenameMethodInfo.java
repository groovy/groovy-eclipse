/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
