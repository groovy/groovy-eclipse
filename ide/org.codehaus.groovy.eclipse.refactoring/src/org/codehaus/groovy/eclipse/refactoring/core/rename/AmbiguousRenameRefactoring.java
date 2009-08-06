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

package org.codehaus.groovy.eclipse.refactoring.core.rename;

import org.codehaus.groovy.eclipse.refactoring.ui.pages.rename.RenameFileSelectionPage;
import org.codehaus.groovy.eclipse.refactoring.ui.pages.rename.AmbiguousRenameFirstPage;

/**
 * main class for the refactoring rename method,
 * changes pages of the wizard in case the refactoring is ambiguous
 * @author reto
 *
 */
public class AmbiguousRenameRefactoring extends RenameRefactoring {

	public AmbiguousRenameRefactoring(IAmbiguousRenameInfo info, String refactoringName) {
		super((RenameInfo)info, refactoringName);
		if(info.refactoringIsAmbiguous()){
			pages.clear();
			pages.add(new AmbiguousRenameFirstPage(refactoringName, info));
			pages.add(new RenameFileSelectionPage(refactoringName,info));
		}
	}

}
