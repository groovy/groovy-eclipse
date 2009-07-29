/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
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
