package org.codehaus.groovy.eclipse.refactoring.core.rename;

import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.ui.pages.rename.RenameFileSelectionPage;

public class ProgrammaticalRenameRefactoring extends GroovyRefactoring {

	protected RenameFileSelectionPage page;
	public ProgrammaticalRenameRefactoring(RenameInfo info, String refactoringName) {
		super(info);
		setName(refactoringName);
		if (info instanceof IAmbiguousRenameInfo) {
			IAmbiguousRenameInfo ambigousInfo = (IAmbiguousRenameInfo)info;
			if(ambigousInfo.refactoringIsAmbiguous()) {
				page = new RenameFileSelectionPage(refactoringName,ambigousInfo);
				pages.add(page);
			}
		}
	}
}
