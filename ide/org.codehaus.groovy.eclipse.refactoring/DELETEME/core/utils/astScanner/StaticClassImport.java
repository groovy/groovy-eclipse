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
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * This class represents the imports like
 * import static java.lang.Math.*
 * 
 * @author martin
 *
 */
public class StaticClassImport extends RefactoringImportNode {
	

    public StaticClassImport(ClassNode type) {
        super(type, null);
    }
    
    @Override
    public String getText() {
		return "import static " + newClassName + ".*";
	}

	@Override
    public void visit(GroovyCodeVisitor visitor) {
		if(visitor instanceof AbstractRefactoringCodeVisitor){
			((AbstractRefactoringCodeVisitor) visitor).visitStaticClassImport(this);
		}
	}
}
