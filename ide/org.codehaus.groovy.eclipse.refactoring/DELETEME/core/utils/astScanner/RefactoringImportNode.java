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
package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;

/**
 * This class represents the base class of the ImportNodes which must
 * be handled differently in the refactorings
 * 
 * @author martin
 *
 */
public abstract class RefactoringImportNode extends ImportNode  {
	
	protected String newClassName;
	
	public RefactoringImportNode(ImportNode importNode) {
		super(importNode.getType(),importNode.getAlias());
		init();
	}
	
	public RefactoringImportNode(ClassNode type, String alias) {
		super(type, alias);
		init();
	}
	
	public void setNewClassName(String newName) {
		this.newClassName = getType().getPackageName() + "." + newName;
	}
	
    @Override
    public abstract String getText();
    
	private void init() {
		this.newClassName = getType().getName();
	}
}
