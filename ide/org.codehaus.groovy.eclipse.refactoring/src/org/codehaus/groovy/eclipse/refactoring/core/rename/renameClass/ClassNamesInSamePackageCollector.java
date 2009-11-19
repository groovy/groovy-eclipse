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

public class ClassNamesInSamePackageCollector extends RefactoringCodeVisitorSupport {

	private final List<String> classNamesInSamePackage = new ArrayList<String>();
	private final ClassNode oldClass;

	public ClassNamesInSamePackageCollector(ModuleNode rootNode, ClassNode oldClass) {
		super(rootNode);
		this.oldClass = oldClass;
	}
	
	@Override
    public void visitClass(ClassNode node) {
		super.visitClass(node);
		if (node.getPackageName() == null && oldClass.getPackageName() == null) {
			classNamesInSamePackage .add(node.getNameWithoutPackage());
		} else if (node.getPackageName() != null &&
				(node.getPackageName().equals(oldClass.getPackageName()))) {
			classNamesInSamePackage.add(node.getNameWithoutPackage());
		}
	}

	public List<String> getClassNamesInSamePackage() {
		return classNamesInSamePackage;
	}

}
