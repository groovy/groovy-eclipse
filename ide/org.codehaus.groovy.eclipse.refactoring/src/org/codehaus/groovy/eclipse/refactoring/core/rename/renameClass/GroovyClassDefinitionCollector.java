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

package org.codehaus.groovy.eclipse.refactoring.core.rename.renameClass;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.RefactoringCodeVisitorSupport;

/**
 * This class is used to collect all groovy class definitions. This is needed to decide, whether the
 * class can be renamed or not. Only the groovy classes found by this collector can be renamed
 * 
 * @author martin
 *
 */
public class GroovyClassDefinitionCollector extends RefactoringCodeVisitorSupport {

	private final List<String> groovyClasses = new ArrayList<String>();
	
	public GroovyClassDefinitionCollector(ModuleNode rootNode) {
		super(rootNode);
	}
	
	@Override
    public void visitClass(ClassNode node) {
		super.visitClass(node);
		groovyClasses.add(node.getName());
	}

	public List<String> getGroovyClasses() {
		return groovyClasses;
	}

}
