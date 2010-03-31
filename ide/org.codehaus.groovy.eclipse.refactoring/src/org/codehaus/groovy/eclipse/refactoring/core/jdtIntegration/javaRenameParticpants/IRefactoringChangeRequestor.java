/* 
 * Copyright (C) 2007, 2008 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.javaRenameParticpants;

import java.util.List;

import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;

/**
 * @author andrew
 *
 */
public interface IRefactoringChangeRequestor extends ITypeRequestor {
    public List<ITextSelection> getMatchLocations();
}
