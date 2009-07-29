/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.utils;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class StatusHelper {

	public static RefactoringStatus convertStatus(IStatus stat) {
		RefactoringStatus refStatus = new RefactoringStatus();
		for(IStatus st : stat.getChildren()) {
			refStatus.addEntry(st.getSeverity(), st.getMessage(), null, st.getPlugin(), st.getCode());
		}
		return refStatus;
	}
	
}
