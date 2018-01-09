/*
 * Copyright 2009-2017 the original author or authors.
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

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ImportResolver;

/**
 * This class represents the imports like
 * import groovy.lang.Closure as MyClosure
 */
public class ClassImport extends RefactoringImportNode {

	private String newAlias;

	public ClassImport(ImportNode importNode) {
		super(importNode);
		newAlias = getAlias();
	}

    @Override
    public String getText() {
		if (ImportResolver.isExplicitAlias(this)) {
			return "import " + newClassName + " as " + newAlias;
		}
        return "import " + newClassName;
	}

    public void setNewAlias(String newAlias) {
    	this.newAlias = newAlias;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
    	if(visitor instanceof AbstractRefactoringCodeVisitor){
			((AbstractRefactoringCodeVisitor) visitor).visitClassImport(this);
		}
    }
}
