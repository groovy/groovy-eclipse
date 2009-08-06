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
package org.codehaus.groovy.eclipse.refactoring.ui.pages.rename;

import org.codehaus.groovy.eclipse.refactoring.core.rename.IAmbiguousRenameInfo;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameInfo;

public class AmbiguousRenameFirstPage extends RenamePage{

	public AmbiguousRenameFirstPage(String name, IAmbiguousRenameInfo info) {
		super(name, (RenameInfo)info);
	}
	
	/**
	 * weird implementation, finish button 
	 * is still 'clickable' but nothing happens.
	 * 
	 * it does however serve my needs
	 */
	@Override
    protected boolean performFinish() {
		return false;
	}
	
}
